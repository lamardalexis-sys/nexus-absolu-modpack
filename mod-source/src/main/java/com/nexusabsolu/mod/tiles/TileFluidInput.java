package com.nexusabsolu.mod.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileFluidInput extends TileEntity {

    private final FluidTank tank = new FluidTank(16000);
    private BlockPos masterPos = null;

    public BlockPos getMasterPos() { return masterPos; }
    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        markDirty();
    }

    public FluidTank getTank() { return tank; }
    public int getFluidAmount() { return tank.getFluidAmount(); }

    public FluidStack drain(int amount) {
        FluidStack drained = tank.drain(amount, true);
        if (drained != null && drained.amount > 0) markDirty();
        return drained;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        tank.writeToNBT(nbt);
        if (masterPos != null) {
            nbt.setInteger("MasterX", masterPos.getX());
            nbt.setInteger("MasterY", masterPos.getY());
            nbt.setInteger("MasterZ", masterPos.getZ());
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.readFromNBT(nbt);
        if (nbt.hasKey("MasterX")) {
            masterPos = new BlockPos(
                nbt.getInteger("MasterX"),
                nbt.getInteger("MasterY"),
                nbt.getInteger("MasterZ"));
        }
    }

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return true;
        return super.hasCapability(cap, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) tank;
        return super.getCapability(cap, facing);
    }
}
