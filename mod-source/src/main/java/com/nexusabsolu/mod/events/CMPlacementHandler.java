package com.nexusabsolu.mod.events;

import com.nexusabsolu.mod.compat.CM3Bridge;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Au moment ou un joueur pose un bloc Compact Machine, si on est en DIM 144
 * (interieur d'une autre CM), on inscrit le roomId de la room parente dans
 * le TileEntity du bloc place.
 *
 * Permet de retracer la chaine matryoshka : chaque bloc CM "se souvient" de
 * la room dans laquelle il a ete pose.
 *
 * Le Localisateur Voss (item op) lit ce NBT pour permettre au joueur de
 * naviguer la chaine matryoshka en remontant les parents.
 *
 * NBT format:
 *   nexusabsolu:parent_room_id (Integer)  -- roomId de la room parente
 *   nexusabsolu:placed_in_dim144 (Boolean) -- true si pose en DIM 144
 *
 * NB: pour les blocs CM places en overworld (root), aucun NBT n'est ecrit.
 */
public class CMPlacementHandler {

    public static final String NBT_PARENT_ROOM_ID = "nexusabsolu:parent_room_id";
    public static final String NBT_PLACED_IN_DIM144 = "nexusabsolu:placed_in_dim144";

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.getWorld().isRemote) return;

        // Seulement les blocs CompactMachines
        net.minecraft.block.Block block = event.getPlacedBlock().getBlock();
        if (block.getRegistryName() == null
                || !"compactmachines3:machine".equals(block.getRegistryName().toString())) {
            return;
        }

        World world = event.getWorld();
        BlockPos pos = event.getPos();

        // Seulement si pose en DIM 144 (interieur d'une CM)
        if (world.provider.getDimension() != 144) return;

        // Lit le roomId de la room courante (la parente du bloc qu'on vient de poser)
        int parentRoomId = CM3Bridge.getIdForPos(world, pos);
        if (parentRoomId < 0) {
            FMLLog.log.warn("[CMPlacement] Cannot determine parent roomId at " + pos);
            return;
        }

        // Inscrit le NBT sur le TileEntity du bloc place
        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            FMLLog.log.warn("[CMPlacement] No TE at " + pos + " after placement");
            return;
        }

        NBTTagCompound nbt = te.getTileData();
        nbt.setInteger(NBT_PARENT_ROOM_ID, parentRoomId);
        nbt.setBoolean(NBT_PLACED_IN_DIM144, true);
        te.markDirty();

        if (event.getPlayer() instanceof EntityPlayerMP) {
            FMLLog.log.info("[CMPlacement] " + event.getPlayer().getName()
                + " placed CM at " + pos + " (parent room=" + parentRoomId + ")");
        }
    }
}
