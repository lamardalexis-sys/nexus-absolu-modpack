package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.blocks.machines.BlockAutoScavenger;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.ItemPioche;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
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
    private static final int MINE_INTERVAL = 10; // ticks between each swing (0.5s)

    private static final int SLOT_PICKAXE = 0;  // Input: our custom pickaxe
    private static final int SLOT_OUT_START = 1;
    private static final int SLOT_OUT_END = 5;
    private static final int TOTAL_SLOTS = 6;

    private static final int[] SLOTS_TOP = {SLOT_PICKAXE};
    private static final int[] SLOTS_BOTTOM = {1, 2, 3, 4, 5};
    private static final int[] SLOTS_NONE = {};

    // -- State --
    private ItemStack[] inventory = new ItemStack[TOTAL_SLOTS];
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INPUT);
    private int mineTimer = 0;
    private boolean processing = false;
    private Random rand = new Random();
    private EnumFacing cachedFacing = EnumFacing.NORTH;

    public TileAutoScavenger() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    // -- Accessors for TESR --
    public boolean isProcessing() { return processing; }
    public int getMineTimer() { return mineTimer; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    public ItemStack getPickaxeStack() { return inventory[SLOT_PICKAXE]; }

    public EnumFacing getFacing() {
        if (world != null) {
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockAutoScavenger) {
                cachedFacing = state.getValue(BlockAutoScavenger.FACING);
            }
        }
        return cachedFacing;
    }

    // -- Processing --

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean wasProcessing = processing;

        if (canMine()) {
            if (energyStorage.getEnergyStored() >= RF_PER_TICK) {
                energyStorage.drainInternal(RF_PER_TICK);
                processing = true;
                mineTimer++;

                if (mineTimer >= MINE_INTERVAL) {
                    doMineSwing();
                    mineTimer = 0;
                }
                markDirty();
            } else {
                processing = false;
            }
        } else {
            if (mineTimer > 0) mineTimer = 0;
            processing = false;
        }

        if (wasProcessing != processing || (processing && mineTimer == 0)) {
            syncToClient();
        }
    }

    /** Check if we can mine: need a pickaxe + CM wall in front. */
    private boolean canMine() {
        // Need a pickaxe in the slot
        ItemStack tool = inventory[SLOT_PICKAXE];
        if (tool.isEmpty() || !(tool.getItem() instanceof ItemPioche)) return false;

        // Check block in front
        EnumFacing facing = getFacing();
        BlockPos frontPos = pos.offset(facing);
        Block frontBlock = world.getBlockState(frontPos).getBlock();

        if (frontBlock.getRegistryName() == null) return false;
        return frontBlock.getRegistryName().toString().equals("compactmachines3:wall");
    }

    /** Execute one mining swing: generate drops + damage pickaxe. */
    private void doMineSwing() {
        ItemStack tool = inventory[SLOT_PICKAXE];
        if (tool.isEmpty() || !(tool.getItem() instanceof ItemPioche)) return;

        ItemPioche pioche = (ItemPioche) tool.getItem();
        int multiplier = pioche.getDustMultiplier();

        // Generate drops (exact same logic as ScavengeEventHandler)
        double r = rand.nextDouble();

        if (multiplier <= 1) {
            // Pioche Fragmentee: wall_dust guaranteed + bonus
            insertIntoOutput(new ItemStack(ModItems.WALL_DUST, 1 + rand.nextInt(2)));
            if (r < 0.30)      insertIntoOutput(new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
            else if (r < 0.50) insertIntoOutput(new ItemStack(Items.FLINT, 1));
            else if (r < 0.65) insertIntoOutput(new ItemStack(Items.CLAY_BALL, 1));
        } else {
            // Pioche Renforcee: grits + compose + wall_dust
            if (r < 0.12)       insertIntoOutput(new ItemStack(ModItems.IRON_GRIT, 1));
            else if (r < 0.25)  insertIntoOutput(new ItemStack(ModItems.COPPER_GRIT, 1));
            else if (r < 0.35)  insertIntoOutput(new ItemStack(ModItems.TIN_GRIT, 1));
            else if (r < 0.45)  insertIntoOutput(new ItemStack(Items.COAL, 1));
            else if (r < 0.53)  insertIntoOutput(new ItemStack(Items.REDSTONE, 1));
            else if (r < 0.58)  insertIntoOutput(new ItemStack(ModItems.NICKEL_GRIT, 1));
            else if (r < 0.635) insertIntoOutput(new ItemStack(ModItems.COMPOSE_A, 1));
            else                insertIntoOutput(new ItemStack(ModItems.WALL_DUST, 1));
        }

        // Damage the pickaxe
        int newDamage = tool.getItemDamage() + 1;
        if (newDamage >= tool.getMaxDamage()) {
            // Pickaxe broke
            inventory[SLOT_PICKAXE] = ItemStack.EMPTY;
        } else {
            tool.setItemDamage(newDamage);
        }
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
        // Output full: item is lost (machine is full)
    }

    // -- ISidedInventory --

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (side == EnumFacing.UP) return SLOTS_TOP;
        if (side == EnumFacing.DOWN) return SLOTS_BOTTOM;
        return SLOTS_NONE;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return index == SLOT_PICKAXE && direction == EnumFacing.UP
            && stack.getItem() instanceof ItemPioche;
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
        if (index == SLOT_PICKAXE) return stack.getItem() instanceof ItemPioche;
        return false;
    }
    @Override public int getField(int id) {
        switch (id) {
            case 0: return mineTimer;
            case 1: return energyStorage.getEnergyStored();
            case 2: return processing ? 1 : 0;
            default: return 0;
        }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: mineTimer = value; break;
            case 1: energyStorage.setEnergy(value); break;
            case 2: processing = (value == 1); break;
        }
    }
    @Override public int getFieldCount() { return 3; }
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
        compound.setInteger("MineTimer", mineTimer);
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
        mineTimer = compound.getInteger("MineTimer");
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
