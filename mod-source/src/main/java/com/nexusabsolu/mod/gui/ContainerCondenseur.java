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

    public ContainerCondenseur(InventoryPlayer playerInv, TileCondenseur tile) {
        this.tile = tile;

        // Input slots (2x2 grid on the left)
        // Slot 0: Compact Machine 1 (x=26, y=22)
        addSlotToContainer(new Slot(tile, 0, 26, 22));
        // Slot 1: Compact Machine 2 (x=50, y=22)
        addSlotToContainer(new Slot(tile, 1, 50, 22));
        // Slot 2: Key (x=26, y=46)
        addSlotToContainer(new Slot(tile, 2, 26, 46));
        // Slot 3: Catalyst (x=50, y=46)
        addSlotToContainer(new Slot(tile, 3, 50, 46));

        // Output slot (right side, x=130, y=34)
        addSlotToContainer(new SlotOutput(tile, 4, 130, 34));

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
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
        }
        processTime = tile.getField(0);
        maxProcessTime = tile.getField(1);
        energy = tile.getField(2);
        maxEnergy = tile.getField(3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
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

            // Output slot -> player inventory
            if (index == 4) {
                if (!mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
            }
            // Player inventory -> input slots
            else if (index >= 5) {
                if (!mergeItemStack(stackInSlot, 0, 4, false)) return ItemStack.EMPTY;
            }
            // Input slots -> player inventory
            else {
                if (!mergeItemStack(stackInSlot, 5, 41, false)) return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
        }
        return itemstack;
    }

    // Output-only slot
    static class SlotOutput extends Slot {
        public SlotOutput(TileCondenseur tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false; // Can't put items in output
        }
    }
}
