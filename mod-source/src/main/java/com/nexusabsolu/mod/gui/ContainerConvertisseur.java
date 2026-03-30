package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;

public class ContainerConvertisseur extends Container {

    private final TileConvertisseur tile;
    private int[] cachedFields = new int[15];

    public ContainerConvertisseur(InventoryPlayer playerInv, TileConvertisseur tile) {
        this.tile = tile;

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    playerInv, col + row * 9 + 9, 8 + col * 18, 95 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(
                playerInv, col, 8 + col * 18, 153));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            for (int i = 0; i < 15; i++) {
                int val = tile.getField(i);
                if (cachedFields[i] != val) {
                    listener.sendWindowProperty(this, i, val);
                    cachedFields[i] = val;
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    // Button clicks from GUI: id 0-5 = toggle output face
    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (id >= 0 && id < 6) {
            tile.toggleOutputFace(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tile.getPos().getX() + 0.5,
            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }

    public TileConvertisseur getTile() { return tile; }
}
