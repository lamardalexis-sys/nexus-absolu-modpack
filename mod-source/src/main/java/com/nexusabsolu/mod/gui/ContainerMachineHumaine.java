package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

public class ContainerMachineHumaine extends Container {

    private final TileMachineHumaine tile;
    private final int[] cached = new int[TileMachineHumaine.FIELD_COUNT];

    // GUI is 200 wide (vs 176 vanilla). Slot positions in GUI coords.
    // Food slot, bucket in, bucket out are in the machine area.
    // Player inv starts below the machine panel.

    // xSize=200 => player inv centered: offset = (200-162)/2 = 19
    // ySize=220 => inv rows start at y=138 (after machine panel + separator)
    private static final int INV_X = 19;
    private static final int INV_Y = 138;
    private static final int HOTBAR_Y = 196;

    public ContainerMachineHumaine(InventoryPlayer playerInv,
                                    TileMachineHumaine tile) {
        this.tile = tile;

        // Slot 0: Food input (left side of machine panel)
        addSlotToContainer(new SlotFood(tile, 0, 14, 38));

        // Slot 1: Bucket empty input (right of output tank)
        addSlotToContainer(new SlotBucket(tile, 1, 178, 28));

        // Slot 2: Bucket filled output (below empty bucket)
        addSlotToContainer(new SlotOutput(tile, 2, 178, 56));

        // Player inventory (3 rows x 9 cols)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                    col + row * 9 + 9,
                    INV_X + col * 18, INV_Y + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col,
                INV_X + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            for (int i = 0; i < TileMachineHumaine.FIELD_COUNT; i++) {
                int val = tile.getField(i);
                if (cached[i] != val) {
                    listener.sendWindowProperty(this, i, val);
                    cached[i] = val;
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        tile.setField(id, data);
    }

    // Button actions from GUI
    @Override
    public boolean enchantItem(EntityPlayer player, int action) {
        return tile.handleAction(action);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        // Machine slots = 0,1,2. Player = 3..38
        if (index < 3) {
            // Machine -> Player
            if (!mergeItemStack(stack, 3, 39, true)) return ItemStack.EMPTY;
        } else {
            // Player -> Machine
            if (stack.getItem() instanceof ItemFood) {
                if (!mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
            } else if (stack.getItem() == net.minecraft.init.Items.BUCKET) {
                if (!mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();

        return original;
    }

    public TileMachineHumaine getTile() { return tile; }

    // --- Custom Slots ---

    static class SlotFood extends Slot {
        SlotFood(TileMachineHumaine tile, int i, int x, int y) {
            super(tile, i, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() instanceof ItemFood;
        }
    }

    static class SlotBucket extends Slot {
        SlotBucket(TileMachineHumaine tile, int i, int x, int y) {
            super(tile, i, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) {
            return stack.getItem() == net.minecraft.init.Items.BUCKET;
        }
    }

    static class SlotOutput extends Slot {
        SlotOutput(TileMachineHumaine tile, int i, int x, int y) {
            super(tile, i, x, y);
        }
        @Override
        public boolean isItemValid(ItemStack stack) { return false; }
    }
}
