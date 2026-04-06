package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.ArrayList;
import java.util.List;

public class TilePortalVoss extends TileEntity implements ITickable {

    // Block types
    private static final int W  = 0;  // nexus_wall
    private static final int W2 = 1;  // nexus_wall_t2
    private static final int V3 = 2;  // vossium_iii_block
    private static final int V4 = 3;  // vossium_iv_block
    private static final int EIN = 4; // energy_input
    private static final int LIN = 5; // fluid_input
    private static final int CD = 6;  // compose_block_d
    private static final int LAVA = 7;// minecraft:lava
    // EC (8) = ecran_controle = THIS block (master), not in structure array

    private static final int ENERGY_REQUIRED = 1000000;
    private static final int FLUID_REQUIRED = 10000; // 10 buckets
    private static final int ENERGY_CAPACITY = 1200000;

    // Structure positions: {dx, dy, dz, type} relative to EC block
    // EC is at layer 5, row 3, col 3 of the build
    private static final List<int[]> STRUCTURE = new ArrayList<>();

    static {
        // === LAYER 0 (y=-5): 7x7 base ===
        // Row z=-3
        addRow(-5, -3, new int[]{W2, W2, W2, EIN, W2, W2, W2});
        // Row z=-2
        addRow(-5, -2, new int[]{W2, W2, V3, V3, V3, W2, W2});
        // Row z=-1
        addRow(-5, -1, new int[]{W2, V3, W2, V3, W2, V3, W2});
        // Row z=0
        addRow(-5, 0, new int[]{W2, V3, V3, V4, V3, V3, LIN});
        // Row z=+1
        addRow(-5, 1, new int[]{W2, V3, W2, V3, W2, V3, W2});
        // Row z=+2
        addRow(-5, 2, new int[]{W2, W2, V3, V3, V3, W2, W2});
        // Row z=+3
        addRow(-5, 3, new int[]{W2, W2, W2, W2, W2, W2, W2});

        // === LAYER 1 (y=-4): walls + lava pool + compose D ===
        add(-2, -4, -2, CD); add(-1, -4, -2, W); add(0, -4, -2, W);
        add(1, -4, -2, W);  add(2, -4, -2, CD);
        add(-2, -4, -1, W); add(-1, -4, -1, LAVA); add(0, -4, -1, LAVA);
        add(1, -4, -1, LAVA); add(2, -4, -1, W);
        add(-2, -4, 0, W);  add(-1, -4, 0, LAVA); add(0, -4, 0, V4);
        add(1, -4, 0, LAVA); add(2, -4, 0, W);
        add(-2, -4, 1, W);  add(-1, -4, 1, LAVA); add(0, -4, 1, LAVA);
        add(1, -4, 1, LAVA); add(2, -4, 1, W);
        add(-2, -4, 2, CD); add(-1, -4, 2, W); add(0, -4, 2, W);
        add(1, -4, 2, W);   add(2, -4, 2, CD);

        // === LAYER 2 (y=-3): 3x3 column ===
        add(-1, -3, -1, W); add(0, -3, -1, W);  add(1, -3, -1, W);
        add(-1, -3, 0, W);  add(0, -3, 0, W2); add(1, -3, 0, W);
        add(-1, -3, 1, W);  add(0, -3, 1, W);  add(1, -3, 1, W);

        // === LAYER 3 (y=-2): twin pillars ===
        add(-1, -2, 0, W2); add(1, -2, 0, W2);

        // === LAYER 4 (y=-1): twin pillars ===
        add(-1, -1, 0, W2); add(1, -1, 0, W2);

        // === LAYER 5 (y=0): EC + pillars (EC is master, not validated) ===
        add(-1, 0, 0, W2); add(1, 0, 0, W2);

        // === LAYER 6 (y=+1): horizontal bridge ===
        add(-2, 1, 0, W2); add(-1, 1, 0, W); add(0, 1, 0, W);
        add(1, 1, 0, W);   add(2, 1, 0, W2);

        // === LAYER 7 (y=+2): wider horns ===
        add(-3, 2, 0, W); add(-2, 2, 0, W);
        add(2, 2, 0, W);  add(3, 2, 0, W);

        // === LAYER 8 (y=+3): horn tips ===
        add(-3, 3, 0, W); add(3, 3, 0, W);
    }

    private static void addRow(int y, int z, int[] types) {
        for (int i = 0; i < types.length; i++) {
            int x = i - 3; // col 0 = x=-3
            STRUCTURE.add(new int[]{x, y, z, types[i]});
        }
    }

    private static void add(int x, int y, int z, int type) {
        STRUCTURE.add(new int[]{x, y, z, type});
    }

    // 4 rotation matrices for x,z: {cos, -sin, sin, cos}
    private static final int[][] ROTATIONS = {
        { 1, 0, 0, 1},   // 0°
        { 0, 1,-1, 0},   // 90°
        {-1, 0, 0,-1},   // 180°
        { 0,-1, 1, 0},   // 270°
    };

    // State
    private InternalEnergyStorage energyStorage =
        new InternalEnergyStorage(ENERGY_CAPACITY, 2000);
    private boolean structureFormed = false;
    private int activeRotation = -1;
    private BlockPos energyInputPos = null;
    private BlockPos fluidInputPos = null;
    private boolean portalActive = false;

    // === Structure validation ===

    public boolean checkStructure() {
        if (world == null || world.isRemote) return structureFormed;

        for (int r = 0; r < 4; r++) {
            if (validateRotation(r)) {
                if (!structureFormed || activeRotation != r) {
                    activeRotation = r;
                    structureFormed = true;
                    locateHatches(r);
                    markDirty();
                    syncToClient();
                }
                return true;
            }
        }

        if (structureFormed) {
            structureFormed = false;
            activeRotation = -1;
            energyInputPos = null;
            fluidInputPos = null;
            portalActive = false;
            markDirty();
            syncToClient();
        }
        return false;
    }

    private boolean validateRotation(int r) {
        for (int[] entry : STRUCTURE) {
            BlockPos check = rotateAndOffset(entry[0], entry[1], entry[2], r);
            if (!checkBlockType(check, entry[3])) return false;
        }
        return true;
    }

    private BlockPos rotateAndOffset(int dx, int dy, int dz, int r) {
        int rx = dx * ROTATIONS[r][0] + dz * ROTATIONS[r][1];
        int rz = dx * ROTATIONS[r][2] + dz * ROTATIONS[r][3];
        return pos.add(rx, dy, rz);
    }

    private boolean checkBlockType(BlockPos p, int type) {
        Block block = world.getBlockState(p).getBlock();
        String name = block.getRegistryName() != null
            ? block.getRegistryName().toString() : "";
        switch (type) {
            case W:    return name.equals("nexusabsolu:nexus_wall");
            case W2:   return name.equals("nexusabsolu:nexus_wall_t2");
            case V3:   return name.equals("nexusabsolu:vossium_iii_block");
            case V4:   return name.equals("nexusabsolu:vossium_iv_block");
            case EIN:  return name.equals("nexusabsolu:energy_input");
            case LIN:  return name.equals("nexusabsolu:fluid_input");
            case CD:   return name.equals("nexusabsolu:compose_block_d");
            case LAVA: return block == Blocks.LAVA || block == Blocks.FLOWING_LAVA;
            default:   return false;
        }
    }

    private void locateHatches(int r) {
        // EIN is at (0, -5, -3) in structure coords
        energyInputPos = rotateAndOffset(0, -5, -3, r);
        TileEntity te = world.getTileEntity(energyInputPos);
        if (te instanceof TileEnergyInput) {
            ((TileEnergyInput) te).setMasterPos(pos);
        }
        // LIN is at (3, -5, 0) in structure coords
        fluidInputPos = rotateAndOffset(3, -5, 0, r);
        te = world.getTileEntity(fluidInputPos);
        if (te instanceof TileFluidInput) {
            ((TileFluidInput) te).setMasterPos(pos);
        }
    }

    // === Energy & Fluid ===

    public boolean hasEnoughEnergy() {
        return getEnergyStored() >= ENERGY_REQUIRED;
    }

    public boolean hasEnoughFluid() {
        return getFluidStored() >= FLUID_REQUIRED;
    }

    public int getEnergyStored() {
        int total = energyStorage.getEnergyStored();
        // Also pull from energy hatch
        if (energyInputPos != null) {
            TileEntity te = world.getTileEntity(energyInputPos);
            if (te instanceof TileEnergyInput) {
                total += ((TileEnergyInput) te).getEnergyStored();
            }
        }
        return total;
    }

    public int getFluidStored() {
        if (fluidInputPos == null) return 0;
        TileEntity te = world.getTileEntity(fluidInputPos);
        if (te instanceof TileFluidInput) {
            TileFluidInput hatch = (TileFluidInput) te;
            if (hatch.getTank().getFluid() != null
                && "diarrhee_liquide".equals(hatch.getTank().getFluid().getFluid().getName())) {
                return hatch.getFluidAmount();
            }
        }
        return 0;
    }

    public boolean isStructureFormed() { return structureFormed; }
    public boolean isPortalActive() { return portalActive; }

    // === Activation (Phase 2: teleportation + CM x9 placement) ===

    public void activate(EntityPlayer player) {
        if (!structureFormed || !hasEnoughEnergy() || !hasEnoughFluid()) return;

        // Drain energy
        int toDrain = ENERGY_REQUIRED;
        // Drain from hatch first
        if (energyInputPos != null) {
            TileEntity te = world.getTileEntity(energyInputPos);
            if (te instanceof TileEnergyInput) {
                TileEnergyInput hatch = (TileEnergyInput) te;
                int drained = hatch.drain(Math.min(toDrain, hatch.getEnergyStored()));
                toDrain -= drained;
            }
        }
        if (toDrain > 0) {
            energyStorage.drainInternal(toDrain);
        }

        // Drain fluid
        if (fluidInputPos != null) {
            TileEntity te = world.getTileEntity(fluidInputPos);
            if (te instanceof TileFluidInput) {
                ((TileFluidInput) te).drain(FLUID_REQUIRED);
            }
        }

        portalActive = true;
        markDirty();
        syncToClient();

        // TODO Phase 2: Teleport player to overworld + place CM x9
        player.sendMessage(new TextComponentString(
            "\u00a7d\u00a7l[Portail Voss] \u00a75Le portail s'ouvre...\n"
            + "\u00a77Phase 2 : teleportation en cours de developpement."));
    }

    // === Tick ===

    @Override
    public void update() {
        if (world == null || world.isRemote) return;

        // Check structure every 2 seconds
        if (world.getTotalWorldTime() % 40 == 0) {
            checkStructure();
        }

        // Pull energy from hatch into internal buffer
        if (structureFormed && energyInputPos != null) {
            TileEntity te = world.getTileEntity(energyInputPos);
            if (te instanceof TileEnergyInput) {
                TileEnergyInput hatch = (TileEnergyInput) te;
                int space = energyStorage.getMaxEnergyStored()
                    - energyStorage.getEnergyStored();
                if (space > 0) {
                    int pulled = hatch.drain(Math.min(space, 2000));
                    if (pulled > 0) energyStorage.generateInternal(pulled);
                }
            }
        }
    }

    // === NBT ===

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("Formed", structureFormed);
        nbt.setBoolean("PortalActive", portalActive);
        nbt.setInteger("Rotation", activeRotation);
        nbt.setInteger("Energy", energyStorage.getEnergyStored());
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        structureFormed = nbt.getBoolean("Formed");
        portalActive = nbt.getBoolean("PortalActive");
        activeRotation = nbt.getInteger("Rotation");
        energyStorage.setEnergy(nbt.getInteger("Energy"));
    }

    // === Capabilities ===

    @Override
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(cap, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (cap == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        return super.getCapability(cap, facing);
    }

    // === Sync ===

    private void syncToClient() {
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

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
