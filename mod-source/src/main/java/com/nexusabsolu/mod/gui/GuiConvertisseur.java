package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiConvertisseur extends GuiContainer {

    private final TileConvertisseur tile;
    private boolean configOpen = false;

    private static final int[] TIER_COLORS = {
        0xFF2A1A3E, 0xFFCC4CFF, 0xFFB299E6, 0xFF80CCB2, 0xFF4CE680, 0xFF33FF4C
    };
    private static final String[] TIER_NAMES = {"", "A", "B", "C", "D", "E"};
    private static final String[] FACE_LABELS = {"B", "H", "N", "S", "O", "E"};

    // Config panel dimensions
    private static final int PANEL_W = 80;
    private static final int TAB_W = 12;
    private static final int TAB_H = 20;
    private static final int FACE_BTN = 18;

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

        // === MAIN GUI BACKGROUND (violet) ===
        drawRect(x, y, x + xSize, y + ySize, 0xFF12081C);
        // Borders
        drawRect(x, y, x + xSize, y + 1, 0xFF6B3FA0);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF3A1F5E);
        drawRect(x, y, x + 1, y + ySize, 0xFF6B3FA0);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF3A1F5E);

        // Inner panel
        drawRect(x + 4, y + 16, x + 142, y + 88, 0xFF1E1030);
        drawRect(x + 5, y + 17, x + 141, y + 87, 0xFF261440);

        // === TITLE ===
        String title = "Convertisseur du Dr. Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, x + (xSize - tw) / 2.0F, y + 5, 0xDD88FF);

        // === ENERGY BAR (right side of main GUI) ===
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

        // === INFO TEXT ===
        int rfTick = tile.getCurrentRFPerTick();
        fontRenderer.drawStringWithShadow(rfTick + " RF/t", x + 10, y + 20, 0xFFAA00);
        fontRenderer.drawStringWithShadow(formatNumber(stored) + " / " + formatNumber(max) + " RF",
            x + 10, y + 32, 0x9977BB);

        // Compose indicators
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
            fontRenderer.drawStringWithShadow(letter, sx + (sqSize - lw) / 2.0F, sqY + 3, lColor);
        }

        // Inventory label + slot borders
        fontRenderer.drawStringWithShadow("Inventaire", x + 8, y + 84, 0x8866AA);
        for (Slot slot : this.inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A1F5E);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }

        // === TAB BUTTON (right edge) ===
        int tabX = x + xSize - 1;
        int tabY = y + 20;
        drawRect(tabX, tabY, tabX + TAB_W, tabY + TAB_H, configOpen ? 0xFF1A3050 : 0xFF3A1F5E);
        drawRect(tabX, tabY, tabX + TAB_W, tabY + 1, 0xFF6B3FA0);
        drawRect(tabX, tabY + TAB_H - 1, tabX + TAB_W, tabY + TAB_H, 0xFF3A1F5E);
        drawRect(tabX + TAB_W - 1, tabY, tabX + TAB_W, tabY + TAB_H, 0xFF6B3FA0);
        // Gear icon (simple)
        fontRenderer.drawStringWithShadow("*", tabX + 2, tabY + 6, 0xCCCCCC);

        // === CONFIG PANEL (if open) ===
        if (configOpen) {
            int px = x + xSize + TAB_W - 1;
            int py = y + 10;
            int ph = 110;

            // Panel background
            drawRect(px, py, px + PANEL_W, py + ph, 0xFF0E1828);
            drawRect(px, py, px + PANEL_W, py + 1, 0xFF4488AA);
            drawRect(px, py + ph - 1, px + PANEL_W, py + ph, 0xFF1A3050);
            drawRect(px + PANEL_W - 1, py, px + PANEL_W, py + ph, 0xFF4488AA);

            // Title
            fontRenderer.drawStringWithShadow("Config", px + 6, py + 4, 0xFF88CCEE);

            // 6 face buttons in cross layout:
            //        [UP]
            //  [WEST][FRONT][EAST]
            //        [DOWN]
            // + SOUTH below right
            int cx = px + 22;
            int cy = py + 22;
            int gap = FACE_BTN + 2;

            // UP (1)
            drawOutputBtn(cx + gap, cy, 1);
            // WEST (4), NORTH (2), EAST (5)
            drawOutputBtn(cx, cy + gap, 4);
            drawOutputBtn(cx + gap, cy + gap, 2);
            drawOutputBtn(cx + gap * 2, cy + gap, 5);
            // DOWN (0)
            drawOutputBtn(cx + gap, cy + gap * 2, 0);
            // SOUTH (3) - to the right of EAST
            drawOutputBtn(cx + gap * 2, cy + gap * 2, 3);

            // Legend
            fontRenderer.drawStringWithShadow("\u00a78Orange=ON", px + 4, py + ph - 14, 0x888888);
        }
    }

    private void drawOutputBtn(int bx, int by, int face) {
        boolean on = tile.isOutputFace(face);
        drawRect(bx - 1, by - 1, bx + FACE_BTN + 1, by + FACE_BTN + 1, 0xFF4488AA);
        drawRect(bx, by, bx + FACE_BTN, by + FACE_BTN, on ? 0xFFE67300 : 0xFF1A2030);

        String lbl = FACE_LABELS[face];
        int lw = fontRenderer.getStringWidth(lbl);
        int lColor = on ? 0xFFFFFF : 0x556677;
        fontRenderer.drawStringWithShadow(lbl, bx + (FACE_BTN - lw) / 2.0F, by + 5, lColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int x = this.guiLeft;
        int y = this.guiTop;

        // Tab button click
        int tabX = x + xSize - 1;
        int tabY = y + 20;
        if (mouseX >= tabX && mouseX <= tabX + TAB_W &&
            mouseY >= tabY && mouseY <= tabY + TAB_H) {
            configOpen = !configOpen;
            return;
        }

        // Config panel button clicks
        if (configOpen) {
            int px = x + xSize + TAB_W - 1;
            int py = y + 10;
            int cx = px + 22;
            int cy = py + 22;
            int gap = FACE_BTN + 2;

            // Check each button: face index, bx, by
            int[][] buttons = {
                {1, cx + gap, cy},               // UP
                {4, cx, cy + gap},                // WEST
                {2, cx + gap, cy + gap},          // NORTH
                {5, cx + gap * 2, cy + gap},      // EAST
                {0, cx + gap, cy + gap * 2},      // DOWN
                {3, cx + gap * 2, cy + gap * 2}   // SOUTH
            };

            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mouseX >= bx && mouseX <= bx + FACE_BTN &&
                    mouseY >= by && mouseY <= by + FACE_BTN) {
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
            String tip = tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF";
            drawHoveringText(java.util.Collections.singletonList(tip), mx, my);
        }

        // Config panel tooltips
        if (configOpen) {
            int px = xSize + TAB_W - 1;
            int py = 10;
            int cx = px + 22;
            int cy = py + 22;
            int gap = FACE_BTN + 2;
            String[] names = {"Bas", "Haut", "Nord", "Sud", "Ouest", "Est"};

            int[][] buttons = {
                {1, cx + gap, cy},
                {4, cx, cy + gap},
                {2, cx + gap, cy + gap},
                {5, cx + gap * 2, cy + gap},
                {0, cx + gap, cy + gap * 2},
                {3, cx + gap * 2, cy + gap * 2}
            };

            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mx >= bx && mx <= bx + FACE_BTN && my >= by && my <= by + FACE_BTN) {
                    String state = tile.isOutputFace(face) ? "ON" : "OFF";
                    drawHoveringText(java.util.Collections.singletonList(
                        names[face] + ": " + state), mx, my);
                }
            }
        }
    }
}
