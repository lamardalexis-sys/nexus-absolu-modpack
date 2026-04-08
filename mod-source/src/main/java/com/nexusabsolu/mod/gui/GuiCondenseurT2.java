package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileItemInput;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;

public class GuiCondenseurT2 extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_condenseur_t2.png");

    private final ContainerCondenseurT2 container;
    private final TileCondenseurT2 master;

    private static final int GUI_W = 176;
    private static final int GUI_H = 176;

    public GuiCondenseurT2(InventoryPlayer playerInv, TileCondenseurT2 master,
                            TileItemInput inputTile, TileItemOutput outputTile) {
        super(new ContainerCondenseurT2(playerInv, master, inputTile, outputTile));
        this.container = (ContainerCondenseurT2) inventorySlots;
        this.master = master;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int gx = guiLeft, gy = guiTop;
        long time = master.getWorld() != null ? master.getWorld().getTotalWorldTime() : 0;
        boolean proc = container.getProcessTime() > 0;

        // === SLOT GLOW when processing ===
        if (proc) {
            int glowColor = 0xFF6B3FA0;
            // Input slots glow
            drawRect(gx+13, gy+26, gx+33, gy+46, glowColor);
            drawRect(gx+14, gy+27, gx+32, gy+45, 0xFF080310);
            drawRect(gx+35, gy+26, gx+55, gy+46, glowColor);
            drawRect(gx+36, gy+27, gx+54, gy+45, 0xFF080310);
            drawRect(gx+13, gy+46, gx+33, gy+66, glowColor);
            drawRect(gx+14, gy+47, gx+32, gy+65, 0xFF080310);
            drawRect(gx+35, gy+46, gx+55, gy+66, glowColor);
            drawRect(gx+36, gy+47, gx+54, gy+65, 0xFF080310);
            // Output slot glow
            drawRect(gx+128, gy+37, gx+148, gy+57, glowColor);
            drawRect(gx+129, gy+38, gx+147, gy+56, 0xFF080310);
        }

        // === PROGRESS BAR FILL (vertical, bottom to top) ===
        int barX = gx + 79, barY = gy + 24, barW = 18, barH = 40;
        if (container.getMaxProcessTime() > 0 && container.getProcessTime() > 0) {
            float pct = (float) container.getProcessTime() / container.getMaxProcessTime();
            int fillH = (int)(pct * barH);
            for (int row = 0; row < fillH; row++) {
                float rowPct = (float) row / barH;
                int r = (int)(90 + 100 * rowPct);
                int g = (int)(30 + 40 * rowPct);
                int b = (int)(140 + 80 * rowPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(barX + 1, barY + barH - row - 1,
                         barX + barW - 1, barY + barH - row, color);
            }
            // Bright leading edge
            if (fillH > 0 && fillH < barH)
                drawRect(barX + 1, barY + barH - fillH,
                         barX + barW - 1, barY + barH - fillH + 1, 0xFFCC88FF);
            // Animated scanline
            int scanY = barY + barH - 1 - (int)((time % 40) * barH / 40);
            if (scanY >= barY && scanY < barY + barH)
                drawRect(barX + 1, scanY, barX + barW - 1, scanY + 1, 0x40FFFFFF);
        }

        // === ENERGY BAR FILL (horizontal) ===
        int eBarX = gx + 8, eBarY = gy + 73, eBarW = 60, eBarH = 8;
        int maxE = container.getMaxEnergy();
        int curE = container.getEnergy();
        if (maxE > 0 && curE > 0) {
            float ePct = (float) curE / maxE;
            int fillW = (int)(ePct * eBarW);
            for (int col = 0; col < fillW; col++) {
                float colPct = (float) col / eBarW;
                int r = (int)(60 + 140 * colPct);
                int g = (int)(20 + 30 * colPct);
                int b = (int)(100 + 100 * colPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(eBarX + col, eBarY + 1,
                         eBarX + col + 1, eBarY + eBarH - 1, color);
            }
        }

        // === FLUID BAR (vertical, only if Fluid Input hatch is connected) ===
        if (container.hasFluidHatch()) {
            int fBarX = gx + 158, fBarY = gy + 24, fBarW = 10, fBarH = 40;
            int maxF = container.getFluidCapacity();
            int curF = container.getFluidAmount();
            // Frame + dark inside
            drawRect(fBarX - 1, fBarY - 1, fBarX + fBarW + 1, fBarY + fBarH + 1, 0xFF000000);
            drawRect(fBarX, fBarY, fBarX + fBarW, fBarY + fBarH, 0xFF1A0E08);
            if (maxF > 0 && curF > 0) {
                float fPct = (float) curF / maxF;
                int fillH = (int)(fPct * fBarH);
                // Diarrhée brown-yellow gradient (bottom to top)
                for (int row = 0; row < fillH; row++) {
                    float rowPct = (float) row / fBarH;
                    int r = (int)(80 + 100 * rowPct);
                    int g = (int)(50 + 60 * rowPct);
                    int b = (int)(20 + 20 * rowPct);
                    int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                    drawRect(fBarX, fBarY + fBarH - row - 1,
                             fBarX + fBarW, fBarY + fBarH - row, color);
                }
                // Bright leading edge
                if (fillH > 0 && fillH < fBarH)
                    drawRect(fBarX, fBarY + fBarH - fillH,
                             fBarX + fBarW, fBarY + fBarH - fillH + 1, 0xFFE8C060);
            }
        }

        // === SCANLINE EFFECT over GUI ===
        if (proc) {
            int scanLine = gy + (int)((time % 80) * GUI_H / 80);
            if (scanLine >= gy && scanLine < gy + GUI_H)
                drawRect(gx + 1, scanLine, gx + GUI_W - 1, scanLine + 1,
                    0x08FFFFFF);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        long time = master.getWorld() != null ? master.getWorld().getTotalWorldTime() : 0;
        boolean proc = container.getProcessTime() > 0;
        boolean formed = container.isStructureFormed();

        // Title
        String title = "CONDENSEUR T2";
        fontRenderer.drawString(title,
            (GUI_W - fontRenderer.getStringWidth(title)) / 2, 5, 0xFFBB88FF);

        // Input labels
        fontRenderer.drawString("CM", 18, 21, 0xFF7755AA);
        fontRenderer.drawString("CM", 40, 21, 0xFF7755AA);
        fontRenderer.drawString("Cle", 17, 41, 0xFF554477);
        fontRenderer.drawString("Cat", 39, 41, 0xFF554477);

        // Progress %
        if (proc && container.getMaxProcessTime() > 0) {
            int pct = (container.getProcessTime() * 100) / container.getMaxProcessTime();
            String pctStr = pct + "%";
            fontRenderer.drawString(pctStr,
                88 - fontRenderer.getStringWidth(pctStr) / 2, 66, 0xFF44DD88);
        } else if (formed) {
            fontRenderer.drawString("---", 83, 66, 0xFF554477);
        }

        // Output label
        fontRenderer.drawString("Sortie", 126, 22, 0xFF7755AA);

        // Fluid bar label (only if hatch present)
        if (container.hasFluidHatch()) {
            fontRenderer.drawString("DIA", 156, 65, 0xFFB48060);
        }

        // Structure status
        if (!formed) {
            fontRenderer.drawString("ERREUR STRUCTURE", 110, 40, 0xFFFF6644);
            if (time % 40 < 20)
                fontRenderer.drawString("!", 155, 40, 0xFFFF0000);
        } else if (proc) {
            String quote = master.getCurrentQuote();
            if (quote.length() > 24) quote = quote.substring(0, 24) + "..";
            fontRenderer.drawString(quote, 110, 40, 0xFF338866);
        } else {
            fontRenderer.drawString("En attente", 116, 40, 0xFF554477);
        }

        // Energy text
        String eStr = formatE(container.getEnergy()) + " / "
            + formatE(container.getMaxEnergy()) + " RF";
        fontRenderer.drawString(eStr, 10, 74, 0xFF9966CC);

        // Status bar
        if (proc) {
            fontRenderer.drawString("200 RF/t", 76, 76, 0xFFFF8844);
            int dots = (int)(time % 60 / 20) + 1;
            StringBuilder dotStr = new StringBuilder();
            for (int i = 0; i < dots; i++) dotStr.append(".");
            fontRenderer.drawString("ACTIF" + dotStr, 130, 76, 0xFF44DD88);
        } else if (formed) {
            fontRenderer.drawString("PRET", 140, 76, 0xFF7755AA);
        } else {
            fontRenderer.drawString("HORS LIGNE", 114, 76, 0xFFFF6644);
        }
    }

    private String formatE(int e) {
        if (e >= 1000000) return String.format("%.1fM", e / 1000000.0);
        if (e >= 1000) return String.format("%.1fK", e / 1000.0);
        return String.valueOf(e);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);

        // Energy bar tooltip (over JEI)
        int gx = guiLeft, gy = guiTop;
        if (mx >= gx + 7 && mx <= gx + 69 && my >= gy + 72 && my <= gy + 81) {
            drawHoveringText(Collections.singletonList(
                container.getEnergy() + " / " + container.getMaxEnergy() + " RF"),
                mx, my);
        }

        // Fluid bar tooltip (only if hatch present)
        if (container.hasFluidHatch()
                && mx >= gx + 158 && mx <= gx + 168
                && my >= gy + 24 && my <= gy + 64) {
            drawHoveringText(Collections.singletonList(
                container.getFluidAmount() + " / " + container.getFluidCapacity() + " mB Diarrhée"),
                mx, my);
        }
    }
}
