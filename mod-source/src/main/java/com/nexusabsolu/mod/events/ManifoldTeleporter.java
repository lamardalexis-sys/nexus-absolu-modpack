package com.nexusabsolu.mod.events;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;

/**
 * ManifoldTeleporter - gere la teleportation du joueur vers Age 5 (DIM 145)
 * a la fin du premier trip Cartouche Manifold.
 *
 * Utilise un flag NBT persistent ("nexus_age5_unlocked") pour marquer que le
 * joueur a deja franchi la barriere de simulation. Les utilisations suivantes
 * du Cartouche Manifold ne re-teleportent PAS (juste le trip 8 min normal).
 *
 * La dimension 145 est configuree via JED dans
 * config/justenoughdimensions/dimensions.json (overworld type, tempo 6000 = matin).
 *
 * Phase 4.2 du pipeline Age 4 - "Liberation de la simulation".
 */
public class ManifoldTeleporter {

    public static final int AGE5_DIMENSION_ID = 145;

    public static final String NBT_AGE5_UNLOCKED = "nexus_age5_unlocked";
    public static final String NBT_AGE5_PENDING = "nexus_age5_pending_teleport";

    /**
     * A appeler depuis ItemCartoucheManifold.onItemRightClick() AVANT
     * de consommer la cartouche.
     *
     * Si c'est la PREMIERE injection : marque le NBT pour declencher la
     * teleportation a la fin du trip.
     */
    public static void markFirstInjection(EntityPlayerMP player) {
        NBTTagCompound nbt = ManifoldEffectHandler.getPersistedNBT(player);
        if (!nbt.getBoolean(NBT_AGE5_UNLOCKED)) {
            nbt.setBoolean(NBT_AGE5_PENDING, true);
        }
    }

    /**
     * A appeler depuis ManifoldEffectHandler.triggerCrash() apres
     * applyFatigue(). Si le flag pending est set, teleporte le joueur vers
     * la DIM 145 (Age 5).
     */
    public static void onTripEnd(EntityPlayerMP player) {
        NBTTagCompound nbt = ManifoldEffectHandler.getPersistedNBT(player);
        if (!nbt.getBoolean(NBT_AGE5_PENDING)) {
            return;
        }
        if (nbt.getBoolean(NBT_AGE5_UNLOCKED)) {
            return;
        }

        // Effectue la teleportation vers Age 5
        boolean success = teleportToAge5(player);

        if (success) {
            nbt.setBoolean(NBT_AGE5_UNLOCKED, true);
            nbt.setBoolean(NBT_AGE5_PENDING, false);
            sendLiberationMessages(player);
        }
    }

    private static boolean teleportToAge5(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        WorldServer targetWorld = server.getWorld(AGE5_DIMENSION_ID);
        if (targetWorld == null) {
            // DIM 145 pas chargee - probablement JED config absente
            player.sendMessage(new TextComponentString(
                TextFormatting.DARK_RED
                + "[Nexus] La dimension Age 5 n'est pas chargee. "
                + "Verifie config/justenoughdimensions/dimensions.json."));
            return false;
        }

        // Spawn au centre, hauteur 100 (cf JED config)
        double tx = 0.5;
        double ty = 100.0;
        double tz = 0.5;

        try {
            server.getPlayerList().transferPlayerToDimension(player,
                AGE5_DIMENSION_ID, new SimpleTeleporter(targetWorld));
            player.connection.setPlayerLocation(tx, ty, tz, player.rotationYaw, 0.0F);
            return true;
        } catch (Throwable t) {
            // Si crash en teleport, on log et on revient au crash normal
            player.sendMessage(new TextComponentString(
                TextFormatting.DARK_RED
                + "[Nexus] Teleportation Age 5 a echoue : " + t.getMessage()));
            return false;
        }
    }

    private static void sendLiberationMessages(EntityPlayerMP player) {
        player.sendMessage(new TextComponentString(""));
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "============================================"));
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "         [ A G E   5  -  R E E L ]"));
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "============================================"));
        player.sendMessage(new TextComponentString(
            TextFormatting.WHITE + "" + TextFormatting.ITALIC
            + "Tu ouvres les yeux."));
        player.sendMessage(new TextComponentString(
            TextFormatting.WHITE + "" + TextFormatting.ITALIC
            + "Le ciel n'a plus la meme texture."));
        player.sendMessage(new TextComponentString(
            TextFormatting.WHITE + "" + TextFormatting.ITALIC
            + "L'air n'a plus le meme gout."));
        player.sendMessage(new TextComponentString(""));
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE
            + "Voss avait raison."));
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + "- Bienvenue dans le monde reel, Sujet."));
        player.sendMessage(new TextComponentString(""));
    }

    /**
     * Teleporter minimal qui ne fait pas de Nether/End portal logic.
     * Le joueur arrive a la position exacte (placeInDimension override).
     */
    private static class SimpleTeleporter implements ITeleporter {
        private final WorldServer world;

        SimpleTeleporter(WorldServer world) {
            this.world = world;
        }

        @Override
        public void placeEntity(net.minecraft.world.World w, net.minecraft.entity.Entity entity,
                                float yaw) {
            entity.setPositionAndUpdate(0.5, 100.0, 0.5);
            entity.motionX = 0.0;
            entity.motionY = 0.0;
            entity.motionZ = 0.0;
        }
    }
}
