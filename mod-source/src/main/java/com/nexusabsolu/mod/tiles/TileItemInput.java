package com.nexusabsolu.mod.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileItemInput extends TileEntity implements IInventory {

    private ItemStack[] inventory = new ItemStack[4];
    private BlockPos masterPos = null;

    /**
     * Custom IItemHandler that limits each slot to exactly 1 item.
     *
     * Why: the T2 condenser recipes need 4 distinct items in 4 distinct slots
     * (e.g. 2x Compact Machine + Key + Catalyst). Stackable items would
     * otherwise be all piled into slot 0 by an ItemDuct, leaving slot 1 empty
     * and breaking the recipe match.
     *
     * Strategy: each slot accepts at most 1 item. When a duct tries to insert
     * a stack of 2+ items into slot 0, it gets back the leftover (count - 1)
     * and naturally tries slot 1 next. This forces automatic distribution.
     *
     * The player-facing GUI Slots are also clamped to 1 in ContainerCondenseurT2
     * for consistency.
     */
    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() { return inventory.length; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= inventory.length) return ItemStack.EMPTY;
            return inventory[slot];
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            if (slot < 0 || slot >= inventory.length) return stack;
            // If slot already has anything, reject the entire incoming stack
            // (the duct will try the next slot).
            if (!inventory[slot].isEmpty()) return stack;
            // Slot is empty: accept exactly 1 item.
            if (!simulate) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                inventory[slot] = copy;
                markDirty();
            }
            // Return the rest (stack count - 1).
            ItemStack rest = stack.copy();
            rest.shrink(1);
            return rest;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= inventory.length || amount <= 0) return ItemStack.EMPTY;
            ItemStack existing = inventory[slot];
            if (existing.isEmpty()) return ItemStack.EMPTY;
            int extract = Math.min(amount, existing.getCount());
            ItemStack result = existing.copy();
            result.setCount(extract);
            if (!simulate) {
                existing.shrink(extract);
                if (existing.isEmpty()) inventory[slot] = ItemStack.EMPTY;
                markDirty();
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot) { return 1; }
    };

    public TileItemInput() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    public BlockPos getMasterPos() { return masterPos; }
    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        markDirty();
    }

    // IInventory
    @Override public int getSizeInventory() { return 4; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) {
        if (index < 0 || index >= inventory.length) return ItemStack.EMPTY;
        return inventory[index];
    }
    @Override public ItemStack decrStackSize(int index, int count) {
        if (index < 0 || index >= inventory.length) return ItemStack.EMPTY;
        if (!inventory[index].isEmpty()) {
            ItemStack stack;
            if (inventory[index].getCount() <= count) {
                stack = inventory[index];
                inventory[index] = ItemStack.EMPTY;
            } else {
                stack = inventory[index].splitStack(count);
            }
            markDirty();
            return stack;
        }
        return ItemStack.EMPTY;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        if (index < 0 || index >= inventory.length) return ItemStack.EMPTY;
        ItemStack stack = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return stack;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= inventory.length) return;
        inventory[index] = stack;
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit())
            stack.setCount(getInventoryStackLimit());
        markDirty();
    }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
               player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (stack.isEmpty()) return true;
        String id = stack.getItem().getRegistryName().toString();
        switch (index) {
            case 0: // CM slot 1: compact machines or poop
            case 1: // CM slot 2: compact machines or poop
                return id.equals("compactmachines3:machine")
                    || id.equals("cropdusting:poop");
            case 2: // Key slot: any key or machine frame
                return id.startsWith("nexusabsolu:compact_key")
                    || id.equals("thermalexpansion:frame");
            case 3: // Catalyst slot: any catalyst or nexus wall
                return id.startsWith("nexusabsolu:catalyseur")
                    || id.equals("nexusabsolu:nexus_wall");
            default:
                return false;
        }
    }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() {
        for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Item Input"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    // NBT
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (masterPos != null) {
            compound.setInteger("MasterX", masterPos.getX());
            compound.setInteger("MasterY", masterPos.getY());
            compound.setInteger("MasterZ", masterPos.getZ());
        }
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (!inventory[i].isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        compound.setTag("Items", list);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("MasterX")) {
            masterPos = new BlockPos(
                compound.getInteger("MasterX"),
                compound.getInteger("MasterY"),
                compound.getInteger("MasterZ"));
        }
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < inventory.length) {
                inventory[slot] = new ItemStack(tag);
            }
        }
    }

    // Capability for hoppers/conduits
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return (T) itemHandler;
        return super.getCapability(capability, facing);
    }
}
