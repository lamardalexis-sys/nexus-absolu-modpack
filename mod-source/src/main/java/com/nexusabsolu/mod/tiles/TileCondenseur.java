package com.nexusabsolu.mod.tiles;

import net.minecraft.block.state.IBlockState;
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

public class TileCondenseur extends TileEntity implements ITickable, IInventory {

    private ItemStack[] inventory = new ItemStack[5];
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(50000, 100);
    private int processTime = 0;
    private int maxProcessTime = 200;
    private CondenseurRecipes.Recipe currentRecipe = null;
    private boolean processing = false;
    private boolean structureFormed = false;

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

    public boolean isStructureValid() { return structureFormed; }
    public void setStructureFormed(boolean formed) { this.structureFormed = formed; }

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (structureFormed && canProcess()) {
            if (energyStorage.getEnergyStored() >= 20) {
                energyStorage.drainInternal(20);
                processTime++;
                processing = true;
                if (processTime % 40 == 0) {
                    currentQuote = (currentQuote + 1) % QUOTES.length;
                }
                if (processTime >= maxProcessTime) {
                    processItem();
                    processTime = 0;
                    processing = false;
                    syncToClient();
                }
                // Sync to client every 10 ticks for TESR
                if (processTime % 10 == 0) {
                    syncToClient();
                }
                markDirty();
            }
        } else {
            if (processTime > 0) {
                processTime = 0;
                processing = false;
                markDirty();
                syncToClient();
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

    @Override public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit())
            stack.setCount(getInventoryStackLimit());
        markDirty();
        syncToClient();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
               player.getDistanceSq(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return index < 4; }

    @Override public int getField(int id) {
        switch (id) {
            case 0: return processTime;
            case 1: return maxProcessTime;
            case 2: return energyStorage.getEnergyStored();
            case 3: return energyStorage.getMaxEnergyStored();
            case 4: return structureFormed ? 1 : 0;
            default: return 0;
        }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: processTime = value; break;
            case 1: maxProcessTime = value; break;
            case 2: energyStorage.setEnergy(value); break;
            case 3: break;
            case 4: structureFormed = (value == 1); break;
        }
    }
    @Override public int getFieldCount() { return 5; }
    @Override public void clear() {
        for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Condenseur Dimensionnel"; }
    @Override public boolean hasCustomName() { return true; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ProcessTime", processTime);
        compound.setInteger("MaxProcessTime", maxProcessTime);
        compound.setInteger("Energy", energyStorage.getEnergyStored());
        compound.setBoolean("Formed", structureFormed);
        compound.setBoolean("Processing", processing);
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
        maxProcessTime = compound.getInteger("MaxProcessTime");
        if (maxProcessTime == 0) maxProcessTime = 200;
        int energy = compound.getInteger("Energy");
        energyStorage = new InternalEnergyStorage(50000, 100, energy);
        structureFormed = compound.getBoolean("Formed");
        processing = compound.getBoolean("Processing");
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < inventory.length) {
                inventory[slot] = new ItemStack(tag);
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        return super.getCapability(capability, facing);
    }

    // Client sync for TESR rendering
    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(
            pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
            net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }
}
