package com.nexusabsolu.mod.events;

import com.nexusabsolu.mod.compat.CM3Bridge;
import com.nexusabsolu.mod.util.PlayerVisitedMachines;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Suit les Compact Machines visitees par chaque joueur.
 *
 * Strategie : a chaque tick (~20Hz), si le joueur est en DIM 144 et que
 * c'est un tick "echantillonne" (1x par seconde), on calcule sa roomId
 * via CM3Bridge.getIdForPos et on l'ajoute a sa liste visitee si elle
 * n'y est pas deja.
 *
 * Detection robuste de TOUS les chemins d'entree dans une room :
 *   - PSD vanilla CM3 (descente dans une CM)
 *   - PSD vanilla CM3 (sortie -> remonte dans la room parente)
 *   - /voss_goto custom command (deja supprime, mais resterait OK si
 *     un admin fait un /tp manuel)
 *   - Cle de Liberte (sortie overworld, mais le tracker enregistre
 *     la 9x9 avant la sortie via le tick precedent)
 *
 * Couts : 1 lookup NBT/sec/joueur en DIM 144 = negligeable.
 */
public class MachineVisitTracker {

    /** Tick interval pour echantillonner la position (1x/sec). */
    private static final int TICK_INTERVAL = 20;

    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        // Filtre : seulement cote serveur, seulement DIM 144
        if (player.world.isRemote) return;
        if (player.dimension != 144) return;

        // Throttle : 1x par seconde par joueur
        if (player.world.getTotalWorldTime() % TICK_INTERVAL != 0) return;

        // Calcule la roomId courante
        int roomId = CM3Bridge.getIdForPos(player.world, player.getPosition());
        if (roomId < 0) return;

        PlayerVisitedMachines.addVisited(player, roomId);
    }
}
