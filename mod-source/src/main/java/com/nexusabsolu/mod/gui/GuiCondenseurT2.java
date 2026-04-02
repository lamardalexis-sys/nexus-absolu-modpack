package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileItemInput;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class GuiCondenseurT2 extends GuiContainer {

    private final ContainerCondenseurT2 container;
    private final TileCondenseurT2 master;

    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    public GuiCondenseurT2(InventoryPlayer playerInv, TileCondenseurT2 master, TileItemInput inputTile) {
        super(new ContainerCondenseurT2(playerInv, master, inputTile));
        this.container = (ContainerCondenseurT2) inventorySlots;
        this.master = master;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Dark violet background
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int gx = (width - xSize) / 2;
        int gy = (height - ySize) / 2;

        // Draw solid background (no texture, pure code)
        drawRect(gx, gy, gx + GUI_W, gy + GUI_H, 0xFF1A0A2E);
        // Border
        drawRect(gx, gy, gx + GUI_W, gy + 1, 0xFF6B3FA0);
        drawRect(gx, gy + GUI_H - 1, gx + GUI_W, gy + GUI_H, 0xFF6B3FA0);
        drawRect(gx, gy, gx + 1, gy + GUI_H, 0xFF6B3FA0);
        drawRect(gx + GUI_W - 1, gy, gx + GUI_W, gy + GUI_H, 0xFF6B3FA0);

        // Title area
        drawRect(gx + 2, gy + 2, gx + GUI_W - 2, gy + 14, 0xFF2D1450);

        // Slot backgrounds (4 input slots in 2x2 grid)
        int slotBg = 0xFF0D0520;
        int slotBorder = 0xFF4A2D80;
        drawSlotBox(gx + 34, gy + 19, slotBg, slotBorder);
        drawSlotBox(gx + 52, gy + 19, slotBg, slotBorder);
        drawSlotBox(gx + 34, gy + 37, slotBg, slotBorder);
        drawSlotBox(gx + 52, gy + 37, slotBg, slotBorder);

        // Progress arrow area
        drawProgressArrow(gx + 76, gy + 26);

        // Energy bar
        drawEnergyBar(gx + 10, gy + 18, 10, 42);

        // Player inventory background
        drawRect(gx + 6, gy + 80, gx + GUI_W - 6, gy + GUI_H - 4, 0xFF0D0520);
    }

    private void drawSlotBox(int x, int y, int bg, int border) {
        drawRect(x - 1, y - 1, x + 17, y + 17, border);
        drawRect(x, y, x + 16, y + 16, bg);
    }

    private void drawProgressArrow(int x, int y) {
        int gx = (width - xSize) / 2;
        int gy = (height - ySize) / 2;

        // Arrow background
        drawRect(x, y, x + 22, y + 16, 0xFF0D0520);

        // Fill based on progress
        int process = container.getProcessTime();
        int maxProcess = container.getMaxProcessTime();
        if (maxProcess > 0 && process > 0) {
            float pct = (float) process / (float) maxProcess;
            int fillW = (int)(pct * 22);
            drawRect(x, y, x + fillW, y + 16, 0xFF7B3FD0);

            // Bright leading edge
            if (fillW > 0 && fillW < 22) {
                drawRect(x + fillW - 1, y, x + fillW, y + 16, 0xFFAA66FF);
            }
        }

        // Arrow symbol ">>>"
        if (!container.isStructureFormed()) {
            fontRenderer.drawString("???", x + 3, y + 4, 0xFF666666);
        } else if (process > 0) {
            fontRenderer.drawString(">>>", x + 2, y + 4, 0xFF00FF88);
        } else {
            fontRenderer.drawString(">>>", x + 2, y + 4, 0xFF444444);
        }

        // Output indicator (right side of arrow)
        drawRect(x + 26, y - 1, x + 44, y + 17, 0xFF4A2D80);
        drawRect(x + 27, y, x + 43, y + 16, 0xFF0D0520);
        fontRenderer.drawString("OUT", x + 28, y + 4, 0xFF888888);
    }

    private void drawEnergyBar(int x, int y, int w, int h) {
        int gx = (width - xSize) / 2;
        int gy = (height - ySize) / 2;
        int bx = gx + x;
        int by = gy + y;

        // Background
        drawRect(bx - 1, by - 1, bx + w + 1, by + h + 1, 0xFF4A2D80);
        drawRect(bx, by, bx + w, by + h, 0xFF0D0520);

        // Fill
        int energy = container.getEnergy();
        int maxEnergy = container.getMaxEnergy();
        if (maxEnergy > 0 && energy > 0) {
            float pct = (float) energy / (float) maxEnergy;
            int fillH = (int)(pct * h);
            // Gradient from red (bottom) to green (top)
            int fillTop = by + h - fillH;
            for (int row = 0; row < fillH; row++) {
                float rowPct = (float) row / (float) h;
                int r = (int)(255 * (1.0F - rowPct));
                int g = (int)(100 + 155 * rowPct);
                int b = (int)(50 + 100 * rowPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(bx + 1, fillTop + fillH - row - 1, bx + w - 1, fillTop + fillH - row, color);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        String title = "Condenseur Dimensionnel T2";
        fontRenderer.drawString(title, (GUI_W - fontRenderer.getStringWidth(title)) / 2, 4, 0xFFBB88FF);

        // Structure status
        if (!container.isStructureFormed()) {
            fontRenderer.drawString("Structure incomplete", 35, 58, 0xFFFF4444);
        } else if (container.getProcessTime() > 0) {
            // Processing quote
            String quote = master.getCurrentQuote();
            if (quote.length() > 28) quote = quote.substring(0, 28) + "...";
            fontRenderer.drawString(quote, 35, 58, 0xFF88DDAA);

            // Progress percentage
            int pct = 0;
            if (container.getMaxProcessTime() > 0)
                pct = (container.getProcessTime() * 100) / container.getMaxProcessTime();
            fontRenderer.drawString(pct + "%", 80, 45, 0xFFAAFFAA);
        } else {
            fontRenderer.drawString("En attente...", 35, 58, 0xFF888888);
        }

        // Energy text
        String energyStr = formatEnergy(container.getEnergy());
        fontRenderer.drawString(energyStr, 8, 62, 0xFFAAAAAA);

        // Slot labels
        fontRenderer.drawString("CM", 36, 13, 0xFF666666);
        fontRenderer.drawString("K+C", 52, 13, 0xFF666666);

        // RF/t indicator
        if (container.getProcessTime() > 0) {
            fontRenderer.drawString("200 RF/t", 105, 45, 0xFFFF8844);
        }
    }

    private String formatEnergy(int energy) {
        if (energy >= 1000000) return (energy / 1000000) + "M";
        if (energy >= 1000) return (energy / 1000) + "K";
        return String.valueOf(energy);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}
