package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

/**
 * Container GUI pour les Furnaces Nexus (tous tiers).
 *
 * Layout slots :
 *  0 : input  (x=56, y=17)
 *  1 : fuel   (x=56, y=53)
 *  2 : output (x=116, y=35)
 *  3 : upgrade RF_CONVERTER  (x=152, y=17)
 *  4 : upgrade IO_EXPANSION  (x=152, y=35)
 *  5 : upgrade SPEED_BOOSTER (x=152, y=53)
 *  6 : upgrade EFFICIENCY    (x=152, y=71)
 *  puis inventaire joueur (27 slots) + hotbar (9 slots)
 */
public class ContainerFurnaceNexus extends Container {

    private final TileFurnaceNexus tile;

    /**
     * Helper pour sync serveur->client des valeurs int > 32767.
     * 5 champs : cookProgress, fuelBurnTicks, energy, maxCookTime, fuelTotalBurnTicks.
     * Chaque champ utilise 2 ids (low+high 16 bits). Voir ContainerSyncHelper.
     */
    private final com.nexusabsolu.mod.gui.util.ContainerSyncHelper sync =
        new com.nexusabsolu.mod.gui.util.ContainerSyncHelper(5);

    private static final int FIELD_COOK_PROGRESS = 0;
    private static final int FIELD_FUEL_BURN_TICKS = 1;
    private static final int FIELD_ENERGY = 2;
    private static final int FIELD_MAX_COOK_TIME = 3;
    private static final int FIELD_FUEL_TOTAL_BURN_TICKS = 4;

    /**
     * Nombre de slots input/output visibles pour le tier IO au moment de
     * la creation du Container. Fige pour la duree de vie du Container :
     * si le joueur retire la carte IO pendant qu'il est dans le GUI, les
     * slots actuellement visibles restent mais bloquent les nouvelles
     * insertions via isItemValid dynamique.
     */
    private final int visibleIOSlots;

    // === Layout Mekanism-style colonnes verticales ===
    // INPUT  : colonne gauche, x=41
    // OUTPUT : colonne droite, x=104
    // Espacement vertical : 18 px entre slots
    private static final int INPUT_COL_X = 41;
    private static final int OUTPUT_COL_X = 104;
    private static final int SLOT_VERTICAL_START = 19;
    private static final int SLOT_VERTICAL_STEP = 18;

    public ContainerFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        this.tile = tile;
        this.visibleIOSlots = tile.getIOSlotCount();  // 1, 3, 5, 7 ou 9

        // === 9 SLOTS INPUT (colonne verticale gauche) ===
        // Slots [0 .. visibleIOSlots-1] : positions visibles colonne
        // Slots [visibleIOSlots .. 8]   : positions hors-ecran (-1000)
        // isItemValid check dynamique : interdit si i >= getIOSlotCount() au moment du clic
        for (int i = 0; i < TileFurnaceNexus.SLOT_INPUT_MAX; i++) {
            final int slotIdx = i;
            int posX = (i < visibleIOSlots) ? INPUT_COL_X : -1000;
            int posY = (i < visibleIOSlots)
                ? SLOT_VERTICAL_START + i * SLOT_VERTICAL_STEP
                : -1000;
            addSlotToContainer(new Slot(tile,
                    TileFurnaceNexus.SLOT_INPUT_BASE + i, posX, posY) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    // Re-check tier courant (permet de bloquer si carte retiree)
                    if (slotIdx >= tile.getIOSlotCount()) return false;
                    return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
                }
            });
        }

        // === FUEL (position variable selon tier) ===
        // Tier 0 : position vanilla (41, 51), juste sous le slot input unique
        // Tier >= I : deplace en BAS de la colonne input pour eviter la collision
        //             (sinon fuel y=51 chevauche input[1] y=37 et input[2] y=55)
        int fuelY = (visibleIOSlots <= 1)
            ? 51
            : SLOT_VERTICAL_START + visibleIOSlots * SLOT_VERTICAL_STEP + 2;  // 2px de marge
        addSlotToContainer(new SlotFurnaceFuel(tile, TileFurnaceNexus.SLOT_FUEL, 41, fuelY));

        // === 9 SLOTS OUTPUT (colonne verticale droite) ===
        for (int i = 0; i < TileFurnaceNexus.SLOT_OUTPUT_MAX; i++) {
            int posX = (i < visibleIOSlots) ? OUTPUT_COL_X : -1000;
            int posY = (i < visibleIOSlots)
                ? SLOT_VERTICAL_START + i * SLOT_VERTICAL_STEP
                : -1000;
            addSlotToContainer(new SlotFurnaceOutput(playerInv.player, tile,
                TileFurnaceNexus.SLOT_OUTPUT_BASE + i, posX, posY));
        }

        // === 4 SLOTS UPGRADES (hors-ecran par defaut, GUI les repositionne) ===
        // Voir v1.0.227 pour le pattern ThreadLocal GUI_OPERATION.
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            final FurnaceUpgrade upgrade = up;
            int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
            addSlotToContainer(new Slot(tile, slotIdx, -1000, -1000) {
                @Override
                public int getSlotStackLimit() {
                    return upgrade.maxStackSize;
                }
                @Override
                public boolean isItemValid(ItemStack stack) {
                    if (!tile.isEnhanced()) return false;
                    if (stack.isEmpty()) return true;
                    if (stack.getItem() instanceof com.nexusabsolu.mod.items.ItemFurnaceUpgrade) {
                        com.nexusabsolu.mod.items.ItemFurnaceUpgrade item =
                            (com.nexusabsolu.mod.items.ItemFurnaceUpgrade) stack.getItem();
                        return item.getCategory() == upgrade;
                    }
                    return false;
                }
            });
        }

        // === INVENTAIRE JOUEUR (position y adaptee a ySize dynamique) ===
        // Slot max input/output : y = 19 + (N-1)*18, finit a y = 35 + (N-1)*18
        // Pour ne pas chevaucher l'inventaire vanilla (y=104), on decale :
        //   extraH = max(0, (N-4) * 18)
        // - Tier 0 (N=1) : extraH = 0 (inchange)
        // - Tier I (N=3) : extraH = 0 (inchange)
        // - Tier II (N=5): extraH = 18
        // - Tier III (N=7): extraH = 54
        // - Tier IV (N=9): extraH = 90
        int extraH = Math.max(0, (visibleIOSlots - 4) * SLOT_VERTICAL_STEP);
        int invY = 104 + extraH;
        int hotbarY = 162 + extraH;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    /** Pour le GUI : combien de slots IO sont visibles dans ce container ? */
    public int getVisibleIOSlots() { return visibleIOSlots; }

    // === SHIFT-CLICK ===

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        // v1.0.227 : active le flag GUI_OPERATION pour autoriser decrStackSize/
        // setInventorySlotContents sur les slots upgrades 3-6.
        TileFurnaceNexus.setGuiOperation(true);
        try {
            return doTransferStackInSlot(player, index);
        } finally {
            TileFurnaceNexus.setGuiOperation(false);
        }
    }

    private ItemStack doTransferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();

        // === Indices dans le CONTAINER (avec Phase 2c, 9+9 slots) ===
        //   0..8    : 9 slots INPUT
        //   9       : FUEL
        //   10..18  : 9 slots OUTPUT
        //   19..22  : 4 upgrades (caches par defaut, visible quand upgrades GUI)
        //   23..49  : inventaire joueur (27)
        //   50..58  : hotbar (9)
        final int C_INPUT_BASE = 0;
        final int C_INPUT_END = 9;          // exclusif
        final int C_FUEL = 9;
        final int C_OUTPUT_BASE = 10;
        final int C_OUTPUT_END = 19;
        final int machineSlotsEnd = 23;
        final int playerInvStart = 23;
        final int playerInvEnd = 59;

        if (index < machineSlotsEnd) {
            // Machine -> inventaire
            if (!mergeItemStack(stack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Inventaire -> machine (priorite intelligente)
            if (!FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty()) {
                // Smeltable -> premier slot input libre (merge sur tous les inputs visibles)
                if (!mergeItemStack(stack, C_INPUT_BASE, C_INPUT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (net.minecraft.tileentity.TileEntityFurnace.isItemFuel(stack)) {
                // Fuel -> slot fuel
                if (!mergeItemStack(stack, C_FUEL, C_FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= playerInvStart && index < playerInvStart + 27) {
                // De l'inv principal vers hotbar
                if (!mergeItemStack(stack, playerInvStart + 27, playerInvEnd, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= playerInvStart + 27 && index < playerInvEnd) {
                // De la hotbar vers inv principal
                if (!mergeItemStack(stack, playerInvStart, playerInvStart + 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();

        if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return result;
    }

    // === SYNC CLIENT (progress, fuel ticks, energy, maxCookTime, fuelTotalBurnTicks) ===
    //
    // Utilise ContainerSyncHelper pour le split int->2 shorts afin de contourner
    // la limite 32767 de sendWindowProperty en Minecraft 1.12.2.

    /** Construit le tableau des valeurs actuelles dans l'ordre des FIELD_*. */
    private int[] fetchFields() {
        int[] fields = new int[5];
        fields[FIELD_COOK_PROGRESS] = tile.getCookProgress();
        fields[FIELD_FUEL_BURN_TICKS] = tile.getFuelBurnTicks();
        fields[FIELD_ENERGY] = tile.getEnergyStored();
        fields[FIELD_MAX_COOK_TIME] = tile.getMaxCookTime();
        fields[FIELD_FUEL_TOTAL_BURN_TICKS] = tile.getFuelTotalBurnTicks();
        return fields;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        sync.sendInitial(this, listener, fetchFields());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        sync.detectChanges(this, this.listeners, fetchFields());
    }

    @Override
    public void updateProgressBar(int id, int data) {
        int fieldIdx = sync.receiveProperty(id, data);
        if (fieldIdx < 0) return;  // en attente des high bits
        int value = sync.getField(fieldIdx);
        switch (fieldIdx) {
            case FIELD_COOK_PROGRESS:          tile.setCookProgressClient(value); break;
            case FIELD_FUEL_BURN_TICKS:        tile.setFuelBurnTicksClient(value); break;
            case FIELD_ENERGY:                 tile.setEnergyStoredClient(value); break;
            case FIELD_MAX_COOK_TIME:          tile.setMaxCookTimeClient(value); break;
            case FIELD_FUEL_TOTAL_BURN_TICKS:  tile.setFuelTotalBurnTicksClient(value); break;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tile.getPos().getX() + 0.5,
            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }

    /**
     * v1.0.227 : wrap slotClick avec flag GUI_OPERATION pour autoriser les
     * modifications des slots upgrade 3-6. La protection TileFurnaceNexus
     * bloque toute autre tentative d'extraction (mods tiers, hoppers bypass).
     */
    @Override
    public ItemStack slotClick(int slotId, int dragType,
            net.minecraft.inventory.ClickType clickType, EntityPlayer player) {
        TileFurnaceNexus.setGuiOperation(true);
        try {
            return super.slotClick(slotId, dragType, clickType, player);
        } finally {
            TileFurnaceNexus.setGuiOperation(false);
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        TileFurnaceNexus.setGuiOperation(true);
        try {
            super.onContainerClosed(player);
        } finally {
            TileFurnaceNexus.setGuiOperation(false);
        }
    }

    /**
     * Recoit les clics du GUI cote client et les execute cote serveur.
     * Pattern KRDA : on utilise mc.playerController.sendEnchantPacket cote client,
     * qui arrive ici avec un "id" qu'on decode.
     *
     * Encodage des ids:
     *   0..23  : toggle face (type * 6 + face) - 4 types x 6 faces = 24 ids
     *            ex. id=6 = type 1 face 0 (ITEM_OUT face DOWN)
     *
     * Pour les furnaces on n'expose que 3 types a l'utilisateur (ITEM_IN, ITEM_OUT,
     * FUEL_IN), l'ENERGY etant auto-acceptee partout.
     */
    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        if (id >= 0 && id < 24) {
            int type = id / 6;
            int face = id % 6;
            // On ignore toggle sur ENERGY (type 2) cote user : energy toujours on
            if (type == com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus.SC_TYPE_ENERGY) {
                return false;
            }
            tile.getSideConfig().toggleFace(type, face);
            tile.markDirty();
            return true;
        }
        // v1.0.233 : id=100 = demande ouverture du GUI Upgrades.
        // DOIT etre fait cote SERVEUR avec un EntityPlayerMP. mc.player.openGui
        // cote client n'envoie pas de packet au serveur donc le serveur garde
        // ce Container et les slotClick destines au GUI Upgrades sont traites
        // contre le mauvais Container (=> silent no-op). Ici on ouvre bien
        // le GUI via enchantItem qui est route client->serveur par vanilla.
        if (id == 100 && tile.isEnhanced()) {
            net.minecraft.util.math.BlockPos pos = tile.getPos();
            player.openGui(
                com.nexusabsolu.mod.NexusAbsoluMod.instance,
                com.nexusabsolu.mod.gui.GuiHandler.FURNACE_UPGRADES_GUI,
                player.world,
                pos.getX(), pos.getY(), pos.getZ()
            );
            return true;
        }
        return false;
    }

    public TileFurnaceNexus getTile() { return tile; }
}
