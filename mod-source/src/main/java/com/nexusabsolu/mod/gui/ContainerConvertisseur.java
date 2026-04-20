package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;

public class ContainerConvertisseur extends Container {

    private final TileConvertisseur tile;

    /** Helper sync split int->2 shorts pour eviter overflow 32767. */
    private final com.nexusabsolu.mod.gui.util.ContainerSyncHelper sync =
        new com.nexusabsolu.mod.gui.util.ContainerSyncHelper(15);

    private static final int NUM_FIELDS = 15;

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

    private int[] fetchFields() {
        int[] fields = new int[NUM_FIELDS];
        for (int i = 0; i < NUM_FIELDS; i++) {
            fields[i] = tile.getField(i);
        }
        return fields;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        sync.sendInitial(this, listener, fetchFields());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        sync.detectChanges(this, this.listeners, fetchFields());
    }

    @Override
    public void updateProgressBar(int id, int data) {
        int fieldIdx = sync.receiveProperty(id, data);
        if (fieldIdx < 0) return;
        tile.setField(fieldIdx, sync.getField(fieldIdx));
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
