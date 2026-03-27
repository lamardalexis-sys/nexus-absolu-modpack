package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.blocks.machines.BlockCondenseurFormed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileCondenseurEnergy extends TileEntity {

    private BlockPos masterPos = null;

    private TileCondenseur findMaster() {
        if (masterPos != null) {
            TileEntity te = world.getTileEntity(masterPos);
            if (te instanceof TileCondenseur) return (TileCondenseur) te;
            masterPos = null;
        }
        // Search nearby for master (position 0)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos check = pos.add(dx, 0, dz);
                IBlockState s = world.getBlockState(check);
                if (s.getBlock() instanceof BlockCondenseurFormed) {
                    if (s.getValue(BlockCondenseurFormed.POSITION) == 0) {
                        TileEntity te = world.getTileEntity(check);
                        if (te instanceof TileCondenseur) {
                            masterPos = check;
                            return (TileCondenseur) te;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            TileCondenseur master = findMaster();
            if (master != null) {
                return master.getCapability(capability, facing);
            }
            // Fallback: dummy storage that does nothing
            return CapabilityEnergy.ENERGY.cast(new IEnergyStorage() {
                public int receiveEnergy(int max, boolean sim) { return 0; }
                public int extractEnergy(int max, boolean sim) { return 0; }
                public int getEnergyStored() { return 0; }
                public int getMaxEnergyStored() { return 0; }
                public boolean canExtract() { return false; }
                public boolean canReceive() { return true; }
            });
        }
        return super.getCapability(capability, facing);
    }
}
