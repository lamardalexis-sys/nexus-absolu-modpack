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
     * 10 champs syncs = 5 valeurs int * 2 shorts chacune (low 16 bits + high 16 bits).
     *
     * sendWindowProperty cote Minecraft 1.12.2 envoie un SHORT SIGNE (-32768..32767).
     * Pour des valeurs int qui peuvent depasser 32767 (notamment energyStored pour les
     * tiers T6+, ou fuelBurnTicks avec du coal block = 16000 * speed upgrade), il faut
     * splitter :
     *   id 2k   = bits bas (stockes dans short via (short)value)
     *   id 2k+1 = bits hauts ((short)(value >>> 16))
     *
     * Layout des ids :
     *   0,1 = cookProgress       (<<32767 OK en pratique mais split par coherence)
     *   2,3 = fuelBurnTicks      (coal block * speed upgrade peut depasser)
     *   4,5 = energyStored       (T6+ = 60k+, critique)
     *   6,7 = maxCookTime        (<<32767 OK)
     *   8,9 = fuelTotalBurnTicks (coal block = 16000, avec upgrades possible >32767)
     *
     * Pattern emprunte a Thermal Expansion et Mekanism qui utilisent ce split pour
     * tous leurs containers.
     */
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
    //
    // Pattern split int -> 2 shorts : sendWindowProperty envoie un short signe.
    // Pour un int N :
    //   - bits bas : (short)N        -> reconstitues cote client par (int)((short)low) & 0xFFFF
    //     (le cast (short) tronque automatiquement, puis & 0xFFFF pour non-sign-extend)
    //   - bits hauts : (short)(N >>> 16)  -> cote client (high << 16)
    // Total : low | high = int d'origine, exact.

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] currentFields = {
            tile.getCookProgress(),         // id 0/1
            tile.getFuelBurnTicks(),        // id 2/3
            tile.getEnergyStored(),         // id 4/5
            tile.getMaxCookTime(),          // id 6/7
            tile.getFuelTotalBurnTicks()    // id 8/9
        };
        // Envoie a tous les listeners, puis update cachedFields APRES la boucle
        // (sinon seul le premier listener recoit la mise a jour)
        for (IContainerListener listener : this.listeners) {
            for (int i = 0; i < currentFields.length; i++) {
                if (cachedFields[i] != currentFields[i]) {
                    int value = currentFields[i];
                    // Low 16 bits
                    listener.sendWindowProperty(this, i * 2, value & 0xFFFF);
                    // High 16 bits
                    listener.sendWindowProperty(this, i * 2 + 1, (value >>> 16) & 0xFFFF);
                }
            }
        }
        for (int i = 0; i < currentFields.length; i++) {
            cachedFields[i] = currentFields[i];
        }
    }

    /**
     * Recoit un short client-side. On reconstitue l'int en combinant low/high
     * via un buffer intermediaire (pendingFields[]) car les 2 paquets low et
     * high arrivent separement.
     */
    private final int[] pendingFields = new int[5];

    @Override
    public void updateProgressBar(int id, int data) {
        // data arrive comme int mais en realite = short tronque cote serveur.
        // On le convertit en unsigned 16-bit pour avoir la vraie valeur du split.
        int unsigned = data & 0xFFFF;

        int fieldIdx = id / 2;
        boolean isHighBits = (id % 2) == 1;

        if (fieldIdx < 0 || fieldIdx >= pendingFields.length) return;

        if (isHighBits) {
            // Combine les high bits avec les low bits deja recus
            pendingFields[fieldIdx] = (pendingFields[fieldIdx] & 0x0000FFFF) | (unsigned << 16);
            // On applique la valeur finale au tile (apres reception low+high)
            int finalValue = pendingFields[fieldIdx];
            applyFieldToTile(fieldIdx, finalValue);
        } else {
            // Stocke les low bits, attend le high
            pendingFields[fieldIdx] = (pendingFields[fieldIdx] & 0xFFFF0000) | unsigned;
        }
    }

    private void applyFieldToTile(int fieldIdx, int value) {
        switch (fieldIdx) {
            case 0: tile.setCookProgressClient(value); break;
            case 1: tile.setFuelBurnTicksClient(value); break;
            case 2: tile.setEnergyStoredClient(value); break;
            case 3: tile.setMaxCookTimeClient(value); break;
            case 4: tile.setFuelTotalBurnTicksClient(value); break;
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
