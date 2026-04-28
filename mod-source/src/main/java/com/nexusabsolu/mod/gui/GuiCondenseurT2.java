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

    private static final int GUI_W = 220;
    private static final int GUI_H = 220;

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

        // === SLOT GLOW when processing (new 220x220 coords) ===
        if (proc) {
            int glowColor = 0xFF6B3FA0;
            // Input slots 2x2 at (36,30) (56,30) (36,59) (56,59)
            drawRect(gx+36, gy+30, gx+54, gy+48, glowColor);
            drawRect(gx+37, gy+31, gx+53, gy+47, 0xFF080310);
            drawRect(gx+56, gy+30, gx+74, gy+48, glowColor);
            drawRect(gx+57, gy+31, gx+73, gy+47, 0xFF080310);
            drawRect(gx+36, gy+59, gx+54, gy+77, glowColor);
            drawRect(gx+37, gy+60, gx+53, gy+76, 0xFF080310);
            drawRect(gx+56, gy+59, gx+74, gy+77, glowColor);
            drawRect(gx+57, gy+60, gx+73, gy+76, 0xFF080310);
            // Output slot glow at (150, 42)
            drawRect(gx+150, gy+42, gx+168, gy+60, glowColor);
            drawRect(gx+151, gy+43, gx+167, gy+59, 0xFF080310);
        }

        // === PROGRESS BAR FILL (vertical, center, bottom to top) ===
        // Recess at (94, 28) w=18 h=56, fill inside with 1px padding
        int barX = gx + 95, barY = gy + 29, barW = 16, barH = 54;
        if (container.getMaxProcessTime() > 0 && container.getProcessTime() > 0) {
            float pct = (float) container.getProcessTime() / container.getMaxProcessTime();
            int fillH = (int)(pct * barH);
            for (int row = 0; row < fillH; row++) {
                float rowPct = (float) row / barH;
                int r = (int)(90 + 100 * rowPct);
                int g = (int)(30 + 40 * rowPct);
                int b = (int)(140 + 80 * rowPct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | b;
                drawRect(barX, barY + barH - row - 1,
                         barX + barW, barY + barH - row, color);
            }
            // Bright leading edge
            if (fillH > 0 && fillH < barH)
                drawRect(barX, barY + barH - fillH,
                         barX + barW, barY + barH - fillH + 1, 0xFFCC88FF);
            // Animated scanline
            int scanY = barY + barH - 1 - (int)((time % 40) * barH / 40);
            if (scanY >= barY && scanY < barY + barH)
                drawRect(barX, scanY, barX + barW, scanY + 1, 0x40FFFFFF);
        }

        // === ENERGY BAR FILL (horizontal, full width) ===
        // Recess at (10, 96) w=200 h=8
        int eBarX = gx + 10, eBarY = gy + 96, eBarW = 200, eBarH = 8;
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
                drawRect(eBarX + col, eBarY,
                         eBarX + col + 1, eBarY + eBarH, color);
            }
        }

        // === FLUID BAR (vertical, LEFT side, only if Fluid Input hatch is connected) ===
        // Recess at (10, 26) w=12 h=62
        if (container.hasFluidHatch()) {
            int fBarX = gx + 11, fBarY = gy + 27, fBarW = 10, fBarH = 60;
            int maxF = container.getFluidCapacity();
            int curF = container.getFluidAmount();
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

        // Title (centered at top)
        String title = "CONDENSEUR T2";
        fontRenderer.drawString(title,
            (GUI_W - fontRenderer.getStringWidth(title)) / 2, 8, 0xFFBB88FF);

        // Input slot labels
        // Slots at (37,31) (57,31) (37,60) (57,60)
        // Row 1 labels above slots (y=23, slots start y=31)
        fontRenderer.drawString("CM", 40, 23, 0xFF7755AA);
        fontRenderer.drawString("CM", 60, 23, 0xFF7755AA);
        // Row 2 labels between rows (y=51, slots end y=47, slot 2 starts y=60)
        fontRenderer.drawString("Cle", 39, 51, 0xFF7755AA);
        fontRenderer.drawString("Cat", 59, 51, 0xFF7755AA);

        // Progress % (below progress bar at x=95..111, y=29..83)
        if (proc && container.getMaxProcessTime() > 0) {
            int pct = (container.getProcessTime() * 100) / container.getMaxProcessTime();
            String pctStr = pct + "%";
            fontRenderer.drawString(pctStr,
                103 - fontRenderer.getStringWidth(pctStr) / 2, 86, 0xFF44DD88);
        } else if (formed) {
            fontRenderer.drawString("---",
                103 - fontRenderer.getStringWidth("---") / 2, 86, 0xFF554477);
        }

        // Output label (above output slot at 150,42)
        fontRenderer.drawString("Sortie", 145, 30, 0xFF7755AA);

        // Fluid bar label (below fluid bar at 11,27..87) - only if hatch present
        if (container.hasFluidHatch()) {
            fontRenderer.drawString("DIA", 10, 90, 0xFFB48060);
        }

        // Structure status (in the horizontal strip around y=78-90)
        if (!formed) {
            fontRenderer.drawString("ERREUR STRUCTURE", 125, 66, 0xFFFF6644);
            if (time % 40 < 20)
                fontRenderer.drawString("!", 205, 66, 0xFFFF0000);
        } else if (proc) {
            String quote = master.getCurrentQuote();
            if (quote.length() > 26) quote = quote.substring(0, 26) + "..";
            fontRenderer.drawString(quote, 125, 66, 0xFF338866);
        } else {
            fontRenderer.drawString("En attente", 135, 66, 0xFF554477);
        }

        // Energy text (below energy bar at 10,96..104)
        String eStr = formatE(container.getEnergy()) + " / "
            + formatE(container.getMaxEnergy()) + " RF";
        fontRenderer.drawString(eStr, 10, 108, 0xFF9966CC);

        // Status bar (right side of energy text)
        if (proc) {
            fontRenderer.drawString("200 RF/t", 110, 108, 0xFFFF8844);
            int dots = (int)(time % 60 / 20) + 1;
            StringBuilder dotStr = new StringBuilder();
            for (int i = 0; i < dots; i++) dotStr.append(".");
            fontRenderer.drawString("ACTIF" + dotStr, 170, 108, 0xFF44DD88);
        } else if (formed) {
            fontRenderer.drawString("PRET", 180, 108, 0xFF7755AA);
        } else {
            fontRenderer.drawString("HORS LIGNE", 160, 108, 0xFFFF6644);
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

        int gx = guiLeft, gy = guiTop;

        // Energy bar tooltip (now horizontal at 10,96..210,104)
        if (mx >= gx + 10 && mx <= gx + 210 && my >= gy + 96 && my <= gy + 104) {
            drawHoveringText(Collections.singletonList(
                container.getEnergy() + " / " + container.getMaxEnergy() + " RF"),
                mx, my);
        }

        // Fluid bar tooltip (now on the LEFT at 11,27..21,87) - only if hatch present
        if (container.hasFluidHatch()
                && mx >= gx + 10 && mx <= gx + 22
                && my >= gy + 26 && my <= gy + 88) {
            drawHoveringText(Collections.singletonList(
                container.getFluidAmount() + " / " + container.getFluidCapacity() + " mB Diarrhée"),
                mx, my);
        }
    }
}
