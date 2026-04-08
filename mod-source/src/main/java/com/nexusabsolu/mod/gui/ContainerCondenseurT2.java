package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileItemInput;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerCondenseurT2 extends Container {

    private final TileCondenseurT2 master;
    private final TileItemInput inputTile;
    private final TileItemOutput outputTile;

    // Synced fields
    private int processTime = 0;
    private int maxProcessTime = 0;
    private int energy = 0;
    private int maxEnergy = 0;
    private int structureFormed = 0;
    private int fluidAmount = 0;
    private int fluidCapacity = 0;

    public ContainerCondenseurT2(InventoryPlayer playerInv, TileCondenseurT2 master, TileItemInput inputTile, TileItemOutput outputTile) {
        this.master = master;
        this.inputTile = inputTile;
        this.outputTile = outputTile;

        // 4 input slots (from INPUT hatch) -- 2x2 grid at (37,31) / (57,31) / (37,51) / (57,51)
        // SlotInput limits each slot to 1 item to ensure ItemDuct/Servo
        // automation distributes items across the 4 slots instead of
        // stacking them all in slot 0 (which would break recipe matching).
        if (inputTile != null) {
            addSlotToContainer(new SlotInput(inputTile, 0, 37, 31));  // CM 1
            addSlotToContainer(new SlotInput(inputTile, 1, 57, 31));  // CM 2
            addSlotToContainer(new SlotInput(inputTile, 2, 37, 51));  // Key
            addSlotToContainer(new SlotInput(inputTile, 3, 57, 51));  // Catalyst
        } else {
            for (int i = 0; i < 4; i++) {
                addSlotToContainer(new Slot(new DummyInventory(), i, -999, -999));
            }
        }

        // 1 output slot (from OUTPUT hatch) -- slot index 4, at (151, 43)
        if (outputTile != null) {
            addSlotToContainer(new SlotOutput(outputTile, 0, 151, 43));
        } else {
            addSlotToContainer(new Slot(new DummyInventory(), 0, -999, -999));
        }

        // Player inventory (slots 5-31) - centered at x=30
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 30 + col * 18, 131 + row * 18));
            }
        }
        // Player hotbar (slots 32-40) - centered at x=30, y=193
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 30 + col * 18, 193));
        }
    }

    // Output-only slot: can take out but not put in
    private static class SlotOutput extends Slot {
        public SlotOutput(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) { return false; }
    }

    // Input slot: limited to 1 item to force ItemDuct distribution
    private static class SlotInput extends Slot {
        public SlotInput(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }
        @Override
        public int getSlotStackLimit() { return 1; }
        @Override
        public int getItemStackLimit(ItemStack stack) { return 1; }
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
            if (fluidAmount != master.getFluidAmount())
                listener.sendWindowProperty(this, 5, master.getFluidAmount());
            if (fluidCapacity != master.getFluidCapacity())
                listener.sendWindowProperty(this, 6, master.getFluidCapacity());
        }
        processTime = master.getProcessTime();
        maxProcessTime = master.getMaxProcessTime();
        energy = master.getEnergyStored();
        maxEnergy = master.getMaxEnergyStored();
        structureFormed = master.isStructureValid() ? 1 : 0;
        fluidAmount = master.getFluidAmount();
        fluidCapacity = master.getFluidCapacity();
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
            case 5: fluidAmount = data; break;
            case 6: fluidCapacity = data; break;
        }
    }

    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return maxProcessTime; }
    public int getEnergy() { return energy; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getFluidAmount() { return fluidAmount; }
    public int getFluidCapacity() { return fluidCapacity; }
    public boolean hasFluidHatch() { return fluidCapacity > 0; }
    public boolean isStructureFormed() { return structureFormed == 1; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack stackInSlot = slot.getStack();
        ItemStack original = stackInSlot.copy();

        if (index < 4) {
            // From input slots to player
            if (!mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
        } else if (index == 4) {
            // From output slot to player
            if (!mergeItemStack(stackInSlot, 5, 41, true)) return ItemStack.EMPTY;
        } else {
            // From player to machine: find the right input slot
            boolean merged = false;
            if (inputTile != null) {
                for (int i = 0; i < 4; i++) {
                    if (inputTile.isItemValidForSlot(i, stackInSlot)) {
                        if (mergeItemStack(stackInSlot, i, i + 1, false)) {
                            merged = true;
                            break;
                        }
                    }
                }
            }
            if (!merged) return ItemStack.EMPTY;
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
