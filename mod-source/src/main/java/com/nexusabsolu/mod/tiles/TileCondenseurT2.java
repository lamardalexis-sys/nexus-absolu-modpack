package com.nexusabsolu.mod.tiles;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import com.nexusabsolu.mod.tiles.TileEnergyInput;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

public class TileCondenseurT2 extends TileEntity implements ITickable {

    // -- Constants --
    private static final int ENERGY_CAPACITY = 1000000;
    private static final int ENERGY_MAX_INPUT = 500;
    private static final int RF_PER_TICK = 200;
    private static final int SYNC_INTERVAL = 10;

    // -- Block type IDs for structure validation --
    private static final int NEXUS_WALL = 0;
    private static final int REDSTONE = 1;
    private static final int GLASS = 2;
    private static final int INPUT = 3;
    private static final int OUTPUT = 4;
    private static final int VOSSIUM2 = 5;
    private static final int ENERGY_IN = 6;

    // Structure: 26 positions as {depth, height, width, blockType}
    // Relative to master at (0, 0, 0)
    // depth = into structure, height = vertical, width = lateral
    private static final int[][] STRUCTURE = {
        // Bottom layer (h=-1): 9 blocks
        {0, -1, -1, NEXUS_WALL}, {0, -1, 0, NEXUS_WALL}, {0, -1, 1, NEXUS_WALL},
        {1, -1, -1, NEXUS_WALL}, {1, -1, 0, NEXUS_WALL}, {1, -1, 1, NEXUS_WALL},
        {2, -1, -1, NEXUS_WALL}, {2, -1, 0, ENERGY_IN},  {2, -1, 1, NEXUS_WALL},
        // Middle layer (h=0): 8 blocks (excluding master)
        {0,  0, -1, GLASS},      {0,  0, 1, GLASS},
        {1,  0, -1, INPUT},      {1,  0, 0, VOSSIUM2},   {1,  0, 1, OUTPUT},
        {2,  0, -1, NEXUS_WALL}, {2,  0, 0, NEXUS_WALL}, {2,  0, 1, NEXUS_WALL},
        // Top layer (h=+1): 9 blocks
        {0,  1, -1, GLASS},      {0,  1, 0, GLASS},      {0,  1, 1, GLASS},
        {1,  1, -1, GLASS},      {1,  1, 0, GLASS},      {1,  1, 1, GLASS},
        {2,  1, -1, NEXUS_WALL}, {2,  1, 0, GLASS},      {2,  1, 1, NEXUS_WALL},
    };

    // 4 rotation matrices: {depth_dx, depth_dz, width_dx, width_dz}
    private static final int[][] ROTATIONS = {
        {-1,  0,  0,  1},  // R0: depth=-X, width=+Z
        { 1,  0,  0, -1},  // R1: depth=+X, width=-Z
        { 0, -1, -1,  0},  // R2: depth=-Z, width=-X
        { 0,  1,  1,  0},  // R3: depth=+Z, width=+X
    };

    // -- State --
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INPUT);
    private boolean structureFormed = false;
    private boolean processing = false;
    private int processTime = 0;
    private int maxProcessTime = 1800; // 90 seconds default
    private int activeRotation = -1;
    private BlockPos inputPos = null;
    private BlockPos outputPos = null;
    private BlockPos energyInputPos = null;

    // Quotes for GUI
    private int currentQuote = 0;
    public static final String[] QUOTES = {
        "Resonance dimensionnelle detectee...",
        "Les parois vibrent a la bonne frequence.",
        "Compression spatiale en cours.",
        "Le Vossium canalise l'energie.",
        "Phase 2 : stabilisation...",
        "Prototype XII. Celui-ci ne devrait pas exploser.",
        "Les dimensions convergent.",
        "Voss sourit. Quelque part.",
    };

    // -- Structure validation --

    public boolean checkStructure() {
        if (world == null) return false;

        for (int r = 0; r < ROTATIONS.length; r++) {
            if (validateRotation(r)) {
                activeRotation = r;
                // Locate INPUT, OUTPUT, and ENERGY positions
                inputPos = getWorldPos(1, 0, -1, r);
                outputPos = getWorldPos(1, 0, 1, r);
                energyInputPos = getWorldPos(2, -1, 0, r);
                linkHatches();
                if (!structureFormed) {
                    structureFormed = true;
                    formWalls(r);
                    markDirty();
                    syncToClient();
                }
                return true;
            }
        }

        if (structureFormed) {
            unformWalls();
            structureFormed = false;
            activeRotation = -1;
            inputPos = null;
            outputPos = null;
            energyInputPos = null;
            processing = false;
            processTime = 0;
            markDirty();
            syncToClient();
        }
        return false;
    }

    private boolean validateRotation(int r) {
        for (int[] entry : STRUCTURE) {
            int depth = entry[0], height = entry[1], width = entry[2], type = entry[3];
            BlockPos checkPos = getWorldPos(depth, height, width, r);
            if (!checkBlockType(checkPos, type)) return false;
        }
        return true;
    }

    private BlockPos getWorldPos(int depth, int height, int width, int r) {
        int dx = depth * ROTATIONS[r][0] + width * ROTATIONS[r][2];
        int dz = depth * ROTATIONS[r][1] + width * ROTATIONS[r][3];
        return pos.add(dx, height, dz);
    }

    private boolean checkBlockType(BlockPos checkPos, int type) {
        IBlockState state = world.getBlockState(checkPos);
        Block block = state.getBlock();
        String name = block.getRegistryName() != null ? block.getRegistryName().toString() : "";

        switch (type) {
            case NEXUS_WALL:
                return name.equals("nexusabsolu:nexus_wall")
                    || name.equals("nexusabsolu:condenseur_t2_wall");
            case REDSTONE:
                return block == Blocks.REDSTONE_BLOCK;
            case GLASS:
                return block == Blocks.GLASS || name.contains("glass")
                    || name.equals("nexusabsolu:condenseur_t2_wall");
            case INPUT:
                return name.equals("nexusabsolu:item_input");
            case OUTPUT:
                return name.equals("nexusabsolu:item_output");
            case VOSSIUM2:
                return name.equals("nexusabsolu:vossium_ii_block");
            case ENERGY_IN:
                return name.equals("nexusabsolu:energy_input");
            default:
                return false;
        }
    }

    private void linkHatches() {
        if (inputPos != null) {
            TileEntity te = world.getTileEntity(inputPos);
            if (te instanceof TileItemInput) {
                ((TileItemInput) te).setMasterPos(pos);
            }
        }
        if (outputPos != null) {
            TileEntity te = world.getTileEntity(outputPos);
            if (te instanceof TileItemOutput) {
                ((TileItemOutput) te).setMasterPos(pos);
            }
        }
        if (energyInputPos != null) {
            TileEntity te = world.getTileEntity(energyInputPos);
            if (te instanceof TileEnergyInput) {
                ((TileEnergyInput) te).setMasterPos(pos);
            }
        }
    }

    /** Pull RF from the Energy Input hatch into the master's buffer. */
    private void pullEnergyFromHatch() {
        if (energyInputPos == null) return;
        TileEntity te = world.getTileEntity(energyInputPos);
        if (te instanceof TileEnergyInput) {
            TileEnergyInput hatch = (TileEnergyInput) te;
            int space = energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored();
            if (space > 0) {
                int pulled = hatch.drain(Math.min(space, ENERGY_MAX_INPUT));
                if (pulled > 0) {
                    energyStorage.generateInternal(pulled);
                }
            }
        }
    }

    /** Replace all Nexus Walls AND Glass in the structure with invisible formed blocks. */
    private void formWalls(int r) {
        if (world == null || world.isRemote) return;
        IBlockState formedState = com.nexusabsolu.mod.init.ModBlocks.CONDENSEUR_T2_WALL.getDefaultState();
        for (int[] entry : STRUCTURE) {
            int type = entry[3];
            if (type == NEXUS_WALL || type == GLASS) {
                BlockPos wallPos = getWorldPos(entry[0], entry[1], entry[2], r);
                IBlockState current = world.getBlockState(wallPos);
                String name = current.getBlock().getRegistryName() != null
                    ? current.getBlock().getRegistryName().toString() : "";
                if (name.equals("nexusabsolu:nexus_wall") || name.contains("glass")
                    || name.equals("nexusabsolu:condenseur_t2_wall")) {
                    world.setBlockState(wallPos, formedState, 2);
                }
            }
        }
    }

    /** Restore formed blocks back to their originals. */
    private void unformWalls() {
        if (world == null || world.isRemote || activeRotation < 0) return;
        for (int[] entry : STRUCTURE) {
            int type = entry[3];
            if (type == NEXUS_WALL || type == GLASS) {
                BlockPos wallPos = getWorldPos(entry[0], entry[1], entry[2], activeRotation);
                IBlockState current = world.getBlockState(wallPos);
                String name = current.getBlock().getRegistryName() != null
                    ? current.getBlock().getRegistryName().toString() : "";
                if (name.equals("nexusabsolu:condenseur_t2_wall")) {
                    if (type == NEXUS_WALL) {
                        world.setBlockState(wallPos, com.nexusabsolu.mod.init.ModBlocks.NEXUS_WALL.getDefaultState(), 2);
                    } else {
                        world.setBlockState(wallPos, net.minecraft.init.Blocks.GLASS.getDefaultState(), 2);
                    }
                }
            }
        }
    }

    /** Called by BlockCondenseurT2Wall when a formed wall is broken. */
    public void onStructureBroken() {
        if (structureFormed) {
            unformWalls();
            structureFormed = false;
            activeRotation = -1;
            inputPos = null;
            outputPos = null;
            energyInputPos = null;
            processing = false;
            processTime = 0;
            markDirty();
            syncToClient();
        }
    }

    // -- Processing --

    @Override
    public void update() {
        if (world.isRemote) return;

        // Recheck structure every 20 ticks (1 second)
        if (world.getTotalWorldTime() % 20 == 0) {
            checkStructure();
        }

        if (!structureFormed) return;

        // Pull RF from energy input hatch into master buffer
        pullEnergyFromHatch();

        TileItemInput inputTile = getInputTile();
        TileItemOutput outputTile = getOutputTile();
        if (inputTile == null || outputTile == null) return;

        // Check if we can process
        CondenseurRecipes.Recipe recipe = findRecipe(inputTile);

        if (recipe != null && outputTile.getStackInSlot(0).isEmpty()) {
            if (energyStorage.getEnergyStored() >= RF_PER_TICK) {
                energyStorage.drainInternal(RF_PER_TICK);
                processTime++;
                processing = true;

                if (processTime % 40 == 0) {
                    currentQuote = (currentQuote + 1) % QUOTES.length;
                }

                if (processTime >= maxProcessTime) {
                    // Processing complete
                    ItemStack result = recipe.getOutput();
                    outputTile.setInventorySlotContents(0, result.copy());
                    // Consume inputs
                    for (int i = 0; i < 4; i++) {
                        inputTile.decrStackSize(i, 1);
                    }
                    processTime = 0;
                    processing = false;
                    syncToClient();
                }

                if (processTime % SYNC_INTERVAL == 0) {
                    syncToClient();
                }
                markDirty();
            }
        } else {
            if (processing || processTime > 0) {
                processTime = 0;
                processing = false;
                markDirty();
                syncToClient();
            }
        }
    }

    private CondenseurRecipes.Recipe findRecipe(TileItemInput inputTile) {
        ItemStack s0 = inputTile.getStackInSlot(0);
        ItemStack s1 = inputTile.getStackInSlot(1);
        ItemStack s2 = inputTile.getStackInSlot(2);
        ItemStack s3 = inputTile.getStackInSlot(3);
        CondenseurRecipes.Recipe recipe = CondenseurRecipes.findRecipe(s0, s1, s2, s3);
        if (recipe != null) {
            maxProcessTime = recipe.processTime;
        }
        return recipe;
    }

    private TileItemInput getInputTile() {
        if (inputPos == null) return null;
        TileEntity te = world.getTileEntity(inputPos);
        return te instanceof TileItemInput ? (TileItemInput) te : null;
    }

    private TileItemOutput getOutputTile() {
        if (outputPos == null) return null;
        TileEntity te = world.getTileEntity(outputPos);
        return te instanceof TileItemOutput ? (TileItemOutput) te : null;
    }

    // -- Accessors for TESR and GUI --

    public boolean isStructureValid() { return structureFormed; }
    public boolean isProcessing() { return processing; }
    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return maxProcessTime; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    public int getActiveRotation() { return activeRotation; }
    public BlockPos getInputPos() { return inputPos; }
    public BlockPos getOutputPos() { return outputPos; }
    public String getCurrentQuote() { return QUOTES[currentQuote % QUOTES.length]; }

    public int getProcessPercent() {
        if (maxProcessTime == 0) return 0;
        return (processTime * 100) / maxProcessTime;
    }

    /** Get the center of the 3x3x3 structure in world coords (for TESR rendering). */
    public BlockPos getStructureCenter() {
        if (activeRotation < 0) return pos;
        return getWorldPos(1, 0, 0, activeRotation);
    }

    /** Bounding box corners: [0]=min, [1]=max in world coords */
    public int[] getStructureBounds() {
        if (activeRotation < 0) return new int[]{pos.getX(), pos.getY(), pos.getZ(),
                                                   pos.getX()+1, pos.getY()+1, pos.getZ()+1};
        BlockPos c0 = getWorldPos(0, -1, -1, activeRotation);
        BlockPos c1 = getWorldPos(2,  1,  1, activeRotation);
        return new int[]{
            Math.min(c0.getX(), c1.getX()),
            Math.min(c0.getY(), c1.getY()),
            Math.min(c0.getZ(), c1.getZ()),
            Math.max(c0.getX(), c1.getX()) + 1,
            Math.max(c0.getY(), c1.getY()) + 1,
            Math.max(c0.getZ(), c1.getZ()) + 1
        };
    }

    /** Direction the front face (master/glass) faces, as dx,dz */
    public int[] getFrontDirection() {
        if (activeRotation < 0) return new int[]{0, 0};
        // depth=0 is front, depth increases towards back
        // depth direction = ROTATIONS[r][0], ROTATIONS[r][1]
        // So front faces OPPOSITE to depth direction
        return new int[]{-ROTATIONS[activeRotation][0], -ROTATIONS[activeRotation][1]};
    }

    /** Get item stacks from the INPUT tile for TESR rendering. */
    public ItemStack getInputStack(int slot) {
        TileItemInput input = getInputTile();
        if (input == null) return ItemStack.EMPTY;
        return input.getStackInSlot(slot);
    }

    /** Get the output item for TESR rendering. */
    public ItemStack getOutputStack() {
        TileItemOutput output = getOutputTile();
        if (output == null) return ItemStack.EMPTY;
        return output.getStackInSlot(0);
    }

    // -- Energy capability --

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        return super.getCapability(capability, facing);
    }

    // -- Bounding box for TESR --

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    // -- Sync --

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
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    // -- NBT --

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ProcessTime", processTime);
        compound.setInteger("MaxProcessTime", maxProcessTime);
        compound.setInteger("Energy", energyStorage.getEnergyStored());
        compound.setBoolean("Formed", structureFormed);
        compound.setBoolean("Processing", processing);
        compound.setInteger("Rotation", activeRotation);
        if (inputPos != null) {
            compound.setInteger("InputX", inputPos.getX());
            compound.setInteger("InputY", inputPos.getY());
            compound.setInteger("InputZ", inputPos.getZ());
        }
        if (outputPos != null) {
            compound.setInteger("OutputX", outputPos.getX());
            compound.setInteger("OutputY", outputPos.getY());
            compound.setInteger("OutputZ", outputPos.getZ());
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        processTime = compound.getInteger("ProcessTime");
        maxProcessTime = compound.getInteger("MaxProcessTime");
        if (maxProcessTime == 0) maxProcessTime = 1800;
        int energy = compound.getInteger("Energy");
        energyStorage = new InternalEnergyStorage(ENERGY_CAPACITY, ENERGY_MAX_INPUT, energy);
        structureFormed = compound.getBoolean("Formed");
        processing = compound.getBoolean("Processing");
        activeRotation = compound.getInteger("Rotation");
        if (compound.hasKey("InputX")) {
            inputPos = new BlockPos(
                compound.getInteger("InputX"),
                compound.getInteger("InputY"),
                compound.getInteger("InputZ"));
        }
        if (compound.hasKey("OutputX")) {
            outputPos = new BlockPos(
                compound.getInteger("OutputX"),
                compound.getInteger("OutputY"),
                compound.getInteger("OutputZ"));
        }
    }
}
