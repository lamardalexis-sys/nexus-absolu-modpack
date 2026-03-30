package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiConvertisseur extends GuiContainer {

    private final TileConvertisseur tile;
    private boolean configOpen = false;

    public boolean isConfigOpen() { return configOpen; }

    private static final int[] TIER_COLORS = {
        0xFF2A1A3E, 0xFFCC4CFF, 0xFFB299E6, 0xFF80CCB2, 0xFF4CE680, 0xFF33FF4C
    };
    private static final String[] TIER_NAMES = {"", "A", "B", "C", "D", "E"};
    private static final String[] FACE_LABELS = {"B", "H", "N", "S", "O", "E"};
    private static final String[] FACE_NAMES = {"Bas", "Haut", "Nord", "Sud", "Ouest", "Est"};

    private static final int BTN = 20;
    private static final int GAP = 2;

    public GuiConvertisseur(InventoryPlayer playerInv, TileConvertisseur tile) {
        super(new ContainerConvertisseur(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.guiLeft;
        int y = this.guiTop;

        // === MAIN BACKGROUND ===
        drawRect(x, y, x + xSize, y + ySize, 0xFF12081C);
        drawRect(x, y, x + xSize, y + 1, 0xFF6B3FA0);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF3A1F5E);
        drawRect(x, y, x + 1, y + ySize, 0xFF6B3FA0);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF3A1F5E);

        // Title
        String title = "Convertisseur du Dr. Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, x + (xSize - tw) / 2.0F, y + 5, 0xDD88FF);

        // === ENERGY BAR ===
        int barX = x + 150;
        int barY = y + 18;
        int barW = 16;
        int barH = 66;
        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF6B3FA0);
        drawRect(barX, barY, barX + barW, barY + barH, 0xFF0A0410);

        int stored = tile.getEnergyStored();
        int max = tile.getMaxEnergyStored();
        if (max > 0 && stored > 0) {
            float ratio = (float) stored / max;
            int fillH = (int)(barH * ratio);
            for (int fy = 0; fy < fillH; fy++) {
                float t = (float) fy / barH;
                int r = (int)(180 + 75 * t);
                int g = (int)(20 * t);
                int c = 0xFF000000 | (Math.min(r, 255) << 16) | (g << 8) | 20;
                drawRect(barX + 1, barY + barH - 1 - fy,
                         barX + barW - 1, barY + barH - fy, c);
            }
        }
        fontRenderer.drawStringWithShadow("RF", barX + 3, barY + barH + 3, 0xAA6666);

        if (!configOpen) {
            // === NORMAL VIEW ===
            drawRect(x + 4, y + 16, x + 142, y + 88, 0xFF1E1030);
            drawRect(x + 5, y + 17, x + 141, y + 87, 0xFF261440);

            int rfTick = tile.getCurrentRFPerTick();
            fontRenderer.drawStringWithShadow(rfTick + " RF/t", x + 10, y + 20, 0xFFAA00);
            fontRenderer.drawStringWithShadow(
                formatNumber(stored) + " / " + formatNumber(max) + " RF",
                x + 10, y + 32, 0x9977BB);

            int[] faceData = tile.getFaceData();
            int count = tile.getComposeCount();
            fontRenderer.drawStringWithShadow("Composes: " + count + "/6", x + 10, y + 48, 0x8866AA);

            int sqSize = 14;
            int sqGap = 3;
            int sqX = x + 10;
            int sqY = y + 60;
            for (int i = 0; i < 6; i++) {
                int sx = sqX + i * (sqSize + sqGap);
                int tier = faceData[i];
                int color = (tier >= 0 && tier <= 5) ? TIER_COLORS[tier] : TIER_COLORS[0];
                drawRect(sx - 1, sqY - 1, sx + sqSize + 1, sqY + sqSize + 1, 0xFF6B3FA0);
                drawRect(sx, sqY, sx + sqSize, sqY + sqSize, color);
                String letter = tier > 0 ? TIER_NAMES[tier] : FACE_LABELS[i];
                int lColor = tier > 0 ? 0xFFFFFF : 0x554466;
                int lw = fontRenderer.getStringWidth(letter);
                fontRenderer.drawStringWithShadow(letter,
                    sx + (sqSize - lw) / 2.0F, sqY + 3, lColor);
            }

            // Config button (bottom of info panel)
            drawConfigBtn(x + 110, y + 74, false);

        } else {
            // === CONFIG VIEW (replaces info panel) ===
            drawRect(x + 4, y + 16, x + 142, y + 88, 0xFF0E1828);
            drawRect(x + 5, y + 17, x + 141, y + 87, 0xFF162236);

            fontRenderer.drawStringWithShadow("Sortie Energie", x + 10, y + 19, 0xFF88CCEE);

            // Cross layout inside panel
            //        [H]
            //  [O]   [N]   [E]
            //        [S]
            //        [B]
            int cx = x + 50;
            int cy = y + 32;

            drawOutputBtn(cx + BTN + GAP, cy, 1);                     // H (up)
            drawOutputBtn(cx, cy + BTN + GAP, 4);                      // O (west)
            drawOutputBtn(cx + BTN + GAP, cy + BTN + GAP, 2);          // N (north/front)
            drawOutputBtn(cx + (BTN + GAP) * 2, cy + BTN + GAP, 5);   // E (east)
            drawOutputBtn(cx + BTN + GAP, cy + (BTN + GAP) * 2, 3);   // S (south)

            // B (down) - below S
            drawOutputBtn(cx, cy + (BTN + GAP) * 2, 0);               // B (down)

            // Back button
            drawConfigBtn(x + 110, y + 74, true);
        }

        // Inventory label + slot borders
        fontRenderer.drawStringWithShadow("Inventaire", x + 8, y + 84, 0x8866AA);
        for (Slot slot : this.inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A1F5E);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }
    }

    private void drawOutputBtn(int bx, int by, int face) {
        boolean on = tile.isOutputFace(face);
        drawRect(bx - 1, by - 1, bx + BTN + 1, by + BTN + 1, 0xFF4488AA);
        drawRect(bx, by, bx + BTN, by + BTN, on ? 0xFFE67300 : 0xFF1A2030);

        String lbl = FACE_LABELS[face];
        int lw = fontRenderer.getStringWidth(lbl);
        int lColor = on ? 0xFFFFFF : 0x556677;
        fontRenderer.drawStringWithShadow(lbl,
            bx + (BTN - lw) / 2.0F, by + 6, lColor);
    }

    private void drawConfigBtn(int bx, int by, boolean isBack) {
        drawRect(bx - 1, by - 1, bx + 24 + 1, by + 10 + 1, 0xFF6B3FA0);
        drawRect(bx, by, bx + 24, by + 10, isBack ? 0xFF3A1F5E : 0xFF261440);
        String lbl = isBack ? "< OK" : "CFG";
        int lw = fontRenderer.getStringWidth(lbl);
        fontRenderer.drawStringWithShadow(lbl, bx + (24 - lw) / 2.0F, by + 1, 0xCCCCCC);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int x = this.guiLeft;
        int y = this.guiTop;

        // Config/Back button
        int btnX = x + 110;
        int btnY = y + 74;
        if (mouseX >= btnX && mouseX <= btnX + 24 && mouseY >= btnY && mouseY <= btnY + 10) {
            configOpen = !configOpen;
            return;
        }

        // Output button clicks (only in config view)
        if (configOpen) {
            int cx = x + 50;
            int cy = y + 32;

            int[][] buttons = {
                {1, cx + BTN + GAP, cy},
                {4, cx, cy + BTN + GAP},
                {2, cx + BTN + GAP, cy + BTN + GAP},
                {5, cx + (BTN + GAP) * 2, cy + BTN + GAP},
                {3, cx + BTN + GAP, cy + (BTN + GAP) * 2},
                {0, cx, cy + (BTN + GAP) * 2}
            };

            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mouseX >= bx && mouseX <= bx + BTN &&
                    mouseY >= by && mouseY <= by + BTN) {
                    mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, face);
                    break;
                }
            }
        }
    }

    private String formatNumber(int n) {
        if (n >= 1000000) return String.format("%.1fM", n / 1000000.0);
        if (n >= 1000) return String.format("%.1fK", n / 1000.0);
        return String.valueOf(n);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int mx = mouseX - guiLeft;
        int my = mouseY - guiTop;

        // Energy bar tooltip
        if (mx >= 150 && mx <= 166 && my >= 18 && my <= 84) {
            drawHoveringText(java.util.Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF"), mx, my);
        }

        // Output button tooltips
        if (configOpen) {
            int cx = 50;
            int cy = 32;
            int[][] buttons = {
                {1, cx + BTN + GAP, cy},
                {4, cx, cy + BTN + GAP},
                {2, cx + BTN + GAP, cy + BTN + GAP},
                {5, cx + (BTN + GAP) * 2, cy + BTN + GAP},
                {3, cx + BTN + GAP, cy + (BTN + GAP) * 2},
                {0, cx, cy + (BTN + GAP) * 2}
            };
            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mx >= bx && mx <= bx + BTN && my >= by && my <= by + BTN) {
                    String state = tile.isOutputFace(face) ? "ON" : "OFF";
                    drawHoveringText(java.util.Collections.singletonList(
                        FACE_NAMES[face] + ": " + state), mx, my);
                }
            }
        }
    }
}
