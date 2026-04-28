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

    /**
     * Helper pour sync serveur->client des valeurs int > 32767.
     * Capacite energie d'un T2 peut depasser 32767 (bug latent sans split).
     * Voir ContainerSyncHelper pour le protocole.
     */
    private final com.nexusabsolu.mod.gui.util.ContainerSyncHelper sync =
        new com.nexusabsolu.mod.gui.util.ContainerSyncHelper(7);

    private static final int FIELD_PROCESS_TIME     = 0;
    private static final int FIELD_MAX_PROCESS_TIME = 1;
    private static final int FIELD_ENERGY           = 2;
    private static final int FIELD_MAX_ENERGY       = 3;
    private static final int FIELD_STRUCTURE_FORMED = 4;
    private static final int FIELD_FLUID_AMOUNT     = 5;
    private static final int FIELD_FLUID_CAPACITY   = 6;

    // Valeurs cote client (remplies depuis updateProgressBar pour le GUI)
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

        // 4 input slots (from INPUT hatch) -- 2x2 grid at (37,31) / (57,31) / (37,60) / (57,60)
        // SlotInput limits each slot to 1 item to ensure ItemDuct/Servo
        // automation distributes items across the 4 slots instead of
        // stacking them all in slot 0 (which would break recipe matching).
        if (inputTile != null) {
            addSlotToContainer(new SlotInput(inputTile, 0, 37, 31));  // CM 1
            addSlotToContainer(new SlotInput(inputTile, 1, 57, 31));  // CM 2
            addSlotToContainer(new SlotInput(inputTile, 2, 37, 60));  // Key
            addSlotToContainer(new SlotInput(inputTile, 3, 57, 60));  // Catalyst
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

    /** Construit le tableau des valeurs actuelles serveur dans l'ordre FIELD_*. */
    private int[] fetchFields() {
        int[] fields = new int[7];
        fields[FIELD_PROCESS_TIME]     = master.getProcessTime();
        fields[FIELD_MAX_PROCESS_TIME] = master.getMaxProcessTime();
        fields[FIELD_ENERGY]           = master.getEnergyStored();
        fields[FIELD_MAX_ENERGY]       = master.getMaxEnergyStored();
        fields[FIELD_STRUCTURE_FORMED] = master.isStructureValid() ? 1 : 0;
        fields[FIELD_FLUID_AMOUNT]     = master.getFluidAmount();
        fields[FIELD_FLUID_CAPACITY]   = master.getFluidCapacity();
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
        if (fieldIdx < 0) return;  // en attente des high bits
        int value = sync.getField(fieldIdx);
        switch (fieldIdx) {
            case FIELD_PROCESS_TIME:     processTime = value; break;
            case FIELD_MAX_PROCESS_TIME: maxProcessTime = value; break;
            case FIELD_ENERGY:           energy = value; break;
            case FIELD_MAX_ENERGY:       maxEnergy = value; break;
            case FIELD_STRUCTURE_FORMED: structureFormed = value; break;
            case FIELD_FLUID_AMOUNT:     fluidAmount = value; break;
            case FIELD_FLUID_CAPACITY:   fluidCapacity = value; break;
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
