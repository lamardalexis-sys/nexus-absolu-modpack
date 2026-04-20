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
    // 5 champs syncs : cookProgress, fuelBurnTicks, energy, maxCookTime, fuelTotalBurnTicks
    private int[] cachedFields = new int[5];

    public ContainerFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        this.tile = tile;

        // === SLOTS MACHINE ===
        // Positions matchent la texture gui_furnace.png v6 (ySize=186)
        // 0 : input dessine a (40, 18) -> slot a (41, 19)
        addSlotToContainer(new Slot(tile, TileFurnaceNexus.SLOT_INPUT, 41, 19) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
            }
        });
        // 1 : fuel dessine a (40, 50) -> slot a (41, 51)
        addSlotToContainer(new SlotFurnaceFuel(tile, TileFurnaceNexus.SLOT_FUEL, 41, 51));
        // 2 : output_large dessine a (100, 20) 26x26 -> slot a (104, 24)
        addSlotToContainer(new SlotFurnaceOutput(
            playerInv.player, tile, TileFurnaceNexus.SLOT_OUTPUT, 104, 24));

        // === 4 SLOTS UPGRADES (hors-ecran par defaut, GUI les repositionne dans le panneau) ===
        // v1.0.227 : retour Slot classique. La protection contre extraction externe
        // est maintenant faite par TileFurnaceNexus via le flag ThreadLocal
        // GUI_OPERATION : setGuiOperation(true) avant chaque slotClick/transferStack-
        // InSlot dans ce Container, puis setGuiOperation(false) apres (via try/finally).
        // Toutes les autres tentatives d'extraction (mods externes, hoppers bypass,
        // etc.) sont bloquees au niveau decrStackSize/removeStackFromSlot/setInventory-
        // SlotContents qui refusent les operations sur slots 3-6.
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            final FurnaceUpgrade upgrade = up;
            int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
            // Position -1000 = cache. Le GUI les deplace quand upgradesOpen = true.
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

        // === INVENTAIRE JOUEUR (positions ajustees a ySize=186) ===
        // Texture: inventaire rangees y=103,121,139, hotbar y=161
        // Slot MC = xPos/yPos + 1 par rapport au frame texture
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    playerInv, col + row * 9 + 9, 8 + col * 18, 104 + row * 18));
            }
        }
        // Hotbar a y=162 (frame y=161 + 1)
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 162));
        }
    }

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

        int machineSlotsEnd = 7;        // slots 0-6 = machine
        int playerInvStart = 7;         // slots 7-33 = inv + hotbar
        int playerInvEnd = 43;

        if (index < machineSlotsEnd) {
            // Machine -> inventaire
            if (!mergeItemStack(stack, playerInvStart, playerInvEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Inventaire -> machine (priorite intelligente)
            if (!FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty()) {
                // Smeltable -> slot input
                if (!mergeItemStack(stack, TileFurnaceNexus.SLOT_INPUT,
                        TileFurnaceNexus.SLOT_INPUT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (net.minecraft.tileentity.TileEntityFurnace.isItemFuel(stack)) {
                // Fuel -> slot fuel
                if (!mergeItemStack(stack, TileFurnaceNexus.SLOT_FUEL,
                        TileFurnaceNexus.SLOT_FUEL + 1, false)) {
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] currentFields = {
            tile.getCookProgress(),
            tile.getFuelBurnTicks(),          // id=1 : ticks restants sur fuel
            tile.getEnergyStored(),
            tile.getMaxCookTime(),
            tile.getFuelTotalBurnTicks()      // id=4 : ticks totaux du fuel (pour ratio flamme)
        };
        // Pattern ContainerCondenseur : envoie d'abord a tous les listeners,
        // puis update cachedFields APRES la boucle (sinon seul le premier
        // listener recoit la mise a jour)
        for (IContainerListener listener : this.listeners) {
            for (int i = 0; i < currentFields.length; i++) {
                if (cachedFields[i] != currentFields[i]) {
                    listener.sendWindowProperty(this, i, currentFields[i]);
                }
            }
        }
        for (int i = 0; i < currentFields.length; i++) {
            cachedFields[i] = currentFields[i];
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        // Client receive, apply to local copy of tile
        switch (id) {
            case 0: tile.setCookProgressClient(data); break;
            case 1: tile.setFuelBurnTicksClient(data); break;
            case 2: tile.setEnergyStoredClient(data); break;
            case 3: tile.setMaxCookTimeClient(data); break;
            case 4: tile.setFuelTotalBurnTicksClient(data); break;
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
        return false;
    }

    public TileFurnaceNexus getTile() { return tile; }
}
