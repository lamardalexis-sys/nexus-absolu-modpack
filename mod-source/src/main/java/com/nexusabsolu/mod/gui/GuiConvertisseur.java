package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiConvertisseur extends GuiContainer {

    private final TileConvertisseur tile;

    // Tier colors: 0=empty, 1=A, 2=B, 3=C, 4=D, 5=E
    private static final int[] TIER_COLORS = {
        0xFF1A1A24,  // empty - dark
        0xFFCC4CFF,  // A - purple
        0xFFB299E6,  // B - lavender
        0xFF80CCB2,  // C - turquoise
        0xFF4CE680,  // D - green
        0xFF33FF4C   // E - bright green
    };

    private static final String[] TIER_NAMES = {
        "-", "A", "B", "C", "D", "E"
    };

    private static final String[] FACE_LABELS = {
        "Bas", "Haut", "Nord", "Sud", "Ouest", "Est"
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

        // Dark background
        drawRect(x, y, x + xSize, y + ySize, 0xFF0E0E14);
        // Border
        drawRect(x, y, x + xSize, y + 1, 0xFF2A1A3E);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF2A1A3E);
        drawRect(x, y, x + 1, y + ySize, 0xFF2A1A3E);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF2A1A3E);

        // Title
        String title = "Convertisseur du Dr. Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawString("\u00a7d" + title, x + (xSize - tw) / 2, y + 6, 0xFFFFFF);

        // === FACE DETECTION GRID ===
        // Cross layout:
        //        [UP]
        // [WEST][NORTH][EAST][SOUTH]
        //        [DOWN]
        int boxSize = 22;
        int gridX = x + 20;
        int gridY = y + 22;
        int gap = 2;

        int[] faceData = tile.getFaceData();

        // Row 0: UP (centered above NORTH)
        drawFaceBox(gridX + (boxSize + gap), gridY, boxSize, faceData[1], 1);

        // Row 1: WEST, NORTH, EAST, SOUTH
        drawFaceBox(gridX, gridY + boxSize + gap, boxSize, faceData[4], 4);
        drawFaceBox(gridX + (boxSize + gap), gridY + boxSize + gap, boxSize, faceData[2], 2);
        drawFaceBox(gridX + 2 * (boxSize + gap), gridY + boxSize + gap, boxSize, faceData[5], 5);
        drawFaceBox(gridX + 3 * (boxSize + gap), gridY + boxSize + gap, boxSize, faceData[3], 3);

        // Row 2: DOWN (centered below NORTH)
        drawFaceBox(gridX + (boxSize + gap), gridY + 2 * (boxSize + gap), boxSize, faceData[0], 0);

        // === ENERGY BAR ===
        int barX = x + 145;
        int barY = y + 20;
        int barW = 16;
        int barH = 52;

        // Bar background
        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF2A1A3E);
        drawRect(barX, barY, barX + barW, barY + barH, 0xFF0A0A10);

        // Bar fill (red to green gradient based on fill)
        int stored = tile.getEnergyStored();
        int max = tile.getMaxEnergyStored();
        if (max > 0 && stored > 0) {
            float ratio = (float) stored / max;
            int fillH = (int)(barH * ratio);
            int fillColor = ratio < 0.33F ? 0xFFFF3333 :
                            ratio < 0.66F ? 0xFFFFAA33 : 0xFF33FF33;
            drawRect(barX, barY + barH - fillH, barX + barW, barY + barH, fillColor);
        }

        // RF label
        fontRenderer.drawString("\u00a77RF", barX + 2, barY + barH + 4, 0xFFFFFF);

        // === INFO TEXT ===
        int textX = x + 10;
        int textY = y + 74;

        int rfTick = tile.getCurrentRFPerTick();
        int count = tile.getComposeCount();

        fontRenderer.drawString("\u00a7e" + rfTick + " RF/t", textX, textY, 0xFFFFFF);
        fontRenderer.drawString("\u00a77" + count + "/6 blocs", textX + 60, textY, 0xFFFFFF);

        // Energy numbers
        String energyStr = formatNumber(stored) + "/" + formatNumber(max);
        fontRenderer.drawString("\u00a7a" + energyStr, textX, textY - 10, 0xFFFFFF);

        // Player inventory area separator
        drawRect(x + 7, y + 82, x + xSize - 7, y + 83, 0xFF2A1A3E);
    }

    private void drawFaceBox(int x, int y, int size, int tier, int faceIndex) {
        // Border
        drawRect(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF2A1A3E);
        // Fill with tier color
        int color = tier >= 0 && tier <= 5 ? TIER_COLORS[tier] : TIER_COLORS[0];
        drawRect(x, y, x + size, y + size, color);

        // Tier letter centered
        String letter = tier > 0 ? TIER_NAMES[tier] : "";
        if (!letter.isEmpty()) {
            int lw = fontRenderer.getStringWidth(letter);
            fontRenderer.drawStringWithShadow(letter,
                x + (size - lw) / 2.0F, y + (size - 8) / 2.0F, 0xFFFFFF);
        }

        // Face label below
        String label = FACE_LABELS[faceIndex];
        int lw = fontRenderer.getStringWidth(label);
        // Only draw if it fits
        if (lw <= size + 4) {
            fontRenderer.drawString("\u00a78" + label,
                x + (size - lw) / 2, y + size + 2, 0x666666);
        }
    }

    private String formatNumber(int n) {
        if (n >= 1000000) return String.format("%.1fM", n / 1000000.0);
        if (n >= 1000) return String.format("%.1fK", n / 1000.0);
        return String.valueOf(n);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Nothing extra needed
    }
}
