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

public class TileItemOutput extends TileEntity implements IInventory {

    private ItemStack[] inventory = new ItemStack[1];
    private BlockPos masterPos = null;
    private InvWrapper itemHandler;

    public TileItemOutput() {
        inventory[0] = ItemStack.EMPTY;
        itemHandler = new InvWrapper(this);
    }

    public BlockPos getMasterPos() { return masterPos; }
    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        markDirty();
    }

    // IInventory
    @Override public int getSizeInventory() { return 1; }
    @Override public boolean isEmpty() { return inventory[0].isEmpty(); }
    @Override public ItemStack getStackInSlot(int index) {
        if (index != 0) return ItemStack.EMPTY;
        return inventory[0];
    }
    @Override public ItemStack decrStackSize(int index, int count) {
        if (index != 0 || inventory[0].isEmpty()) return ItemStack.EMPTY;
        ItemStack stack;
        if (inventory[0].getCount() <= count) {
            stack = inventory[0];
            inventory[0] = ItemStack.EMPTY;
        } else {
            stack = inventory[0].splitStack(count);
        }
        markDirty();
        return stack;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack stack = inventory[0];
        inventory[0] = ItemStack.EMPTY;
        return stack;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        if (index != 0) return;
        inventory[0] = stack;
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
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return false; }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { inventory[0] = ItemStack.EMPTY; }
    @Override public String getName() { return "Item Output"; }
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
        if (!inventory[0].isEmpty()) {
            NBTTagCompound tag = new NBTTagCompound();
            inventory[0].writeToNBT(tag);
            compound.setTag("OutputItem", tag);
        }
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
        if (compound.hasKey("OutputItem")) {
            inventory[0] = new ItemStack(compound.getCompoundTag("OutputItem"));
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
