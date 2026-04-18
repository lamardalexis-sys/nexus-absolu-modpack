package com.nexusabsolu.mod.tiles.furnaces;

import com.nexusabsolu.mod.tiles.InternalEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * TileEntity generique pour les Furnaces Nexus. Parametree par FurnaceTier.
 *
 * Layout inventaire (Sprint A, pre-upgrades) :
 *   slot 0 : input (item a cuire)
 *   slot 1 : fuel (coal, charcoal, coal block)
 *   slot 2 : output
 *   slot 3 : RF_CONVERTER upgrade     (reserve Sprint C)
 *   slot 4 : IO_EXPANSION upgrade     (reserve Sprint C)
 *   slot 5 : SPEED_BOOSTER upgrade    (reserve Sprint C)
 *   slot 6 : EFFICIENCY_CARD upgrade  (reserve Sprint C)
 *
 * Mode energie:
 *   - Par defaut : coal (lit slot 1, consomme un coal = N operations)
 *   - Avec upgrade RF_CONVERTER (slot 3) : consomme RF du buffer interne,
 *     au rythme baseRfPerTick * consoMultiplier.
 *
 * Logique de cook simple (Sprint A, sans upgrades actives) :
 *   - Verifie recette furnace vanilla existe pour input
 *   - Verifie fuel disponible (coal OU energie RF si natif RF)
 *   - Progresse maxCookTime = tier.baseCookTime() ticks
 *   - Output vers slot 2 si stackable
 */
public class TileFurnaceNexus extends TileEntity implements ITickable, IInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_UPGRADE_BASE = 3;  // 3..6 pour les 4 upgrades
    public static final int TOTAL_SLOTS = 7;

    // Coal bonus : charbon = 2x ops, charcoal = 1.5x, coal block = 16x (config Alexis)
    private static final int OPS_PER_COAL = 2;
    private static final int OPS_PER_CHARCOAL = 1;   // 1.5 arrondi a 1 (vanilla-compat)
    private static final int OPS_PER_COAL_BLOCK = 16;

    private FurnaceTier tier;
    private ItemStack[] inventory;
    private InternalEnergyStorage energyStorage;

    private int cookProgress = 0;          // ticks de cuisson en cours
    private int maxCookTime = 200;          // ticks requis (depend du tier)
    private int fuelRemaining = 0;          // operations restantes sur le coal actuel

    public TileFurnaceNexus() {
        this(FurnaceTier.IRON);  // default pour lecture NBT
    }

    public TileFurnaceNexus(FurnaceTier tier) {
        this.tier = tier;
        this.inventory = new ItemStack[TOTAL_SLOTS];
        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
        this.energyStorage = new InternalEnergyStorage(tier.baseEnergyCapacity(), 1000);
        this.maxCookTime = tier.baseCookTime();
    }

    public FurnaceTier getTier() { return tier; }
    public int getCookProgress() { return cookProgress; }
    public int getMaxCookTime() { return maxCookTime; }
    public int getFuelRemaining() { return fuelRemaining; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return energyStorage.getMaxEnergyStored(); }

    /** Mode RF actif si l'upgrade RF_CONVERTER est presente OU si le tier est nativeRF. */
    public boolean isRFMode() {
        if (tier.nativeRF) return true;
        ItemStack rfSlot = inventory[SLOT_UPGRADE_BASE + FurnaceUpgrade.RF_CONVERTER.slotIndex];
        return !rfSlot.isEmpty();
    }

    // === TICK ===

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean wasActive = cookProgress > 0 || fuelRemaining > 0;

        // 1. Recette disponible ?
        ItemStack input = inventory[SLOT_INPUT];
        if (input.isEmpty()) {
            resetProgress();
            return;
        }
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
        if (result.isEmpty()) {
            resetProgress();
            return;
        }
        // Output stackable ?
        ItemStack output = inventory[SLOT_OUTPUT];
        if (!output.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(output, result)) {
                resetProgress();
                return;
            }
            if (output.getCount() + result.getCount() > output.getMaxStackSize()) {
                resetProgress();
                return;
            }
        }

        // 2. Fuel disponible ?
        if (!consumeFuelIfNeeded()) {
            resetProgress();
            return;
        }

        // 3. Cuisson progress
        cookProgress++;
        if (cookProgress >= maxCookTime) {
            // Execute la cuisson
            ItemStack copy = result.copy();
            if (output.isEmpty()) {
                inventory[SLOT_OUTPUT] = copy;
            } else {
                output.grow(copy.getCount());
            }
            input.shrink(1);
            if (input.getCount() <= 0) inventory[SLOT_INPUT] = ItemStack.EMPTY;
            cookProgress = 0;
            // Decremente fuel (1 op consommee)
            if (!isRFMode()) fuelRemaining--;
            markDirty();
        }

        boolean isActive = cookProgress > 0 || fuelRemaining > 0;
        if (wasActive != isActive) markDirty();
    }

    private void resetProgress() {
        if (cookProgress > 0) {
            cookProgress = 0;
            markDirty();
        }
    }

    /**
     * Consomme du fuel si necessaire pour que le furnace continue a tourner ce tick.
     * Retourne true si le furnace peut tourner ce tick.
     *
     * Mode coal : si fuelRemaining > 0 on consomme rien (le coal actuel dure),
     *             sinon on prend un item du slot fuel et on recharge fuelRemaining.
     * Mode RF   : on consomme baseRfPerTick par tick. Si pas assez, on s'arrete.
     */
    private boolean consumeFuelIfNeeded() {
        if (isRFMode()) {
            // Mode RF : consomme tick-by-tick
            if (energyStorage.getEnergyStored() < tier.baseRfPerTick) return false;
            energyStorage.drainInternal(tier.baseRfPerTick);
            return true;
        }

        // Mode coal : un stack entier couvre N operations
        if (fuelRemaining > 0) return true;

        ItemStack fuel = inventory[SLOT_FUEL];
        if (fuel.isEmpty()) return false;

        int ops = getCoalOps(fuel);
        if (ops <= 0) return false;

        fuelRemaining = ops;
        fuel.shrink(1);
        if (fuel.getCount() <= 0) inventory[SLOT_FUEL] = ItemStack.EMPTY;
        markDirty();
        return true;
    }

    /** Nombre d'operations qu'un ItemStack de fuel peut alimenter. */
    private int getCoalOps(ItemStack fuel) {
        Item item = fuel.getItem();
        if (item == Items.COAL) {
            // Coal OR charcoal (meta 0 = coal, meta 1 = charcoal)
            return fuel.getMetadata() == 1 ? OPS_PER_CHARCOAL : OPS_PER_COAL;
        }
        if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() == Blocks.COAL_BLOCK) {
            return OPS_PER_COAL_BLOCK;
        }
        // Fallback : vanilla furnace burn time -> 1 op par 200 ticks
        int burnTime = net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(fuel);
        if (burnTime >= 200) return burnTime / 200;
        return 0;
    }

    // === NBT ===

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("tier", tier.registryName);
        nbt.setInteger("cookProgress", cookProgress);
        nbt.setInteger("maxCookTime", maxCookTime);
        nbt.setInteger("fuelRemaining", fuelRemaining);
        nbt.setInteger("energy", energyStorage.getEnergyStored());

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!inventory[i].isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                inventory[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        nbt.setTag("items", items);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("tier")) {
            this.tier = FurnaceTier.byName(nbt.getString("tier"));
            this.maxCookTime = tier.baseCookTime();
            // Re-init energy storage avec la capacite du tier (mais preserve l'energie stockee)
            int storedEnergy = nbt.getInteger("energy");
            this.energyStorage = new InternalEnergyStorage(tier.baseEnergyCapacity(), 1000, storedEnergy);
        }
        this.cookProgress = nbt.getInteger("cookProgress");
        if (nbt.hasKey("maxCookTime")) this.maxCookTime = nbt.getInteger("maxCookTime");
        this.fuelRemaining = nbt.getInteger("fuelRemaining");

        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
        NBTTagList items = nbt.getTagList("items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                inventory[slot] = new ItemStack(itemTag);
            }
        }
    }

    // === Capabilities (RF + ItemHandler) ===

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return true;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return (T) energyStorage;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) new InvWrapper(this);
        }
        return super.getCapability(capability, facing);
    }

    // === IInventory (boilerplate) ===

    @Override public int getSizeInventory() { return TOTAL_SLOTS; }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) { return inventory[index]; }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = inventory[index];
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result;
        if (stack.getCount() <= count) {
            result = stack;
            inventory[index] = ItemStack.EMPTY;
        } else {
            result = stack.splitStack(count);
            if (stack.getCount() == 0) inventory[index] = ItemStack.EMPTY;
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack s = inventory[index];
        inventory[index] = ItemStack.EMPTY;
        return s;
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
        return world.getTileEntity(pos) == this
            && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_OUTPUT) return false;
        if (index == SLOT_FUEL) return getCoalOps(stack) > 0;
        if (index == SLOT_INPUT) return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
        // Upgrade slots : valide seulement si c'est le bon type d'item (check Sprint C)
        return true;
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() {
        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
    }

    @Override public String getName() { return "container.nexus.furnace_" + tier.registryName; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
}
