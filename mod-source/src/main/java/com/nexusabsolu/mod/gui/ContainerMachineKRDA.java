package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMachineKRDA extends Container {

    private final TileMachineKRDA tile;
    private final int[] cached = new int[TileMachineKRDA.FIELD_COUNT];

    private static final int INV_X = 19;
    private static final int INV_Y = 138;
    private static final int HOTBAR_Y = 196;

    public ContainerMachineKRDA(InventoryPlayer playerInv, TileMachineKRDA tile) {
        this.tile = tile;

        // Slot 0: Signalum input
        addSlotToContainer(new Slot(tile, 0, 14, 45) {
            @Override
            public boolean isItemValid(ItemStack s) {
                return tile.isItemValidForSlot(0, s);
            }
        });
        // Slot 1: Signalhee output
        addSlotToContainer(new Slot(tile, 1, 160, 45) {
            @Override
            public boolean isItemValid(ItemStack s) { return false; }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                    col + row * 9 + 9,
                    INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col,
                INV_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener l : this.listeners) {
            for (int i = 0; i < TileMachineKRDA.FIELD_COUNT; i++) {
                int val = tile.getField(i);
                if (cached[i] != val) {
                    l.sendWindowProperty(this, i, val);
                    cached[i] = val;
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int action) {
        return tile.handleAction(action);
    }

    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return tile.isUsableByPlayer(p);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (index < 2) {
            if (!mergeItemStack(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else {
            if (tile.isItemValidForSlot(0, stack)) {
                if (!mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        return original;
    }

    public TileMachineKRDA getTile() { return tile; }
}
