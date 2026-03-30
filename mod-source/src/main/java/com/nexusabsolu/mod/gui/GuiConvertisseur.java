package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class GuiConvertisseur extends GuiContainer {

    private final TileConvertisseur tile;

    private static final int[] TIER_COLORS = {
        0xFF2A1A3E,  // empty - dark purple
        0xFFCC4CFF,  // A purple
        0xFFB299E6,  // B lavender
        0xFF80CCB2,  // C turquoise
        0xFF4CE680,  // D green
        0xFF33FF4C   // E bright green
    };

    private static final String[] TIER_NAMES = {"", "A", "B", "C", "D", "E"};
    private static final String[] FACE_LABELS = {"B", "H", "N", "S", "O", "E"};

    // Output button positions (relative to guiLeft/guiTop)
    private static final int BTN_Y = 72;
    private static final int BTN_SIZE = 14;
    private static final int BTN_GAP = 4;
    private static final int BTN_START_X = 10;

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

        // === DARK PURPLE BACKGROUND ===
        drawRect(x, y, x + xSize, y + ySize, 0xFF12081C);
        // Bright purple border
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

        // === ENERGY BAR (right side) ===
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
            // Red gradient fill
            for (int fy = 0; fy < fillH; fy++) {
                float t = (float) fy / barH;
                int r = (int)(180 + 75 * t);
                int g = (int)(20 * t);
                int b = 20;
                int c = 0xFF000000 | (Math.min(r, 255) << 16) | (g << 8) | b;
                drawRect(barX + 1, barY + barH - 1 - fy,
                         barX + barW - 1, barY + barH - fy, c);
            }
        }

        // RF label under bar
        String rfLabel = "RF";
        int rlw = fontRenderer.getStringWidth(rfLabel);
        fontRenderer.drawStringWithShadow(rfLabel, barX + (barW - rlw) / 2.0F, barY + barH + 3, 0xAA6666);

        // === RF/T + ENERGY INFO ===
        int rfTick = tile.getCurrentRFPerTick();
        String rfStr = rfTick + " RF/t";
        fontRenderer.drawStringWithShadow(rfStr, x + 10, y + 20, 0xFFAA00);

        String eStr = formatNumber(stored) + " / " + formatNumber(max) + " RF";
        fontRenderer.drawStringWithShadow(eStr, x + 10, y + 32, 0x9977BB);

        // === COMPOSE FACE INDICATORS ===
        int[] faceData = tile.getFaceData();
        int count = tile.getComposeCount();
        fontRenderer.drawStringWithShadow("Composes: " + count + "/6", x + 10, y + 46, 0x8866AA);

        int sqSize = 14;
        int sqGap = 3;
        int sqX = x + 10;
        int sqY = y + 56;

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

        // === OUTPUT DIRECTION BUTTONS ===
        fontRenderer.drawStringWithShadow("Sortie:", x + 10, y + BTN_Y, 0x8866AA);

        for (int i = 0; i < 6; i++) {
            int bx = x + BTN_START_X + 44 + i * (BTN_SIZE + BTN_GAP);
            int by = y + BTN_Y - 1;
            boolean on = tile.isOutputFace(i);

            // Button border
            drawRect(bx - 1, by - 1, bx + BTN_SIZE + 1, by + BTN_SIZE + 1, 0xFF6B3FA0);
            // Button fill: orange if ON, dark if OFF
            int btnColor = on ? 0xFFE67300 : 0xFF1A1024;
            drawRect(bx, by, bx + BTN_SIZE, by + BTN_SIZE, btnColor);

            // Label
            String lbl = FACE_LABELS[i];
            int lbw = fontRenderer.getStringWidth(lbl);
            int lbColor = on ? 0xFFFFFF : 0x554466;
            fontRenderer.drawStringWithShadow(lbl,
                bx + (BTN_SIZE - lbw) / 2.0F, by + 3, lbColor);
        }

        // === INVENTORY SLOTS (draw slot borders) ===
        for (Slot slot : this.inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            // Slot background
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A1F5E);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }

        // Inventory label
        fontRenderer.drawStringWithShadow("Inventaire", x + 8, y + 84, 0x8866AA);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // Check output button clicks
        for (int i = 0; i < 6; i++) {
            int bx = guiLeft + BTN_START_X + 44 + i * (BTN_SIZE + BTN_GAP);
            int by = guiTop + BTN_Y - 1;
            if (mouseX >= bx && mouseX <= bx + BTN_SIZE &&
                mouseY >= by && mouseY <= by + BTN_SIZE) {
                mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, i);
                break;
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
        // Tooltip on energy bar
        int mx = mouseX - guiLeft;
        int my = mouseY - guiTop;
        if (mx >= 150 && mx <= 166 && my >= 18 && my <= 84) {
            String tip = tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF";
            drawHoveringText(java.util.Collections.singletonList(tip), mx, my);
        }

        // Tooltip on output buttons
        for (int i = 0; i < 6; i++) {
            int bx = BTN_START_X + 44 + i * (BTN_SIZE + BTN_GAP);
            int by = BTN_Y - 1;
            if (mx >= bx && mx <= bx + BTN_SIZE && my >= by && my <= by + BTN_SIZE) {
                String[] names = {"Bas", "Haut", "Nord", "Sud", "Ouest", "Est"};
                String state = tile.isOutputFace(i) ? "ON" : "OFF";
                drawHoveringText(java.util.Collections.singletonList(
                    names[i] + ": " + state), mx, my);
            }
        }
    }
}
