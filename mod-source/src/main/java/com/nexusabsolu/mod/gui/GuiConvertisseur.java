package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileConvertisseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;

public class GuiConvertisseur extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_convertisseur.png");

    private final TileConvertisseur tile;
    private boolean configOpen = false;

    public boolean isConfigOpen() { return configOpen; }

    private static final int[] TIER_COLORS = {
        0xFF2A1A3E, 0xFFCC4CFF, 0xFFB299E6, 0xFF80CCB2, 0xFF4CE680, 0xFF33FF4C
    };
    private static final String[] TIER_NAMES = {"", "A", "B", "C", "D", "E"};
    // v1.0.289 (Alexis) : labels relatifs a la machine, plus intuitifs.
    //   Avant : B/H/N/S/O/E (conventions cardinales)
    //   Apres : Ba/H/Ar/Av/Ga/Dr (relatifs a la machine)
    // Voir GuiFurnaceNexus pour le mapping detaille EnumFacing <-> label.
    private static final String[] FACE_LABELS = {"Ba", "H", "Ar", "Av", "Ga", "Dr"};
    private static final String[] FACE_NAMES = {
        "Bas", "Haut", "Arriere", "Avant", "Gauche", "Droite"
    };

    public GuiConvertisseur(InventoryPlayer playerInv, TileConvertisseur tile) {
        super(new ContainerConvertisseur(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 176;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft, y = guiTop;

        // === ENERGY BAR FILL ===
        int stored = tile.getEnergyStored();
        int max = tile.getMaxEnergyStored();
        int barH = 66;
        if (max > 0 && stored > 0) {
            float ratio = (float) stored / max;
            int fillH = (int)(barH * ratio);
            for (int fy = 0; fy < fillH; fy++) {
                float t = (float) fy / barH;
                int r = (int)(180 + 75 * t);
                int g = (int)(20 * t);
                int c = 0xFF000000 | (Math.min(r, 255) << 16) | (g << 8) | 20;
                drawRect(x + 151, y + 83 - fy, x + 165, y + 84 - fy, c);
            }
        }

        // === COMPOSE FACE INDICATORS ===
        int[] faceData = tile.getFaceData();
        for (int i = 0; i < 6; i++) {
            int sx = x + 10 + i * 17;
            int sy = y + 60;
            int tier = faceData[i];
            int color = (tier >= 0 && tier <= 5) ? TIER_COLORS[tier] : TIER_COLORS[0];
            drawRect(sx, sy, sx + 13, sy + 13, color);
        }

        // === CONFIG TAB (right edge) ===
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(x + xSize - 2, y + 18, 200, 0, 15, 17);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Title
        String title = "Convertisseur du Dr. Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 5, 0xDD88FF);

        // RF info
        int rfTick = tile.getCurrentRFPerTick();
        fontRenderer.drawStringWithShadow(rfTick + " RF/t", 10, 20, 0xFFAA00);
        fontRenderer.drawStringWithShadow(
            formatNum(tile.getEnergyStored()) + " / " + formatNum(tile.getMaxEnergyStored()) + " RF",
            10, 32, 0x9977BB);

        // Compose count
        fontRenderer.drawStringWithShadow("Composes: " + tile.getComposeCount() + "/6",
            10, 48, 0x8866AA);

        // Face letters on indicators
        int[] faceData = tile.getFaceData();
        for (int i = 0; i < 6; i++) {
            int tier = faceData[i];
            String letter = tier > 0 ? TIER_NAMES[tier] : FACE_LABELS[i];
            int lColor = tier > 0 ? 0xFFFFFF : 0x554466;
            int lw = fontRenderer.getStringWidth(letter);
            fontRenderer.drawStringWithShadow(letter,
                10 + i * 17 + (13 - lw) / 2.0F, 63, lColor);
        }

        // Energy label
        fontRenderer.drawStringWithShadow("RF", 153, 86, 0xAA6666);

        // Inventory label
        fontRenderer.drawStringWithShadow("Inventaire", 8, 84, 0x8866AA);

        // Config tab
        fontRenderer.drawStringWithShadow("*", xSize + 2, 22,
            configOpen ? 0xFFDD88FF : 0xFFCCCCCC);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);

        // Config panel (over JEI)
        if (configOpen) drawConfigPanel(mx, my);

        // Energy tooltip
        int x = guiLeft, y = guiTop;
        if (mx>=x+149 && mx<=x+167 && my>=y+17 && my<=y+85)
            drawHoveringText(Collections.singletonList(
                tile.getEnergyStored()+" / "+tile.getMaxEnergyStored()+" RF"), mx, my);
    }

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft + xSize + 4;
        int py = guiTop + 10;
        int pw = 100;
        int ph = 108;

        drawRect(px-2, py-2, px+pw+2, py+ph+2, 0xFF3A1F5E);
        drawRect(px-1, py-1, px+pw+1, py+ph+1, 0xFF8855BB);
        drawRect(px, py, px+pw, py+ph, 0xFF1A1030);
        drawRect(px+1, py+1, px+pw-1, py+14, 0xFF261440);
        fontRenderer.drawStringWithShadow("\u00a7d\u2699 Configuration", px+5, py+3, 0xDD88FF);
        drawRect(px+3, py+14, px+pw-3, py+15, 0xFF6B3FA0);

        // Cross layout buttons
        int bs = 24, bg = 2;
        int cx = px + (pw-(bs*3+bg*2))/2;
        int cy = py + 20;
        drawOutputBtn(cx+bs+bg, cy, bs, 1, mx, my);        // UP
        drawOutputBtn(cx, cy+bs+bg, bs, 4, mx, my);        // WEST (Ga)
        // v1.0.293 (Alexis) : alignement sur layout four (coherence UX multi-machines).
        //   Avant : NORTH centre + SOUTH bas-centre + DOWN bas-gauche
        //   Apres : SOUTH centre + NORTH bas-gauche + DOWN bas-centre
        drawOutputBtn(cx+bs+bg, cy+bs+bg, bs, 3, mx, my);  // SOUTH (Av, centre/front)
        drawOutputBtn(cx+(bs+bg)*2, cy+bs+bg, bs, 5, mx, my); // EAST (Dr)
        drawOutputBtn(cx, cy+(bs+bg)*2, bs, 2, mx, my);    // NORTH (Ar, bas-gauche)
        drawOutputBtn(cx+bs+bg, cy+(bs+bg)*2, bs, 0, mx, my); // DOWN (Ba, bas-centre)

        // Tooltips (ordre aligne sur drawOutputBtn : SOUTH centre, NORTH bas-gauche, DOWN bas-centre)
        int[][] btns = {
            {1,cx+bs+bg,cy}, {4,cx,cy+bs+bg},
            {3,cx+bs+bg,cy+bs+bg}, {5,cx+(bs+bg)*2,cy+bs+bg},
            {2,cx,cy+(bs+bg)*2}, {0,cx+bs+bg,cy+(bs+bg)*2}
        };
        for (int[] b : btns) {
            if (mx>=b[1] && mx<=b[1]+bs && my>=b[2] && my<=b[2]+bs) {
                String state = tile.isOutputFace(b[0]) ? "\u00a7aON" : "\u00a7cOFF";
                drawHoveringText(Collections.singletonList(
                    FACE_NAMES[b[0]]+": "+state), mx, my);
            }
        }
    }

    private void drawOutputBtn(int bx, int by, int sz, int face, int mx, int my) {
        boolean on = tile.isOutputFace(face);
        boolean hov = mx>=bx && mx<=bx+sz && my>=by && my<=by+sz;
        int bl = hov ? 0xFFAA77DD : 0xFF8855BB;
        drawRect(bx-1, by-1, bx+sz+1, by, bl);
        drawRect(bx-1, by-1, bx, by+sz+1, bl);
        drawRect(bx, by+sz, bx+sz+1, by+sz+1, 0xFF2A1540);
        drawRect(bx+sz, by, bx+sz+1, by+sz+1, 0xFF2A1540);
        if (on) {
            drawRect(bx, by, bx+sz, by+sz, 0xFF994D00);
            drawRect(bx+1, by+1, bx+sz-1, by+sz-1, 0xFFE67300);
            drawRect(bx+2, by+2, bx+sz-2, by+sz-2, 0xFFFF8811);
            drawRect(bx+3, by+3, bx+sz-3, by+sz-3, 0xFFE67300);
        } else {
            drawRect(bx, by, bx+sz, by+sz, 0xFF0E0818);
            drawRect(bx+1, by+1, bx+sz-1, by+sz-1, 0xFF1A1030);
            drawRect(bx+2, by+2, bx+sz-2, by+sz-2, 0xFF221540);
        }
        String lbl = FACE_LABELS[face];
        int lw = fontRenderer.getStringWidth(lbl);
        fontRenderer.drawStringWithShadow(lbl,
            bx+(sz-lw)/2.0F, by+(sz-8)/2.0F,
            on ? 0xFFFFFF : 0xFF554466);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int x = guiLeft, y = guiTop;

        // Config tab
        if (mx>=x+xSize-2 && mx<=x+xSize+12 && my>=y+18 && my<=y+34) {
            configOpen = !configOpen; return;
        }

        if (configOpen) {
            int px = x+xSize+4, py = y+10, pw = 100;
            int bs = 24, bg = 2;
            int cx = px+(pw-(bs*3+bg*2))/2, cy = py+20;
            int[][] btns = {
                {1,cx+bs+bg,cy}, {4,cx,cy+bs+bg},
                {3,cx+bs+bg,cy+bs+bg}, {5,cx+(bs+bg)*2,cy+bs+bg},
                {2,cx,cy+(bs+bg)*2}, {0,cx+bs+bg,cy+(bs+bg)*2}
            };
            for (int[] b : btns) {
                if (mx>=b[1] && mx<=b[1]+bs && my>=b[2] && my<=b[2]+bs) {
                    mc.playerController.sendEnchantPacket(
                        inventorySlots.windowId, b[0]);
                    return;
                }
            }
        }
        super.mouseClicked(mx, my, btn);
    }

    private String formatNum(int n) {
        if (n >= 1000000) return String.format("%.1fM", n / 1000000.0);
        if (n >= 1000) return String.format("%.1fK", n / 1000.0);
        return String.valueOf(n);
    }
}
