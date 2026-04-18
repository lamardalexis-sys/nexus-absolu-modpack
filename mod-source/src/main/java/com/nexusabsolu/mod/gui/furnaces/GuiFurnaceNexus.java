package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.SideConfig;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * GUI Furnaces Nexus - v4 avec 3 onglets en haut (MAIN, CONFIG I/O, UPGRADES).
 *
 * Pattern KRDA + extension multi-onglets:
 *   - Onglets dessines au-dessus du GUI (depassent de 20px au-dessus)
 *   - Clic sur onglet = switch activeTab
 *   - Selon activeTab: rendu du panneau principal OU config OU upgrades
 *   - Slots upgrades sont "caches hors-ecran" quand activeTab != UPGRADES
 *
 * Positions onglets (dans le sprite sheet):
 *   MAIN    (inactive: 176,0 / active: 176,28)
 *   CONFIG  (inactive: 206,0 / active: 206,28)
 *   UPGRADE (inactive: 176,56 / active: 206,56)
 */
public class GuiFurnaceNexus extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_furnace.png");

    private final TileFurnaceNexus tile;

    // Onglet actif (0=main, 1=config, 2=upgrades)
    private int activeTab = 0;
    private static final int TAB_MAIN = 0;
    private static final int TAB_CONFIG = 1;
    private static final int TAB_UPGRADES = 2;

    // Constantes layout onglets (dimensions + positions relatives au GUI)
    private static final int TAB_W = 30;
    private static final int TAB_H = 25;
    private static final int TAB_Y_ABOVE = -22;  // les onglets depassent au-dessus du GUI
    // Positions X absolues des onglets (dans le GUI, relatives au guiLeft)
    private static final int[] TAB_X = {6, 40, 74};  // main, config, upgrades

    // Positions dans le sprite sheet (U,V) par tab et par etat
    // [tab][active(0)/inactive(1)] = {u, v}
    private static final int[][][] TAB_UV = {
        {{176, 28}, {176, 0}},    // MAIN: active @ (176,28), inactive @ (176,0)
        {{206, 28}, {206, 0}},    // CONFIG
        {{206, 56}, {176, 56}},   // UPGRADES
    };

    // Couleurs Mekanism pour config faces (NONE/IN/OUT/BOTH)
    private static final int COL_NONE = 0xFF333338;       // gris sombre
    private static final int COL_IN = 0xFF3375BB;         // bleu
    private static final int COL_OUT = 0xFFD87C2B;        // orange
    private static final int COL_BOTH = 0xFF9B3FBA;       // violet
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
    // BACKGROUND - rendu conditionnel selon activeTab
    // ======================================================================

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        // Re-positionne les slots upgrades selon l'onglet actif
        updateUpgradeSlotPositions();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);

        // Fond principal (toujours dessine, quel que soit l'onglet)
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // === ONGLETS EN HAUT (dessine avant les autres couches) ===
        drawTabs();

        // === CONTENU SELON L'ONGLET ACTIF ===
        int x = guiLeft, y = guiTop;
        switch (activeTab) {
            case TAB_MAIN:
                drawMainPanel(x, y);
                break;
            case TAB_CONFIG:
                drawConfigPanel(x, y, mx, my);
                break;
            case TAB_UPGRADES:
                drawUpgradesPanel(x, y);
                break;
        }
    }

    /** Dessine les 3 onglets au-dessus du GUI. */
    private void drawTabs() {
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int tabY = guiTop + TAB_Y_ABOVE;
        for (int t = 0; t < 3; t++) {
            int activeIdx = (t == activeTab) ? 0 : 1;
            int u = TAB_UV[t][activeIdx][0];
            int v = TAB_UV[t][activeIdx][1];
            drawTexturedModalRect(guiLeft + TAB_X[t], tabY, u, v, TAB_W, TAB_H + 3);
        }
    }

    /** Rendu panneau principal (barres, progress, flamme) - identique v1.0.184. */
    private void drawMainPanel(int x, int y) {
        // Barre RF
        fillBarVertical(x + 152, y + 15, 6, 58,
            tile.getEnergyStored(), tile.getMaxEnergy(),
            0xFFCC4444, 0xFFFF6666);

        // Flamme fuel
        int fuel = tile.getFuelRemaining();
        int fuelMax = Math.max(1, fuel);
        if (fuel > 0) {
            fillBarVertical(x + 79, y + 37, 12, 13, fuel, fuelMax,
                0xFFCC3D10, 0xFFFF8830);
        } else if (tile.getEnergyStored() > 0 && tile.getCookProgress() > 0) {
            fillBarVertical(x + 79, y + 37, 12, 13, 1, 1,
                0xFF4455CC, 0xFF7788FF);
        }

        // Progress arrow
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(20.0F * prog / maxP);
            int tierCol = getTierProgressColor(tile.getTier());
            int tierBright = getTierProgressBright(tile.getTier());
            drawRect(x + 94, y + 41, x + 94 + fillW, y + 45, tierCol);
            if (fillW > 2) {
                drawRect(x + 94, y + 41, x + 94 + fillW, y + 42, tierBright);
            }
        }
    }

    /** Rendu panneau Config I/O (cube 3D 6 faces + legende). */
    private void drawConfigPanel(int x, int y, int mx, int my) {
        // Zone dessinee PAR-DESSUS le panneau machine (qui reste visible derriere)
        // On couvre la zone machine (4,4 -> W-5, 78) avec un fond assombri
        drawRect(x + 4, y + 4, x + xSize - 4, y + 78, 0xE01A0530);

        // Titre
        fontRenderer.drawStringWithShadow("Configuration I/O", x + 8, y + 8, 0xFFDD88FF);

        // Cube 3D : 6 faces en croix style Mekanism
        SideConfig sc = tile.getSideConfig();
        int bs = 18;  // taille d'une face
        int bg = 2;   // gap entre faces
        int cx = x + xSize / 2 - (bs + bg);  // centre du cube approx
        int cy = y + 24;

        // Face HAUT (up, face index 1)
        drawFaceBtn(cx + bs + bg, cy, bs, 1, sc, mx, my);
        // Face OUEST (W, face 4) | face SUD (S, face 3 = front) | face EST (E, face 5)
        drawFaceBtn(cx, cy + bs + bg, bs, 4, sc, mx, my);
        drawFaceBtn(cx + bs + bg, cy + bs + bg, bs, 3, sc, mx, my);
        drawFaceBtn(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, sc, mx, my);
        // Face NORD (N, face 2 = back) en dessous de sud
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 2, bs, 2, sc, mx, my);
        // Face BAS (D, face 0) sous NORD
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 3, bs, 0, sc, mx, my);

        // Legende couleurs (a droite du cube)
        int legX = x + 120, legY = y + 24;
        fontRenderer.drawStringWithShadow("Legende:", legX, legY, 0xFFBBAADD);
        drawRect(legX, legY + 12, legX + 10, legY + 20, COL_NONE);
        fontRenderer.drawString("Aucun", legX + 14, legY + 12, 0xFF888899);
        drawRect(legX, legY + 22, legX + 10, legY + 30, COL_IN);
        fontRenderer.drawString("Input", legX + 14, legY + 22, 0xFF66AADD);
        drawRect(legX, legY + 32, legX + 10, legY + 40, COL_OUT);
        fontRenderer.drawString("Output", legX + 14, legY + 32, 0xFFEE9955);
        drawRect(legX, legY + 42, legX + 10, legY + 50, COL_BOTH);
        fontRenderer.drawString("Input+Out", legX + 14, legY + 42, 0xFFCC88EE);
    }

    /**
     * Dessine un bouton de face. Clic cycle None -> In -> Out -> Both -> None.
     * Pour l'affichage on combine ITEM_IN et ITEM_OUT pour connaitre la couleur.
     */
    private void drawFaceBtn(int bx, int by, int sz, int face,
                              SideConfig sc, int mx, int my) {
        boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
        boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
        boolean fuel = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
        boolean hov = mx >= bx && mx <= bx + sz && my >= by && my <= by + sz;

        int color;
        if (fuel) {
            // Fuel input uses distinct color brown/gold
            color = 0xFFB08830;
        } else if (in && out) {
            color = COL_BOTH;
        } else if (in) {
            color = COL_IN;
        } else if (out) {
            color = COL_OUT;
        } else {
            color = COL_NONE;
        }

        int border = hov ? COL_BORDER_HOV : COL_BORDER;
        // Bordure
        drawRect(bx - 1, by - 1, bx + sz + 1, by, border);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, border);
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);

        // Corps
        drawRect(bx, by, bx + sz, by + sz, color);

        // Label face (centre)
        int lw = fontRenderer.getStringWidth(FACE_LABELS[face]);
        fontRenderer.drawStringWithShadow(FACE_LABELS[face],
            bx + (sz - lw) / 2.0F, by + (sz - 8) / 2.0F, 0xFFFFFFFF);
    }

    /** Rendu panneau Upgrades (les 4 slots upgrade en grand au centre). */
    private void drawUpgradesPanel(int x, int y) {
        // Assombrir le panneau machine
        drawRect(x + 4, y + 4, x + xSize - 4, y + 78, 0xE01A0530);

        // Titre
        fontRenderer.drawStringWithShadow("Upgrades", x + 8, y + 8, 0xFFDD88FF);

        // Les slots upgrades sont dessines automatiquement par GuiContainer
        // (via Container.inventorySlots) - mais on les repositionne dans
        // updateUpgradeSlotPositions() pour qu'ils soient au centre ici.
        //
        // On dessine juste les 4 cadres de slot pour visuel coherent
        int slotStartX = x + (xSize - 18 * 4 - 3 * 4) / 2;  // centre horizontalement
        int slotY = y + 30;

        for (int i = 0; i < 4; i++) {
            int sx = slotStartX + i * 22;
            drawRect(sx, slotY, sx + 18, slotY + 18, 0xFF0A0818);
            drawRect(sx + 1, slotY + 1, sx + 17, slotY + 17, 0xFF1A1030);
            // Bordure violet Nexus
            drawRect(sx, slotY, sx + 18, slotY + 1, COL_BORDER);
            drawRect(sx, slotY, sx + 1, slotY + 18, COL_BORDER);
            drawRect(sx, slotY + 17, sx + 18, slotY + 18, COL_BORDER);
            drawRect(sx + 17, slotY, sx + 18, slotY + 18, COL_BORDER);
        }

        // Labels sous les slots
        String[] labels = {"RF", "I/O", "SPD", "EFF"};
        for (int i = 0; i < 4; i++) {
            int sx = slotStartX + i * 22;
            int lw = fontRenderer.getStringWidth(labels[i]);
            fontRenderer.drawStringWithShadow(labels[i],
                sx + (18 - lw) / 2.0F, slotY + 20, 0xFF8866AA);
        }
    }

    /**
     * Repositionne les 4 slots upgrade dans le Container selon activeTab.
     * Si onglet UPGRADES : slots visibles au centre.
     * Sinon : hors-ecran (x=-1000) pour cacher.
     */
    private void updateUpgradeSlotPositions() {
        // Slots upgrade = indices 3-6 dans inventorySlots (apres 0 input, 1 fuel, 2 output)
        int slotStartX = (xSize - 18 * 4 - 3 * 4) / 2 + 1;  // centre
        int slotY = 30;
        for (int i = 0; i < 4; i++) {
            Slot slot = inventorySlots.inventorySlots.get(3 + i);
            if (activeTab == TAB_UPGRADES) {
                slot.xPos = slotStartX + i * 22;
                slot.yPos = slotY;
            } else {
                // Hors-ecran : empeche le clic et le rendu
                slot.xPos = -1000;
                slot.yPos = -1000;
            }
        }
    }

    // ======================================================================
    // FOREGROUND (titre + labels selon onglet)
    // ======================================================================

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        FurnaceTier tier = tile.getTier();

        // Titre (toujours visible, peu importe l'onglet)
        String title = getTierDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = getTierTitleColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 4, titleColor);

        // Label "Inventaire" (toujours en bas)
        fontRenderer.drawStringWithShadow("Inventaire", 8, 83, 0xFF8866AA);

        // Elements specifiques au panneau MAIN
        if (activeTab == TAB_MAIN) {
            fontRenderer.drawStringWithShadow("E", 151, 6, 0xFFFF6666);
            String speedStr = "x" + tier.speedMultiplier;
            fontRenderer.drawStringWithShadow(speedStr, 96, 52, 0xFF8866AA);
        }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        drawCustomTooltips(mx, my);
    }

    private void drawCustomTooltips(int mx, int my) {
        int x = guiLeft;
        int y = guiTop;

        // Tooltips des onglets
        int tabY = y + TAB_Y_ABOVE;
        String[] tabNames = {"Principal", "Config I/O", "Upgrades"};
        for (int t = 0; t < 3; t++) {
            if (inRect(mx, my, x + TAB_X[t], tabY, TAB_W, TAB_H + 3)) {
                drawHoveringText(Collections.singletonList(tabNames[t]), mx, my);
                return;  // un seul tooltip suffit
            }
        }

        // Tooltips panneau MAIN
        if (activeTab == TAB_MAIN) {
            if (inRect(mx, my, x + 152, y + 15, 6, 58)) {
                drawHoveringText(Collections.singletonList(
                    tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"), mx, my);
            }
            if (inRect(mx, my, x + 79, y + 37, 12, 13)) {
                String status = tile.getFuelRemaining() > 0
                    ? tile.getFuelRemaining() + " operations"
                    : (tile.getEnergyStored() > 0 ? "Mode RF" : "Vide");
                drawHoveringText(Collections.singletonList(
                    "Combustible: " + status), mx, my);
            }
            if (inRect(mx, my, x + 94, y + 38, 20, 10)) {
                int pct = tile.getMaxCookTime() > 0
                    ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
                drawHoveringText(Collections.singletonList(
                    "Cuisson: " + pct + "%"), mx, my);
            }
        }

        // Tooltips panneau CONFIG (boutons des faces)
        if (activeTab == TAB_CONFIG) {
            SideConfig sc = tile.getSideConfig();
            int bs = 18, bg = 2;
            int cx = x + xSize / 2 - (bs + bg);
            int cy = y + 24;
            int[][] btns = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {3, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {2, cx + bs + bg, cy + (bs + bg) * 2},
                {0, cx + bs + bg, cy + (bs + bg) * 3},
            };
            for (int[] b : btns) {
                int face = b[0], bx = b[1], by = b[2];
                if (mx >= bx && mx <= bx + bs && my >= by && my <= by + bs) {
                    boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
                    boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
                    boolean fuel = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
                    String status;
                    if (fuel) status = "\u00A76Fuel IN";
                    else if (in && out) status = "\u00A7dInput + Output";
                    else if (in) status = "\u00A79Input";
                    else if (out) status = "\u00A76Output";
                    else status = "\u00A77Aucun";
                    drawHoveringText(Arrays.asList(
                        FACE_NAMES[face],
                        status,
                        "\u00A78Clic: cycle IN/OUT/BOTH/NONE",
                        "\u00A78Maj+Clic: toggle Fuel IN"
                    ), mx, my);
                    return;
                }
            }
        }
    }

    // ======================================================================
    // SOURIS - gestion clics onglets + boutons config
    // ======================================================================

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        // 1. Check clic sur onglets
        int tabY = guiTop + TAB_Y_ABOVE;
        for (int t = 0; t < 3; t++) {
            if (mx >= guiLeft + TAB_X[t] && mx <= guiLeft + TAB_X[t] + TAB_W
                && my >= tabY && my <= tabY + TAB_H + 3) {
                activeTab = t;
                return;
            }
        }

        // 2. Check clic sur boutons face (onglet CONFIG)
        if (activeTab == TAB_CONFIG) {
            SideConfig sc = tile.getSideConfig();
            int bs = 18, bg = 2;
            int cx = guiLeft + xSize / 2 - (bs + bg);
            int cy = guiTop + 24;
            int[][] btns = {
                {1, cx + bs + bg, cy},
                {4, cx, cy + bs + bg},
                {3, cx + bs + bg, cy + bs + bg},
                {5, cx + (bs + bg) * 2, cy + bs + bg},
                {2, cx + bs + bg, cy + (bs + bg) * 2},
                {0, cx + bs + bg, cy + (bs + bg) * 3},
            };
            for (int[] b : btns) {
                int face = b[0], bx = b[1], by = b[2];
                if (mx >= bx && mx <= bx + bs && my >= by && my <= by + bs) {
                    boolean shift = isShiftKeyDown();
                    if (shift) {
                        // Toggle FUEL_IN
                        sc.toggleFace(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
                        int id = TileFurnaceNexus.SC_TYPE_FUEL_IN * 6 + face;
                        mc.playerController.sendEnchantPacket(
                            inventorySlots.windowId, id);
                    } else {
                        // Cycle: None -> In -> Out -> Both -> None
                        boolean in = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_IN, face);
                        boolean out = sc.isFaceActive(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face);
                        // Etat actuel 00, 01, 10, 11 = 0, 1, 2, 3
                        int curState = (in ? 1 : 0) | (out ? 2 : 0);
                        int nextState = (curState + 1) % 4;
                        boolean newIn = (nextState & 1) != 0;
                        boolean newOut = (nextState & 2) != 0;
                        // Applique localement (feedback visuel immediat)
                        sc.setFace(TileFurnaceNexus.SC_TYPE_ITEM_IN, face, newIn);
                        sc.setFace(TileFurnaceNexus.SC_TYPE_ITEM_OUT, face, newOut);
                        // Envoie au serveur les changements (via toggle)
                        if (newIn != in) {
                            int id = TileFurnaceNexus.SC_TYPE_ITEM_IN * 6 + face;
                            mc.playerController.sendEnchantPacket(
                                inventorySlots.windowId, id);
                        }
                        if (newOut != out) {
                            int id = TileFurnaceNexus.SC_TYPE_ITEM_OUT * 6 + face;
                            mc.playerController.sendEnchantPacket(
                                inventorySlots.windowId, id);
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

    private void fillBarVertical(int bx, int by, int bw, int bh,
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

    private boolean inRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
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
}
