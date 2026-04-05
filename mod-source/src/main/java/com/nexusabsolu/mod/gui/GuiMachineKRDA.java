package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.SideConfig;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import java.io.IOException;
import java.util.Collections;

public class GuiMachineKRDA extends GuiContainer {

    private final TileMachineKRDA tile;
    private boolean configOpen = false;
    private int configType = 0;

    private static final int[] TYPE_COLORS = {
        0xFFCC6600, // signalum orange
        0xFF8B6914, // diarrhee brown
        0xFFCC4444, // energy red
        0xFF30A030  // output green
    };
    private static final int[] TYPE_BRIGHT = {
        0xFFFF8800, 0xFFCCAA44, 0xFFFF6666, 0xFF50CC50
    };
    private static final String[] TYPE_LABELS = {"S", "D", "E", "O"};
    private static final String[] TYPE_NAMES = {
        "Signalum", "Diarrhee", "Energy", "Sortie"
    };
    private static final String[] FACE_LABELS = {"B","H","N","S","O","E"};
    private static final String[] FACE_NAMES = {
        "Bas","Haut","Nord","Sud","Ouest","Est"
    };

    private static final int BAR_Y = 16;
    private static final int BAR_H = 96;
    private static final int BAR_W = 14;

    public GuiMachineKRDA(InventoryPlayer playerInv, TileMachineKRDA tile) {
        super(new ContainerMachineKRDA(playerInv, tile));
        this.tile = tile;
        this.xSize = 200;
        this.ySize = 220;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = guiLeft;
        int y = guiTop;

        // Background
        drawRect(x, y, x + xSize, y + ySize, 0xFF12081C);
        drawRect(x, y, x + xSize, y + 1, 0xFF6B3FA0);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF3A1F5E);
        drawRect(x, y, x + 1, y + ySize, 0xFF6B3FA0);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF3A1F5E);

        // Title
        String title = "Machine Voss KRDA125";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title,
            x + (xSize - tw) / 2.0F, y + 5, 0xFFFF8800);

        // Inner panel
        drawRect(x + 4, y + BAR_Y - 2, x + 196, y + BAR_Y + BAR_H + 4,
            0xFF1E1030);
        drawRect(x + 5, y + BAR_Y - 1, x + 195, y + BAR_Y + BAR_H + 3,
            0xFF261440);

        // Diarrhee tank bar (spaced for label clarity)
        drawBar(x + 36, y + BAR_Y, BAR_W, BAR_H,
            tile.getField(1), TileMachineKRDA.TANK_CAPACITY,
            0xFF6B4513, 0xFFA06820, "Diarr.");

        // Bio-E bar
        drawBar(x + 66, y + BAR_Y, BAR_W, BAR_H,
            tile.getField(0), TileMachineKRDA.RF_CAPACITY,
            0xFFCC4444, 0xFFFF6666, "Bio-E");

        // Progress
        int px = x + 86;
        int py = y + 35;
        int pw = 56;
        int ph = 40;
        drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF3A1F5E);
        drawRect(px, py, px + pw, py + ph, 0xFF0E0818);
        int prog = tile.getField(3);
        int maxP = TileMachineKRDA.PROCESS_TIME;
        if (maxP > 0 && prog > 0) {
            float ratio = (float) prog / maxP;
            drawRect(px, py, px + (int)(pw * ratio), py + ph, 0xFF3A1F5E);
        }
        String pct = (maxP > 0 ? (prog * 100 / maxP) : 0) + "%";
        int ptw = fontRenderer.getStringWidth(pct);
        fontRenderer.drawStringWithShadow(pct,
            px + (pw - ptw) / 2.0F, py + (ph - 8) / 2.0F, 0xFFFF8800);
        fontRenderer.drawStringWithShadow("\u00bb",
            px - 8, py + (ph - 8) / 2.0F, 0xFFFF8800);
        fontRenderer.drawStringWithShadow("\u00bb",
            px + pw + 2, py + (ph - 8) / 2.0F, 0xFFFF8800);
        fontRenderer.drawStringWithShadow("Transmutation",
            px + (pw - fontRenderer.getStringWidth("Transmutation")) / 2.0F,
            py + ph + 3, 0xFF8866AA);

        // Info (two lines to avoid cramping)
        fontRenderer.drawStringWithShadow(
            TileMachineKRDA.RF_PER_TICK + " Bio-E/t",
            x + 8, y + BAR_Y + BAR_H + 6, 0xFF8866AA);
        fontRenderer.drawStringWithShadow(
            TileMachineKRDA.FLUID_PER_CYCLE + " mB/op",
            x + 8, y + BAR_Y + BAR_H + 16, 0xFF8866AA);

        // Separator + inv label
        drawRect(x + 6, y + 128, x + 194, y + 129, 0xFF3A1F5E);
        fontRenderer.drawStringWithShadow("Inventaire",
            x + 8, y + 130, 0xFF8866AA);

        // Slot backgrounds
        for (Slot slot : inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            boolean isMachine = slot.slotNumber < 2;
            drawRect(sx, sy, sx + 18, sy + 18,
                isMachine ? 0xFF6B3FA0 : 0xFF3A1F5E);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }
        fontRenderer.drawStringWithShadow("IN",
            x + 14, y + 63, 0xFFCC6600);
        fontRenderer.drawStringWithShadow("OUT",
            x + 158, y + 63, 0xFF30A030);

        // Config tabs
        int tx = x - 28;
        int ty = y + 16;
        boolean gearHov = mx >= tx && mx <= tx + 26 && my >= ty && my <= ty + 24;
        drawRect(tx, ty, tx + 26, ty + 24, configOpen
            ? 0xFF4A2A70 : (gearHov ? 0xFF3A1F5E : 0xFF261440));
        drawRect(tx, ty, tx + 26, ty + 1, 0xFF6B3FA0);
        drawRect(tx + 25, ty, tx + 26, ty + 24, 0xFF6B3FA0);
        fontRenderer.drawStringWithShadow("*", tx + 10, ty + 8,
            configOpen ? 0xFFFF8800 : 0xFFCCCCCC);
    }

    private void drawBar(int bx, int by, int bw, int bh,
                          int val, int max, int color, int shine,
                          String label) {
        drawRect(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF3A1F5E);
        drawRect(bx, by, bx + bw, by + bh, 0xFF0A0410);
        if (max > 0 && val > 0) {
            float r = Math.min(1.0F, (float) val / max);
            int fH = (int)(bh * r);
            drawRect(bx + 1, by + bh - fH, bx + bw - 1, by + bh, color);
            if (fH > 2)
                drawRect(bx + 1, by + bh - fH,
                         bx + bw - 1, by + bh - fH + 2, shine);
        }
        int lw = fontRenderer.getStringWidth(label);
        fontRenderer.drawStringWithShadow(label,
            bx + (bw - lw) / 2.0F, by + bh + 3,
            (color & 0x00FFFFFF) | 0xFF000000);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {}

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        if (configOpen) drawConfigPanel(mx, my);
        drawBarTooltips(mx, my);
    }

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft - 170;
        int py = guiTop + 44;
        int pw = 160;
        int ph = 200;

        drawRect(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF3A1F5E);
        drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF8855BB);
        drawRect(px, py, px + pw, py + ph, 0xFF1A1030);

        String title = "Side Config - " + TYPE_NAMES[configType];
        fontRenderer.drawStringWithShadow(title, px + 6, py + 4, 0xFFFF8800);
        drawRect(px + 4, py + 15, px + pw - 4, py + 16, 0xFF6B3FA0);

        for (int t = 0; t < 4; t++) {
            int tbx = px - 28;
            int tby = py + 20 + t * 32;
            boolean sel = (t == configType);
            boolean hov = mx >= tbx && mx <= tbx + 26
                       && my >= tby && my <= tby + 28;
            drawRect(tbx, tby, tbx + 26, tby + 28,
                sel ? TYPE_COLORS[t] : (hov ? 0xFF3A1F5E : 0xFF1E1030));
            drawRect(tbx, tby, tbx + 26, tby + 1,
                sel ? TYPE_BRIGHT[t] : 0xFF3A1F5E);
            int lw = fontRenderer.getStringWidth(TYPE_LABELS[t]);
            fontRenderer.drawStringWithShadow(TYPE_LABELS[t],
                tbx + (26 - lw) / 2.0F, tby + 10,
                sel ? 0xFFFFFFFF : 0xFF8866AA);
        }

        SideConfig sc = tile.getSideConfig();
        int bs = 36;
        int bg = 3;
        int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
        int cy = py + 26;

        drawFaceBtn(cx + bs + bg, cy, bs, 1, sc, mx, my);
        drawFaceBtn(cx, cy + bs + bg, bs, 4, sc, mx, my);
        drawFaceBtn(cx + bs + bg, cy + bs + bg, bs, 2, sc, mx, my);
        drawFaceBtn(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, sc, mx, my);
        drawFaceBtn(cx, cy + (bs + bg) * 2, bs, 0, sc, mx, my);
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 2, bs, 3, sc, mx, my);

        int tbY = py + 155;
        boolean ej = sc.isEject(configType);
        drawRect(px + 8, tbY, px + 72, tbY + 18,
            ej ? TYPE_COLORS[configType] : 0xFF0E0818);
        fontRenderer.drawStringWithShadow("Eject:" + (ej ? "ON" : "OFF"),
            px + 12, tbY + 5, ej ? 0xFFFFFFFF : 0xFF554466);
        boolean ap = sc.isAutoPull(configType);
        drawRect(px + 82, tbY, px + pw - 6, tbY + 18,
            ap ? TYPE_COLORS[configType] : 0xFF0E0818);
        fontRenderer.drawStringWithShadow("Pull:" + (ap ? "ON" : "OFF"),
            px + 86, tbY + 5, ap ? 0xFFFFFFFF : 0xFF554466);

        int[][] btns = {
            {1, cx + bs + bg, cy},
            {4, cx, cy + bs + bg},
            {2, cx + bs + bg, cy + bs + bg},
            {5, cx + (bs + bg) * 2, cy + bs + bg},
            {0, cx, cy + (bs + bg) * 2},
            {3, cx + bs + bg, cy + (bs + bg) * 2}
        };
        for (int[] b : btns) {
            if (mx >= b[1] && mx <= b[1] + bs
             && my >= b[2] && my <= b[2] + bs) {
                boolean active = sc.isFaceActive(configType, b[0]);
                String state = active ? "\u00a7aON" : "\u00a7cOFF";
                drawHoveringText(Collections.singletonList(
                    FACE_NAMES[b[0]] + " " + TYPE_NAMES[configType]
                    + ": " + state), mx, my);
            }
        }
    }

    private void drawFaceBtn(int bx, int by, int sz, int face,
                              SideConfig sc, int mx, int my) {
        boolean active = sc.isFaceActive(configType, face);
        boolean hov = mx >= bx && mx <= bx + sz && my >= by && my <= by + sz;
        int bl = hov ? 0xFFAA77DD : 0xFF8855BB;
        drawRect(bx - 1, by - 1, bx + sz + 1, by, bl);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, bl);
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        if (active) {
            drawRect(bx, by, bx + sz, by + sz, TYPE_COLORS[configType]);
            drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, TYPE_BRIGHT[configType]);
            drawRect(bx + 4, by + 4, bx + sz - 4, by + sz - 4, TYPE_COLORS[configType]);
        } else {
            drawRect(bx, by, bx + sz, by + sz, 0xFF0E0818);
            drawRect(bx + 1, by + 1, bx + sz - 1, by + sz - 1, 0xFF1A1030);
        }
        String lbl = FACE_LABELS[face];
        int lw = fontRenderer.getStringWidth(lbl);
        fontRenderer.drawStringWithShadow(lbl,
            bx + (sz - lw) / 2.0F, by + (sz - 8) / 2.0F,
            active ? 0xFFFFFFFF : 0xFF554466);
    }

    private void drawBarTooltips(int mx, int my) {
        int x = guiLeft;
        int y = guiTop;
        if (mx >= x + 36 && mx <= x + 50
         && my >= y + BAR_Y && my <= y + BAR_Y + BAR_H) {
            drawHoveringText(Collections.singletonList(
                tile.getField(1) + " / " + TileMachineKRDA.TANK_CAPACITY
                + " mB Diarrhee"), mx, my);
        }
        if (mx >= x + 66 && mx <= x + 80
         && my >= y + BAR_Y && my <= y + BAR_Y + BAR_H) {
            drawHoveringText(Collections.singletonList(
                tile.getField(0) + " / " + TileMachineKRDA.RF_CAPACITY
                + " Bio-E"), mx, my);
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int gx = guiLeft;
        int gy = guiTop;

        int tx = gx - 28;
        int ty = gy + 16;
        if (mx >= tx && mx <= tx + 26 && my >= ty && my <= ty + 24) {
            configOpen = !configOpen; return;
        }

        if (configOpen) {
            int px = gx - 170;
            int py = gy + 44;
            int pw = 160;
            for (int t = 0; t < 4; t++) {
                int tbx = px - 28;
                int tby = py + 20 + t * 32;
                if (mx >= tbx && mx <= tbx + 26
                 && my >= tby && my <= tby + 28) {
                    configType = t; return;
                }
            }
            int bs = 36;
            int bg = 3;
            int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
            int cy = py + 26;
            int[][] btns = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {2, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {0, cx, cy + (bs + bg) * 2},
                {3, cx + bs + bg, cy + (bs + bg) * 2}
            };
            for (int[] b : btns) {
                if (mx >= b[1] && mx <= b[1] + bs
                 && my >= b[2] && my <= b[2] + bs) {
                    mc.playerController.sendEnchantPacket(
                        inventorySlots.windowId, configType * 6 + b[0]);
                    return;
                }
            }
            int tbY = py + 155;
            if (mx >= px + 8 && mx <= px + 72
             && my >= tbY && my <= tbY + 18) {
                mc.playerController.sendEnchantPacket(
                    inventorySlots.windowId, 24 + configType);
                return;
            }
            if (mx >= px + 82 && mx <= px + pw - 6
             && my >= tbY && my <= tbY + 18) {
                mc.playerController.sendEnchantPacket(
                    inventorySlots.windowId, 28 + configType);
                return;
            }
        }
        super.mouseClicked(mx, my, btn);
    }
}
