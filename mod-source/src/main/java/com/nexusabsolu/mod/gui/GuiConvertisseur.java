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
    private static final int BGAP = 2;
    private static final int PANEL_W = 90;
    private static final int PANEL_H = 100;

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

        // Inner panel
        drawRect(x + 4, y + 16, x + 142, y + 88, 0xFF1E1030);
        drawRect(x + 5, y + 17, x + 141, y + 87, 0xFF261440);

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

        // === INFO ===
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
            fontRenderer.drawStringWithShadow(letter, sx + (sqSize - lw) / 2.0F, sqY + 3, lColor);
        }

        // CFG tab (right edge of GUI, like Thermal tabs)
        int cbx = x + xSize - 2;
        int cby = y + 18;
        drawRect(cbx, cby, cbx + 14, cby + 16, configOpen ? 0xFF4A2A70 : 0xFF261440);
        drawRect(cbx, cby, cbx + 14, cby + 1, 0xFF6B3FA0);
        drawRect(cbx, cby + 15, cbx + 14, cby + 16, 0xFF3A1F5E);
        drawRect(cbx + 13, cby, cbx + 14, cby + 16, 0xFF6B3FA0);
        fontRenderer.drawStringWithShadow("*", cbx + 4, cby + 4, 0xCCCCCC);

        // Inventory
        fontRenderer.drawStringWithShadow("Inventaire", x + 8, y + 84, 0x8866AA);
        for (Slot slot : this.inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A1F5E);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Empty - everything in drawScreen
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

        // === CONFIG PANEL (drawn AFTER JEI) ===
        if (configOpen) {
            int px = guiLeft + xSize + 4;
            int py = guiTop + 10;
            int pw = 100;
            int ph = 108;

            // Panel 3D border (Thermal style: light top-left, dark bottom-right)
            drawRect(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF3A1F5E);
            drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF8855BB);
            drawRect(px, py, px + pw, py + ph, 0xFF1A1030);
            // Inner lighter border
            drawRect(px + 1, py + 1, px + pw - 1, py + 14, 0xFF261440);

            // Title bar
            fontRenderer.drawStringWithShadow("\u00a7d\u2699 Configuration", px + 5, py + 3, 0xDD88FF);

            // Separator line
            drawRect(px + 3, py + 14, px + pw - 3, py + 15, 0xFF6B3FA0);

            // Cross layout - 24x24 buttons
            int bs = 24;
            int bg = 2;
            int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
            int cy = py + 20;

            //        [H]
            //  [O]   [N]   [E]
            //  [B]   [S]
            drawOutputBtn3D(cx + bs + bg, cy, bs, 1, mouseX, mouseY);
            drawOutputBtn3D(cx, cy + bs + bg, bs, 4, mouseX, mouseY);
            drawOutputBtn3D(cx + bs + bg, cy + bs + bg, bs, 2, mouseX, mouseY);
            drawOutputBtn3D(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, mouseX, mouseY);
            drawOutputBtn3D(cx, cy + (bs + bg) * 2, bs, 0, mouseX, mouseY);
            drawOutputBtn3D(cx + bs + bg, cy + (bs + bg) * 2, bs, 3, mouseX, mouseY);
        }

        // === TOOLTIPS (drawn last, over everything) ===
        if (mouseX >= guiLeft + 150 && mouseX <= guiLeft + 166 &&
            mouseY >= guiTop + 18 && mouseY <= guiTop + 84) {
            drawHoveringText(java.util.Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF"),
                mouseX, mouseY);
        }

        if (configOpen) {
            int px = guiLeft + xSize + 4;
            int py = guiTop + 10;
            int pw = 100;
            int bs = 24;
            int bg = 2;
            int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
            int cy = py + 20;
            int[][] buttons = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {2, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {0, cx, cy + (bs + bg) * 2},
                {3, cx + bs + bg, cy + (bs + bg) * 2}
            };
            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mouseX >= bx && mouseX <= bx + bs &&
                    mouseY >= by && mouseY <= by + bs) {
                    String state = tile.isOutputFace(face) ? "\u00a7aON" : "\u00a7cOFF";
                    drawHoveringText(java.util.Collections.singletonList(
                        FACE_NAMES[face] + ": " + state), mouseX, mouseY);
                }
            }
        }
    }

    private void drawOutputBtn3D(int bx, int by, int sz, int face, int mx, int my) {
        boolean on = tile.isOutputFace(face);
        boolean hovered = mx >= bx && mx <= bx + sz && my >= by && my <= by + sz;

        // 3D outer border (light top-left, dark bottom-right)
        drawRect(bx - 1, by - 1, bx + sz + 1, by, hovered ? 0xFFAA77DD : 0xFF8855BB);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, hovered ? 0xFFAA77DD : 0xFF8855BB);
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);

        if (on) {
            // Active: orange center with inner glow
            drawRect(bx, by, bx + sz, by + sz, 0xFF994D00);
            drawRect(bx + 1, by + 1, bx + sz - 1, by + sz - 1, 0xFFE67300);
            drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, 0xFFFF8811);
            // Inner bright spot
            drawRect(bx + 3, by + 3, bx + sz - 3, by + sz - 3, 0xFFE67300);
        } else {
            // Inactive: dark recessed
            drawRect(bx, by, bx + sz, by + sz, 0xFF0E0818);
            drawRect(bx + 1, by + 1, bx + sz - 1, by + sz - 1, 0xFF1A1030);
            drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, 0xFF221540);
        }

        // Face label centered
        String lbl = FACE_LABELS[face];
        int lw = fontRenderer.getStringWidth(lbl);
        int lColor = on ? 0xFFFFFF : 0xFF554466;
        fontRenderer.drawStringWithShadow(lbl,
            bx + (sz - lw) / 2.0F, by + (sz - 8) / 2.0F, lColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        int x = this.guiLeft;
        int y = this.guiTop;

        // CFG tab (right edge)
        if (mouseX >= x + xSize - 2 && mouseX <= x + xSize + 12 && mouseY >= y + 18 && mouseY <= y + 34) {
            configOpen = !configOpen;
            return;
        }

        // Config panel button clicks
        if (configOpen) {
            int px = x + xSize + 4;
            int py = y + 10;
            int pw = 100;
            int bs = 24;
            int bg = 2;
            int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
            int cy = py + 20;
            int[][] buttons = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {2, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {0, cx, cy + (bs + bg) * 2},
                {3, cx + bs + bg, cy + (bs + bg) * 2}
            };
            for (int[] btn : buttons) {
                int face = btn[0];
                int bx = btn[1];
                int by = btn[2];
                if (mouseX >= bx && mouseX <= bx + bs &&
                    mouseY >= by && mouseY <= by + bs) {
                    mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, face);
                    return;
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private String formatNumber(int n) {
        if (n >= 1000000) return String.format("%.1fM", n / 1000000.0);
        if (n >= 1000) return String.format("%.1fK", n / 1000.0);
        return String.valueOf(n);
    }
}
