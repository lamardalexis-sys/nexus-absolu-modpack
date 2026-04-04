package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileItemInput;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

public class GuiCondenseurT2 extends GuiContainer {

    private final ContainerCondenseurT2 container;
    private final TileCondenseurT2 master;

    private static final int GUI_W = 176;
    private static final int GUI_H = 176;

    // Colors
    private static final int BG_OUTER    = 0xFF0E0618;
    private static final int BG_INNER    = 0xFF160A22;
    private static final int BORDER_GLOW = 0xFF5A2080;
    private static final int BORDER_DIM  = 0xFF2A1040;
    private static final int PANEL_BG    = 0xFF0A0414;
    private static final int PANEL_EDGE  = 0xFF3A1860;
    private static final int SLOT_BG     = 0xFF080310;
    private static final int SLOT_BORDER = 0xFF4A2880;
    private static final int SLOT_ACTIVE = 0xFF6B3FA0;
    private static final int TEXT_TITLE  = 0xFFBB88FF;
    private static final int TEXT_LABEL  = 0xFF7755AA;
    private static final int TEXT_DIM    = 0xFF554477;
    private static final int TEXT_GREEN  = 0xFF44DD88;
    private static final int TEXT_WARN   = 0xFFFF6644;
    private static final int TEXT_QUOTE  = 0xFF338866;
    private static final int GOLD_ACCENT = 0xFFD4A830;

    public GuiCondenseurT2(InventoryPlayer playerInv, TileCondenseurT2 master, TileItemInput inputTile, TileItemOutput outputTile) {
        super(new ContainerCondenseurT2(playerInv, master, inputTile, outputTile));
        this.container = (ContainerCondenseurT2) inventorySlots;
        this.master = master;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int gx = (width - xSize) / 2;
        int gy = (height - ySize) / 2;

        long time = master.getWorld() != null ? master.getWorld().getTotalWorldTime() : 0;
        boolean proc = container.getProcessTime() > 0;

        // === OUTER FRAME ===
        drawRect(gx, gy, gx + GUI_W, gy + GUI_H, BG_OUTER);
        // Double border
        hLine(gx + 1, gx + GUI_W - 2, gy, BORDER_GLOW);
        hLine(gx + 1, gx + GUI_W - 2, gy + GUI_H - 1, BORDER_GLOW);
        vLine(gx, gy + 1, gy + GUI_H - 2, BORDER_GLOW);
        vLine(gx + GUI_W - 1, gy + 1, gy + GUI_H - 2, BORDER_GLOW);
        hLine(gx + 2, gx + GUI_W - 3, gy + 1, BORDER_DIM);
        hLine(gx + 2, gx + GUI_W - 3, gy + GUI_H - 2, BORDER_DIM);

        // === TITLE BAR ===
        drawRect(gx + 3, gy + 3, gx + GUI_W - 3, gy + 15, 0xFF1E0C35);
        hLine(gx + 3, gx + GUI_W - 4, gy + 15, BORDER_GLOW);
        // Gold corner dots
        drawRect(gx + 4, gy + 4, gx + 6, gy + 6, GOLD_ACCENT);
        drawRect(gx + GUI_W - 7, gy + 4, gx + GUI_W - 5, gy + 6, GOLD_ACCENT);

        // === INPUT PANEL (left) ===
        int panelX = gx + 6;
        int panelY = gy + 18;
        int panelW = 62;
        int panelH = 52;
        drawPanel(panelX, panelY, panelW, panelH);

        // Input slots with glowing borders
        int slotColor = proc ? SLOT_ACTIVE : SLOT_BORDER;
        drawSlotBox(gx + 15, gy + 28, slotColor);
        drawSlotBox(gx + 37, gy + 28, slotColor);
        drawSlotBox(gx + 15, gy + 48, slotColor);
        drawSlotBox(gx + 37, gy + 48, slotColor);

        // === PROGRESS PANEL (center) ===
        int progX = gx + 72;
        int progY = gy + 18;
        drawPanel(progX, progY, 32, 52);

        // Progress bar (vertical, bottom to top)
        int barX = gx + 79;
        int barY = gy + 24;
        int barW = 18;
        int barH = 40;
        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, PANEL_EDGE);
        drawRect(barX, barY, barX + barW, barY + barH, SLOT_BG);

        if (container.getMaxProcessTime() > 0 && container.getProcessTime() > 0) {
            float pct = (float) container.getProcessTime() / container.getMaxProcessTime();
            int fillH = (int)(pct * barH);
            // Purple gradient fill from bottom
            for (int row = 0; row < fillH; row++) {
                float rowPct = (float) row / barH;
                int r = (int)(90 + 100 * rowPct);
                int g = (int)(30 + 40 * rowPct);
                int b = (int)(140 + 80 * rowPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(barX + 1, barY + barH - row - 1, barX + barW - 1, barY + barH - row, color);
            }
            // Bright leading edge
            if (fillH > 0 && fillH < barH) {
                drawRect(barX + 1, barY + barH - fillH, barX + barW - 1, barY + barH - fillH + 1, 0xFFCC88FF);
            }
            // Animated scanline over progress
            int scanY = barY + barH - 1 - (int)((time % 40) * barH / 40);
            if (scanY >= barY && scanY < barY + barH) {
                drawRect(barX + 1, scanY, barX + barW - 1, scanY + 1, 0x40FFFFFF);
            }
        }

        // === OUTPUT PANEL (right top) ===
        int outX = gx + 108;
        int outY = gy + 18;
        drawPanel(outX, outY, 62, 52);

        // Output result slot (slot index 4, at 131,40 in container)
        int outSlotColor = proc ? SLOT_ACTIVE : SLOT_BORDER;
        drawSlotBox(gx + 130, gy + 39, outSlotColor);

        // === ENERGY BAR (bottom left) ===
        int eBarX = gx + 8;
        int eBarY = gy + 73;
        int eBarW = 60;
        int eBarH = 8;
        drawRect(eBarX - 1, eBarY - 1, eBarX + eBarW + 1, eBarY + eBarH + 1, PANEL_EDGE);
        drawRect(eBarX, eBarY, eBarX + eBarW, eBarY + eBarH, SLOT_BG);

        int maxE = container.getMaxEnergy();
        int curE = container.getEnergy();
        if (maxE > 0 && curE > 0) {
            float ePct = (float) curE / maxE;
            int fillW = (int)(ePct * eBarW);
            // Purple energy gradient
            for (int col = 0; col < fillW; col++) {
                float colPct = (float) col / eBarW;
                int r = (int)(60 + 140 * colPct);
                int g = (int)(20 + 30 * colPct);
                int b = (int)(100 + 100 * colPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(eBarX + col, eBarY + 1, eBarX + col + 1, eBarY + eBarH - 1, color);
            }
        }

        // === STATUS PANEL (bottom center-right) ===
        int statusX = gx + 72;
        int statusY = gy + 73;
        drawPanel(statusX, statusY, 98, 12);

        // === SCANLINE EFFECT over entire GUI ===
        if (proc) {
            int scanLine = gy + (int)((time % 80) * GUI_H / 80);
            if (scanLine >= gy && scanLine < gy + GUI_H) {
                drawRect(gx + 1, scanLine, gx + GUI_W - 1, scanLine + 1, 0x08FFFFFF);
            }
        }

        // === PLAYER INVENTORY ===
        drawRect(gx + 6, gy + 90, gx + GUI_W - 6, gy + GUI_H - 4, PANEL_BG);
        hLine(gx + 6, gx + GUI_W - 7, gy + 89, BORDER_DIM);
    }

    private void drawPanel(int x, int y, int w, int h) {
        drawRect(x, y, x + w, y + h, PANEL_BG);
        // Border with glow
        hLine(x, x + w - 1, y, PANEL_EDGE);
        hLine(x, x + w - 1, y + h - 1, PANEL_EDGE);
        vLine(x, y, y + h - 1, PANEL_EDGE);
        vLine(x + w - 1, y, y + h - 1, PANEL_EDGE);
    }

    private void drawSlotBox(int x, int y, int border) {
        drawRect(x - 1, y - 1, x + 17, y + 17, border);
        drawRect(x, y, x + 16, y + 16, SLOT_BG);
        // Corner highlights
        drawRect(x, y, x + 1, y + 1, 0xFF3A1860);
        drawRect(x + 15, y + 15, x + 16, y + 16, 0xFF3A1860);
    }

    private void hLine(int x1, int x2, int y, int color) {
        drawRect(x1, y, x2 + 1, y + 1, color);
    }

    private void vLine(int x, int y1, int y2, int color) {
        drawRect(x, y1, x + 1, y2 + 1, color);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        long time = master.getWorld() != null ? master.getWorld().getTotalWorldTime() : 0;
        boolean proc = container.getProcessTime() > 0;
        boolean formed = container.isStructureFormed();

        // Title
        String title = "CONDENSEUR T2";
        fontRenderer.drawString(title, (GUI_W - fontRenderer.getStringWidth(title)) / 2, 5, TEXT_TITLE);

        // Input labels
        fontRenderer.drawString("CM", 18, 21, TEXT_LABEL);
        fontRenderer.drawString("CM", 40, 21, TEXT_LABEL);
        fontRenderer.drawString("Cle", 17, 41, TEXT_DIM);
        fontRenderer.drawString("Cat", 39, 41, TEXT_DIM);

        // Progress percentage
        if (proc && container.getMaxProcessTime() > 0) {
            int pct = (container.getProcessTime() * 100) / container.getMaxProcessTime();
            String pctStr = pct + "%";
            fontRenderer.drawString(pctStr, 88 - fontRenderer.getStringWidth(pctStr) / 2, 66, TEXT_GREEN);
        } else if (formed) {
            fontRenderer.drawString("---", 83, 66, TEXT_DIM);
        }

        // Output label
        fontRenderer.drawString("Sortie", 126, 22, TEXT_LABEL);

        // Structure status
        if (!formed) {
            fontRenderer.drawString("ERREUR STRUCTURE", 110, 40, TEXT_WARN);
            // Blink
            if (time % 40 < 20) {
                fontRenderer.drawString("!", 155, 40, 0xFFFF0000);
            }
        } else if (proc) {
            // Scrolling quote
            String quote = master.getCurrentQuote();
            if (quote.length() > 24) quote = quote.substring(0, 24) + "..";
            fontRenderer.drawString(quote, 110, 40, TEXT_QUOTE);
        } else {
            fontRenderer.drawString("En attente", 116, 40, TEXT_DIM);
        }

        // Energy display
        String eStr = formatEnergy(container.getEnergy()) + " / " + formatEnergy(container.getMaxEnergy()) + " RF";
        fontRenderer.drawString(eStr, 10, 74, 0xFF9966CC);

        // Status bar (bottom right)
        if (proc) {
            fontRenderer.drawString("200 RF/t", 76, 76, 0xFFFF8844);
            // Animated dots
            int dots = (int)(time % 60 / 20) + 1;
            String dotStr = "";
            for (int i = 0; i < dots; i++) dotStr += ".";
            fontRenderer.drawString("ACTIF" + dotStr, 130, 76, TEXT_GREEN);
        } else if (formed) {
            fontRenderer.drawString("PRET", 140, 76, TEXT_LABEL);
        } else {
            fontRenderer.drawString("HORS LIGNE", 114, 76, TEXT_WARN);
        }
    }

    private String formatEnergy(int energy) {
        if (energy >= 1000000) return String.format("%.1fM", energy / 1000000.0);
        if (energy >= 1000) return String.format("%.1fK", energy / 1000.0);
        return String.valueOf(energy);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}
