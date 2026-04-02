package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.blocks.machines.BlockAutoScavenger;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
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
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import java.util.Random;

public class TileAutoScavenger extends TileEntity implements ITickable, ISidedInventory {

    // -- Constants --
    private static final int RF_PER_TICK = 15;
    private static final int ENERGY_CAPACITY = 5000;
    private static final int ENERGY_MAX_INPUT = 100;
    private static final int PROCESS_TIME = 40; // 2 seconds per wall

    private static final int SLOT_INPUT = 0;
    private static final int SLOT_OUT_START = 1;
    private static final int SLOT_OUT_END = 5;
    private static final int TOTAL_SLOTS = 6;

    private static final int[] SLOTS_TOP = {SLOT_INPUT};
    private static final int[] SLOTS_BOTTOM = {1, 2, 3, 4, 5};
    private static final int[] SLOTS_NONE = {};

    // -- Drop table (weighted) --
    private static final int TOTAL_WEIGHT = 100;

    // -- State --
    private ItemStack[] inventory = new ItemStack[TOTAL_SLOTS];
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INPUT);
    private int processTime = 0;
    private boolean processing = false;
    private Random rand = new Random();

    // Cached facing (for TESR)
    private EnumFacing cachedFacing = EnumFacing.NORTH;

    public TileAutoScavenger() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    // -- Accessors for TESR --
    public boolean isProcessing() { return processing; }
    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return PROCESS_TIME; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }

    public EnumFacing getFacing() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockAutoScavenger) {
                cachedFacing = state.getValue(BlockAutoScavenger.FACING);
            }
        }
        return cachedFacing;
    }

    public int getProcessPercent() {
        return (processTime * 100) / PROCESS_TIME;
    }

    // -- Processing --

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean wasProcessing = processing;

        if (canProcess()) {
            if (energyStorage.getEnergyStored() >= RF_PER_TICK) {
                energyStorage.drainInternal(RF_PER_TICK);
                processTime++;
                processing = true;

                if (processTime >= PROCESS_TIME) {
                    doProcess();
                    processTime = 0;
                }
                markDirty();
            } else {
                processing = false;
            }
        } else {
            if (processTime > 0) processTime = 0;
            processing = false;
        }

        // Sync to client when state changes
        if (wasProcessing != processing || (processing && processTime % 10 == 0)) {
            syncToClient();
        }
    }

    private boolean canProcess() {
        // Need a Nexus Wall in input
        if (inventory[SLOT_INPUT].isEmpty()) return false;
        Item inputItem = inventory[SLOT_INPUT].getItem();
        return inputItem == Item.getItemFromBlock(ModBlocks.NEXUS_WALL);
    }

    private void doProcess() {
        // Consume 1 Nexus Wall
        inventory[SLOT_INPUT].shrink(1);

        // Generate drops
        ItemStack drop = generateDrop();
        if (!drop.isEmpty()) {
            insertIntoOutput(drop);
        }
        // Small chance of bonus drop
        if (rand.nextInt(100) < 20) {
            ItemStack bonus = new ItemStack(ModItems.WALL_DUST, 1);
            insertIntoOutput(bonus);
        }
    }

    private ItemStack generateDrop() {
        int roll = rand.nextInt(TOTAL_WEIGHT);

        // 12% each: iron, copper, tin grits = 36%
        if (roll < 12) return new ItemStack(ModItems.IRON_GRIT, 1);
        if (roll < 24) return new ItemStack(ModItems.COPPER_GRIT, 1);
        if (roll < 36) return new ItemStack(ModItems.TIN_GRIT, 1);
        // 8% each: silver, nickel, lead = 24%
        if (roll < 44) return new ItemStack(ModItems.SILVER_GRIT, 1);
        if (roll < 52) return new ItemStack(ModItems.NICKEL_GRIT, 1);
        if (roll < 60) return new ItemStack(ModItems.LEAD_GRIT, 1);
        // 5% each: gold, osmium = 10%
        if (roll < 65) return new ItemStack(ModItems.GOLD_GRIT, 1);
        if (roll < 70) return new ItemStack(ModItems.OSMIUM_GRIT, 1);
        // 30%: wall_dust
        return new ItemStack(ModItems.WALL_DUST, 1);
    }

    private void insertIntoOutput(ItemStack stack) {
        for (int i = SLOT_OUT_START; i <= SLOT_OUT_END; i++) {
            if (inventory[i].isEmpty()) {
                inventory[i] = stack.copy();
                return;
            }
            if (inventory[i].isItemEqual(stack)
                && inventory[i].getCount() < inventory[i].getMaxStackSize()) {
                int space = inventory[i].getMaxStackSize() - inventory[i].getCount();
                int toAdd = Math.min(stack.getCount(), space);
                inventory[i].grow(toAdd);
                stack.shrink(toAdd);
                if (stack.isEmpty()) return;
            }
        }
        // Output full -- drop remains lost (machine is full)
    }

    // -- ISidedInventory --

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        EnumFacing facing = getFacing();
        if (side == EnumFacing.UP) return SLOTS_TOP;
        if (side == EnumFacing.DOWN) return SLOTS_BOTTOM;
        return SLOTS_NONE;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return index == SLOT_INPUT && direction == EnumFacing.UP
            && Item.getItemFromBlock(ModBlocks.NEXUS_WALL) == stack.getItem();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return index >= SLOT_OUT_START && index <= SLOT_OUT_END
            && direction == EnumFacing.DOWN;
    }

    // -- IInventory --

    @Override public int getSizeInventory() { return TOTAL_SLOTS; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) {
        if (index < 0 || index >= TOTAL_SLOTS) return ItemStack.EMPTY;
        return inventory[index];
    }
    @Override public ItemStack decrStackSize(int index, int count) {
        if (index < 0 || index >= TOTAL_SLOTS || inventory[index].isEmpty()) return ItemStack.EMPTY;
        ItemStack result;
        if (inventory[index].getCount() <= count) {
            result = inventory[index];
            inventory[index] = ItemStack.EMPTY;
        } else {
            result = inventory[index].splitStack(count);
        }
        markDirty();
        return result;
    }
    @Override public ItemStack removeStackFromSlot(int index) {
        if (index < 0 || index >= TOTAL_SLOTS) return ItemStack.EMPTY;
        ItemStack stack = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return stack;
    }
    @Override public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < 0 || index >= TOTAL_SLOTS) return;
        inventory[index] = stack;
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit())
            stack.setCount(getInventoryStackLimit());
        markDirty();
    }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this
            && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_INPUT) return Item.getItemFromBlock(ModBlocks.NEXUS_WALL) == stack.getItem();
        return false;
    }
    @Override public int getField(int id) {
        switch (id) {
            case 0: return processTime;
            case 1: return energyStorage.getEnergyStored();
            default: return 0;
        }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: processTime = value; break;
            case 1: energyStorage.setEnergy(value); break;
        }
    }
    @Override public int getFieldCount() { return 2; }
    @Override public void clear() {
        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Auto-Scavenger"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    // -- Capabilities --

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            // Only accept energy from the back face
            EnumFacing back = getFacing().getOpposite();
            return facing == back || facing == null;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return facing == EnumFacing.UP || facing == EnumFacing.DOWN;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            EnumFacing back = getFacing().getOpposite();
            if (facing == back || facing == null) {
                return CapabilityEnergy.ENERGY.cast(energyStorage);
            }
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                return (T) new SidedInvWrapper(this, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    // -- NBT --

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ProcessTime", processTime);
        compound.setInteger("Energy", energyStorage.getEnergyStored());
        compound.setBoolean("Processing", processing);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
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
        energyStorage = new InternalEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INPUT, energy);
        processing = compound.getBoolean("Processing");
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < TOTAL_SLOTS) {
                inventory[slot] = new ItemStack(tag);
            }
        }
    }

    // -- Client sync --

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() { return writeToNBT(new NBTTagCompound()); }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) { readFromNBT(tag); }
}
