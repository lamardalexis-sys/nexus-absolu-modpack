package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.blocks.BlockCompose;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class TileConvertisseur extends TileEntity implements ITickable {

    private final InternalEnergyStorage energy = new InternalEnergyStorage(100000, 0, 1000, 0);
    private int currentRFPerTick = 0;
    private int composeCount = 0;

    // Facteurs décroissants par nombre de blocs
    private static final float[] DECAY = {
        0.0F,   // 0 blocs
        1.0F,   // 1 bloc  = 100%
        0.85F,  // 2 blocs = 85%
        0.75F,  // 3 blocs = 75%
        0.65F,  // 4 blocs = 65%
        0.55F,  // 5 blocs = 55%
        0.50F   // 6 blocs = 50%
    };

    @Override
    public void update() {
        if (world.isRemote) return;

        // Scan every 20 ticks (1 seconde)
        if (world.getTotalWorldTime() % 20 == 0) {
            scanAdjacentBlocks();
        }

        // Generate RF
        if (currentRFPerTick > 0) {
            energy.generateInternal(currentRFPerTick);
        }

        // Push RF to adjacent machines
        if (energy.getEnergyStored() > 0) {
            pushEnergy();
        }
    }

    private void scanAdjacentBlocks() {
        int totalRF = 0;
        int count = 0;

        for (EnumFacing facing : EnumFacing.VALUES) {
            Block neighbor = world.getBlockState(pos.offset(facing)).getBlock();
            if (neighbor instanceof BlockCompose) {
                BlockCompose compose = (BlockCompose) neighbor;
                count++;
                totalRF += compose.getBaseRF();
            }
        }

        composeCount = count;

        if (count > 0 && count <= 6) {
            float decay = DECAY[count];
            currentRFPerTick = (int)(totalRF * decay);
        } else {
            currentRFPerTick = 0;
        }
    }

    private void pushEnergy() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te == null) continue;
            if (te.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                IEnergyStorage target = te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                if (target != null && target.canReceive()) {
                    int toSend = Math.min(energy.getEnergyStored(), 1000);
                    int sent = target.receiveEnergy(toSend, false);
                    if (sent > 0) {
                        energy.drainInternal(sent);
                    }
                }
            }
        }
    }

    public int getCurrentRFPerTick() { return currentRFPerTick; }
    public int getComposeCount() { return composeCount; }
    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(cap, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energy);
        }
        return super.getCapability(cap, facing);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Energy", energy.getEnergyStored());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int stored = compound.getInteger("Energy");
        energy.drainInternal(energy.getEnergyStored());
        energy.generateInternal(stored);
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
