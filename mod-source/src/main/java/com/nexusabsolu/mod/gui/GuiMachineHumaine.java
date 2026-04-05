package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.SideConfig;
import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import java.io.IOException;
import java.util.Collections;

public class GuiMachineHumaine extends GuiContainer {

    private final TileMachineHumaine tile;
    private boolean configOpen = false;
    private int configType = 0; // 0=food,1=water,2=energy,3=output

    // Tab/button colors per type
    private static final int[] TYPE_COLORS = {
        0xFF307830, // food green
        0xFF2870B8, // water blue
        0xFFCC4444, // energy red
        0xFF8B6914  // output brown
    };
    private static final int[] TYPE_COLORS_BRIGHT = {
        0xFF50CC50, 0xFF44AAEE, 0xFFFF6666, 0xFFCCAA44
    };
    private static final String[] TYPE_LABELS = {"F", "W", "E", "O"};
    private static final String[] TYPE_NAMES = {
        "Food", "Water", "Energy", "Output"
    };
    private static final String[] FACE_LABELS = {
        "B", "H", "N", "S", "O", "E"
    };
    private static final String[] FACE_NAMES = {
        "Bas", "Haut", "Nord", "Sud", "Ouest", "Est"
    };

    // Bar positions (local GUI coords)
    private static final int BAR_Y = 16;
    private static final int BAR_H = 96;
    private static final int BAR_W = 14;

    public GuiMachineHumaine(InventoryPlayer playerInv,
                              TileMachineHumaine tile) {
        super(new ContainerMachineHumaine(playerInv, tile));
        this.tile = tile;
        this.xSize = 200;
        this.ySize = 220;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt,
                                                    int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = guiLeft;
        int y = guiTop;

        // === MAIN BACKGROUND ===
        drawRect(x, y, x + xSize, y + ySize, 0xFF12081C);
        // Borders
        drawRect(x, y, x + xSize, y + 1, 0xFF6B3FA0);
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFF3A1F5E);
        drawRect(x, y, x + 1, y + ySize, 0xFF6B3FA0);
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFF3A1F5E);

        // Title
        String title = "Machine Voss Diarh33";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title,
            x + (xSize - tw) / 2.0F, y + 5, 0xDD88FF);

        // Inner panel
        drawRect(x + 4, y + BAR_Y - 2, x + 196, y + BAR_Y + BAR_H + 4,
            0xFF1E1030);
        drawRect(x + 5, y + BAR_Y - 1, x + 195, y + BAR_Y + BAR_H + 3,
            0xFF261440);

        // === BARS (spaced 24px apart to avoid label overlap) ===
        // Food bar: stack count / 64
        int foodCount = tile.getStackInSlot(0).getCount();
        drawBar(x + 28, y + BAR_Y, BAR_W, BAR_H,
            foodCount, 64,
            0xFF307830, 0xFF50CC50, "Food");

        // Water bar: field 1 = water level
        drawBar(x + 52, y + BAR_Y, BAR_W, BAR_H,
            tile.getField(1), TileMachineHumaine.TANK_CAPACITY,
            0xFF2870B8, 0xFF44CCFF, "H2O");

        // Bio-E bar: field 0 = energy
        drawBar(x + 76, y + BAR_Y, BAR_W, BAR_H,
            tile.getField(0), TileMachineHumaine.RF_CAPACITY,
            0xFFCC4444, 0xFFFF6666, "Bio-E");

        // === PROGRESS ===
        int px = x + 100;
        int py = y + 35;
        int pw = 46;
        int ph = 40;
        drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF3A1F5E);
        drawRect(px, py, px + pw, py + ph, 0xFF0E0818);

        int prog = tile.getField(3);
        int maxProg = TileMachineHumaine.PROCESS_TIME;
        if (maxProg > 0 && prog > 0) {
            float ratio = (float) prog / maxProg;
            int fillW = (int)(pw * ratio);
            drawRect(px, py, px + fillW, py + ph, 0xFF3A1F5E);
        }
        String pct = (maxProg > 0 ? (prog * 100 / maxProg) : 0) + "%";
        int ptw = fontRenderer.getStringWidth(pct);
        fontRenderer.drawStringWithShadow(pct,
            px + (pw - ptw) / 2.0F, py + (ph - 8) / 2.0F, 0xDD88FF);

        // Arrows in/out
        fontRenderer.drawStringWithShadow("\u00bb",
            px - 8, py + (ph - 8) / 2.0F, 0xDD88FF);
        fontRenderer.drawStringWithShadow("\u00bb",
            px + pw + 2, py + (ph - 8) / 2.0F, 0xDD88FF);

        fontRenderer.drawStringWithShadow("Digestion",
            px + (pw - fontRenderer.getStringWidth("Digestion")) / 2.0F,
            py + ph + 3, 0xFF8866AA);

        // === OUTPUT TANK ===
        drawBar(x + 154, y + BAR_Y, 16, BAR_H,
            tile.getField(2), TileMachineHumaine.TANK_CAPACITY,
            0xFF6B4513, 0xFFA06820, "Sortie");

        // === INFO LINE ===
        String info = TileMachineHumaine.RF_PER_TICK + " Bio-E/t";
        fontRenderer.drawStringWithShadow(info, x + 8, y + BAR_Y + BAR_H + 8,
            0xFF8866AA);

        // === SEPARATOR ===
        drawRect(x + 6, y + 128, x + 194, y + 129, 0xFF3A1F5E);

        // === INVENTORY LABEL ===
        fontRenderer.drawStringWithShadow("Inventaire",
            x + 8, y + 130, 0xFF8866AA);

        // === SLOT BACKGROUNDS ===
        for (Slot slot : inventorySlots.inventorySlots) {
            int sx = x + slot.xPos - 1;
            int sy = y + slot.yPos - 1;
            boolean isMachine = slot.slotNumber < 3;
            int border = isMachine ? 0xFF6B3FA0 : 0xFF3A1F5E;
            drawRect(sx, sy, sx + 18, sy + 18, border);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF1E1030);
        }

        // Slot labels
        fontRenderer.drawStringWithShadow("Food",
            x + 8, y + 56, 0xFF50803A);
        fontRenderer.drawStringWithShadow("Vide",
            x + 170, y + 48, 0xFF8866AA);
        fontRenderer.drawStringWithShadow("Plein",
            x + 170, y + 86, 0xFF8B6914);

        // === CONFIG TABS (LEFT side, outside GUI) ===
        drawConfigTabs(x, y, mx, my);
    }

    private void drawBar(int bx, int by, int bw, int bh,
                          int value, int max, int color, int shine,
                          String label) {
        // Border
        drawRect(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFF3A1F5E);
        // Background
        drawRect(bx, by, bx + bw, by + bh, 0xFF0A0410);
        // Fill
        if (max > 0 && value > 0) {
            float ratio = Math.min(1.0F, (float) value / max);
            int fillH = (int)(bh * ratio);
            drawRect(bx + 1, by + bh - fillH,
                     bx + bw - 1, by + bh, color);
            // Shine line at top of fill
            if (fillH > 2) {
                drawRect(bx + 1, by + bh - fillH,
                         bx + bw - 1, by + bh - fillH + 2, shine);
            }
        }
        // Label below
        int lw = fontRenderer.getStringWidth(label);
        fontRenderer.drawStringWithShadow(label,
            bx + (bw - lw) / 2.0F, by + bh + 3,
            (color & 0x00FFFFFF) | 0xFF000000);
    }

    private void drawConfigTabs(int gx, int gy, int mx, int my) {
        int tx = gx - 28;
        int ty = gy + 16;

        // Config gear tab
        boolean gearHov = mx >= tx && mx <= tx + 26 && my >= ty && my <= ty + 24;
        drawRect(tx, ty, tx + 26, ty + 24, configOpen
            ? 0xFF4A2A70 : (gearHov ? 0xFF3A1F5E : 0xFF261440));
        drawRect(tx, ty, tx + 26, ty + 1, 0xFF6B3FA0);
        drawRect(tx + 25, ty, tx + 26, ty + 24, 0xFF6B3FA0);
        fontRenderer.drawStringWithShadow("*", tx + 10, ty + 8,
            configOpen ? 0xFFDD88FF : 0xFFCCCCCC);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Empty - use drawScreen for everything over JEI
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);

        // === CONFIG PANEL (OVER JEI) ===
        if (configOpen) {
            drawConfigPanel(mx, my);
        }

        // === TOOLTIPS (OVER JEI) ===
        drawBarTooltips(mx, my);
    }

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft - 170;
        int py = guiTop + 44;
        int pw = 160;
        int ph = 200;

        // Panel background
        drawRect(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF3A1F5E);
        drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF8855BB);
        drawRect(px, py, px + pw, py + ph, 0xFF1A1030);

        // Title
        String title = "Side Config - " + TYPE_NAMES[configType];
        fontRenderer.drawStringWithShadow(title, px + 6, py + 4, 0xFFDD88FF);
        drawRect(px + 4, py + 15, px + pw - 4, py + 16, 0xFF6B3FA0);

        // Type tabs (vertical, left of panel)
        for (int t = 0; t < 4; t++) {
            int tbx = px - 28;
            int tby = py + 20 + t * 32;
            boolean sel = (t == configType);
            boolean hov = mx >= tbx && mx <= tbx + 26
                       && my >= tby && my <= tby + 28;
            int bg = sel ? TYPE_COLORS[t] : (hov ? 0xFF3A1F5E : 0xFF1E1030);
            drawRect(tbx, tby, tbx + 26, tby + 28, bg);
            drawRect(tbx, tby, tbx + 26, tby + 1,
                sel ? TYPE_COLORS_BRIGHT[t] : 0xFF3A1F5E);
            int lw = fontRenderer.getStringWidth(TYPE_LABELS[t]);
            fontRenderer.drawStringWithShadow(TYPE_LABELS[t],
                tbx + (26 - lw) / 2.0F, tby + 10,
                sel ? 0xFFFFFFFF : 0xFF8866AA);
        }

        // Cube deplied - cross layout
        //       [H]
        // [O]   [N]   [E]
        // [B]   [S]
        int bs = 36; // button size
        int bg = 3;  // gap
        int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
        int cy = py + 26;

        SideConfig sc = tile.getSideConfig();
        // UP=1
        drawFaceBtn(cx + bs + bg, cy, bs, 1, sc, mx, my);
        // WEST=4, NORTH=2, EAST=5
        drawFaceBtn(cx, cy + bs + bg, bs, 4, sc, mx, my);
        drawFaceBtn(cx + bs + bg, cy + bs + bg, bs, 2, sc, mx, my);
        drawFaceBtn(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, sc, mx, my);
        // DOWN=0, SOUTH=3
        drawFaceBtn(cx, cy + (bs + bg) * 2, bs, 0, sc, mx, my);
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 2, bs, 3, sc, mx, my);

        // Eject / Auto-pull toggles
        int tbY = py + 155;

        boolean ej = sc.isEject(configType);
        drawRect(px + 8, tbY, px + 72, tbY + 18,
            ej ? TYPE_COLORS[configType] : 0xFF0E0818);
        if (!ej) drawRect(px + 8, tbY, px + 72, tbY + 18, 0xFF0E0818);
        drawRect(px + 8, tbY, px + 72, tbY + 1, 0xFF3A1F5E);
        String ejTxt = "Eject:" + (ej ? "ON" : "OFF");
        fontRenderer.drawStringWithShadow(ejTxt, px + 12, tbY + 5,
            ej ? 0xFFFFFFFF : 0xFF554466);

        boolean ap = sc.isAutoPull(configType);
        drawRect(px + 82, tbY, px + pw - 6, tbY + 18,
            ap ? TYPE_COLORS[configType] : 0xFF0E0818);
        if (!ap) drawRect(px + 82, tbY, px + pw - 6, tbY + 18, 0xFF0E0818);
        drawRect(px + 82, tbY, px + pw - 6, tbY + 1, 0xFF3A1F5E);
        String apTxt = "Pull:" + (ap ? "ON" : "OFF");
        fontRenderer.drawStringWithShadow(apTxt, px + 86, tbY + 5,
            ap ? 0xFFFFFFFF : 0xFF554466);

        // Face tooltips
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

        // 3D border
        int borderLight = hov ? 0xFFAA77DD : 0xFF8855BB;
        drawRect(bx - 1, by - 1, bx + sz + 1, by, borderLight);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, borderLight);
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);

        if (active) {
            int c = TYPE_COLORS[configType];
            int cb = TYPE_COLORS_BRIGHT[configType];
            drawRect(bx, by, bx + sz, by + sz, c);
            drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, cb);
            drawRect(bx + 4, by + 4, bx + sz - 4, by + sz - 4, c);
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

        // Food bar
        if (isInRect(mx, my, x + 28, y + BAR_Y, BAR_W, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                "Food: " + (tile.getStackInSlot(0).isEmpty()
                    ? "Vide" : tile.getStackInSlot(0).getDisplayName())),
                mx, my);
        }
        // Water bar
        if (isInRect(mx, my, x + 52, y + BAR_Y, BAR_W, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                tile.getField(1) + " / " + TileMachineHumaine.TANK_CAPACITY
                + " mB H2O"), mx, my);
        }
        // Bio-E bar
        if (isInRect(mx, my, x + 76, y + BAR_Y, BAR_W, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                tile.getField(0) + " / " + TileMachineHumaine.RF_CAPACITY
                + " Bio-E"), mx, my);
        }
        // Output tank
        if (isInRect(mx, my, x + 154, y + BAR_Y, 16, BAR_H)) {
            drawHoveringText(Collections.singletonList(
                tile.getField(2) + " / " + TileMachineHumaine.TANK_CAPACITY
                + " mB"), mx, my);
        }
    }

    private boolean isInRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    // ==================== MOUSE CLICKS ====================

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int gx = guiLeft;
        int gy = guiTop;

        // Config gear tab
        int tx = gx - 28;
        int ty = gy + 16;
        if (mx >= tx && mx <= tx + 26 && my >= ty && my <= ty + 24) {
            configOpen = !configOpen;
            return;
        }

        if (configOpen) {
            int px = gx - 170;
            int py = gy + 44;
            int pw = 160;

            // Type tabs
            for (int t = 0; t < 4; t++) {
                int tbx = px - 28;
                int tby = py + 20 + t * 32;
                if (mx >= tbx && mx <= tbx + 26
                 && my >= tby && my <= tby + 28) {
                    configType = t;
                    return;
                }
            }

            // Face buttons
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
                    int action = configType * 6 + b[0];
                    mc.playerController.sendEnchantPacket(
                        inventorySlots.windowId, action);
                    return;
                }
            }

            // Eject toggle
            int tbY = py + 155;
            if (mx >= px + 8 && mx <= px + 72
             && my >= tbY && my <= tbY + 18) {
                mc.playerController.sendEnchantPacket(
                    inventorySlots.windowId, 24 + configType);
                return;
            }
            // Auto-pull toggle
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
