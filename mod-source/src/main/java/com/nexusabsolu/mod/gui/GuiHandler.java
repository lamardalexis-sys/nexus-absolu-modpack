package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileAtelier;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileConvertisseur;
import com.nexusabsolu.mod.tiles.TileItemInput;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int CONDENSEUR_GUI = 0;
    public static final int ATELIER_GUI = 1;
    public static final int CONVERTISSEUR_GUI = 2;
    public static final int CONDENSEUR_T2_GUI = 3;
    public static final int AUTO_SCAVENGER_GUI = 4;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (ID == CONDENSEUR_GUI && te instanceof TileCondenseur) {
            return new ContainerCondenseur(player.inventory, (TileCondenseur) te);
        }
        if (ID == ATELIER_GUI && te instanceof TileAtelier) {
            return new ContainerAtelier(player.inventory, (TileAtelier) te);
        }
        if (ID == CONVERTISSEUR_GUI && te instanceof TileConvertisseur) {
            return new ContainerConvertisseur(player.inventory, (TileConvertisseur) te);
        }
        if (ID == CONDENSEUR_T2_GUI && te instanceof TileCondenseurT2) {
            TileCondenseurT2 master = (TileCondenseurT2) te;
            TileItemInput inputTile = null;
            TileItemOutput outputTile = null;
            BlockPos inputPos = master.getInputPos();
            if (inputPos != null) {
                TileEntity inputTE = world.getTileEntity(inputPos);
                if (inputTE instanceof TileItemInput) inputTile = (TileItemInput) inputTE;
            }
            BlockPos outputPos = master.getOutputPos();
            if (outputPos != null) {
                TileEntity outputTE = world.getTileEntity(outputPos);
                if (outputTE instanceof TileItemOutput) outputTile = (TileItemOutput) outputTE;
            }
            return new ContainerCondenseurT2(player.inventory, master, inputTile, outputTile);
        }
        if (ID == AUTO_SCAVENGER_GUI && te instanceof TileAutoScavenger) {
            return new ContainerAutoScavenger(player.inventory, (TileAutoScavenger) te);
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
        if (ID == CONVERTISSEUR_GUI && te instanceof TileConvertisseur) {
            return new GuiConvertisseur(player.inventory, (TileConvertisseur) te);
        }
        if (ID == CONDENSEUR_T2_GUI && te instanceof TileCondenseurT2) {
            TileCondenseurT2 master = (TileCondenseurT2) te;
            TileItemInput inputTile = null;
            TileItemOutput outputTile = null;
            BlockPos inputPos = master.getInputPos();
            if (inputPos != null) {
                TileEntity inputTE = world.getTileEntity(inputPos);
                if (inputTE instanceof TileItemInput) inputTile = (TileItemInput) inputTE;
            }
            BlockPos outputPos = master.getOutputPos();
            if (outputPos != null) {
                TileEntity outputTE = world.getTileEntity(outputPos);
                if (outputTE instanceof TileItemOutput) outputTile = (TileItemOutput) outputTE;
            }
            return new GuiCondenseurT2(player.inventory, master, inputTile, outputTile);
        }
        if (ID == AUTO_SCAVENGER_GUI && te instanceof TileAutoScavenger) {
            return new GuiAutoScavenger(player.inventory, (TileAutoScavenger) te);
        }
        return null;
    }
}
