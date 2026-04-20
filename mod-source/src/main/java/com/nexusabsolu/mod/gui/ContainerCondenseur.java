package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCondenseur extends Container {

    private final TileCondenseur tile;

    /** Helper sync avec split low/high pour eviter overflow 32767 (energy Condenseur). */
    private final com.nexusabsolu.mod.gui.util.ContainerSyncHelper sync =
        new com.nexusabsolu.mod.gui.util.ContainerSyncHelper(6);

    // Le tile utilise getField/setField(id) pour 6 champs.
    // On conserve les memes indices pour compatibilite.
    private static final int NUM_FIELDS = 6;

    public ContainerCondenseur(InventoryPlayer playerInv, TileCondenseur tile) {
        this.tile = tile;

        // Input slots (2x2) - positions match GUI
        addSlotToContainer(new Slot(tile, 0, 36, 39));   // CM slot 1
        addSlotToContainer(new Slot(tile, 1, 60, 39));   // CM slot 2
        addSlotToContainer(new Slot(tile, 2, 36, 63));   // Key slot
        addSlotToContainer(new Slot(tile, 3, 60, 63));   // Catalyst slot

        // Output slot
        addSlotToContainer(new SlotOutput(tile, 4, 160, 50));

        // Player inventory (offset down for bigger GUI)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                    30 + col * 18, 130 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 30 + col * 18, 192));
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
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        int fieldIdx = sync.receiveProperty(id, data);
        if (fieldIdx < 0) return;
        tile.setField(fieldIdx, sync.getField(fieldIdx));
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int action) {
        // action 0 = toggle auto/manual
        // action 1 = manual start
        if (action == 0) {
            tile.toggleAutoMode();
            return true;
        } else if (action == 1) {
            tile.manualStart();
            return true;
        }
        return false;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            itemstack = stackInSlot.copy();

            if (index == 4) {
                if (!mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
            } else if (index >= 5) {
                if (!mergeItemStack(stackInSlot, 0, 4, false)) return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(stackInSlot, 5, 41, false)) return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemstack;
    }

    static class SlotOutput extends Slot {
        public SlotOutput(TileCondenseur tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
