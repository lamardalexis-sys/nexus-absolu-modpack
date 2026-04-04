package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileMachineKRDA extends TileEntity implements ITickable, IInventory {

    public static final int RF_PER_TICK = 50;
    public static final int RF_CAPACITY = 500000;
    public static final int RF_MAX_RECEIVE = 500;
    public static final int TANK_CAPACITY = 4000;
    public static final int FLUID_PER_CYCLE = 500; // mB diarrhee
    public static final int PROCESS_TIME = 200;     // 10 seconds

    public static final int SLOT_INPUT = 0;   // signalum
    public static final int SLOT_OUTPUT = 1;  // signalhee
    public static final int INV_SIZE = 2;
    public static final int FIELD_COUNT = 10;

    private final InternalEnergyStorage energy =
        new InternalEnergyStorage(RF_CAPACITY, RF_MAX_RECEIVE);
    private final FluidTank diarrheeTank = new FluidTank(TANK_CAPACITY);
    private final NonNullList<ItemStack> inventory =
        NonNullList.withSize(INV_SIZE, ItemStack.EMPTY);
    private final SideConfig sideConfig = new SideConfig();
    private int progress = 0;

    public TileMachineKRDA() {
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

        if (sideConfig.isEject(SideConfig.TYPE_OUTPUT)) autoEjectItems();
        if (sideConfig.isAutoPull(SideConfig.TYPE_WATER)) autoPullFluid();
        if (sideConfig.isAutoPull(SideConfig.TYPE_FOOD)) autoPullItems();
    }

    private boolean canProcess() {
        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty() || !isSignalum(input)) return false;
        if (diarrheeTank.getFluidAmount() < FLUID_PER_CYCLE) return false;
        if (energy.getEnergyStored() < RF_PER_TICK) return false;
        // Check output space
        ItemStack out = inventory.get(SLOT_OUTPUT);
        if (!out.isEmpty()) {
            if (out.getItem() != ModItems.SIGNALHEE_INGOT) return false;
            if (out.getCount() >= out.getMaxStackSize()) return false;
        }
        return true;
    }

    private boolean isSignalum(ItemStack stack) {
        String regName = stack.getItem().getRegistryName().toString();
        String unloc = stack.getItem().getUnlocalizedName();
        return regName.contains("signalum") && regName.contains("ingot")
            || unloc.contains("signalum") && unloc.contains("ingot")
            || regName.equals("thermalfoundation:material")
                && stack.getMetadata() == 165;
    }

    private void finishProcess() {
        inventory.get(SLOT_INPUT).shrink(1);
        if (inventory.get(SLOT_INPUT).isEmpty()) {
            inventory.set(SLOT_INPUT, ItemStack.EMPTY);
        }
        diarrheeTank.drain(FLUID_PER_CYCLE, true);

        ItemStack out = inventory.get(SLOT_OUTPUT);
        if (out.isEmpty()) {
            inventory.set(SLOT_OUTPUT,
                new ItemStack(ModItems.SIGNALHEE_INGOT, 1));
        } else {
            out.grow(1);
        }
    }

    private void autoEjectItems() {
        ItemStack out = inventory.get(SLOT_OUTPUT);
        if (out.isEmpty()) return;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, face.ordinal()))
                continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null) continue;
            if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    face.getOpposite())) continue;
            IItemHandler h = te.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
            if (h == null) continue;
            for (int s = 0; s < h.getSlots(); s++) {
                ItemStack remain = h.insertItem(s, out.copy(), false);
                int sent = out.getCount() - (remain.isEmpty() ? 0 : remain.getCount());
                if (sent > 0) {
                    out.shrink(sent);
                    if (out.isEmpty()) {
                        inventory.set(SLOT_OUTPUT, ItemStack.EMPTY);
                        return;
                    }
                }
            }
        }
    }

    private void autoPullFluid() {
        if (diarrheeTank.getFluidAmount() >= TANK_CAPACITY) return;
        Fluid diarrhee = FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee == null) return;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!sideConfig.isFaceActive(SideConfig.TYPE_WATER, face.ordinal()))
                continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null) continue;
            if (!te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                    face.getOpposite())) continue;
            IFluidHandler h = te.getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
            if (h == null) continue;
            int space = TANK_CAPACITY - diarrheeTank.getFluidAmount();
            FluidStack pulled = h.drain(
                new FluidStack(diarrhee, Math.min(space, 100)), true);
            if (pulled != null && pulled.amount > 0) {
                diarrheeTank.fill(pulled, true);
            }
        }
    }

    private void autoPullItems() {
        ItemStack cur = inventory.get(SLOT_INPUT);
        if (!cur.isEmpty() && cur.getCount() >= cur.getMaxStackSize()) return;
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!sideConfig.isFaceActive(SideConfig.TYPE_FOOD, face.ordinal()))
                continue;
            TileEntity te = world.getTileEntity(pos.offset(face));
            if (te == null) continue;
            if (!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                    face.getOpposite())) continue;
            IItemHandler h = te.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, face.getOpposite());
            if (h == null) continue;
            for (int s = 0; s < h.getSlots(); s++) {
                ItemStack avail = h.extractItem(s, 1, true);
                if (!avail.isEmpty() && isSignalum(avail)) {
                    ItemStack extracted = h.extractItem(s, 1, false);
                    if (cur.isEmpty()) {
                        inventory.set(SLOT_INPUT, extracted);
                        return;
                    } else if (ItemStack.areItemsEqual(cur, extracted)) {
                        cur.grow(extracted.getCount());
                        return;
                    }
                }
            }
        }
    }

    // ==================== FIELD SYNC ====================

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return energy.getEnergyStored();
            case 1: return diarrheeTank.getFluidAmount();
            case 2: return 0; // unused (compat with shared GUI code)
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
            case 1:
                diarrheeTank.drain(Integer.MAX_VALUE, true);
                Fluid f = FluidRegistry.getFluid("diarrhee_liquide");
                if (value > 0 && f != null)
                    diarrheeTank.fill(new FluidStack(f, value), true);
                break;
            case 3: progress = value; break;
            case 4: sideConfig.setFaceBits(0, value); break;
            case 5: sideConfig.setFaceBits(1, value); break;
            case 6: sideConfig.setFaceBits(2, value); break;
            case 7: sideConfig.setFaceBits(3, value); break;
            case 8: sideConfig.setEjectBits(value); break;
            case 9: sideConfig.setAutoPullBits(value); break;
        }
    }

    @Override public int getFieldCount() { return FIELD_COUNT; }

    public boolean handleAction(int action) {
        if (action >= 0 && action < 24) {
            sideConfig.toggleFace(action / 6, action % 6);
            markDirty(); return true;
        }
        if (action >= 24 && action < 28) {
            sideConfig.toggleEject(action - 24);
            markDirty(); return true;
        }
        if (action >= 28 && action < 32) {
            sideConfig.toggleAutoPull(action - 28);
            markDirty(); return true;
        }
        return false;
    }

    // ==================== GETTERS ====================

    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergy() { return RF_CAPACITY; }
    public int getFluidLevel() { return diarrheeTank.getFluidAmount(); }
    public int getTankCapacity() { return TANK_CAPACITY; }
    public int getProgress() { return progress; }
    public int getMaxProgress() { return PROCESS_TIME; }
    public SideConfig getSideConfig() { return sideConfig; }

    // ==================== IInventory ====================

    @Override public int getSizeInventory() { return INV_SIZE; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
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
            r = s.copy(); inventory.set(i, ItemStack.EMPTY);
        } else { r = s.splitStack(count); }
        markDirty(); return r;
    }
    @Override public ItemStack removeStackFromSlot(int i) {
        ItemStack s = getStackInSlot(i);
        inventory.set(i, ItemStack.EMPTY); return s;
    }
    @Override public void setInventorySlotContents(int i, ItemStack stack) {
        if (i >= 0 && i < INV_SIZE) {
            inventory.set(i, stack);
            if (!stack.isEmpty() && stack.getCount() > 64)
                stack.setCount(64);
            markDirty();
        }
    }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer p) {
        return p.getDistanceSq(
            pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5) <= 64.0;
    }
    @Override public void openInventory(EntityPlayer p) {}
    @Override public void closeInventory(EntityPlayer p) {}
    @Override public boolean isItemValidForSlot(int i, ItemStack s) {
        if (i == SLOT_INPUT) return isSignalum(s);
        return false;
    }
    @Override public String getName() { return "container.machine_krda"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public void clear() {
        for (int i = 0; i < INV_SIZE; i++) inventory.set(i, ItemStack.EMPTY);
    }

    // ==================== CAPABILITIES ====================

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY)
            return facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_ENERGY, facing.ordinal());
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_WATER, facing.ordinal());
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) return true;
            return sideConfig.isFaceActive(SideConfig.TYPE_FOOD, facing.ordinal())
                || sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, facing.ordinal());
        }
        return super.hasCapability(cap, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(energy);
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                new FillOnlyHandler(diarrheeTank));
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            boolean inp = facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_FOOD, facing.ordinal());
            boolean out = facing == null
                || sideConfig.isFaceActive(SideConfig.TYPE_OUTPUT, facing.ordinal());
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(
                new SidedItemHandler(inp, out));
        }
        return super.getCapability(cap, facing);
    }

    private class SidedItemHandler implements IItemHandler {
        private final boolean allowInput, allowOutput;
        SidedItemHandler(boolean inp, boolean out) {
            this.allowInput = inp; this.allowOutput = out;
        }
        @Override public int getSlots() { return INV_SIZE; }
        @Override public ItemStack getStackInSlot(int s) {
            return inventory.get(s);
        }
        @Override public ItemStack insertItem(int s, ItemStack stack, boolean sim) {
            if (s != SLOT_INPUT || !allowInput || !isSignalum(stack)) return stack;
            ItemStack cur = inventory.get(SLOT_INPUT);
            if (cur.isEmpty()) {
                if (!sim) { inventory.set(SLOT_INPUT, stack.copy()); markDirty(); }
                return ItemStack.EMPTY;
            }
            if (!ItemStack.areItemsEqual(cur, stack)
                || !ItemStack.areItemStackTagsEqual(cur, stack)) return stack;
            int space = 64 - cur.getCount();
            int ins = Math.min(stack.getCount(), space);
            if (ins <= 0) return stack;
            if (!sim) { cur.grow(ins); markDirty(); }
            ItemStack rem = stack.copy(); rem.shrink(ins);
            return rem.isEmpty() ? ItemStack.EMPTY : rem;
        }
        @Override public ItemStack extractItem(int s, int amount, boolean sim) {
            if (s != SLOT_OUTPUT || !allowOutput) return ItemStack.EMPTY;
            ItemStack out = inventory.get(SLOT_OUTPUT);
            if (out.isEmpty()) return ItemStack.EMPTY;
            int ext = Math.min(out.getCount(), amount);
            ItemStack result = out.copy(); result.setCount(ext);
            if (!sim) { out.shrink(ext);
                if (out.isEmpty()) inventory.set(SLOT_OUTPUT, ItemStack.EMPTY);
                markDirty();
            }
            return result;
        }
        @Override public int getSlotLimit(int s) { return 64; }
    }

    private class FillOnlyHandler implements IFluidHandler {
        private final FluidTank tank;
        FillOnlyHandler(FluidTank t) { this.tank = t; }
        @Override public IFluidTankProperties[] getTankProperties() {
            return tank.getTankProperties();
        }
        @Override public int fill(FluidStack r, boolean d) {
            if (r == null) return 0;
            Fluid diarrhee = FluidRegistry.getFluid("diarrhee_liquide");
            if (diarrhee == null || !r.getFluid().equals(diarrhee)) return 0;
            return tank.fill(r, d);
        }
        @Override public FluidStack drain(FluidStack r, boolean d) { return null; }
        @Override public FluidStack drain(int m, boolean d) { return null; }
    }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Energy", energy.getEnergyStored());
        nbt.setInteger("Progress", progress);
        NBTTagCompound tTag = new NBTTagCompound();
        diarrheeTank.writeToNBT(tTag);
        nbt.setTag("DiarrheeTank", tTag);
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
        if (nbt.hasKey("DiarrheeTank"))
            diarrheeTank.readFromNBT(nbt.getCompoundTag("DiarrheeTank"));
        sideConfig.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < INV_SIZE) inventory.set(slot, new ItemStack(tag));
        }
    }

    @Override public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }
    @Override public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }
}
