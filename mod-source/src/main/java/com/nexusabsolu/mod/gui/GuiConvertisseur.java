package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiConvertisseur extends GuiContainer {

    private final TileConvertisseur tile;

    private static final int[] TIER_COLORS = {
        0xFF4A4A4A,  // empty
        0xFFCC4CFF,  // A purple
        0xFFB299E6,  // B lavender
        0xFF80CCB2,  // C turquoise
        0xFF4CE680,  // D green
        0xFF33FF4C   // E bright green
    };

    private static final String[] TIER_NAMES = {
        "", "A", "B", "C", "D", "E"
    };

    public GuiConvertisseur(InventoryPlayer playerInv, TileConvertisseur tile) {
        super(new ContainerConvertisseur(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.guiLeft;
        int y = this.guiTop;

        // Background (Thermal gray)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);
        drawRect(x, y, x + xSize, y + 1, 0xFFFFFFFF);
        drawRect(x, y, x + 1, y + ySize, 0xFFFFFFFF);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF555555);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF555555);

        // Inner panel
        drawRect(x + 7, y + 17, x + xSize - 7, y + 80, 0xFF8B8B8B);
        drawRect(x + 8, y + 18, x + xSize - 8, y + 79, 0xFFADADAD);

        // Title
        String title = "Convertisseur du Dr. Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, x + (xSize - tw) / 2, y + 6, 0x404040);

        // Energy bar (right side, tall, red fill)
        int barX = x + 152;
        int barY = y + 20;
        int barW = 14;
        int barH = 56;

        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF373737);
        drawRect(barX, barY, barX + barW, barY + barH, 0xFF1A0A0A);

        int stored = tile.getEnergyStored();
        int max = tile.getMaxEnergyStored();
        if (max > 0 && stored > 0) {
            float ratio = (float) stored / max;
            int fillH = (int)(barH * ratio);
            drawRect(barX, barY + barH - fillH, barX + barW, barY + barH, 0xFFCC1111);
            if (fillH > 1) {
                drawRect(barX, barY + barH - fillH, barX + barW, barY + barH - fillH + 1, 0xFFFF3333);
            }
        }

        // RF/t
        int rfTick = tile.getCurrentRFPerTick();
        String rfStr = rfTick + " RF/t";
        int rw = fontRenderer.getStringWidth(rfStr);
        fontRenderer.drawString(rfStr, x + 50 - rw / 2, y + 24, 0xFFAA00);

        // Energy stored
        String eStr = formatNumber(stored) + " / " + formatNumber(max) + " RF";
        int ew = fontRenderer.getStringWidth(eStr);
        fontRenderer.drawString(eStr, x + 50 - ew / 2, y + 36, 0x404040);

        // Compose indicators
        int[] faceData = tile.getFaceData();
        int count = tile.getComposeCount();

        fontRenderer.drawString("Blocs: " + count + "/6", x + 12, y + 52, 0x404040);

        int sqSize = 12;
        int sqGap = 3;
        int sqStartX = x + 12;
        int sqY = y + 63;
        String[] labels = {"B", "H", "N", "S", "O", "E"};

        for (int i = 0; i < 6; i++) {
            int sx = sqStartX + i * (sqSize + sqGap);
            int tier = faceData[i];
            int color = tier >= 0 && tier <= 5 ? TIER_COLORS[tier] : TIER_COLORS[0];

            drawRect(sx - 1, sqY - 1, sx + sqSize + 1, sqY + sqSize + 1, 0xFF373737);
            drawRect(sx, sqY, sx + sqSize, sqY + sqSize, color);

            String letter = tier > 0 ? TIER_NAMES[tier] : labels[i];
            int lColor = tier > 0 ? 0xFFFFFF : 0x606060;
            int lw = fontRenderer.getStringWidth(letter);
            fontRenderer.drawStringWithShadow(letter,
                sx + (sqSize - lw) / 2.0F, sqY + 2, lColor);
        }

        // Inventory label
        fontRenderer.drawString("Inventory", x + 8, y + 82, 0x404040);
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
        if (mx >= 152 && mx <= 166 && my >= 20 && my <= 76) {
            String tip = tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF";
            drawHoveringText(java.util.Collections.singletonList(tip), mx, my);
        }
    }
}
