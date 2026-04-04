package com.nexusabsolu.mod.tiles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileMachineHumaine extends TileEntity implements ITickable, IInventory {

    // --- Constants ---
    public static final int RF_PER_TICK = 10;
    public static final int RF_CAPACITY = 100000;
    public static final int RF_MAX_RECEIVE = 100;
    public static final int TANK_CAPACITY = 4000;
    public static final int WATER_PER_CYCLE = 100;  // mB
    public static final int OUTPUT_PER_CYCLE = 250;  // mB
    public static final int PROCESS_TIME = 200;  // ticks (10s)

    public static final int SLOT_FOOD = 0;
    public static final int SLOT_BUCKET_IN = 1;
    public static final int SLOT_BUCKET_OUT = 2;
    public static final int INV_SIZE = 3;
    public static final int FIELD_COUNT = 10;

    // --- State ---
    private final InternalEnergyStorage energy =
        new InternalEnergyStorage(RF_CAPACITY, RF_MAX_RECEIVE);
    private final FluidTank waterTank = new FluidTank(TANK_CAPACITY);
    private final FluidTank outputTank = new FluidTank(TANK_CAPACITY);
    private final NonNullList<ItemStack> inventory =
        NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
    private final SideConfig sideConfig = new SideConfig();
    private int progress = 0;

    public TileMachineHumaine() {
        sideConfig.setDefaults();
    }

    // ==================== PROCESSING ====================

    @Override
    public void update() {
        if (world.isRemote) return;

        if (canProcess()) {
            energy.drainInternal(RF_PER_TICK);
            progress++;
            if (progress >= PROCESS_TIME) {
                finishProcess();
                progress = 0;
            }
            markDirty();
        } else if (progress > 0) {
            progress = 0;
            markDirty();
        }

        tryFillBucket();

        if (sideConfig.isEject(SideConfig.TYPE_OUTPUT)) autoEjectFluid();
        if (sideConfig.isAutoPull(SideConfig.TYPE_WATER)) autoPullFluid();
    }

    private boolean canProcess() {
        ItemStack food = inventory.get(SLOT_FOOD);
        if (food.isEmpty() || !(food.getItem() instanceof ItemFood)) return false;
        if (waterTank.getFluidAmount() < WATER_PER_CYCLE) return false;
        if (energy.getEnergyStored() < RF_PER_TICK) return false;
        if (outputTank.getFluidAmount() + OUTPUT_PER_CYCLE > TANK_CAPACITY) return false;
        return true;
    }

    private void finishProcess() {
        ItemStack food = inventory.get(SLOT_FOOD);
        int foodValue = 1;
        if (food.getItem() instanceof ItemFood) {
            foodValue = ((ItemFood) food.getItem()).getHealAmount(food);
        }
        food.shrink(1);
        if (food.isEmpty()) inventory.set(SLOT_FOOD, ItemStack.EMPTY);

        // Drain water
        waterTank.drain(WATER_PER_CYCLE, true);

        // Produce output (bonus from food value)
        Fluid diarrhee = FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee != null) {
            int amount = OUTPUT_PER_CYCLE + (foodValue * 25);
            amount = Math.min(amount, TANK_CAPACITY - outputTank.getFluidAmount());
            outputTank.fill(new FluidStack(diarrhee, amount), true);
        }
    }

    private void tryFillBucket() {
        ItemStack bucketIn = inventory.get(SLOT_BUCKET_IN);
        ItemStack bucketOut = inventory.get(SLOT_BUCKET_OUT);
        if (bucketIn.isEmpty() || bucketIn.getItem() != Items.BUCKET) return;
        if (!bucketOut.isEmpty()) return;
        if (outputTank.getFluidAmount() < Fluid.BUCKET_VOLUME) return;

        Fluid diarrhee = FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee == null) return;

        ItemStack filled = FluidUtil.getFilledBucket(
            new FluidStack(diarrhee, Fluid.BUCKET_VOLUME));
        if (filled.isEmpty()) return;

        outputTank.drain(Fluid.BUCKET_VOLUME, true);
        bucketIn.shrink(1);
        if (bucketIn.isEmpty()) inventory.set(SLOT_BUCKET_IN, ItemStack.EMPTY);
        inventory.set(SLOT_BUCKET_OUT, filled);
    }

    private void autoEjectFluid() {
        if (outputTank.getFluidAmount() <= 0) return;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, face.ordinal())) continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null) continue;
            IFluidHandler h = getNeighborFluidHandler(te, face);
            if (h == null) continue;
            FluidStack offer = outputTank.drain(100, false);
            if (offer != null && offer.amount > 0) {
                int sent = h.fill(offer, true);
                if (sent > 0) outputTank.drain(sent, true);
            }
        }
    }

    private void autoPullFluid() {
        if (waterTank.getFluidAmount() >= TANK_CAPACITY) return;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!sideConfig.isFaceActive(SideConfig.TYPE_WATER, face.ordinal())) continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null) continue;
            IFluidHandler h = getNeighborFluidHandler(te, face);
            if (h == null) continue;
            int space = TANK_CAPACITY - waterTank.getFluidAmount();
            FluidStack pulled = h.drain(
                new FluidStack(FluidRegistry.WATER, Math.min(space, 100)), true);
            if (pulled != null && pulled.amount > 0) {
                waterTank.fill(pulled, true);
            }
        }
    }

    private IFluidHandler getNeighborFluidHandler(TileEntity te, EnumFacing myFace) {
        EnumFacing theirFace = myFace.getOpposite();
        if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, theirFace)) {
            return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, theirFace);
        }
        return null;
    }

    // ==================== FIELD SYNC ====================

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return energy.getEnergyStored();
            case 1: return waterTank.getFluidAmount();
            case 2: return outputTank.getFluidAmount();
            case 3: return progress;
            case 4: return sideConfig.getFaceBits(0);
            case 5: return sideConfig.getFaceBits(1);
            case 6: return sideConfig.getFaceBits(2);
            case 7: return sideConfig.getFaceBits(3);
            case 8: return sideConfig.getEjectBits();
            case 9: return sideConfig.getAutoPullBits();
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: energy.setEnergy(value); break;
            case 1: syncTankLevel(waterTank, value, FluidRegistry.WATER); break;
            case 2: syncTankLevel(outputTank, value,
                       FluidRegistry.getFluid("diarrhee_liquide")); break;
            case 3: progress = value; break;
            case 4: sideConfig.setFaceBits(0, value); break;
            case 5: sideConfig.setFaceBits(1, value); break;
            case 6: sideConfig.setFaceBits(2, value); break;
            case 7: sideConfig.setFaceBits(3, value); break;
            case 8: sideConfig.setEjectBits(value); break;
            case 9: sideConfig.setAutoPullBits(value); break;
        }
    }

    private void syncTankLevel(FluidTank tank, int amount, Fluid fluid) {
        tank.drain(Integer.MAX_VALUE, true);
        if (amount > 0 && fluid != null) {
            tank.fill(new FluidStack(fluid, amount), true);
        }
    }

    @Override
    public int getFieldCount() { return FIELD_COUNT; }

    // ==================== BUTTON ACTIONS ====================

    public boolean handleAction(int action) {
        if (action >= 0 && action < 24) {
            sideConfig.toggleFace(action / 6, action % 6);
            markDirty();
            return true;
        }
        if (action >= 24 && action < 28) {
            sideConfig.toggleEject(action - 24);
            markDirty();
            return true;
        }
        if (action >= 28 && action < 32) {
            sideConfig.toggleAutoPull(action - 28);
            markDirty();
            return true;
        }
        return false;
    }

    // ==================== GETTERS ====================

    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergy() { return RF_CAPACITY; }
    public int getWaterLevel() { return waterTank.getFluidAmount(); }
    public int getOutputLevel() { return outputTank.getFluidAmount(); }
    public int getTankCapacity() { return TANK_CAPACITY; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROCESS_TIME; }
    public SideConfig getSideConfig() { return sideConfig; }

    // ==================== IInventory ====================

    @Override public int getSizeInventory() { return INV_SIZE; }

    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) { if (!s.isEmpty()) return false; }
        return true;
    }

    @Override public ItemStack getStackInSlot(int i) {
        return (i >= 0 && i < INV_SIZE) ? inventory.get(i) : ItemStack.EMPTY;
    }

    @Override public ItemStack decrStackSize(int i, int count) {
        ItemStack s = getStackInSlot(i);
        if (s.isEmpty()) return ItemStack.EMPTY;
        ItemStack r;
        if (s.getCount() <= count) {
            r = s.copy();
            inventory.set(i, ItemStack.EMPTY);
        } else {
            r = s.splitStack(count);
        }
        markDirty();
        return r;
    }

    @Override public ItemStack removeStackFromSlot(int i) {
        ItemStack s = getStackInSlot(i);
        inventory.set(i, ItemStack.EMPTY);
        return s;
    }

    @Override public void setInventorySlotContents(int i, ItemStack stack) {
        if (i >= 0 && i < INV_SIZE) {
            inventory.set(i, stack);
            if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
                stack.setCount(getInventoryStackLimit());
            }
            markDirty();
        }
    }

    @Override public int getInventoryStackLimit() { return 64; }

    @Override public boolean isUsableByPlayer(EntityPlayer p) {
        return p.getDistanceSq(
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override public void openInventory(EntityPlayer p) {}
    @Override public void closeInventory(EntityPlayer p) {}

    @Override public boolean isItemValidForSlot(int i, ItemStack s) {
        if (i == SLOT_FOOD) return s.getItem() instanceof ItemFood;
        if (i == SLOT_BUCKET_IN) return s.getItem() == Items.BUCKET;
        return false;
    }

    @Override public String getName() { return "container.machine_humaine"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public void clear() {
        for (int i = 0; i < INV_SIZE; i++) inventory.set(i, ItemStack.EMPTY);
    }

    // ==================== CAPABILITIES ====================

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY) {
            return facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_ENERGY, facing.ordinal());
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == null) return true;
            return sideConfig.isFaceActive(SideConfig.TYPE_WATER, facing.ordinal())
                || sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, facing.ordinal());
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_FOOD, facing.ordinal());
        }
        return super.hasCapability(cap, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energy);
        }
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            boolean w = facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_WATER, facing.ordinal());
            boolean o = facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, facing.ordinal());
            IFluidHandler handler;
            if (w && o) handler = new DualFluidHandler();
            else if (w) handler = new FillOnlyHandler(waterTank);
            else if (o) handler = new DrainOnlyHandler(outputTank);
            else return super.getCapability(cap, facing);
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(handler);
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                new FoodItemHandler());
        }
        return super.getCapability(cap, facing);
    }

    // --- Capability Wrappers ---

    private class FoodItemHandler implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int s) {
            return s == 0 ? inventory.get(SLOT_FOOD) : ItemStack.EMPTY;
        }
        @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) {
            if (s != 0 || stack.isEmpty()
                || !(stack.getItem() instanceof ItemFood)) return stack;
            ItemStack cur = inventory.get(SLOT_FOOD);
            if (cur.isEmpty()) {
                if (!sim) { inventory.set(SLOT_FOOD, stack.copy()); markDirty(); }
                return ItemStack.EMPTY;
            }
            if (!ItemStack.areItemsEqual(cur, stack)
                || !ItemStack.areItemStackTagsEqual(cur, stack)) return stack;
            int space = getInventoryStackLimit() - cur.getCount();
            int ins = Math.min(stack.getCount(), space);
            if (ins <= 0) return stack;
            if (!sim) { cur.grow(ins); markDirty(); }
            ItemStack rem = stack.copy();
            rem.shrink(ins);
            return rem.isEmpty() ? ItemStack.EMPTY : rem;
        }
        @Override public ItemStack extractItem(int s, int a, boolean sim) {
            return ItemStack.EMPTY;
        }
        @Override public int getSlotLimit(int s) { return 64; }
    }

    private class FillOnlyHandler implements IFluidHandler {
        private final FluidTank tank;
        FillOnlyHandler(FluidTank t) { this.tank = t; }
        @Override public IFluidTankProperties[] getTankProperties() {
            return tank.getTankProperties();
        }
        @Override public int fill(FluidStack r, boolean doFill) {
            return tank.fill(r, doFill);
        }
        @Override public FluidStack drain(FluidStack r, boolean d) { return null; }
        @Override public FluidStack drain(int max, boolean d) { return null; }
    }

    private class DrainOnlyHandler implements IFluidHandler {
        private final FluidTank tank;
        DrainOnlyHandler(FluidTank t) { this.tank = t; }
        @Override public IFluidTankProperties[] getTankProperties() {
            return tank.getTankProperties();
        }
        @Override public int fill(FluidStack r, boolean doFill) { return 0; }
        @Override public FluidStack drain(FluidStack r, boolean d) {
            return tank.drain(r, d);
        }
        @Override public FluidStack drain(int max, boolean d) {
            return tank.drain(max, d);
        }
    }

    private class DualFluidHandler implements IFluidHandler {
        @Override public IFluidTankProperties[] getTankProperties() {
            IFluidTankProperties[] a = waterTank.getTankProperties();
            IFluidTankProperties[] b = outputTank.getTankProperties();
            IFluidTankProperties[] c = new IFluidTankProperties[a.length + b.length];
            System.arraycopy(a, 0, c, 0, a.length);
            System.arraycopy(b, 0, c, a.length, b.length);
            return c;
        }
        @Override public int fill(FluidStack r, boolean d) {
            return waterTank.fill(r, d);
        }
        @Override public FluidStack drain(FluidStack r, boolean d) {
            return outputTank.drain(r, d);
        }
        @Override public FluidStack drain(int max, boolean d) {
            return outputTank.drain(max, d);
        }
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Energy", energy.getEnergyStored());
        nbt.setInteger("Progress", progress);

        NBTTagCompound wTag = new NBTTagCompound();
        waterTank.writeToNBT(wTag);
        nbt.setTag("WaterTank", wTag);

        NBTTagCompound oTag = new NBTTagCompound();
        outputTank.writeToNBT(oTag);
        nbt.setTag("OutputTank", oTag);

        sideConfig.writeToNBT(nbt);

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < INV_SIZE; i++) {
            if (!inventory.get(i).isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory.get(i).writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        nbt.setTag("Items", list);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy.setEnergy(nbt.getInteger("Energy"));
        progress = nbt.getInteger("Progress");

        if (nbt.hasKey("WaterTank")) {
            waterTank.readFromNBT(nbt.getCompoundTag("WaterTank"));
        }
        if (nbt.hasKey("OutputTank")) {
            outputTank.readFromNBT(nbt.getCompoundTag("OutputTank"));
        }
        sideConfig.readFromNBT(nbt);

        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < INV_SIZE) {
                inventory.set(slot, new ItemStack(tag));
            }
        }
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
