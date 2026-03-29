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
    private int processTime;
    private int maxProcessTime;
    private int energy;
    private int maxEnergy;
    private int structureValid;
    private int autoMode;

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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            if (processTime != tile.getField(0))
                listener.sendWindowProperty(this, 0, tile.getField(0));
            if (maxProcessTime != tile.getField(1))
                listener.sendWindowProperty(this, 1, tile.getField(1));
            if (energy != tile.getField(2))
                listener.sendWindowProperty(this, 2, tile.getField(2));
            if (maxEnergy != tile.getField(3))
                listener.sendWindowProperty(this, 3, tile.getField(3));
            if (structureValid != tile.getField(4))
                listener.sendWindowProperty(this, 4, tile.getField(4));
            if (autoMode != tile.getField(5))
                listener.sendWindowProperty(this, 5, tile.getField(5));
        }
        processTime = tile.getField(0);
        maxProcessTime = tile.getField(1);
        energy = tile.getField(2);
        maxEnergy = tile.getField(3);
        structureValid = tile.getField(4);
        autoMode = tile.getField(5);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
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
