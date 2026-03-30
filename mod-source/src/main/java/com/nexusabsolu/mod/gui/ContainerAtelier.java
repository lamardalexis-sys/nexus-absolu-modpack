package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileAtelier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAtelier extends Container {

    private final TileAtelier tile;

    public ContainerAtelier(InventoryPlayer playerInv, TileAtelier tile) {
        this.tile = tile;

        // Input slot 1 (left)
        addSlotToContainer(new Slot(tile, 0, 38, 35));
        // Input slot 2 (right)
        addSlotToContainer(new Slot(tile, 1, 68, 35));
        // Output slot
        addSlotToContainer(new SlotOutput(tile, 2, 128, 35));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public boolean enchantItem(EntityPlayer player, int action) {
        if (action == 0) {
            tile.tryCraft();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (index < 3) {
            // From atelier to player
            if (!mergeItemStack(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else {
            // From player to atelier inputs
            if (!mergeItemStack(stack, 0, 2, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();

        return original;
    }

    // Output-only slot
    static class SlotOutput extends Slot {
        public SlotOutput(TileAtelier tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) { return false; }
    }
}
