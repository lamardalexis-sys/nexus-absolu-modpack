package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container pour le GUI dedie Upgrades du Furnace Nexus.
 *
 * Pattern inspire de Mekanism (GuiUpgradeManagement / ContainerUpgradeManagement) :
 * au lieu d'avoir un side-panel qui fait conflict avec les tooltips, on ouvre
 * un GUI complet dedie quand le joueur clique sur l'onglet Upgrades du Furnace.
 *
 * Layout slots :
 *  0 : upgrade RF_CONVERTER  (slot 3 du TileEntity)
 *  1 : upgrade IO_EXPANSION  (slot 4)
 *  2 : upgrade SPEED_BOOSTER (slot 5)
 *  3 : upgrade EFFICIENCY    (slot 6)
 *  puis inventaire joueur (27 slots) + hotbar (9 slots) = slots 4..39
 *
 * Les 4 slots upgrade pointent vers le MEME TileEntity que ContainerFurnaceNexus.
 * Donc toute modification est synchronisee automatiquement entre les deux GUI.
 */
public class ContainerFurnaceUpgrades extends Container {

    private final TileFurnaceNexus tile;

    public ContainerFurnaceUpgrades(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        this.tile = tile;

        // === 4 SLOTS UPGRADES (carre 2x2 centre) ===
        // Positions calculees pour que le carre soit centre horizontalement dans un GUI 176x166
        // slotSize = 18 vanilla, gap = 4, totalW = 40, startX = (176 - 40) / 2 = 68
        // startY = 20 (sous le titre)
        int slotSize = 18;
        int gap = 4;
        int totalW = slotSize * 2 + gap;
        int startX = (176 - totalW) / 2;  // = 68
        int startY = 30;

        int[][] slotPositions = {
            {0, 0}, {1, 0}, {0, 1}, {1, 1}
        };

        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            final FurnaceUpgrade upgrade = up;
            int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
            int col = slotPositions[up.slotIndex][0];
            int row = slotPositions[up.slotIndex][1];
            int sx = startX + col * (slotSize + gap);
            int sy = startY + row * (slotSize + gap);

            addSlotToContainer(new Slot(tile, slotIdx, sx, sy) {
                @Override
                public int getSlotStackLimit() {
                    return upgrade.maxStackSize;
                }
                @Override
                public boolean isItemValid(ItemStack stack) {
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

        // === INVENTAIRE JOUEUR ===
        // GUI hauteur 166 vanilla, inventaire rangees y=84,102,120, hotbar y=142
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(
                    playerInv, col + row * 9 + 9,
                    8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();

        // Slots 0-3 = upgrades, 4-30 = inventaire joueur, 31-39 = hotbar
        if (index < 4) {
            // Upgrade -> inventaire joueur
            if (!mergeItemStack(stack, 4, 40, true)) return ItemStack.EMPTY;
        } else {
            // Inventaire/hotbar -> upgrade slot (si valide)
            if (stack.getItem() instanceof com.nexusabsolu.mod.items.ItemFurnaceUpgrade) {
                com.nexusabsolu.mod.items.ItemFurnaceUpgrade upItem =
                    (com.nexusabsolu.mod.items.ItemFurnaceUpgrade) stack.getItem();
                int targetSlot = upItem.getCategory().slotIndex;  // 0-3
                if (!mergeItemStack(stack, targetSlot, targetSlot + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 31) {
                // Inventaire -> hotbar
                if (!mergeItemStack(stack, 31, 40, false)) return ItemStack.EMPTY;
            } else {
                // Hotbar -> inventaire
                if (!mergeItemStack(stack, 4, 31, false)) return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
        else slot.onSlotChanged();
        return result;
    }
}
