package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileAtelier;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int CONDENSEUR_GUI = 0;
    public static final int ATELIER_GUI = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == CONDENSEUR_GUI && te instanceof TileCondenseur) {
            return new ContainerCondenseur(player.inventory, (TileCondenseur) te);
        }
        if (ID == ATELIER_GUI && te instanceof TileAtelier) {
            return new ContainerAtelier(player.inventory, (TileAtelier) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == CONDENSEUR_GUI && te instanceof TileCondenseur) {
            return new GuiCondenseur(player.inventory, (TileCondenseur) te);
        }
        if (ID == ATELIER_GUI && te instanceof TileAtelier) {
            return new GuiAtelier(player.inventory, (TileAtelier) te);
        }
        return null;
    }
}
