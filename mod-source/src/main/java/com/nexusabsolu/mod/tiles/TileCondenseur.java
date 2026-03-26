package com.nexusabsolu.mod.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class TileCondenseur extends TileEntity implements ITickable, IInventory {

    // 4 input slots + 1 output slot = 5 total
    private ItemStack[] inventory = new ItemStack[5];
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(50000, 100); // 50K max, 100 RF/t input
    private int processTime = 0;
    private int maxProcessTime = 200;
    private CondenseurRecipes.Recipe currentRecipe = null; // 10 seconds at 20 ticks/sec
    private boolean processing = false;

    // Quote system
    private int currentQuote = 0;
    public static final String[] QUOTES = {
        "Deux espaces negocient...",
        "La matiere proteste. Ignorez-la.",
        "Phase instable. C'est normal. Probablement.",
        "Les dimensions se compriment.",
        "Prototype VII. Les six premiers ont explose.",
        "Ne reculez pas.",
        "La realite se plie...",
        "Voss avait raison. Comme toujours."
    };

    public TileCondenseur() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (canProcess()) {
            if (energyStorage.getEnergyStored() >= 20) { // 20 RF/tick
                energyStorage.drainInternal(20);
                processTime++;
                processing = true;

                // Change quote every 2 seconds
                if (processTime % 40 == 0) {
                    currentQuote = (currentQuote + 1) % QUOTES.length;
                }

                if (processTime >= maxProcessTime) {
                    processItem();
                    processTime = 0;
                    processing = false;
                }
                markDirty();
            }
        } else {
            if (processTime > 0) {
                processTime = 0;
                processing = false;
                markDirty();
            }
        }
    }

    private boolean canProcess() {
        if (!inventory[4].isEmpty()) return false;
        currentRecipe = CondenseurRecipes.findRecipe(
            inventory[0], inventory[1], inventory[2], inventory[3]);
        if (currentRecipe != null) {
            maxProcessTime = currentRecipe.processTime;
        }
        return currentRecipe != null;
    }

    private void processItem() {
        if (currentRecipe == null) return;
        ItemStack result = currentRecipe.getOutput();
        if (!result.isEmpty()) {
            inventory[4] = result.copy();
            for (int i = 0; i < 4; i++) {
                inventory[i].shrink(1);
            }
        }
        currentRecipe = null;
    }

    // Energy
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return maxProcessTime; }
    public boolean isProcessing() { return processing; }
    public String getCurrentQuote() { return QUOTES[currentQuote % QUOTES.length]; }

    public int getProcessPercent() {
        if (maxProcessTime == 0) return 0;
        return (processTime * 100) / maxProcessTime;
    }

    // IInventory
    @Override public int getSizeInventory() { return 5; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) { return inventory[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count) {
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

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
               player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index < 4; // Only input slots
    }
    @Override public int getField(int id) {
        switch (id) {
            case 0: return processTime;
            case 1: return maxProcessTime;
            case 2: return energyStorage.getEnergyStored();
            case 3: return energyStorage.getMaxEnergyStored();
            default: return 0;
        }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: processTime = value; break;
            case 1: maxProcessTime = value; break;
        }
    }
    @Override public int getFieldCount() { return 4; }
    @Override public void clear() {
        for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Condenseur Dimensionnel"; }
    @Override public boolean hasCustomName() { return true; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    // NBT
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ProcessTime", processTime);
        compound.setInteger("Energy", energyStorage.getEnergyStored());
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
        processTime = compound.getInteger("ProcessTime");
        int energy = compound.getInteger("Energy");
        energyStorage = new InternalEnergyStorage(50000, 100, energy);
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < inventory.length) {
                inventory[slot] = new ItemStack(tag);
            }
        }
    }

    // Capabilities (RF input)
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        }
        return super.getCapability(capability, facing);
    }
}
