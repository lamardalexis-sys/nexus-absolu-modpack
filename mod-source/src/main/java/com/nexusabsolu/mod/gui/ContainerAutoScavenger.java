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

    /**
     * Helper sync serveur->client. energy peut depasser 32767 (voir
     * ContainerSyncHelper pour la raison).
     */
    private final com.nexusabsolu.mod.gui.util.ContainerSyncHelper sync =
        new com.nexusabsolu.mod.gui.util.ContainerSyncHelper(3);

    private static final int FIELD_PROCESS_TIME = 0;
    private static final int FIELD_ENERGY       = 1;
    private static final int FIELD_SPEED_LEVEL  = 2;

    private int processTime = 0;
    private int energy = 0;
    private int speedLevel = 1;

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

    private int[] fetchFields() {
        int[] fields = new int[3];
        fields[FIELD_PROCESS_TIME] = tile.getMineTimer();
        fields[FIELD_ENERGY]       = tile.getEnergyStored();
        fields[FIELD_SPEED_LEVEL]  = tile.getSpeedLevel();
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
        int value = sync.getField(fieldIdx);
        switch (fieldIdx) {
            case FIELD_PROCESS_TIME: processTime = value; break;
            case FIELD_ENERGY:       energy = value; break;
            case FIELD_SPEED_LEVEL:  speedLevel = value; break;
        }
    }

    public int getProcessTime() { return processTime; }
    public int getEnergy() { return energy; }
    public int getSpeedLevel() { return speedLevel; }

    /** Compute RF/t client-side for display (same formula as tile) */
    public int getRfPerTick() {
        double exponent = (speedLevel - 1) / 6.0;
        return (int) Math.round(15.0 * Math.pow(100.0 / 15.0, exponent));
    }

    /** Compute interval client-side for display */
    public int getMineInterval() {
        return 100 - (speedLevel - 1) * 85 / 6;
    }

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
