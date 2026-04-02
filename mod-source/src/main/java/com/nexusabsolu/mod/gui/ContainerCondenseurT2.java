package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileItemInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCondenseurT2 extends Container {

    private final TileCondenseurT2 master;
    private final TileItemInput inputTile;

    // Synced fields
    private int processTime = 0;
    private int maxProcessTime = 0;
    private int energy = 0;
    private int maxEnergy = 0;
    private int structureFormed = 0;

    public ContainerCondenseurT2(InventoryPlayer playerInv, TileCondenseurT2 master, TileItemInput inputTile) {
        this.master = master;
        this.inputTile = inputTile;

        // 4 input slots (from INPUT hatch) -- 2x2 grid
        if (inputTile != null) {
            addSlotToContainer(new Slot(inputTile, 0, 35, 20));  // CM 1
            addSlotToContainer(new Slot(inputTile, 1, 53, 20));  // CM 2
            addSlotToContainer(new Slot(inputTile, 2, 35, 38));  // Key
            addSlotToContainer(new Slot(inputTile, 3, 53, 38));  // Catalyst
        } else {
            // Dummy slots if input tile not available
            for (int i = 0; i < 4; i++) {
                addSlotToContainer(new Slot(new DummyInventory(), i, -999, -999));
            }
        }

        // Player inventory (slots 4-30)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar (slots 31-39)
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return master.getWorld().getTileEntity(master.getPos()) == master
            && player.getDistanceSq(master.getPos().getX() + 0.5,
                                    master.getPos().getY() + 0.5,
                                    master.getPos().getZ() + 0.5) <= 64;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (processTime != master.getProcessTime())
                listener.sendWindowProperty(this, 0, master.getProcessTime());
            if (maxProcessTime != master.getMaxProcessTime())
                listener.sendWindowProperty(this, 1, master.getMaxProcessTime());
            if (energy != master.getEnergyStored())
                listener.sendWindowProperty(this, 2, master.getEnergyStored());
            if (maxEnergy != master.getMaxEnergyStored())
                listener.sendWindowProperty(this, 3, master.getMaxEnergyStored());
            int formed = master.isStructureValid() ? 1 : 0;
            if (structureFormed != formed)
                listener.sendWindowProperty(this, 4, formed);
        }
        processTime = master.getProcessTime();
        maxProcessTime = master.getMaxProcessTime();
        energy = master.getEnergyStored();
        maxEnergy = master.getMaxEnergyStored();
        structureFormed = master.isStructureValid() ? 1 : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        switch (id) {
            case 0: processTime = data; break;
            case 1: maxProcessTime = data; break;
            case 2: energy = data; break;
            case 3: maxEnergy = data; break;
            case 4: structureFormed = data; break;
        }
    }

    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return maxProcessTime; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public boolean isStructureFormed() { return structureFormed == 1; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getStack();
        ItemStack original = stackInSlot.copy();

        if (index < 4) {
            // From machine to player
            if (!mergeItemStack(stackInSlot, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            // From player to machine input
            if (!mergeItemStack(stackInSlot, 0, 4, false)) return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return original;
    }

    // Dummy inventory for when input tile is unavailable
    private static class DummyInventory implements net.minecraft.inventory.IInventory {
        private ItemStack[] stacks = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
        public int getSizeInventory() { return 4; }
        public boolean isEmpty() { return true; }
        public ItemStack getStackInSlot(int i) { return ItemStack.EMPTY; }
        public ItemStack decrStackSize(int i, int c) { return ItemStack.EMPTY; }
        public ItemStack removeStackFromSlot(int i) { return ItemStack.EMPTY; }
        public void setInventorySlotContents(int i, ItemStack s) {}
        public int getInventoryStackLimit() { return 64; }
        public void markDirty() {}
        public boolean isUsableByPlayer(EntityPlayer p) { return false; }
        public void openInventory(EntityPlayer p) {}
        public void closeInventory(EntityPlayer p) {}
        public boolean isItemValidForSlot(int i, ItemStack s) { return false; }
        public int getField(int i) { return 0; }
        public void setField(int i, int v) {}
        public int getFieldCount() { return 0; }
        public void clear() {}
        public String getName() { return ""; }
        public boolean hasCustomName() { return false; }
        public net.minecraft.util.text.ITextComponent getDisplayName() {
            return new net.minecraft.util.text.TextComponentString("");
        }
    }
}
