package com.nexusabsolu.mod.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Ecoute les changements de dimension des joueurs.
 *
 * Declenche le flag age2Reached dans NexusProgressionData la premiere
 * fois qu'un joueur passe de la dimension Compact Machines (144)
 * vers l'Overworld (0).
 *
 * Une fois ce flag pose, les auto-scavengers et scavenge manuels
 * arretent de droper les mats "banals" (voir TileAutoScavenger et
 * ScavengeEventHandler).
 */
public class DimensionChangeHandler {

    private static final int COMPACT_MACHINES_DIM = 144;
    private static final int OVERWORLD_DIM = 0;

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.fromDim == COMPACT_MACHINES_DIM && event.toDim == OVERWORLD_DIM) {
            EntityPlayer player = event.player;
            if (player.world.isRemote) return;
            NexusProgressionData.markAge2Reached(player.world);
        }
    }
}
