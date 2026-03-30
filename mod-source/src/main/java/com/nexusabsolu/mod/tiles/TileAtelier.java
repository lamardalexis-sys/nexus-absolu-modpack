package com.nexusabsolu.mod.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class TileAtelier extends TileEntity implements IInventory {

    private ItemStack[] inventory = new ItemStack[3]; // 0,1=input, 2=output

    public TileAtelier() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    public void tryCraft() {
        AtelierRecipes.Recipe recipe = AtelierRecipes.findRecipe(inventory[0], inventory[1]);
        if (recipe != null && inventory[2].isEmpty()) {
            inventory[0].shrink(recipe.input1Count);
            inventory[1].shrink(recipe.input2Count);
            inventory[2] = recipe.getOutput();
            markDirty();
        }
    }

    // IInventory
    @Override public int getSizeInventory() { return 3; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < 3 ? inventory[index] : ItemStack.EMPTY;
    }
    @Override public ItemStack decrStackSize(int index, int count) {
        if (index >= 0 && index < 3 && !inventory[index].isEmpty()) {
            ItemStack result = inventory[index].splitStack(count);
            markDirty();
            return result;
        }
        return ItemStack.EMPTY;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        if (index >= 0 && index < 3) {
            ItemStack s = inventory[index];
            inventory[index] = ItemStack.EMPTY;
            markDirty();
            return s;
        }
        return ItemStack.EMPTY;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= 0 && index < 3) {
            inventory[index] = stack;
            if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit())
                stack.setCount(getInventoryStackLimit());
            markDirty();
        }
    }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
            player.getDistanceSq(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) <= 64.0;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index < 2; // only inputs
    }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() {
        for (int i = 0; i < 3; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Atelier du Dr. Voss"; }
    @Override public boolean hasCustomName() { return true; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 3; i++) {
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
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < 3; i++) inventory[i] = ItemStack.EMPTY;
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < 3) inventory[slot] = new ItemStack(tag);
        }
    }
}
