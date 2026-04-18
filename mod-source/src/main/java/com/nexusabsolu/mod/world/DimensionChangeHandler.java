package com.nexusabsolu.mod.world;

import com.nexusabsolu.mod.NexusAbsoluMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/**
 * Ecoute les changements de dimension des joueurs.
 *
 * Responsabilites:
 *  1. Sortie CM -> Overworld : marque age2Reached dans NexusProgressionData
 *     (premiere fois qu'un joueur passe de DIM144 vers DIM0).
 *     Coupe les drops "banals" des auto-scavengers une fois pose.
 *
 *  2. Entree Overworld -> CM (DIM144), PREMIERE FOIS seulement :
 *     applique Nausea II pendant 10 secondes (effet thematique du
 *     transfert dimensionnel "reprendre ses esprits"). Reset aussi
 *     la radiation NuclearCraft du joueur a 0 par securite, au cas
 *     ou il aurait ete irradie avant (sas de decontamination narratif).
 *     Flag par-joueur stocke dans PERSISTED_NBT_TAG du player.
 */
public class DimensionChangeHandler {

    private static final int COMPACT_MACHINES_DIM = 144;
    private static final int OVERWORLD_DIM = 0;

    /** Cle NBT persistante stockee sur le joueur pour tracker la 1ere entree CM. */
    private static final String NBT_KEY_FIRST_CM_ENTRY = "nexusabsolu.firstCMEntryDone";

    /** Duree de l'effet Nausea a l'entree CM : 200 ticks = 10 secondes. */
    private static final int NAUSEA_DURATION_TICKS = 200;

    /** Amplifier Nausea : 1 = "Nausea II" (plus visible qu'amplifier 0). */
    private static final int NAUSEA_AMPLIFIER = 1;

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;

        // Cas 1 : sortie CM -> Overworld (declencheur Age 2)
        if (event.fromDim == COMPACT_MACHINES_DIM && event.toDim == OVERWORLD_DIM) {
            NexusProgressionData.markAge2Reached(player.world);
            return;
        }

        // Cas 2 : entree depuis n'importe ou vers CM (DIM144), 1ere fois seulement
        if (event.toDim == COMPACT_MACHINES_DIM && event.fromDim != COMPACT_MACHINES_DIM) {
            handleFirstCMEntry(player);
        }
    }

    /**
     * Applique l'effet thematique "transfert dimensionnel" a la 1ere entree CM :
     *  - Nausea II pendant 10s (ecran qui tangue)
     *  - Reset rad NuclearCraft a 0 (safety net contre le bug debuffs 90%)
     *
     * Idempotent : ne se declenche qu'une fois par joueur (NBT persistant).
     */
    private void handleFirstCMEntry(EntityPlayer player) {
        NBTTagCompound persisted = getOrCreatePersistedData(player);
        if (persisted.getBoolean(NBT_KEY_FIRST_CM_ENTRY)) {
            // Joueur deja entre une fois -> pas d'effet, juste retour normal
            return;
        }

        // Marque le flag AVANT les effets : si une exception plante plus bas,
        // on ne veut pas spam l'effet a chaque retour.
        persisted.setBoolean(NBT_KEY_FIRST_CM_ENTRY, true);

        // Nausea II 10s (ambient=true pour moins d'intensite visuelle, showParticles=false)
        player.addPotionEffect(new PotionEffect(
            MobEffects.NAUSEA, NAUSEA_DURATION_TICKS, NAUSEA_AMPLIFIER, true, false));

        // Reset radiation NuclearCraft (si NC present) via commande serveur
        // try/catch robuste : si NC absent ou commande non dispo, on ignore silencieusement
        tryResetRadiation(player);
    }

    /**
     * Recupere (ou cree) le compound NBT persistant du joueur (ForgeData).
     * Ce NBT survit a la mort/respawn du joueur, contrairement au NBT standard.
     */
    private NBTTagCompound getOrCreatePersistedData(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        }
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    /**
     * Tente d'executer /radiation set <player> 0 pour nettoyer la rad du joueur.
     * NuclearCraft expose cette commande. Si elle n'existe pas (NC absent ou
     * version differente), on log en debug et on continue sans planter.
     */
    private void tryResetRadiation(EntityPlayer player) {
        if (!(player instanceof EntityPlayerMP)) return;
        EntityPlayerMP mp = (EntityPlayerMP) player;
        if (mp.world.getMinecraftServer() == null) return;

        try {
            String cmd = "radiation set " + mp.getName() + " 0";
            mp.world.getMinecraftServer().getCommandManager()
                .executeCommand(mp.world.getMinecraftServer(), cmd);
        } catch (Throwable t) {
            NexusAbsoluMod.LOGGER.debug(
                "Nexus: reset radiation command failed (NC absent ou cmd indispo): "
                + t.getMessage());
        }
    }
}
