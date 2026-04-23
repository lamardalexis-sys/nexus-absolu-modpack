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
import net.minecraftforge.items.wrapper.SidedInvWrapper;

/**
 * TileEntity generique pour les Furnaces Nexus. Parametree par FurnaceTier.
 *
 * Layout inventaire (v1.0.248+ Phase 2 IO Expansion, 23 slots) :
 *   0..8   : 9 INPUT slots (selon tier IO : 1, 3, 5, 7 ou 9 utilises)
 *   9..17  : 9 OUTPUT slots (miroirs des inputs)
 *   18     : FUEL (coal, charcoal, coal block)
 *   19     : RF_CONVERTER upgrade
 *   20     : IO_EXPANSION upgrade (IO I..IV selon tier)
 *   21     : SPEED_BOOSTER upgrade (stack jusqu'a 8)
 *   22     : EFFICIENCY_CARD upgrade (stack jusqu'a 10)
 *
 * Mode energie:
 *   - Par defaut : coal (lit slot 18, consomme un coal = N operations)
 *   - Avec upgrade RF_CONVERTER (slot 19) ou tier.nativeRF=true :
 *     consomme RF du buffer interne, au rythme baseRfPerTick * consoMultiplier.
 *
 * Logique de cook parallele (v1.0.249+) :
 *   - Pour chaque paire (input[i], output[i]) ou i < getIOSlotCount() :
 *     - Verifie recette furnace vanilla existe pour input
 *     - Verifie output peut accepter le resultat
 *   - Si >= 1 paire active : consomme fuel * activeCount par tick
 *   - Progresse maxCookTime = tier.baseCookTime() ticks (meme timer pour toutes paires)
 *   - Output vers slot correspondant au meme index (quand timer atteint max)
 */
public class TileFurnaceNexus extends TileEntity implements ITickable,
        net.minecraft.inventory.ISidedInventory {

    // === NOUVEAUX INDICES SLOTS (v1.0.248 Phase 2 IO Expansion) ===
    // Structure etendue pour supporter IO I..IV (1/3/5/7/9 inputs+outputs).
    //
    // Ancienne structure (v1.0.247 et avant) : 7 slots
    //   0 INPUT, 1 FUEL, 2 OUTPUT, 3..6 upgrades
    //
    // Nouvelle structure : 23 slots
    //   0..8  : 9 slots INPUT (seuls les N premiers sont utilises selon tier IO)
    //   9..17 : 9 slots OUTPUT (miroir des inputs, par position)
    //   18    : FUEL (1 slot)
    //   19..22: 4 upgrades (RF_CONVERTER, IO_EXPANSION, SPEED_BOOSTER, EFFICIENCY)
    //
    // Migration des saves v1.0.247 geree dans readFromNBT via InvFormat.
    public static final int SLOT_INPUT_BASE = 0;
    public static final int SLOT_INPUT_MAX = 9;
    public static final int SLOT_OUTPUT_BASE = 9;
    public static final int SLOT_OUTPUT_MAX = 9;
    public static final int SLOT_FUEL = 18;
    public static final int SLOT_UPGRADE_BASE = 19;  // 19..22

    /** Alias pour retro-compat code : pointe sur le 1er input. */
    public static final int SLOT_INPUT = SLOT_INPUT_BASE;
    /** Alias pour retro-compat code : pointe sur le 1er output. */
    public static final int SLOT_OUTPUT = SLOT_OUTPUT_BASE;

    /** 23 slots total. Inventory unique (pattern Mekanism) avec flag GUI_OPERATION
     *  pour bloquer extraction externe sur les slots upgrades 19..22. */
    public static final int TOTAL_SLOTS = 23;

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
    /** Inventaire NonNullList de 23 slots (INPUT[0..8], OUTPUT[9..17], FUEL[18],
     *  UPGRADES[19..22]). Voir header de classe pour detail.
     *  NonNullList garantit qu'aucun element n'est null (ItemStack.EMPTY a la place).
     *  Compatible avec ItemStackHelper.saveAllItems/loadAllItems. */
    private net.minecraft.util.NonNullList<ItemStack> inventory;
    private InternalEnergyStorage energyStorage;

    /** Flag thread-local : true pendant les operations issues du GUI Container.
     *  Seules les operations GUI ont le droit de modifier les slots upgrade (3-6).
     *  Protege contre extraction externe par mods tiers qui appelleraient
     *  tile.decrStackSize() ou tile.setInventorySlotContents() en ignorant
     *  ISidedInventory.canExtractItem. */
    private static final ThreadLocal<Boolean> GUI_OPERATION =
        ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Active le flag GUI_OPERATION autour d'une operation Container.
     *  Appelee depuis les Containers avant/apres chaque manipulation de slot upgrade. */
    public static void setGuiOperation(boolean active) {
        GUI_OPERATION.set(active);
    }

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
    /** v1.0.256 : flag auto-sort (distribue stack dans tous les inputs actifs). */
    private boolean autoSortEnabled = false;

    public TileFurnaceNexus() {
        this(FurnaceTier.IRON);  // default pour lecture NBT
    }

    public TileFurnaceNexus(FurnaceTier tier) {
        this.tier = tier;
        this.inventory = net.minecraft.util.NonNullList.withSize(TOTAL_SLOTS, ItemStack.EMPTY);
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

    /** v1.0.256 : true si l'auto-sort des inputs est active. */
    public boolean isAutoSortEnabled() { return autoSortEnabled; }
    public void toggleAutoSort() {
        autoSortEnabled = !autoSortEnabled;
        markDirty();
        // Force un block update pour que le client voie le nouvel etat du flag.
        // Sans ca, le bouton S ne changerait pas de couleur cote client apres clic.
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /** Applique l'Upgrade Kit : debloque RF + slots upgrade. Irreversible. */
    public void applyEnhancement() {
        if (isEnhanced) return;  // deja enhanced, no-op
        this.isEnhanced = true;
        markDirty();
        // Sync client via notifyBlockUpdate pour que le BlockState update les LED
        if (world != null && !world.isRemote) {
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public int getMaxCookTime() { return maxCookTime; }
    /** Ticks restants sur le fuel actuel (0 = pas de fuel actif). */
    public int getFuelBurnTicks() { return fuelBurnTicks; }
    /** Ticks totaux du fuel consomme (pour calculer le ratio de flamme). */
    public int getFuelTotalBurnTicks() { return fuelTotalBurnTicks; }

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
        ItemStack rfSlot = inventory.get(SLOT_UPGRADE_BASE + FurnaceUpgrade.RF_CONVERTER.slotIndex);
        return !rfSlot.isEmpty();
    }

    /** True si le furnace est actuellement en train de cuire (flamme visible). */
    public boolean isActivelyCooking() {
        return (fuelBurnTicks > 0 || (isRFMode() && energyStorage.getEnergyStored() > 0))
            && cookProgress > 0;
    }

    /** Nombre d'items dans le slot SPEED_BOOSTER (0 si vide). Public pour le GUI. */
    public int getSpeedBoosterCount() {
        ItemStack slot = inventory.get(SLOT_UPGRADE_BASE + FurnaceUpgrade.SPEED_BOOSTER.slotIndex);
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    /** Nombre d'items dans le slot EFFICIENCY (0 si vide). Public pour le GUI. */
    public int getEfficiencyCount() {
        ItemStack slot = inventory.get(SLOT_UPGRADE_BASE + FurnaceUpgrade.EFFICIENCY.slotIndex);
        return slot.isEmpty() ? 0 : slot.getCount();
    }

    /**
     * Tier de la carte IO_EXPANSION installee, 0 si aucune.
     *  0 : 1 input + 1 output (vanilla)
     *  1 : 3 inputs + 3 outputs
     *  2 : 5 + 5
     *  3 : 7 + 7
     *  4 : 9 + 9
     */
    public int getIOTier() {
        ItemStack slot = inventory.get(SLOT_UPGRADE_BASE + FurnaceUpgrade.IO_EXPANSION.slotIndex);
        if (slot.isEmpty()) return 0;
        if (!(slot.getItem() instanceof com.nexusabsolu.mod.items.ItemFurnaceUpgrade)) return 0;
        return ((com.nexusabsolu.mod.items.ItemFurnaceUpgrade) slot.getItem()).getTier();
    }

    /** Nombre de slots input (= nombre de slots output) selon le tier IO. */
    public int getIOSlotCount() {
        return 1 + 2 * getIOTier();  // tier 0 = 1, 1 = 3, 2 = 5, 3 = 7, 4 = 9
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
     *  - SPEED_BOOSTER : x1.30 par item (cumulatif multiplicatif)
     *    Avant v1.0.265 : x1.40 (trop violent)
     *    v1.0.265 (bug) : +30% lineaire (trop faible, Alexis : 'il faut consommer plus')
     *    v1.0.266 : x1.30 cumulatif (compromis raisonnable)
     *  - EFFICIENCY : x0.80 par item (cumulatif multiplicatif = -20%/item)
     *
     * Avec 8 Speed : 1.30^8 = 8.16 (avant 14.76). Toujours eleve mais gerable
     * avec la capacite augmentee du four (v1.0.266 : × 10) et une energy cube
     * branchee.
     */
    public int getEffectiveRfPerTick() {
        int spdCount = getSpeedBoosterCount();
        int effCount = getEfficiencyCount();
        float consoMult = (float) (Math.pow(1.30, spdCount) * Math.pow(0.80, effCount));
        return Math.max(1, (int)(tier.baseRfPerTick * consoMult));
    }

    // === TICK ===

    @Override
    public void update() {
        if (world.isRemote) return;

        // v1.0.259 : purge le RF orphelin si le four n'est pas en mode RF.
        // Un save precedent (avant ce fix) peut avoir stocke du RF alors que
        // le four n'est pas enhanced ou n'a pas de carte RF Converter. Ce
        // RF est invisible a l'usage donc on le purge pour eviter la confusion
        // ('24k RF dans un four qui ne peut pas les utiliser').
        if (!isRFMode() && energyStorage.getEnergyStored() > 0) {
            energyStorage.drainInternal(energyStorage.getEnergyStored());
            markDirty();
        }

        // v1.0.240 FIX : doAutoIO doit TOUJOURS tourner tous les 10 ticks,
        // meme si le four est vide/sans fuel/sans recette. Sinon les hoppers
        // auto-pull configures par l'utilisateur ne peuvent pas AMORCER le
        // four : ils attendraient un four deja actif pour tirer du fuel,
        // ce qui est impossible sans fuel initial.
        //
        // On tick le counter en tete, et on delegue a tryCookTick() pour
        // la logique de cuisson qui peut return early sans bloquer l'IO.
        ioTickCounter++;
        if (ioTickCounter >= 10) {
            ioTickCounter = 0;
            doAutoIO();
        }

        tryCookTick();
    }

    /**
     * Logique de cuisson d'un tick, mode PARALLELE ou SEQUENTIEL selon autoSort.
     *
     * - autoSort ON  : cuisson parallele, toutes les paires actives cuisent
     *                  ensemble, conso fuel/RF x nombre de paires actives,
     *                  timer global partage.
     * - autoSort OFF : cuisson sequentielle, SEULE la premiere paire active
     *                  (i le plus bas) cuit. Les autres attendent. Conso fuel/RF
     *                  de base (1x), comme un four classique. Comportement
     *                  attendu par l'utilisateur quand il veut remplir chaque
     *                  slot manuellement sans les voir tous cuire en meme temps.
     *
     * Pour chaque paire (input[i], output[i]) ou i < getIOSlotCount() :
     *   - Si input[i] a une recette valide ET output[i] peut accepter le resultat,
     *     cette paire est consideree ACTIVE.
     */
    private void tryCookTick() {
        boolean wasActive = cookProgress > 0 || fuelBurnTicks > 0;

        int slotCount = getIOSlotCount();

        // 1. Identifier les paires actives et cacher leurs resultats
        ItemStack[] cachedResults = new ItemStack[SLOT_INPUT_MAX];
        int activeCount = 0;
        for (int i = 0; i < slotCount; i++) {
            ItemStack input = inventory.get(SLOT_INPUT_BASE + i);
            if (input.isEmpty()) continue;
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(input);
            if (result.isEmpty()) continue;
            ItemStack output = inventory.get(SLOT_OUTPUT_BASE + i);
            if (!output.isEmpty()) {
                if (!ItemHandlerHelper.canItemStacksStack(output, result)) continue;
                if (output.getCount() + result.getCount() > output.getMaxStackSize()) continue;
            }
            cachedResults[i] = result;
            activeCount++;

            // v1.0.279 : mode SEQUENTIEL (autoSort OFF) => on ne prend que la
            // premiere paire active. On sort de la boucle et on cuit uniquement
            // ce slot. Comportement 'four vanilla classique' applique a chaque
            // slot individuellement dans l'ordre.
            if (!autoSortEnabled) break;
        }

        if (activeCount == 0) {
            resetProgress();
            updateActiveState(wasActive);
            return;
        }

        // 2. Fuel : conso = activeCount x baseRF (parallele) ou 1x (sequentiel).
        // En mode sequentiel, activeCount vaut toujours 0 ou 1 apres le break.
        if (!consumeFuelIfNeeded(activeCount)) {
            resetProgress();
            updateActiveState(wasActive);
            return;
        }

        // 3. Progress
        maxCookTime = getEffectiveMaxCookTime();
        cookProgress++;
        if (cookProgress >= maxCookTime) {
            // Execute la cuisson pour chaque paire active (1 paire en sequentiel,
            // N paires en parallele).
            for (int i = 0; i < slotCount; i++) {
                ItemStack result = cachedResults[i];
                if (result == null) continue;

                ItemStack input = inventory.get(SLOT_INPUT_BASE + i);
                ItemStack output = inventory.get(SLOT_OUTPUT_BASE + i);

                if (output.isEmpty()) {
                    inventory.set(SLOT_OUTPUT_BASE + i, result.copy());
                } else {
                    output.grow(result.getCount());
                }
                input.shrink(1);
                if (input.getCount() <= 0) inventory.set(SLOT_INPUT_BASE + i, ItemStack.EMPTY);
            }
            cookProgress = 0;
            markDirty();
        }

        updateActiveState(wasActive);
    }

    /**
     * Re-render le blockstate si l'etat actif a change depuis le debut du tick.
     * Force la transition LED grise <-> LED cyan et texture eteinte <-> active.
     */
    private void updateActiveState(boolean wasActive) {
        boolean isActive = cookProgress > 0 || fuelBurnTicks > 0;
        if (wasActive != isActive) {
            markDirty();
            net.minecraft.block.state.IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    /**
     * Auto pull/push items selon SideConfig, toutes les 10 ticks.
     * - Face ITEM_IN  : pull du voisin vers SLOT_INPUT (filtre : smeltable)
     * - Face FUEL_IN  : pull du voisin vers SLOT_FUEL (filtre : getCoalOps > 0)
     * - Face ITEM_OUT : push du SLOT_OUTPUT vers voisin
     * Energy est gere automatiquement par la capability CapabilityEnergy sur toutes faces.
     */
    /**
     * Auto pull/push items selon SideConfig, toutes les 10 ticks.
     * - Face ITEM_IN  : pull du voisin vers 1er input libre (parmi les slots actifs)
     * - Face FUEL_IN  : pull du voisin vers SLOT_FUEL
     * - Face ITEM_OUT : push depuis tous les outputs non-vides vers voisin
     *
     * Energy est gere automatiquement par la capability CapabilityEnergy sur toutes faces.
     *
     * v1.0.249 : adapte a la Phase 2 IO Expansion. Push parcourt tous les slots
     * output[0..N-1] selon getIOSlotCount(). Pull cherche le 1er input libre
     * dans [0..N-1] ou le 1er input stackable avec un item extrait.
     */
    private void doAutoIO() {
        int slotCount = getIOSlotCount();  // 1, 3, 5, 7 ou 9

        for (int faceIdx = 0; faceIdx < 6; faceIdx++) {
            boolean outActive = sideConfig.isFaceActive(SC_TYPE_ITEM_OUT, faceIdx);
            boolean inActive = sideConfig.isFaceActive(SC_TYPE_ITEM_IN, faceIdx);
            boolean fuelActive = sideConfig.isFaceActive(SC_TYPE_FUEL_IN, faceIdx);
            if (!outActive && !inActive && !fuelActive) continue;

            EnumFacing face = EnumFacing.getFront(faceIdx);
            net.minecraft.util.math.BlockPos neighborPos = getPos().offset(face);
            TileEntity neighbor = world.getTileEntity(neighborPos);
            if (neighbor == null) continue;

            EnumFacing opposite = face.getOpposite();
            net.minecraftforge.items.IItemHandler neighborHandler = neighbor.getCapability(
                CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opposite);
            if (neighborHandler == null) continue;

            // ITEM_OUT : push depuis tous les slots output actifs
            if (outActive) {
                for (int i = 0; i < slotCount; i++) {
                    int slotIdx = SLOT_OUTPUT_BASE + i;
                    ItemStack out = inventory.get(slotIdx);
                    if (out.isEmpty()) continue;
                    ItemStack remaining = ItemHandlerHelper.insertItemStacked(neighborHandler, out.copy(), false);
                    int inserted = out.getCount() - remaining.getCount();
                    if (inserted > 0) {
                        out.shrink(inserted);
                        if (out.getCount() <= 0) inventory.set(slotIdx, ItemStack.EMPTY);
                        markDirty();
                    }
                }
            }

            // ITEM_IN : pull vers le 1er slot input qui peut accepter (stackable ou vide)
            if (inActive) {
                pullFromNeighborToAnyInput(neighborHandler, slotCount);
            }

            // FUEL_IN : pull fuels vers SLOT_FUEL (toujours unique)
            if (fuelActive) {
                pullFromNeighborToSlot(neighborHandler, SLOT_FUEL, false, true);
            }
        }
    }

    /**
     * Pull smeltables depuis le voisin vers le PREMIER input slot qui peut
     * l'accepter parmi [0..slotCount-1]. Priorite : slots deja remplis stackables
     * en premier (pour remplir avant d'etendre), puis slots vides.
     */
    private void pullFromNeighborToAnyInput(net.minecraftforge.items.IItemHandler handler, int slotCount) {
        // Essayer chaque slot input, dans l'ordre
        for (int i = 0; i < slotCount; i++) {
            int slotIdx = SLOT_INPUT_BASE + i;
            ItemStack current = inventory.get(slotIdx);
            if (!current.isEmpty() && current.getCount() >= current.getMaxStackSize()) continue;
            // pullFromNeighborToSlot retourne dès qu'il a pulled 1 fois. Ici on le
            // laisse tenter chaque input jusqu'a un succes. Un seul pull par tick IO.
            int before = countItem(slotIdx);
            pullFromNeighborToSlot(handler, slotIdx, true, false);
            if (countItem(slotIdx) > before) return;  // pull reussi, stop
        }
    }

    /** Helper : count d'un slot (0 si empty). */
    private int countItem(int slotIdx) {
        ItemStack s = inventory.get(slotIdx);
        return s.isEmpty() ? 0 : s.getCount();
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
        ItemStack current = inventory.get(targetSlot);
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
                inventory.set(targetSlot, taken);
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
     * Consomme du fuel pour N paires actives en parallele.
     *
     * - Mode RF : consomme baseRfPerTick * multiplier RF ce tick
     *             (plus la machine a de slots actifs, plus elle consomme)
     * - Mode coal : consomme 'multiplier' ticks de burn d'un coup.
     *               Si fuelBurnTicks epuise, recharge depuis SLOT_FUEL.
     *
     * Retourne true si la conso a reussi (fuel suffisant trouve), false sinon.
     * En cas d'echec partiel (ex. 5 ticks consumed sur 7 demandes), l'etat du
     * fuel reste dans l'etat consomme (fuel brule mais cuisson echouee ce tick).
     */
    private boolean consumeFuelIfNeeded(int multiplier) {
        if (multiplier <= 0) return true;

        if (isRFMode()) {
            int rfConso = getEffectiveRfPerTick() * multiplier;
            if (energyStorage.getEnergyStored() < rfConso) return false;
            energyStorage.drainInternal(rfConso);
            return true;
        }

        // Mode coal : consomme 'multiplier' ticks de burn, en rechargeant au besoin
        int remaining = multiplier;
        boolean dirty = false;
        while (remaining > 0) {
            if (fuelBurnTicks > 0) {
                int canConsume = Math.min(remaining, fuelBurnTicks);
                fuelBurnTicks -= canConsume;
                remaining -= canConsume;
                if (fuelBurnTicks <= 0) {
                    fuelTotalBurnTicks = 0;
                    dirty = true;
                }
            } else {
                // Plus de burn en cours : recharge depuis SLOT_FUEL
                ItemStack fuel = inventory.get(SLOT_FUEL);
                if (fuel.isEmpty()) {
                    if (dirty) markDirty();
                    return false;
                }
                int burnTime = net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(fuel);
                if (burnTime <= 0) {
                    if (dirty) markDirty();
                    return false;
                }
                fuelTotalBurnTicks = burnTime;
                fuelBurnTicks = burnTime;
                fuel.shrink(1);
                if (fuel.getCount() <= 0) inventory.set(SLOT_FUEL, ItemStack.EMPTY);
                dirty = true;
            }
        }
        if (dirty) markDirty();
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
            if (!inventory.get(i).isEmpty()) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setInteger("Slot", i);
                inventory.get(i).writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        nbt.setTag("items", items);
        // v1.0.248 : marque le format v2 (23 slots) pour differencier des saves v1 (7 slots)
        nbt.setInteger("InvFormat", 2);

        // Side config (6 faces x 4 types + eject/pull bits)
        NBTTagCompound scTag = new NBTTagCompound();
        sideConfig.writeToNBT(scTag);
        nbt.setTag("sideConfig", scTag);

        // v1.0.212 : flag Enhanced
        nbt.setBoolean("enhanced", isEnhanced);
        nbt.setBoolean("autoSort", autoSortEnabled);

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

        // v1.0.239 : Collections.fill plus idiomatique que le loop explicite
        java.util.Collections.fill(inventory, ItemStack.EMPTY);
        // v1.0.248 : migration NBT pour Phase 2 IO Expansion.
        //   Ancien format (InvFormat absent / == 0) : 7 slots
        //     0=INPUT, 1=FUEL, 2=OUTPUT, 3..6=upgrades
        //   Nouveau format (InvFormat == 2) : 23 slots
        //     0..8=INPUT, 9..17=OUTPUT, 18=FUEL, 19..22=upgrades
        int invFormat = nbt.getInteger("InvFormat");
        NBTTagList items = nbt.getTagList("items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getInteger("Slot");
            if (invFormat < 2) {
                slot = migrateSlotIndexV1(slot);
            }
            if (slot >= 0 && slot < TOTAL_SLOTS) {
                inventory.set(slot, new ItemStack(itemTag));
            }
        }

        // Migration v1.0.226 : tag "Augments" separe -> redirige vers inventory.get(3-6)
        if (nbt.hasKey("Augments")) {
            NBTTagList augList = nbt.getTagList("Augments", 10);
            for (int i = 0; i < augList.tagCount(); i++) {
                NBTTagCompound itemTag = augList.getCompoundTagAt(i);
                int slot = itemTag.getInteger("Slot");
                if (slot >= 0 && slot < 4) {
                    inventory.set(SLOT_UPGRADE_BASE + slot, new ItemStack(itemTag));
                }
            }
        }

        // Migration v1.0.218-221 : tag "upgrades" separe -> inventory.get(3-6)
        if (nbt.hasKey("upgrades")) {
            NBTTagList upgList = nbt.getTagList("upgrades", 10);
            for (int i = 0; i < upgList.tagCount(); i++) {
                NBTTagCompound itemTag = upgList.getCompoundTagAt(i);
                int slot = itemTag.getInteger("Slot");
                if (slot >= 0 && slot < 4) {
                    inventory.set(SLOT_UPGRADE_BASE + slot, new ItemStack(itemTag));
                }
            }
        }

        // Side config (si present dans le save, sinon garde les defaults)
        if (nbt.hasKey("sideConfig")) {
            sideConfig.readFromNBT(nbt.getCompoundTag("sideConfig"));
        }

        // v1.0.212 : flag Enhanced
        this.isEnhanced = nbt.getBoolean("enhanced");
        this.autoSortEnabled = nbt.getBoolean("autoSort");
    }

    /**
     * Migration des indices d'inventory du format v1 (v1.0.247 et avant) vers
     * le format v2 (v1.0.248+, Phase 2 IO Expansion).
     *
     * Format v1 (7 slots) :
     *   0 = INPUT, 1 = FUEL, 2 = OUTPUT, 3..6 = upgrades (RF, IO, SPEED, EFF)
     *
     * Format v2 (23 slots) :
     *   0..8  = INPUT   (SLOT_INPUT_BASE)
     *   9..17 = OUTPUT  (SLOT_OUTPUT_BASE)
     *   18    = FUEL
     *   19..22 = upgrades (SLOT_UPGRADE_BASE + 0..3)
     *
     * Chaque item du v1 est mappe vers son equivalent v2 :
     *   v1 slot 0 (INPUT)    -> v2 slot 0  (1er input)
     *   v1 slot 1 (FUEL)     -> v2 slot 18
     *   v1 slot 2 (OUTPUT)   -> v2 slot 9  (1er output)
     *   v1 slot 3..6 (upg)   -> v2 slot 19..22
     *
     * @param oldSlot indice v1 lu depuis le NBT
     * @return indice v2 equivalent, ou -1 si invalide
     */
    private int migrateSlotIndexV1(int oldSlot) {
        switch (oldSlot) {
            case 0: return SLOT_INPUT_BASE;          // INPUT
            case 1: return SLOT_FUEL;                 // FUEL
            case 2: return SLOT_OUTPUT_BASE;          // OUTPUT
            case 3: return SLOT_UPGRADE_BASE + 0;     // RF_CONVERTER
            case 4: return SLOT_UPGRADE_BASE + 1;     // IO_EXPANSION
            case 5: return SLOT_UPGRADE_BASE + 2;     // SPEED_BOOSTER
            case 6: return SLOT_UPGRADE_BASE + 3;     // EFFICIENCY
            default: return -1;
        }
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
        if (capability == CapabilityEnergy.ENERGY) {
            // v1.0.259 : ne PAS exposer la capability energy si le four n'est
            // pas en mode RF. Sinon des cables/machines remplissent inutilement
            // le stockage interne avec du RF que le four ne peut meme pas utiliser
            // (ex. energy cube Mekanism qui pousse en continu).
            return isRFMode();
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            // Meme check que hasCapability : pas de capability en mode coal
            if (!isRFMode()) return null;
            return (T) energyStorage;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            // Pattern Thermal TileInventory.getCapability :
            // - facing != null : SidedInvWrapper (respecte ISidedInventory)
            // - facing == null : InvWrapper sur l'IInventory (23 slots mais
            //   protection upgrade via flag GUI_OPERATION dans les operations)
            if (facing != null) {
                return (T) new SidedInvWrapper(this, facing);
            } else {
                return (T) new net.minecraftforge.items.wrapper.InvWrapper(this);
            }
        }
        return super.getCapability(capability, facing);
    }

    // === IInventory (boilerplate) ===

    @Override public int getSizeInventory() { return TOTAL_SLOTS; }
    @Override public boolean isEmpty() {
        // On considere vide si les 3 slots exposes + les 4 upgrades sont vides
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int index) { return inventory.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        // v1.0.227 : protection slots upgrade contre extraction externe.
        // SEULEMENT cote serveur. Cote client c'est juste le miroir de l'etat
        // serveur, le bloquer casse la synchronisation SPacketWindowItems.
        if (index >= SLOT_UPGRADE_BASE && !GUI_OPERATION.get()
            && world != null && !world.isRemote) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result;
        if (stack.getCount() <= count) {
            result = stack;
            inventory.set(index, ItemStack.EMPTY);
        } else {
            result = stack.splitStack(count);
            if (stack.getCount() == 0) inventory.set(index, ItemStack.EMPTY);
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        // v1.0.227 : protection slots upgrade (seulement cote serveur)
        if (index >= SLOT_UPGRADE_BASE && !GUI_OPERATION.get()
            && world != null && !world.isRemote) {
            return ItemStack.EMPTY;
        }
        ItemStack s = inventory.get(index);
        inventory.set(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // v1.0.227 : protection slots upgrade contre modification externe.
        // Bloque SEULEMENT cote serveur. Cote client c'est le miroir de
        // l'etat serveur (SPacketWindowItems/SPacketSetSlot) et doit
        // toujours etre a jour.
        if (index >= SLOT_UPGRADE_BASE && !GUI_OPERATION.get()
            && world != null && !world.isRemote) {
            return;
        }
        inventory.set(index, stack);
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
        // OUTPUT : rien ne peut etre insere manuellement dans un output
        if (isOutputSlot(index)) return false;
        // FUEL : seulement du fuel valide
        if (index == SLOT_FUEL) return getCoalOps(stack) > 0;
        // INPUT (tous les 9) : seulement des smeltables
        if (isInputSlot(index)) {
            return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
        }
        // Slots upgrade : valides si c'est le bon type d'upgrade item
        if (index >= SLOT_UPGRADE_BASE && index < TOTAL_SLOTS) {
            if (!isEnhanced) return false;
            if (stack.isEmpty()) return true;
            if (!(stack.getItem() instanceof com.nexusabsolu.mod.items.ItemFurnaceUpgrade)) return false;
            com.nexusabsolu.mod.items.ItemFurnaceUpgrade item =
                (com.nexusabsolu.mod.items.ItemFurnaceUpgrade) stack.getItem();
            return (SLOT_UPGRADE_BASE + item.getCategory().slotIndex) == index;
        }
        return false;
    }

    /** Helper : slot dans la plage INPUT [0..8]. */
    public static boolean isInputSlot(int index) {
        return index >= SLOT_INPUT_BASE && index < SLOT_INPUT_BASE + SLOT_INPUT_MAX;
    }
    /** Helper : slot dans la plage OUTPUT [9..17]. */
    public static boolean isOutputSlot(int index) {
        return index >= SLOT_OUTPUT_BASE && index < SLOT_OUTPUT_BASE + SLOT_OUTPUT_MAX;
    }

    // === ISidedInventory : CRITICAL securite slots upgrades ===
    //
    // Sans ISidedInventory, un InvWrapper(this) expose TOUS les slots (y compris
    // les 4 upgrades), ce qui permet a un hopper/pipe adjacent d'EXTRAIRE les
    // upgrades du four. Bug ingame rapporte v1.0.216 : la Carte d'Efficience
    // sort du four toute seule.
    //
    // v1.0.248 : on expose maintenant TOUS les slots input/fuel/output aux faces
    // exterieures (9 inputs + 9 outputs + 1 fuel = 19 slots). Les slots upgrades
    // restent cachees des hoppers.
    private static final int[] EXPOSED_SLOTS;
    static {
        EXPOSED_SLOTS = new int[SLOT_INPUT_MAX + SLOT_OUTPUT_MAX + 1];
        int idx = 0;
        for (int i = 0; i < SLOT_INPUT_MAX; i++) EXPOSED_SLOTS[idx++] = SLOT_INPUT_BASE + i;
        for (int i = 0; i < SLOT_OUTPUT_MAX; i++) EXPOSED_SLOTS[idx++] = SLOT_OUTPUT_BASE + i;
        EXPOSED_SLOTS[idx] = SLOT_FUEL;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return EXPOSED_SLOTS;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        // INPUT : seulement smeltables, ET seulement dans les slots ACTIFS selon tier IO
        // FUEL  : seulement fuel (coal/charcoal/etc.)
        // OUTPUT : rien ne peut y etre insere
        // v1.0.271 : ajout du check getIOSlotCount pour eviter que des hoppers
        // inserent dans les slots input 1-8 quand il n'y a pas de carte IO
        // (tier 0 = seulement slot 0 actif).
        if (isInputSlot(index)) {
            if (index - SLOT_INPUT_BASE >= getIOSlotCount()) return false;
            return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
        }
        if (index == SLOT_FUEL) return getCoalOps(stack) > 0;
        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        // Seuls les OUTPUT peuvent etre extraits (recettes cuites)
        // v1.0.271 : meme check pour les outputs : seulement slots actifs
        if (isOutputSlot(index)) {
            return index - SLOT_OUTPUT_BASE < getIOSlotCount();
        }
        return false;
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() {
        java.util.Collections.fill(inventory, ItemStack.EMPTY);
    }

    @Override public String getName() { return "container.nexus.furnace_" + tier.registryName; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
}
