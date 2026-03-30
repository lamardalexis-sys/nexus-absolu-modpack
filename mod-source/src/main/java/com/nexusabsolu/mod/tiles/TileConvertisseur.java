package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.blocks.BlockCompose;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileConvertisseur extends TileEntity implements ITickable {

    private final InternalEnergyStorage energy = new InternalEnergyStorage(50000, 0, 500, 0);
    private int currentRFPerTick = 0;
    private int composeCount = 0;

    // Per-face: 0=empty, 1=A, 2=B, 3=C, 4=D, 5=E
    private int[] faceData = new int[6];

    // Output config: which faces push energy
    private boolean[] outputFaces = {true, true, true, true, true, true};

    private static final float[] DECAY = {
        0.0F, 1.0F, 0.85F, 0.75F, 0.65F, 0.55F, 0.50F
    };

    @Override
    public void update() {
        if (world.isRemote) return;

        if (world.getTotalWorldTime() % 20 == 0) {
            scanAdjacentBlocks();
        }

        if (currentRFPerTick > 0) {
            energy.generateInternal(currentRFPerTick);
        }

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
                String t = compose.getTier();
                int tier = t.equals("A") ? 1 : t.equals("B") ? 2 :
                           t.equals("C") ? 3 : t.equals("D") ? 4 :
                           t.equals("E") ? 5 : 0;
                faceData[facing.ordinal()] = tier;
            } else {
                faceData[facing.ordinal()] = 0;
            }
        }

        composeCount = count;

        if (count > 0 && count <= 6) {
            currentRFPerTick = (int)(totalRF * DECAY[count]);
        } else {
            currentRFPerTick = 0;
        }
    }

    private void pushEnergy() {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (!outputFaces[facing.ordinal()]) continue;
            TileEntity te = world.getTileEntity(pos.offset(facing));
            if (te == null) continue;
            if (te.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                IEnergyStorage target = te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());
                if (target != null && target.canReceive()) {
                    int toSend = Math.min(energy.getEnergyStored(), 500);
                    int sent = target.receiveEnergy(toSend, false);
                    if (sent > 0) {
                        energy.drainInternal(sent);
                    }
                }
            }
        }
    }

    public void toggleOutputFace(int face) {
        if (face >= 0 && face < 6) {
            outputFaces[face] = !outputFaces[face];
            markDirty();
        }
    }

    public boolean isOutputFace(int face) {
        return face >= 0 && face < 6 && outputFaces[face];
    }

    public int getCurrentRFPerTick() { return currentRFPerTick; }
    public int getComposeCount() { return composeCount; }
    public int getEnergyStored() { return energy.getEnergyStored(); }
    public int getMaxEnergyStored() { return energy.getMaxEnergyStored(); }
    public int[] getFaceData() { return faceData; }

    // Fields: 0=energy, 1=rfPerTick, 2=count, 3-8=faceData, 9-14=outputFaces
    public int getField(int id) {
        if (id == 0) return energy.getEnergyStored();
        if (id == 1) return currentRFPerTick;
        if (id == 2) return composeCount;
        if (id >= 3 && id <= 8) return faceData[id - 3];
        if (id >= 9 && id <= 14) return outputFaces[id - 9] ? 1 : 0;
        return 0;
    }

    public void setField(int id, int value) {
        if (id == 0) {
            energy.drainInternal(energy.getEnergyStored());
            energy.generateInternal(value);
        } else if (id == 1) {
            currentRFPerTick = value;
        } else if (id == 2) {
            composeCount = value;
        } else if (id >= 3 && id <= 8) {
            faceData[id - 3] = value;
        } else if (id >= 9 && id <= 14) {
            outputFaces[id - 9] = value != 0;
        }
    }

    public int getFieldCount() { return 15; }

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
        byte flags = 0;
        for (int i = 0; i < 6; i++) {
            if (outputFaces[i]) flags |= (1 << i);
        }
        compound.setByte("OutputFaces", flags);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        int stored = compound.getInteger("Energy");
        energy.drainInternal(energy.getEnergyStored());
        energy.generateInternal(stored);
        if (compound.hasKey("OutputFaces")) {
            byte flags = compound.getByte("OutputFaces");
            for (int i = 0; i < 6; i++) {
                outputFaces[i] = (flags & (1 << i)) != 0;
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
