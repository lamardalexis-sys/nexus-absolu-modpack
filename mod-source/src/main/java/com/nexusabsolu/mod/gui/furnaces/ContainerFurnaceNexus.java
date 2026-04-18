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
    private int[] cachedFields = new int[4];  // cookProgress, fuelRemaining, energy, maxCookTime

    public ContainerFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        this.tile = tile;

        // === SLOTS MACHINE ===
        // Positions matchent la texture gui_furnace.png
        // 0 : input (56, 17) correspond au slot dessine a (55, 16)
        addSlotToContainer(new Slot(tile, TileFurnaceNexus.SLOT_INPUT, 56, 17) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty();
            }
        });
        // 1 : fuel (56, 53) correspond a (55, 52) dans la texture
        addSlotToContainer(new SlotFurnaceFuel(tile, TileFurnaceNexus.SLOT_FUEL, 56, 53));
        // 2 : output (120, 34) correspond a OUTPUT_LARGE (116, 30) centre
        addSlotToContainer(new SlotFurnaceOutput(
            playerInv.player, tile, TileFurnaceNexus.SLOT_OUTPUT, 120, 34));

        // === 4 SLOTS UPGRADES (162, 16/34/52/70) ===
        // Matchent la texture upgrade slots at (161, 15+18*i)
        int upgradeX = 162;
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            final FurnaceUpgrade upgrade = up;
            int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
            int yPos = 16 + up.slotIndex * 18;
            addSlotToContainer(new Slot(tile, slotIdx, upgradeX, yPos) {
                @Override
                public int getSlotStackLimit() {
                    return upgrade.maxStackSize;
                }
                @Override
                public boolean isItemValid(ItemStack stack) {
                    // Sprint B validera par type
                    return true;
                }
            });
        }

        // === INVENTAIRE JOUEUR (27 slots) ===
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    playerInv, col + row * 9 + 9, 8 + col * 18, 95 + row * 18));
            }
        }
        // === HOTBAR (9 slots) ===
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 153));
        }
    }

    // === SHIFT-CLICK ===

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
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

    // === SYNC CLIENT (progress, fuel, energy) ===

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] currentFields = {
            tile.getCookProgress(),
            tile.getFuelRemaining(),
            tile.getEnergyStored(),
            tile.getMaxCookTime()
        };
        for (IContainerListener listener : this.listeners) {
            for (int i = 0; i < currentFields.length; i++) {
                if (cachedFields[i] != currentFields[i]) {
                    listener.sendWindowProperty(this, i, currentFields[i]);
                    cachedFields[i] = currentFields[i];
                }
            }
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        // Client receive, apply to local copy of tile
        switch (id) {
            case 0: tile.setCookProgressClient(data); break;
            case 1: tile.setFuelRemainingClient(data); break;
            case 2: tile.setEnergyStoredClient(data); break;
            case 3: tile.setMaxCookTimeClient(data); break;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(tile.getPos().getX() + 0.5,
            tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }

    public TileFurnaceNexus getTile() { return tile; }
}
