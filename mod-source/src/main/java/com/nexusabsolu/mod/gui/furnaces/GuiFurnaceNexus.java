package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.SideConfig;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * GUI Furnaces Nexus v5 - pattern Convertisseur du Dr Voss.
 *
 *  - Tout visible en permanence (pas d'onglets)
 *  - Upgrades en CARRE 2x2 a droite
 *  - Petit onglet * sur le bord droit pour ouvrir/fermer un panneau Config I/O
 *  - Panneau Config I/O apparait a cote du GUI a droite (ne masque pas l'inventaire)
 *
 * Layout (xSize=176, ySize=166) :
 *   INPUT slot    (55, 16)  18x18
 *   FUEL slot     (55, 52)  18x18
 *   PROGRESS zone (74, 38)  20x10
 *   OUTPUT slot   (96, 30)  26x26
 *   UPGRADE 2x2 at positions (126,16) (144,16) (126,34) (144,34)
 *   RF BAR horizontale (126, 58) 36x7
 *   Config tab cliquable a (xSize-2, 18) qui depasse a droite
 */
public class GuiFurnaceNexus extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_furnace.png");

    private final TileFurnaceNexus tile;
    private boolean configOpen = false;

    // Couleurs Mekanism pour config faces (None/In/Out/Both + Fuel special)
    private static final int COL_NONE = 0xFF333338;
    private static final int COL_IN = 0xFF3375BB;
    private static final int COL_OUT = 0xFFD87C2B;
    private static final int COL_BOTH = 0xFF9B3FBA;
    private static final int COL_FUEL = 0xFFB08830;
    private static final int COL_BORDER = 0xFF8855BB;
    private static final int COL_BORDER_HOV = 0xFFCC88EE;

    private static final String[] FACE_LABELS = {"B", "H", "N", "S", "O", "E"};
    private static final String[] FACE_NAMES = {
        "Bas", "Haut", "Nord", "Sud", "Ouest", "Est"
    };

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    // ======================================================================
    // BACKGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft;
        int y = guiTop;

        // === BARRE RF horizontale (sous les upgrades) ===
        // Zone dessinee : (126, 58) a (161, 64) soit 36x7 max fill
        fillBarHorizontal(x + 127, y + 59, 34, 5,
            tile.getEnergyStored(), tile.getMaxEnergy(),
            0xFFCC4444, 0xFFFF6666);

        // === INDICATEUR FLAMME (dessine DANS le slot FUEL en overlay) ===
        // Quand du fuel est actif, on dessine une petite flamme orange dans le slot
        // a (55, 52). On la superpose mais on la met transparente au centre
        // pour ne pas cacher l'item fuel.
        // Actually : on dessine un petit indicateur 4x4 en coin du slot fuel.
        int fuel = tile.getFuelRemaining();
        boolean rfActive = tile.getEnergyStored() > 0 && tile.getCookProgress() > 0;
        if (fuel > 0 || rfActive) {
            // Indicateur en haut-gauche du slot fuel (toujours visible meme avec item)
            int flameX = x + 55;
            int flameY = y + 52;
            if (fuel > 0) {
                // Flamme orange
                drawRect(flameX, flameY, flameX + 3, flameY + 3, 0xFFFF8830);
                drawRect(flameX + 1, flameY + 1, flameX + 2, flameY + 2, 0xFFFFDD40);
            } else {
                // Mode RF : flamme bleue
                drawRect(flameX, flameY, flameX + 3, flameY + 3, 0xFF7788FF);
            }
        }

        // === PROGRESS fleche qui avance ===
        // Zone : (74, 38) a (93, 47) = 20x10
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(20.0F * prog / maxP);
            int tierCol = getTierProgressColor(tile.getTier());
            int tierBright = getTierProgressBright(tile.getTier());
            drawRect(x + 74, y + 41, x + 74 + fillW, y + 45, tierCol);
            if (fillW > 2) {
                drawRect(x + 74, y + 41, x + 74 + fillW, y + 42, tierBright);
            }
        }

        // === ONGLET CONFIG * a droite (depasse du GUI) ===
        // Sprite a (176, 0) dans la texture, 15x17, blitte a (x + xSize - 2, y + 18)
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(x + xSize - 2, y + 18, 176, 0, 15, 17);
    }

    // ======================================================================
    // FOREGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        FurnaceTier tier = tile.getTier();

        // Titre centre (garde la logique tier-colored)
        String title = getTierDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = getTierTitleColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 4, titleColor);

        // Vitesse x sous le progress arrow
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawStringWithShadow(speedStr, 74, 50, 0xFF8866AA);

        // Label "Inventaire"
        fontRenderer.drawStringWithShadow("Inventaire", 8, 83, 0xFF8866AA);

        // Arrows decoratives autour de progress
        fontRenderer.drawStringWithShadow("\u00bb", 66, 40, titleColor);
        fontRenderer.drawStringWithShadow("\u00bb", 94, 40, titleColor);

        // Marker visuel sur l'onglet config quand ouvert
        fontRenderer.drawStringWithShadow(configOpen ? "\u00A7d*" : "\u00A78*",
            xSize + 3, 24, 0xFFFFFFFF);
    }

    // ======================================================================
    // drawScreen : tooltips + config panel
    // ======================================================================

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        drawCustomTooltips(mx, my);
        if (configOpen) drawConfigPanel(mx, my);
    }

    private void drawCustomTooltips(int mx, int my) {
        int x = guiLeft;
        int y = guiTop;

        // RF bar tooltip (zone 126-161, 58-64)
        if (inRect(mx, my, x + 126, y + 58, 36, 7)) {
            drawHoveringText(Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"), mx, my);
        }

        // Progress bar tooltip
        if (inRect(mx, my, x + 74, y + 38, 20, 10)) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(Collections.singletonList(
                "Cuisson: " + pct + "%"), mx, my);
        }

        // Config tab tooltip
        if (inRect(mx, my, x + xSize - 2, y + 18, 15, 17)) {
            drawHoveringText(Collections.singletonList(
                configOpen ? "Fermer Config I/O" : "Ouvrir Config I/O"), mx, my);
        }

        // Tooltips slots upgrades vides (4 positions 2x2)
        int[][] upgradePos = {
            {126, 16}, {144, 16}, {126, 34}, {144, 34}
        };
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            int[] pos = upgradePos[up.slotIndex];
            int sx = x + pos[0], sy = y + pos[1];
            if (inRect(mx, my, sx, sy, 18, 18)) {
                int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
                if (tile.getStackInSlot(slotIdx).isEmpty()) {
                    drawHoveringText(Arrays.asList(
                        "\u00A7b" + getUpgradeLabel(up),
                        "\u00A77" + getUpgradeHint(up),
                        "\u00A78Max stack: " + up.maxStackSize
                    ), mx, my);
                }
            }
        }
    }

    // ======================================================================
    // CONFIG PANEL (apparait a droite du GUI quand configOpen)
    // ======================================================================

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft + xSize + 12;  // decale de 12 pour laisser place a l'onglet
        int py = guiTop + 10;
        int pw = 110;
        int ph = 135;

        // Fond du panneau
        drawRect(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFF3A1F5E);
        drawRect(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF8855BB);
        drawRect(px, py, px + pw, py + ph, 0xFF1A1030);
        // Barre titre
        drawRect(px + 1, py + 1, px + pw - 1, py + 14, 0xFF261440);
        fontRenderer.drawStringWithShadow("\u00A7d\u2699 Config I/O", px + 5, py + 3, 0xFFDD88FF);
        drawRect(px + 3, py + 14, px + pw - 3, py + 15, 0xFF6B3FA0);

        // Boutons face en croix
        SideConfig sc = tile.getSideConfig();
        int bs = 24, bg = 2;
        int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
        int cy = py + 20;

        drawFaceBtn(cx + bs + bg, cy, bs, 1, sc, mx, my);                 // UP
        drawFaceBtn(cx, cy + bs + bg, bs, 4, sc, mx, my);                 // WEST
        drawFaceBtn(cx + bs + bg, cy + bs + bg, bs, 3, sc, mx, my);       // SOUTH (front)
        drawFaceBtn(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, sc, mx, my); // EAST
        drawFaceBtn(cx, cy + (bs + bg) * 2, bs, 0, sc, mx, my);           // DOWN
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 2, bs, 2, sc, mx, my); // NORTH

        // Legende sous la croix
        int legY = cy + (bs + bg) * 3 + 4;
        fontRenderer.drawString("Clic: None/In/Out/Both", px + 4, legY, 0xFF9988BB);
        fontRenderer.drawString("Maj+Clic: Fuel IN", px + 4, legY + 10, 0xFF9988BB);

        // Legende couleurs
        int legCY = legY + 22;
        drawRect(px + 4, legCY, px + 10, legCY + 6, COL_IN);
        fontRenderer.drawString("In", px + 13, legCY - 1, 0xFF66AADD);
        drawRect(px + 34, legCY, px + 40, legCY + 6, COL_OUT);
        fontRenderer.drawString("Out", px + 43, legCY - 1, 0xFFEE9955);
        drawRect(px + 4, legCY + 10, px + 10, legCY + 16, COL_BOTH);
        fontRenderer.drawString("Both", px + 13, legCY + 9, 0xFFCC88EE);
        drawRect(px + 34, legCY + 10, px + 40, legCY + 16, COL_FUEL);
        fontRenderer.drawString("Fuel", px + 43, legCY + 9, 0xFFEEAA44);

        // Tooltips faces en hover
        int[][] btns = {
            {1, cx + bs + bg, cy},
            {4, cx, cy + bs + bg},
            {3, cx + bs + bg, cy + bs + bg},
            {5, cx + (bs + bg) * 2, cy + bs + bg},
            {0, cx, cy + (bs + bg) * 2},
            {2, cx + bs + bg, cy + (bs + bg) * 2}
        };
        for (int[] b : btns) {
            int face = b[0], bx = b[1], by = b[2];
            if (mx >= bx && mx <= bx + bs && my >= by && my <= by + bs) {
                boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
                boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
                boolean fuelF = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
                String status;
                if (fuelF) status = "\u00A76Fuel IN";
                else if (in && out) status = "\u00A7dInput + Output";
                else if (in) status = "\u00A79Input";
                else if (out) status = "\u00A76Output";
                else status = "\u00A77Aucun";
                drawHoveringText(Arrays.asList(
                    FACE_NAMES[face],
                    status
                ), mx, my);
            }
        }
    }

    private void drawFaceBtn(int bx, int by, int sz, int face,
                              SideConfig sc, int mx, int my) {
        boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
        boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
        boolean fuelF = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
        boolean hov = mx >= bx && mx <= bx + sz && my >= by && my <= by + sz;

        int color;
        if (fuelF) color = COL_FUEL;
        else if (in && out) color = COL_BOTH;
        else if (in) color = COL_IN;
        else if (out) color = COL_OUT;
        else color = COL_NONE;

        int border = hov ? COL_BORDER_HOV : COL_BORDER;

        // Bordure (haut-gauche clair)
        drawRect(bx - 1, by - 1, bx + sz + 1, by, border);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, border);
        // Bordure (bas-droite sombre)
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);

        // Corps degrade
        drawRect(bx, by, bx + sz, by + sz, color);
        // Leger highlight interne
        int colorBright = brighten(color, 0.3f);
        drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, colorBright);
        drawRect(bx + 4, by + 4, bx + sz - 4, by + sz - 4, color);

        // Label face (centre)
        int lw = fontRenderer.getStringWidth(FACE_LABELS[face]);
        fontRenderer.drawStringWithShadow(FACE_LABELS[face],
            bx + (sz - lw) / 2.0F, by + (sz - 8) / 2.0F, 0xFFFFFFFF);
    }

    // ======================================================================
    // SOURIS
    // ======================================================================

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int x = guiLeft;
        int y = guiTop;

        // 1. Clic sur onglet Config (depasse a droite du GUI)
        if (mx >= x + xSize - 2 && mx <= x + xSize + 13
            && my >= y + 18 && my <= y + 35) {
            configOpen = !configOpen;
            return;
        }

        // 2. Si config ouverte, check clics sur boutons face
        if (configOpen) {
            int px = x + xSize + 12;
            int py = y + 10;
            int pw = 110;
            int bs = 24, bg = 2;
            int cx = px + (pw - (bs * 3 + bg * 2)) / 2;
            int cy = py + 20;
            int[][] btns = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {3, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {0, cx, cy + (bs + bg) * 2},
                {2, cx + bs + bg, cy + (bs + bg) * 2}
            };
            for (int[] b : btns) {
                int face = b[0], bx = b[1], by = b[2];
                if (mx >= bx && mx <= bx + bs && my >= by && my <= by + bs) {
                    SideConfig sc = tile.getSideConfig();
                    if (isShiftKeyDown()) {
                        // Toggle FUEL_IN
                        sc.toggleFace(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
                        int id = TileFurnaceNexus.SC_TYPE_FUEL_IN * 6 + face;
                        mc.playerController.sendEnchantPacket(
                            inventorySlots.windowId, id);
                    } else {
                        // Cycle None -> In -> Out -> Both -> None
                        boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
                        boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
                        int curState = (in ? 1 : 0) | (out ? 2 : 0);
                        int nextState = (curState + 1) % 4;
                        boolean newIn = (nextState & 1) != 0;
                        boolean newOut = (nextState & 2) != 0;
                        sc.setFace(TileFurnaceNexus.SC_TYPE_ITEM_IN, face, newIn);
                        sc.setFace(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face, newOut);
                        if (newIn != in) {
                            mc.playerController.sendEnchantPacket(
                                inventorySlots.windowId,
                                TileFurnaceNexus.SC_TYPE_ITEM_IN * 6 + face);
                        }
                        if (newOut != out) {
                            mc.playerController.sendEnchantPacket(
                                inventorySlots.windowId,
                                TileFurnaceNexus.SC_TYPE_ITEM_OUT * 6 + face);
                        }
                    }
                    return;
                }
            }
        }

        super.mouseClicked(mx, my, btn);
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private void fillBarHorizontal(int bx, int by, int bw, int bh,
                                    int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillW = (int)(bw * ratio);
        drawRect(bx, by, bx + fillW, by + bh, color);
        if (fillW > 2) {
            drawRect(bx, by, bx + fillW, by + 1, shine);
        }
    }

    private boolean inRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    private int brighten(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) + (255 - ((color >> 16) & 0xFF)) * factor);
        int g = (int)(((color >> 8) & 0xFF) + (255 - ((color >> 8) & 0xFF)) * factor);
        int b = (int)((color & 0xFF) + (255 - (color & 0xFF)) * factor);
        return 0xFF000000 | (Math.min(255, r) << 16) | (Math.min(255, g) << 8) | Math.min(255, b);
    }

    private String getTierDisplayName(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return "Fourneau de Fer";
            case GOLD:       return "Fourneau d'Or";
            case INVAR:      return "Fourneau d'Invar";
            case EMERADIC:   return "Fourneau Emeradic";
            case VOSSIUM_IV: return "Fourneau Vossium IV";
            default:         return "Fourneau " + tier.registryName;
        }
    }

    private int getTierTitleColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFFCCCCCC;
            case GOLD:       return 0xFFFFDD60;
            case INVAR:      return 0xFFBBDDBB;
            case EMERADIC:   return 0xFF80E690;
            case VOSSIUM_IV: return 0xFFC070FF;
            default:         return 0xFFDD88FF;
        }
    }

    private int getTierProgressColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFF808090;
            case GOLD:       return 0xFFC8A032;
            case INVAR:      return 0xFF96A596;
            case EMERADIC:   return 0xFF46AA5A;
            case VOSSIUM_IV: return 0xFF783CAA;
            default:         return 0xFF5050A0;
        }
    }

    private int getTierProgressBright(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFFDDDDEE;
            case GOLD:       return 0xFFFFE680;
            case INVAR:      return 0xFFDDEEDD;
            case EMERADIC:   return 0xFF80FF90;
            case VOSSIUM_IV: return 0xFFCC80FF;
            default:         return 0xFF9090FF;
        }
    }

    private String getUpgradeLabel(FurnaceUpgrade up) {
        switch (up) {
            case RF_CONVERTER:  return "Convertisseur RF";
            case IO_EXPANSION:  return "Extension I/O";
            case SPEED_BOOSTER: return "Accelerateur";
            case EFFICIENCY:    return "Carte Efficience";
            default:            return up.registrySuffix;
        }
    }

    private String getUpgradeHint(FurnaceUpgrade up) {
        switch (up) {
            case RF_CONVERTER:  return "Coal -> RF, +5% vitesse";
            case IO_EXPANSION:  return "Plus de slots (RF requis)";
            case SPEED_BOOSTER: return "+30% vitesse, +40% conso /stack";
            case EFFICIENCY:    return "-8% conso /stack";
            default:            return "";
        }
    }
}
