package com.nexusabsolu.mod.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEnergyInput extends TileEntity {

    private final InternalEnergyStorage energy = new InternalEnergyStorage(100000, 1000);
    private BlockPos masterPos = null;

    public BlockPos getMasterPos() { return masterPos; }
    public void setMasterPos(BlockPos pos) {
        this.masterPos = pos;
        markDirty();
    }

    public IEnergyStorage getEnergyStorage() { return energy; }

    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }

    /**
     * Called by the Condenseur T2 master to drain RF from this input.
     */
    public int drain(int amount) {
        int drained = Math.min(amount, energy.getEnergyStored());
        energy.drainInternal(drained);
        markDirty();
        return drained;
    }

    // === NBT ===

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Energy", energy.getEnergyStored());
        if (masterPos != null) {
            compound.setInteger("MasterX", masterPos.getX());
            compound.setInteger("MasterY", masterPos.getY());
            compound.setInteger("MasterZ", masterPos.getZ());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        energy.setEnergy(compound.getInteger("Energy"));
        if (compound.hasKey("MasterX")) {
            masterPos = new BlockPos(
                compound.getInteger("MasterX"),
                compound.getInteger("MasterY"),
                compound.getInteger("MasterZ"));
        }
    }

    // === Capabilities (accept RF from all sides) ===

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) energy;
        return super.getCapability(capability, facing);
    }
}
