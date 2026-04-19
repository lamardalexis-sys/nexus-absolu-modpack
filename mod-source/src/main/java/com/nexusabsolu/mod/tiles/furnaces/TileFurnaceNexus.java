package com.nexusabsolu.mod.tiles.furnaces;

import com.nexusabsolu.mod.tiles.InternalEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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

    // Types SideConfig pour les furnaces (indice dans SideConfig.faceConfig[type])
    public static final int SC_TYPE_ITEM_IN  = 0;
    public static final int SC_TYPE_ITEM_OUT = 1;
    public static final int SC_TYPE_ENERGY   = 2;
    public static final int SC_TYPE_FUEL_IN  = 3;

    // Coal ops : calcule dynamiquement depuis le burn time vanilla (compatible tous mods).
    // 1 op = tier.baseCookTime() ticks, donc ops = burnTime / baseCookTime.
    // Exemples :
    //   - Coal (1600 ticks) dans Iron Furnace (base 160) = 10 ops
    //   - Coal block (16000 ticks) dans Vossium IV (base 50) = 320 ops
    //   - Tout fuel Forge-register (lava bucket, blaze rod, etc.) fonctionne aussi

    private FurnaceTier tier;
    private ItemStack[] inventory;
    private InternalEnergyStorage energyStorage;

    private int cookProgress = 0;          // ticks de cuisson en cours
    private int maxCookTime = 200;          // ticks requis (depend du tier)

    // Systeme fuel style vanilla : ticks restants + ticks totaux pour la flamme
    // qui descend progressivement.
    private int fuelBurnTicks = 0;         // ticks restants sur le fuel actuel
    private int fuelTotalBurnTicks = 0;    // ticks totaux du fuel actuel (pour ratio flamme)

    // Configuration des 6 faces pour auto I/O (style Mekanism)
    private final com.nexusabsolu.mod.tiles.SideConfig sideConfig =
        new com.nexusabsolu.mod.tiles.SideConfig();

    // Tick counter pour throttle les operations I/O cross-faces (tous les 10 ticks)
    private int ioTickCounter = 0;

    // v1.0.212 : flag "Enhanced" debloque par l'Upgrade Kit
    // - false (defaut) : furnace basique, juste input/fuel/output + config I/O
    // - true : debloque jauge RF + 4 slots upgrade + onglet Upgrades
    // Hereditee via NBT de l'ItemStack (ItemBlock) pour que le kit se
    // transmette lors des crafts de furnace tier superieur.
    private boolean isEnhanced = false;

    public TileFurnaceNexus() {
        this(FurnaceTier.IRON);  // default pour lecture NBT
    }

    public TileFurnaceNexus(FurnaceTier tier) {
        this.tier = tier;
        this.inventory = new ItemStack[TOTAL_SLOTS];
        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
        this.energyStorage = new InternalEnergyStorage(tier.baseEnergyCapacity(), 1000);
        this.maxCookTime = tier.baseCookTime();
        // Defaults Mekanism-like : output face = bas, rien d'autre
        this.sideConfig.setFace(SC_TYPE_ITEM_OUT, 0, true);  // face 0 = DOWN
        // Energy accepte partout (decide par Alexis v1.0.184)
        for (int f = 0; f < 6; f++) {
            this.sideConfig.setFace(SC_TYPE_ENERGY, f, true);
        }
    }

    public com.nexusabsolu.mod.tiles.SideConfig getSideConfig() { return sideConfig; }

    public FurnaceTier getTier() { return tier; }
    public int getCookProgress() { return cookProgress; }

    // v1.0.212 : getters/setters pour le flag Enhanced
    public boolean isEnhanced() { return isEnhanced; }

    /** Applique l'Upgrade Kit : debloque RF + slots upgrade. Irreversible. */
    public void applyEnhancement() {
        if (isEnhanced) return;  // deja enhanced, no-op
        this.isEnhanced = true;
        markDirty();
        // Sync client via notifyBlockUpdate pour que le BlockState update les LED
        if (world != null && !world.isRemote) {
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    /** Setter direct utilise au placement du bloc (lit le NBT de l'ItemStack). */
    public void setEnhancedFromItemStack(boolean enhanced) {
        this.isEnhanced = enhanced;
    }
    public int getMaxCookTime() { return maxCookTime; }
    /** Ticks restants sur le fuel actuel (0 = pas de fuel actif). */
    public int getFuelBurnTicks() { return fuelBurnTicks; }
    /** Ticks totaux du fuel consomme (pour calculer le ratio de flamme). */
    public int getFuelTotalBurnTicks() { return fuelTotalBurnTicks; }

    /**
     * Alias retro-compat : true si un fuel brule actuellement.
     * Utilise par les ancien codes (GUI, BlockFurnaceNexus.getActualState...).
     */
    public int getFuelRemaining() { return fuelBurnTicks > 0 ? 1 : 0; }
    public int getEnergyStored() { return energyStorage.getEnergyStored(); }
    public int getMaxEnergy() { return energyStorage.getMaxEnergyStored(); }

    // Setters client-side : appeles par ContainerFurnaceNexus.updateProgressBar
    // pour refleter l'etat serveur dans le GUI client.
    public void setCookProgressClient(int v) { this.cookProgress = v; }
    public void setFuelBurnTicksClient(int v) { this.fuelBurnTicks = v; }
    public void setFuelTotalBurnTicksClient(int v) { this.fuelTotalBurnTicks = v; }
    public void setEnergyStoredClient(int v) { this.energyStorage.setEnergy(v); }
    public void setMaxCookTimeClient(int v) { this.maxCookTime = v; }

    /** Mode RF actif si l'upgrade RF_CONVERTER est presente OU si le tier est nativeRF. */
    public boolean isRFMode() {
        if (tier.nativeRF) return true;
        ItemStack rfSlot = inventory[SLOT_UPGRADE_BASE + FurnaceUpgrade.RF_CONVERTER.slotIndex];
        return !rfSlot.isEmpty();
    }

    /** Nombre d'items dans le slot SPEED_BOOSTER (0 si vide). */
    private int getSpeedBoosterCount() {
        ItemStack slot = inventory[SLOT_UPGRADE_BASE + FurnaceUpgrade.SPEED_BOOSTER.slotIndex];
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    /** Nombre d'items dans le slot EFFICIENCY (0 si vide). */
    private int getEfficiencyCount() {
        ItemStack slot = inventory[SLOT_UPGRADE_BASE + FurnaceUpgrade.EFFICIENCY.slotIndex];
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    /**
     * Temps de cuisson effectif en ticks en tenant compte :
     *  - vitesse base du tier (baseCookTime)
     *  - bonus RF_CONVERTER si mode RF (+5%)
     *  - bonus SPEED_BOOSTER (+30% par item, stackable 8 fois)
     *
     * Retourne un nb de ticks minimum de 1.
     */
    public int getEffectiveMaxCookTime() {
        float speedMult = 1.0F;
        // Bonus RF converter
        if (isRFMode() && !tier.nativeRF) speedMult += 0.05F;
        // Bonus speed boosters : +30% par item stacke
        speedMult += getSpeedBoosterCount() * 0.30F;
        // Applique sur le temps de base : temps / multiplicateur
        int baseCook = tier.baseCookTime();
        int effective = (int)(baseCook / speedMult);
        return Math.max(1, effective);
    }

    /**
     * Consommation RF/tick effective en mode RF, avec :
     *  - baseRfPerTick du tier
     *  - SPEED_BOOSTER : x1.40 par item (cumulatif multiplicatif)
     *  - EFFICIENCY : x0.92 par item (cumulatif multiplicatif)
     */
    public int getEffectiveRfPerTick() {
        float consoMult = 1.0F;
        int spdCount = getSpeedBoosterCount();
        for (int i = 0; i < spdCount; i++) consoMult *= 1.40F;
        int effCount = getEfficiencyCount();
        for (int i = 0; i < effCount; i++) consoMult *= 0.92F;
        return Math.max(1, (int)(tier.baseRfPerTick * consoMult));
    }

    // === TICK ===

    @Override
    public void update() {
        if (world.isRemote) return;

        boolean wasActive = cookProgress > 0 || fuelBurnTicks > 0;

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

        // 2. Fuel disponible ? (consumeFuelIfNeeded decremente fuelBurnTicks de 1)
        if (!consumeFuelIfNeeded()) {
            resetProgress();
            return;
        }

        // 3. Cuisson progress (utilise le temps effectif avec upgrades)
        maxCookTime = getEffectiveMaxCookTime();
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
            // Note: fuelBurnTicks est decremente tick-par-tick dans consumeFuelIfNeeded
            // Pas de decrement supplementaire ici (ancien systeme ops = supprime)
            markDirty();
        }

        boolean isActive = cookProgress > 0 || fuelBurnTicks > 0;
        if (wasActive != isActive) markDirty();

        // Auto I/O selon SideConfig (tous les 10 ticks = 0.5s = confort joueur)
        ioTickCounter++;
        if (ioTickCounter >= 10) {
            ioTickCounter = 0;
            doAutoIO();
        }
    }

    /**
     * Auto pull/push items selon SideConfig, toutes les 10 ticks.
     * - Face ITEM_IN  : pull du voisin vers SLOT_INPUT (filtre : smeltable)
     * - Face FUEL_IN  : pull du voisin vers SLOT_FUEL (filtre : getCoalOps > 0)
     * - Face ITEM_OUT : push du SLOT_OUTPUT vers voisin
     * Energy est gere automatiquement par la capability CapabilityEnergy sur toutes faces.
     */
    private void doAutoIO() {
        for (int faceIdx = 0; faceIdx < 6; faceIdx++) {
            EnumFacing face = EnumFacing.getFront(faceIdx);
            // Position du voisin
            net.minecraft.util.math.BlockPos neighborPos = getPos().offset(face);
            TileEntity neighbor = world.getTileEntity(neighborPos);
            if (neighbor == null) continue;

            // Capability opposee (face qui fait face a notre face)
            EnumFacing opposite = face.getOpposite();

            net.minecraftforge.items.IItemHandler neighborHandler = neighbor.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite);
            if (neighborHandler == null) continue;

            // ITEM_OUT : push SLOT_OUTPUT vers voisin
            if (sideConfig.isFaceActive(SC_TYPE_ITEM_OUT, faceIdx)) {
                ItemStack out = inventory[SLOT_OUTPUT];
                if (!out.isEmpty()) {
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(neighborHandler, out.copy(), false);
                    int inserted = out.getCount() - remaining.getCount();
                    if (inserted > 0) {
                        out.shrink(inserted);
                        if (out.getCount() <= 0) inventory[SLOT_OUTPUT] = ItemStack.EMPTY;
                        markDirty();
                    }
                }
            }

            // ITEM_IN : pull smeltables vers SLOT_INPUT
            if (sideConfig.isFaceActive(SC_TYPE_ITEM_IN, faceIdx)) {
                pullFromNeighborToSlot(neighborHandler, SLOT_INPUT, true, false);
            }

            // FUEL_IN : pull fuels vers SLOT_FUEL
            if (sideConfig.isFaceActive(SC_TYPE_FUEL_IN, faceIdx)) {
                pullFromNeighborToSlot(neighborHandler, SLOT_FUEL, false, true);
            }
        }
    }

    /**
     * Tire un item depuis le voisin vers un slot local, selon le filtre.
     * @param filterSmeltable true = seuls les items smeltables sont acceptes
     * @param filterFuel true = seuls les items fuel (coal/charcoal/coal block) sont acceptes
     */
    private void pullFromNeighborToSlot(net.minecraftforge.items.IItemHandler handler,
                                         int targetSlot,
                                         boolean filterSmeltable,
                                         boolean filterFuel) {
        ItemStack current = inventory[targetSlot];
        // Si deja plein ou stack max, skip
        if (!current.isEmpty() && current.getCount() >= current.getMaxStackSize()) return;

        for (int s = 0; s < handler.getSlots(); s++) {
            ItemStack extracted = handler.extractItem(s, 64, true);  // simulate
            if (extracted.isEmpty()) continue;

            // Filtre
            if (filterSmeltable && FurnaceRecipes.instance().getSmeltingResult(extracted).isEmpty()) continue;
            if (filterFuel && getCoalOps(extracted) <= 0) continue;

            // Combien on peut accepter dans notre slot ?
            int canAccept;
            if (current.isEmpty()) {
                canAccept = Math.min(extracted.getMaxStackSize(), extracted.getCount());
            } else {
                if (!ItemHandlerHelper.canItemStacksStack(current, extracted)) continue;
                canAccept = Math.min(
                    current.getMaxStackSize() - current.getCount(),
                    extracted.getCount());
            }
            if (canAccept <= 0) continue;

            // Extraction reelle
            ItemStack taken = handler.extractItem(s, canAccept, false);
            if (taken.isEmpty()) continue;

            if (current.isEmpty()) {
                inventory[targetSlot] = taken;
            } else {
                current.grow(taken.getCount());
            }
            markDirty();
            return;  // un slot source pull par tick = suffit
        }
    }

    private void resetProgress() {
        if (cookProgress > 0) {
            cookProgress = 0;
            markDirty();
        }
    }

    /**
     * Consomme du fuel (1 tick). Style vanilla :
     * - Mode RF : consomme baseRfPerTick RF par tick (+ modifs upgrades)
     * - Mode coal : decremente fuelBurnTicks de 1 par tick. Si arrive a 0,
     *   prend un nouveau fuel dans le slot et initialise fuelBurnTicks.
     *
     * La flamme visible (ratio fuelBurnTicks / fuelTotalBurnTicks) descend
     * donc progressivement sur la duree du fuel (comme vanilla).
     */
    private boolean consumeFuelIfNeeded() {
        if (isRFMode()) {
            // Mode RF : consomme tick-by-tick (avec upgrades)
            int rfConso = getEffectiveRfPerTick();
            if (energyStorage.getEnergyStored() < rfConso) return false;
            energyStorage.drainInternal(rfConso);
            return true;
        }

        // Mode coal (ticks-based)
        if (fuelBurnTicks > 0) {
            fuelBurnTicks--;
            if (fuelBurnTicks <= 0) {
                fuelTotalBurnTicks = 0;
                markDirty();
            }
            return true;
        }

        // Plus de fuel : essaye d'en consommer un nouveau
        ItemStack fuel = inventory[SLOT_FUEL];
        if (fuel.isEmpty()) return false;

        int burnTime = net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(fuel);
        if (burnTime <= 0) return false;

        // Initialise le nouveau fuel
        fuelTotalBurnTicks = burnTime;
        fuelBurnTicks = burnTime - 1;  // -1 car on en consomme 1 ce tick
        fuel.shrink(1);
        if (fuel.getCount() <= 0) inventory[SLOT_FUEL] = ItemStack.EMPTY;
        markDirty();
        return true;
    }

    /**
     * Helper pour auto-IO pullFromNeighborToSlot : verifie qu'un item est un fuel valide.
     * Compatible tous mods via TileEntityFurnace.getItemBurnTime (registry Forge).
     */
    private int getCoalOps(ItemStack fuel) {
        // Retourne juste le burn time > 0 pour savoir si c'est un fuel valide.
        // (Plus utilise pour calcul ops, mais gardee pour compatibilite avec doAutoIO)
        return net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(fuel);
    }

    // === NBT ===

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("tier", tier.registryName);
        nbt.setInteger("cookProgress", cookProgress);
        nbt.setInteger("maxCookTime", maxCookTime);
        nbt.setInteger("fuelBurnTicks", fuelBurnTicks);
        nbt.setInteger("fuelTotalBurnTicks", fuelTotalBurnTicks);
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

        // Side config (6 faces x 4 types + eject/pull bits)
        NBTTagCompound scTag = new NBTTagCompound();
        sideConfig.writeToNBT(scTag);
        nbt.setTag("sideConfig", scTag);

        // v1.0.212 : flag Enhanced
        nbt.setBoolean("enhanced", isEnhanced);

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
        // Nouveau format v1.0.197 : ticks-based flame (fuelBurnTicks + fuelTotalBurnTicks)
        // Fallback pour anciens saves avec 'fuelRemaining' (ops-based)
        if (nbt.hasKey("fuelBurnTicks")) {
            this.fuelBurnTicks = nbt.getInteger("fuelBurnTicks");
            this.fuelTotalBurnTicks = nbt.getInteger("fuelTotalBurnTicks");
        } else if (nbt.hasKey("fuelRemaining")) {
            // Migration depuis ancien format : estime les ticks a partir des ops
            int oldOps = nbt.getInteger("fuelRemaining");
            this.fuelBurnTicks = oldOps * Math.max(1, tier.baseCookTime());
            this.fuelTotalBurnTicks = this.fuelBurnTicks;
        }

        for (int i = 0; i < TOTAL_SLOTS; i++) inventory[i] = ItemStack.EMPTY;
        NBTTagList items = nbt.getTagList("items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                inventory[slot] = new ItemStack(itemTag);
            }
        }

        // Side config (si present dans le save, sinon garde les defaults)
        if (nbt.hasKey("sideConfig")) {
            sideConfig.readFromNBT(nbt.getCompoundTag("sideConfig"));
        }

        // v1.0.212 : flag Enhanced
        this.isEnhanced = nbt.getBoolean("enhanced");
    }

    // === Sync client (necessaire pour que isEnhanced se propage aux clients
    //     des que applyEnhancement est appelee, pour mise a jour LED BlockState) ===

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
