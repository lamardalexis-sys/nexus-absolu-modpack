package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerAutoScavenger extends Container {

    private final TileAutoScavenger tile;
    private int processTime = 0;
    private int energy = 0;

    public ContainerAutoScavenger(InventoryPlayer playerInv, TileAutoScavenger tile) {
        this.tile = tile;

        // Slot 0: Input (center top)
        addSlotToContainer(new Slot(tile, 0, 80, 12));

        // Slots 1-5: Output (row)
        for (int i = 0; i < 5; i++) {
            addSlotToContainer(new SlotOutput(tile, 1 + i, 44 + i * 18, 52));
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (processTime != tile.getMineTimer())
                listener.sendWindowProperty(this, 0, tile.getMineTimer());
            if (energy != tile.getEnergyStored())
                listener.sendWindowProperty(this, 1, tile.getEnergyStored());
        }
        processTime = tile.getMineTimer();
        energy = tile.getEnergyStored();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0: processTime = data; break;
            case 1: energy = data; break;
        }
    }

    public int getProcessTime() { return processTime; }
    public int getEnergy() { return energy; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;
        ItemStack stackInSlot = slot.getStack();
        ItemStack original = stackInSlot.copy();

        if (index < 6) {
            if (!mergeItemStack(stackInSlot, 6, 42, true)) return ItemStack.EMPTY;
        } else {
            if (!mergeItemStack(stackInSlot, 0, 1, false)) return ItemStack.EMPTY;
        }
        if (stackInSlot.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        return original;
    }

    // Output-only slot (no insertion by player)
    private static class SlotOutput extends Slot {
        public SlotOutput(TileAutoScavenger inv, int index, int x, int y) {
            super(inv, index, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) { return false; }
    }
}
