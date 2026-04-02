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
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileItemInput extends TileEntity implements IInventory {

    private ItemStack[] inventory = new ItemStack[4];
    private BlockPos masterPos = null;
    private InvWrapper itemHandler;

    public TileItemInput() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
        itemHandler = new InvWrapper(this);
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
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
               player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return true; }
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
