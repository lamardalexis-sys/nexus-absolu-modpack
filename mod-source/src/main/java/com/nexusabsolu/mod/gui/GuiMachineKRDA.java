package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.SideConfig;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;

public class GuiMachineKRDA extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_krda.png");

    private final TileMachineKRDA tile;
    private boolean configOpen = false;
    private int configType = 0;

    public boolean isConfigOpen() { return configOpen; }

    private static final int[] TYPE_COLORS = {
        0xFFCC6600, 0xFF8B6914, 0xFFCC4444, 0xFF30A030
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
        "Bas", "Haut", "Nord", "Sud", "Ouest", "Est"
    };

    public GuiMachineKRDA(InventoryPlayer playerInv, TileMachineKRDA tile) {
        super(new ContainerMachineKRDA(playerInv, tile));
        this.tile = tile;
        this.xSize = 200;
        this.ySize = 220;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft;
        int y = guiTop;

        // === DYNAMIC BAR FILLS ===
        fillBar(x + 36, y + 18, 14, 92, tile.getField(1),
            TileMachineKRDA.TANK_CAPACITY, 0xFF6B4513, 0xFFA06820);
        fillBar(x + 66, y + 18, 14, 92, tile.getField(0),
            TileMachineKRDA.RF_CAPACITY, 0xFFCC4444, 0xFFFF6666);

        // === PROGRESS FILL ===
        int prog = tile.getField(3);
        int maxP = TileMachineKRDA.PROCESS_TIME;
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(56.0F * prog / maxP);
            drawRect(x + 90, y + 34, x + 90 + fillW, y + 76, 0xFF3A1F5E);
        }

        // === CONFIG TAB ===
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(x - 28, y + 16, 200, 0, 27, 25);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Title
        String title = "Machine Voss KRDA125";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 4, 0xFFFF8800);

        // Bar letters
        fontRenderer.drawStringWithShadow("D", 39, 20, 0xFFA06820);
        fontRenderer.drawStringWithShadow("E", 69, 20, 0xFFFF6666);

        // Slot labels
        fontRenderer.drawStringWithShadow("IN", 14, 62, 0xFFCC6600);
        fontRenderer.drawStringWithShadow("OUT", 158, 62, 0xFF30A030);

        // Progress %
        int prog = tile.getField(3);
        int maxP = TileMachineKRDA.PROCESS_TIME;
        String pct = (maxP > 0 ? (prog * 100 / maxP) : 0) + "%";
        int pw = fontRenderer.getStringWidth(pct);
        fontRenderer.drawStringWithShadow(pct, 118 - pw / 2.0F, 51, 0xFFFF8800);

        // Arrows
        fontRenderer.drawStringWithShadow("\u00bb", 83, 51, 0xFFFF8800);
        fontRenderer.drawStringWithShadow("\u00bb", 148, 51, 0xFFFF8800);

        // Labels
        fontRenderer.drawStringWithShadow("Transmutation", 93, 79, 0xFF8866AA);
        fontRenderer.drawStringWithShadow(
            TileMachineKRDA.RF_PER_TICK + " Bio-E/t", 8, 118, 0xFF8866AA);
        fontRenderer.drawStringWithShadow("Inventaire", 8, 129, 0xFF8866AA);

        // Config tab icon
        fontRenderer.drawStringWithShadow("*", -18, 24,
            configOpen ? 0xFFFF8800 : 0xFFCCCCCC);
    }

    private void fillBar(int bx, int by, int bw, int bh,
                          int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillH = (int)(bh * ratio);
        drawRect(bx + 1, by + bh - fillH, bx + bw - 1, by + bh, color);
        if (fillH > 2) {
            drawRect(bx + 1, by + bh - fillH,
                     bx + bw - 1, by + bh - fillH + 2, shine);
        }
    }

    // ==================== drawScreen (over JEI) ====================

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        if (configOpen) drawConfigPanel(mx, my);
        drawBarTooltips(mx, my);
    }

    private void drawBarTooltips(int mx, int my) {
        int x = guiLeft, y = guiTop;
        if (inRect(mx, my, x+36, y+18, 14, 92))
            drawHoveringText(Collections.singletonList(
                tile.getField(1)+"/"+TileMachineKRDA.TANK_CAPACITY+" mB Diarrhee"), mx, my);
        if (inRect(mx, my, x+66, y+18, 14, 92))
            drawHoveringText(Collections.singletonList(
                tile.getField(0)+"/"+TileMachineKRDA.RF_CAPACITY+" Bio-E"), mx, my);
    }

    private boolean inRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    // ==================== SIDE CONFIG ====================

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft-170, py = guiTop+44, pw = 160, ph = 200;

        drawRect(px-2, py-2, px+pw+2, py+ph+2, 0xFF3A1F5E);
        drawRect(px-1, py-1, px+pw+1, py+ph+1, 0xFF8855BB);
        drawRect(px, py, px+pw, py+ph, 0xFF1A1030);

        fontRenderer.drawStringWithShadow("Side Config - "+TYPE_NAMES[configType],
            px+6, py+4, 0xFFFF8800);
        drawRect(px+4, py+15, px+pw-4, py+16, 0xFF6B3FA0);

        for (int i = 0; i < 4; i++) {
            int tbx=px-28, tby=py+20+i*32;
            boolean sel = (i == configType);
            boolean hov = mx>=tbx && mx<=tbx+26 && my>=tby && my<=tby+28;
            drawRect(tbx, tby, tbx+26, tby+28,
                sel ? TYPE_COLORS[i] : (hov ? 0xFF3A1F5E : 0xFF1E1030));
            drawRect(tbx, tby, tbx+26, tby+1,
                sel ? TYPE_BRIGHT[i] : 0xFF3A1F5E);
            int lw = fontRenderer.getStringWidth(TYPE_LABELS[i]);
            fontRenderer.drawStringWithShadow(TYPE_LABELS[i],
                tbx+(26-lw)/2.0F, tby+10, sel ? 0xFFFFFFFF : 0xFF8866AA);
        }

        SideConfig sc = tile.getSideConfig();
        int bs=36, bg=3;
        int cx = px+(pw-(bs*3+bg*2))/2, cy = py+26;
        drawFaceBtn(cx+bs+bg, cy, bs, 1, sc, mx, my);
        drawFaceBtn(cx, cy+bs+bg, bs, 4, sc, mx, my);
        drawFaceBtn(cx+bs+bg, cy+bs+bg, bs, 2, sc, mx, my);
        drawFaceBtn(cx+(bs+bg)*2, cy+bs+bg, bs, 5, sc, mx, my);
        drawFaceBtn(cx, cy+(bs+bg)*2, bs, 0, sc, mx, my);
        drawFaceBtn(cx+bs+bg, cy+(bs+bg)*2, bs, 3, sc, mx, my);

        int tbY = py+155;
        boolean ej = sc.isEject(configType);
        drawRect(px+8, tbY, px+72, tbY+18,
            ej ? TYPE_COLORS[configType] : 0xFF0E0818);
        fontRenderer.drawStringWithShadow("Eject:"+(ej?"ON":"OFF"),
            px+12, tbY+5, ej ? 0xFFFFFFFF : 0xFF554466);
        boolean ap = sc.isAutoPull(configType);
        drawRect(px+82, tbY, px+pw-6, tbY+18,
            ap ? TYPE_COLORS[configType] : 0xFF0E0818);
        fontRenderer.drawStringWithShadow("Pull:"+(ap?"ON":"OFF"),
            px+86, tbY+5, ap ? 0xFFFFFFFF : 0xFF554466);

        int[][] btns = {
            {1,cx+bs+bg,cy}, {4,cx,cy+bs+bg},
            {2,cx+bs+bg,cy+bs+bg}, {5,cx+(bs+bg)*2,cy+bs+bg},
            {0,cx,cy+(bs+bg)*2}, {3,cx+bs+bg,cy+(bs+bg)*2}
        };
        for (int[] b : btns) {
            if (mx>=b[1] && mx<=b[1]+bs && my>=b[2] && my<=b[2]+bs) {
                boolean a = sc.isFaceActive(configType, b[0]);
                drawHoveringText(Collections.singletonList(
                    FACE_NAMES[b[0]]+" "+TYPE_NAMES[configType]+": "
                    +(a?"\u00a7aON":"\u00a7cOFF")), mx, my);
            }
        }
    }

    private void drawFaceBtn(int bx, int by, int sz, int face,
                              SideConfig sc, int mx, int my) {
        boolean active = sc.isFaceActive(configType, face);
        boolean hov = mx>=bx && mx<=bx+sz && my>=by && my<=by+sz;
        int bl = hov ? 0xFFAA77DD : 0xFF8855BB;
        drawRect(bx-1, by-1, bx+sz+1, by, bl);
        drawRect(bx-1, by-1, bx, by+sz+1, bl);
        drawRect(bx, by+sz, bx+sz+1, by+sz+1, 0xFF2A1540);
        drawRect(bx+sz, by, bx+sz+1, by+sz+1, 0xFF2A1540);
        if (active) {
            drawRect(bx, by, bx+sz, by+sz, TYPE_COLORS[configType]);
            drawRect(bx+2, by+2, bx+sz-2, by+sz-2, TYPE_BRIGHT[configType]);
            drawRect(bx+4, by+4, bx+sz-4, by+sz-4, TYPE_COLORS[configType]);
        } else {
            drawRect(bx, by, bx+sz, by+sz, 0xFF0E0818);
            drawRect(bx+1, by+1, bx+sz-1, by+sz-1, 0xFF1A1030);
        }
        int lw = fontRenderer.getStringWidth(FACE_LABELS[face]);
        fontRenderer.drawStringWithShadow(FACE_LABELS[face],
            bx+(sz-lw)/2.0F, by+(sz-8)/2.0F,
            active ? 0xFFFFFFFF : 0xFF554466);
    }

    // ==================== MOUSE ====================

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int gx = guiLeft, gy = guiTop;

        if (mx>=gx-28 && mx<=gx-2 && my>=gy+16 && my<=gy+41) {
            configOpen = !configOpen; return;
        }

        if (configOpen) {
            int px=gx-170, py=gy+44, pw=160;
            for (int i = 0; i < 4; i++) {
                int tbx=px-28, tby=py+20+i*32;
                if (mx>=tbx && mx<=tbx+26 && my>=tby && my<=tby+28) {
                    configType = i; return;
                }
            }
            int bs=36, bg=3;
            int cx=px+(pw-(bs*3+bg*2))/2, cy=py+26;
            int[][] btns = {
                {1,cx+bs+bg,cy}, {4,cx,cy+bs+bg},
                {2,cx+bs+bg,cy+bs+bg}, {5,cx+(bs+bg)*2,cy+bs+bg},
                {0,cx,cy+(bs+bg)*2}, {3,cx+bs+bg,cy+(bs+bg)*2}
            };
            for (int[] b : btns) {
                if (mx>=b[1] && mx<=b[1]+bs && my>=b[2] && my<=b[2]+bs) {
                    mc.playerController.sendEnchantPacket(
                        inventorySlots.windowId, configType*6+b[0]);
                    return;
                }
            }
            int tbY = py+155;
            if (mx>=px+8 && mx<=px+72 && my>=tbY && my<=tbY+18) {
                mc.playerController.sendEnchantPacket(
                    inventorySlots.windowId, 24+configType); return;
            }
            if (mx>=px+82 && mx<=px+pw-6 && my>=tbY && my<=tbY+18) {
                mc.playerController.sendEnchantPacket(
                    inventorySlots.windowId, 28+configType); return;
            }
        }
        super.mouseClicked(mx, my, btn);
    }
}
