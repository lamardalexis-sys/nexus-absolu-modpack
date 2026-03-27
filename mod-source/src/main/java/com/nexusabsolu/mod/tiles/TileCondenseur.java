package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
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
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class TileCondenseur extends TileEntity implements ITickable, IInventory {

    // 4 input slots + 1 output slot = 5 total
    private ItemStack[] inventory = new ItemStack[5];
    private InternalEnergyStorage energyStorage = new InternalEnergyStorage(50000, 100);
    private int processTime = 0;
    private int maxProcessTime = 200;
    private CondenseurRecipes.Recipe currentRecipe = null;
    private boolean processing = false;
    private boolean structureValid = false;
    private int structureCheckTimer = 0;

    // Quote system
    private int currentQuote = 0;
    public static final String[] QUOTES = {
        "Deux espaces negocient...",
        "La matiere proteste. Ignorez-la.",
        "Phase instable. C'est normal. Probablement.",
        "Les dimensions se compriment.",
        "Prototype VII. Les six premiers ont explose.",
        "Ne reculez pas.",
        "La realite se plie...",
        "Voss avait raison. Comme toujours."
    };

    public TileCondenseur() {
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    // ==========================================
    // MULTIBLOC 2x2x2 STRUCTURE CHECK
    // Condenseur is at (0,0,0). Check all 4 possible orientations.
    // Bottom layer: Condenseur + 3x Nexus Wall
    // Top layer: 3x Glass + 1x Nexus Wall
    // ==========================================
    private boolean checkStructure() {
        // Try all 4 rotations of the 2x2x2 structure
        // The condenseur can be in any of the 4 corners of the bottom layer
        int[][] offsets = {
            {1, 0, 0, 0, 0, 1, 1, 0, 1},  // +X, +Z
            {-1, 0, 0, 0, 0, 1, -1, 0, 1}, // -X, +Z
            {1, 0, 0, 0, 0, -1, 1, 0, -1}, // +X, -Z
            {-1, 0, 0, 0, 0, -1, -1, 0, -1} // -X, -Z
        };

        for (int[] o : offsets) {
            BlockPos p1 = pos.add(o[0], o[1], o[2]); // bottom neighbor 1
            BlockPos p2 = pos.add(o[3], o[4], o[5]); // bottom neighbor 2
            BlockPos p3 = pos.add(o[6], o[7], o[8]); // bottom diagonal

            BlockPos t0 = pos.add(0, 1, 0);           // top above condenseur
            BlockPos t1 = p1.add(0, 1, 0);             // top above neighbor 1
            BlockPos t2 = p2.add(0, 1, 0);             // top above neighbor 2
            BlockPos t3 = p3.add(0, 1, 0);             // top above diagonal

            // Bottom: 3x Nexus Wall
            boolean bottomOk = isNexusWall(p1) && isNexusWall(p2) && isNexusWall(p3);

            // Top: 3x Glass + 1x Nexus Wall (any arrangement)
            int glassCount = 0;
            int wallCount = 0;
            for (BlockPos tp : new BlockPos[]{t0, t1, t2, t3}) {
                if (isGlass(tp)) glassCount++;
                else if (isNexusWall(tp)) wallCount++;
            }
            boolean topOk = (glassCount == 3 && wallCount == 1);

            if (bottomOk && topOk) return true;
        }
        return false;
    }

    private boolean isNexusWall(BlockPos p) {
        return world.getBlockState(p).getBlock() == ModBlocks.NEXUS_WALL;
    }

    private boolean isGlass(BlockPos p) {
        Block b = world.getBlockState(p).getBlock();
        return b == Blocks.GLASS || b == Blocks.STAINED_GLASS;
    }

    public boolean isStructureValid() {
        return structureValid;
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        // Check structure every 20 ticks (1 second)
        structureCheckTimer++;
        if (structureCheckTimer >= 20) {
            structureValid = checkStructure();
            structureCheckTimer = 0;
        }

        if (structureValid && canProcess()) {
            if (energyStorage.getEnergyStored() >= 20) {
                energyStorage.drainInternal(20);
                processTime++;
                processing = true;

                if (processTime % 40 == 0) {
                    currentQuote = (currentQuote + 1) % QUOTES.length;
                }

                if (processTime >= maxProcessTime) {
                    processItem();
                    processTime = 0;
                    processing = false;
                }
                markDirty();
            }
        } else {
            if (processTime > 0) {
                processTime = 0;
                processing = false;
                markDirty();
            }
        }
    }

    private boolean canProcess() {
        if (!inventory[4].isEmpty()) return false;
        currentRecipe = CondenseurRecipes.findRecipe(
            inventory[0], inventory[1], inventory[2], inventory[3]);
        if (currentRecipe != null) {
            maxProcessTime = currentRecipe.processTime;
        }
        return currentRecipe != null;
    }

    private void processItem() {
        if (currentRecipe == null) return;
        ItemStack result = currentRecipe.getOutput();
        if (!result.isEmpty()) {
            inventory[4] = result.copy();
            for (int i = 0; i < 4; i++) {
                inventory[i].shrink(1);
            }
        }
        currentRecipe = null;
    }

    // Energy
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergyStored() { return energyStorage.getMaxEnergyStored(); }
    public int getProcessTime() { return processTime; }
    public int getMaxProcessTime() { return maxProcessTime; }
    public boolean isProcessing() { return processing; }
    public String getCurrentQuote() { return QUOTES[currentQuote % QUOTES.length]; }

    public int getProcessPercent() {
        if (maxProcessTime == 0) return 0;
        return (processTime * 100) / maxProcessTime;
    }

    // IInventory
    @Override public int getSizeInventory() { return 5; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) { return inventory[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (!inventory[index].isEmpty()) {
            ItemStack stack;
            if (inventory[index].getCount() <= count) {
                stack = inventory[index];
                inventory[index] = ItemStack.EMPTY;
            } else {
                stack = inventory[index].splitStack(count);
            }
            markDirty();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
               player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index < 4; // Only input slots
    }
    @Override public int getField(int id) {
        switch (id) {
            case 0: return processTime;
            case 1: return maxProcessTime;
            case 2: return energyStorage.getEnergyStored();
            case 3: return energyStorage.getMaxEnergyStored();
            case 4: return structureValid ? 1 : 0;
            default: return 0;
        }
    }
    @Override public void setField(int id, int value) {
        switch (id) {
            case 0: processTime = value; break;
            case 1: maxProcessTime = value; break;
            case 2: energyStorage.setEnergy(value); break;
            case 3: break;
            case 4: structureValid = (value == 1); break;
        }
    }
    @Override public int getFieldCount() { return 5; }
    @Override public void clear() {
        for (int i = 0; i < inventory.length; i++) inventory[i] = ItemStack.EMPTY;
    }
    @Override public String getName() { return "Condenseur Dimensionnel"; }
    @Override public boolean hasCustomName() { return true; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }

    // NBT
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ProcessTime", processTime);
        compound.setInteger("Energy", energyStorage.getEnergyStored());
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
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
        processTime = compound.getInteger("ProcessTime");
        int energy = compound.getInteger("Energy");
        energyStorage = new InternalEnergyStorage(50000, 100, energy);
        NBTTagList list = compound.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < inventory.length) {
                inventory[slot] = new ItemStack(tag);
            }
        }
    }

    // Capabilities (RF input)
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(energyStorage);
        }
        return super.getCapability(capability, facing);
    }
}
