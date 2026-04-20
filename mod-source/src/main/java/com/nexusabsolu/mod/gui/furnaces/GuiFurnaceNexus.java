package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.util.GuiUtils;
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
 * GUI Furnaces Nexus v6 - 2 panneaux separes (config gauche, upgrades droite).
 *
 * Layout (xSize=176, ySize=186) :
 *   Zone machine (haut) : IN + FUEL + PROGRESS + OUTPUT + RF BAR + flamme
 *   Zone inventaire (bas) : 3x9 inv + 9 hotbar (maintenant rentre proprement)
 *
 * Onglets lateraux :
 *   Onglet CONFIG   (cote GAUCHE, x=-15, y=18) : ouvre panneau Config I/O a gauche
 *   Onglet UPGRADES (cote DROIT,  x=xSize-2, y=18) : ouvre panneau Upgrades a droite
 *
 * Un seul panneau peut etre ouvert a la fois (clic sur l'autre ferme l'actif).
 */
public class GuiFurnaceNexus extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_furnace.png");

    private final TileFurnaceNexus tile;
    private boolean configOpen = false;

    public boolean isConfigOpen() { return configOpen; }
    /** @deprecated Plus de side-panel Upgrades depuis v1.0.210, GUI dedie a la place.
     *  Conserve pour compatibilite FurnaceGuiHandler JEI (retourne false). */
    public boolean isUpgradesOpen() { return false; }

    // Couleurs Mekanism SATUREES (feedback Alexis : trop pales dans v1.0.188)
    private static final int COL_NONE = 0xFF3A3A40;
    private static final int COL_IN = 0xFF2299FF;        // bleu vif
    private static final int COL_OUT = 0xFFFF8822;       // orange vif
    private static final int COL_BOTH = 0xFFCC33FF;      // violet vif
    private static final int COL_FUEL = 0xFFFFAA22;      // dore vif
    private static final int COL_BORDER = 0xFFBB77FF;
    private static final int COL_BORDER_HOV = 0xFFEEAAFF;

    private static final String[] FACE_LABELS = {"B", "H", "N", "S", "O", "E"};
    private static final String[] FACE_NAMES = {
        "Bas", "Haut", "Nord", "Sud", "Ouest", "Est"
    };

    // ======================================================================
    // LAYOUT : coordonnees et tailles des elements GUI, centralises ici pour
    // qu'on n'ait pas a chasser les nombres magiques dans chaque methode.
    // Les coordonnees sont en LOCAL (relatives a guiLeft/guiTop).
    // ======================================================================

    /** Jauge RF verticale (cote droit). Zone (140..150, 12..84) = 10x72 fill. */
    private static final int RF_BAR_X = 140;
    private static final int RF_BAR_Y = 12;
    private static final int RF_BAR_W = 10;   // incluant le cadre 1px de chaque cote
    private static final int RF_BAR_H = 72;
    /** Zone fill INTERIEURE de la jauge RF (apres cadre 1px + fond 1px). */
    private static final int RF_FILL_X = RF_BAR_X + 1;
    private static final int RF_FILL_Y = RF_BAR_Y + 1;
    private static final int RF_FILL_W = 8;   // 10 - 2*1px
    private static final int RF_FILL_H = 70;  // 72 - 2*1px

    /** Flamme fuel sous la fleche progress. Zone (69..91, 56..62) = 22x6. */
    private static final int FUEL_FLAME_X = 69;
    private static final int FUEL_FLAME_Y = 56;
    private static final int FUEL_FLAME_W = 22;
    private static final int FUEL_FLAME_H = 6;
    /** Hitbox tooltip flamme un peu plus large que la barre. */
    private static final int FUEL_FLAME_HITBOX_X = 68;
    private static final int FUEL_FLAME_HITBOX_Y = 55;
    private static final int FUEL_FLAME_HITBOX_W = 24;
    private static final int FUEL_FLAME_HITBOX_H = 8;

    /** Fleche progress horizontale. Zone (64..96, 29..36) = 32x7.
     *  Agrandie v1.0.245 (24x4 -> 32x7) pour meilleure visibilite. */
    private static final int PROGRESS_X = 64;
    private static final int PROGRESS_Y = 29;
    private static final int PROGRESS_W = 32;
    private static final int PROGRESS_H = 7;
    /** Hitbox tooltip progress plus large. */
    private static final int PROGRESS_HITBOX_X = 64;
    private static final int PROGRESS_HITBOX_Y = 26;
    private static final int PROGRESS_HITBOX_W = 32;
    private static final int PROGRESS_HITBOX_H = 13;

    /** Onglets lateraux (CONFIG a gauche, UPGRADES a droite). 15x17. */
    private static final int TAB_Y = 18;
    private static final int TAB_W = 15;
    private static final int TAB_H = 17;

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        // ySize dynamique selon tier IO installe (doit matcher Container) :
        //   extraH = max(0, (N-4) * 18) ou N = getIOSlotCount()
        //   - Tier 0/I (1/3 slots)  : 186 (base)
        //   - Tier II (5)           : 186 + 18 = 204
        //   - Tier III (7)          : 186 + 54 = 240
        //   - Tier IV (9)           : 186 + 90 = 276
        int visibleSlots = tile.getIOSlotCount();
        int extraH = Math.max(0, (visibleSlots - 4) * 18);
        this.ySize = 186 + extraH;
    }

    // ======================================================================
    // BACKGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        // Planque les 4 slots upgrade hors-ecran (accessibles via GUI dedie)
        updateUpgradeSlotPositions();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);

        int x = guiLeft;
        int y = guiTop;
        int visibleSlots = tile.getIOSlotCount();
        int extraH = ySize - 186;

        if (visibleSlots == 1) {
            // === Tier 0 : texture vanilla integrale (inchange par rapport a avant) ===
            drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        } else {
            // === Tier >= I : fond uni machine + texture inventaire bas + bordures manuelles ===
            // 1) Fond uni panneau machine (y = 0 a 93+extraH)
            drawRect(x, y, x + xSize, y + 93 + extraH, 0xFF1B0E2A);
            // 2) Texture inventaire (partie bas de la texture originale)
            drawTexturedModalRect(x, y + 93 + extraH, 0, 93, xSize, 93);

            // 3) Bordures des slots INPUT[0..N-1] en colonne gauche
            for (int i = 0; i < visibleSlots; i++) {
                drawSlotBorder(x + 41, y + 19 + i * 18);
            }
            // 4) Bordures des slots OUTPUT[0..N-1] en colonne droite
            for (int i = 0; i < visibleSlots; i++) {
                drawSlotBorder(x + 104, y + 19 + i * 18);
            }
            // 5) Bordure du slot FUEL (position deplacee sous la colonne input)
            int fuelY = 19 + visibleSlots * 18 + 2;  // 2px de marge sous le dernier input
            drawSlotBorder(x + 41, y + fuelY);
        }

        // === RF BAR VERTICALE a droite (style Thermal) ===
        if (tile.isRFMode()) {
            drawRect(x + RF_BAR_X, y + RF_BAR_Y,
                     x + RF_BAR_X + RF_BAR_W, y + RF_BAR_Y + RF_BAR_H, 0xFF1A1A1A);
            drawRect(x + RF_FILL_X, y + RF_FILL_Y,
                     x + RF_FILL_X + RF_FILL_W, y + RF_FILL_Y + RF_FILL_H, 0xFF3D0A0A);
            GuiUtils.fillBarVertical(x + RF_FILL_X, y + RF_FILL_Y, RF_FILL_W, RF_FILL_H,
                tile.getEnergyStored(), tile.getMaxEnergy(),
                0xFFB22222, 0xFFFF8A3C);
            int fillH = tile.getMaxEnergy() > 0
                ? (int)(RF_FILL_H * (float)tile.getEnergyStored() / tile.getMaxEnergy()) : 0;
            if (fillH > 0) {
                int fillBottom = y + RF_FILL_Y + RF_FILL_H;
                drawRect(x + RF_FILL_X, fillBottom - fillH,
                         x + RF_FILL_X + 1, fillBottom, 0xFFFFAA44);
            }
        }

        // === FLAMME fuel indicator ===
        int fuelBurnTicks = tile.getFuelBurnTicks();
        int fuelTotal = tile.getFuelTotalBurnTicks();
        boolean rfActive = tile.isRFMode()
            && tile.getEnergyStored() > 0 && tile.getCookProgress() > 0;

        if (fuelBurnTicks > 0 && fuelTotal > 0) {
            GuiUtils.fillBarHorizontal(x + FUEL_FLAME_X, y + FUEL_FLAME_Y,
                FUEL_FLAME_W, FUEL_FLAME_H,
                fuelBurnTicks, fuelTotal,
                0xFFCC3D10, 0xFFFF8830);
        } else if (rfActive) {
            GuiUtils.fillBarHorizontal(x + FUEL_FLAME_X, y + FUEL_FLAME_Y,
                FUEL_FLAME_W, FUEL_FLAME_H, 1, 1,
                0xFF4455CC, 0xFF7788FF);
        }

        // === PROGRESS fleche qui avance ===
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(PROGRESS_W * (float)prog / maxP);
            int tierCol = FurnaceTierStyle.getProgressColor(tile.getTier());
            int tierBright = FurnaceTierStyle.getProgressBright(tile.getTier());
            drawRect(x + PROGRESS_X, y + PROGRESS_Y,
                     x + PROGRESS_X + fillW, y + PROGRESS_Y + PROGRESS_H, tierCol);
            if (fillW > 2) {
                drawRect(x + PROGRESS_X, y + PROGRESS_Y,
                         x + PROGRESS_X + fillW, y + PROGRESS_Y + 1, tierBright);
            }
        }

        // === ONGLETS LATERAUX ===
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawTexturedModalRect(x - 13, y + TAB_Y, 176, 0, TAB_W, TAB_H);
        if (tile.isEnhanced()) {
            drawTexturedModalRect(x + xSize - 2, y + TAB_Y, 176, 17, TAB_W, TAB_H);
        }
    }

    /**
     * Dessine un slot 16x16 avec cadre 1px style vanilla Minecraft.
     * Position (x, y) = coin top-left du slot interne (ou l'item apparait).
     */
    private void drawSlotBorder(int x, int y) {
        // Cadre exterieur sombre 18x18
        drawRect(x - 1, y - 1, x + 17, y + 17, 0xFF373737);
        // Fond slot 16x16 couleur vanilla
        drawRect(x, y, x + 16, y + 16, 0xFF8B8B8B);
    }

    // ======================================================================
    // FOREGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Pattern Mekanism : reset color GL au cas ou un drawRect/drawString
        // anterieur aurait laisse la couleur dans un etat non-blanc.
        // Empeche les state leaks qui causent les textes "doublons" au
        // rendu du tooltip etendu shift-hover.
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        FurnaceTier tier = tile.getTier();

        // Titre centre
        String title = FurnaceTierStyle.getDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = FurnaceTierStyle.getTitleColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 6, titleColor);

        // Vitesse sous la progress arrow
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawStringWithShadow(speedStr, 68, 40, 0xFF8866AA);

        // Label Inventaire (decale par extraH pour matcher l'inventaire decale)
        int extraH = ySize - 186;
        fontRenderer.drawStringWithShadow("Inventaire", 8, 93 + extraH, 0xFF8866AA);

        // Label RF au-dessus de la barre verticale - uniquement si RF Converter place
        if (tile.isRFMode()) {
            fontRenderer.drawStringWithShadow("RF", 137, 4, 0xFFCC4444);
        }

        // Reset color GL avant de laisser la main au tooltip vanilla
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    // ======================================================================
    // drawScreen : tooltips + panels
    // ======================================================================

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);

        // Panneau Config dessine EN PREMIER (derriere tooltips)
        if (configOpen) drawConfigPanel(mx, my);

        // Tooltips en DERNIER pour qu'ils soient au-dessus de tout.
        // On fait le trick xSize pour que le tooltip des items soit positionne
        // correctement meme quand le panneau Config est ouvert.
        int realXSize = this.xSize;
        int realGuiLeft = this.guiLeft;
        if (configOpen) {
            this.guiLeft -= (CONFIG_W + 6);
            this.xSize += (CONFIG_W + 6);
        }
        renderHoveredToolTip(mx, my);
        this.xSize = realXSize;
        this.guiLeft = realGuiLeft;

        // Custom tooltips (RF bar, progress, flame, onglets) - APRES renderHoveredToolTip
        // pour qu'ils s'affichent par-dessus les tooltip items standards (rare collision)
        drawCustomTooltips(mx, my);
    }

    private void drawCustomTooltips(int mx, int my) {
        int x = guiLeft;
        int y = guiTop;

        // RF bar tooltip - v1.0.234 enrichi style Mek avec conso RF/tick +
        // autonomie restante estimee en secondes
        if (tile.isRFMode() && GuiUtils.inRect(mx, my, x + RF_BAR_X, y + RF_BAR_Y, RF_BAR_W, RF_BAR_H)) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add("\u00a7eEnergie\u00a7r: " + GuiUtils.formatRf(tile.getEnergyStored())
                + " / " + GuiUtils.formatRf(tile.getMaxEnergy()) + " RF");
            int rfPerTick = tile.getEffectiveRfPerTick();
            lines.add("\u00a77Conso: \u00a7f" + rfPerTick + " RF/tick");
            // Autonomie : RF stocke / RF par tick = ticks -> secondes (20 ticks/s)
            if (rfPerTick > 0 && tile.getEnergyStored() > 0) {
                int secondsLeft = tile.getEnergyStored() / (rfPerTick * 20);
                if (secondsLeft > 60) {
                    lines.add("\u00a77Autonomie: \u00a7a" + (secondsLeft / 60) + "m " + (secondsLeft % 60) + "s");
                } else {
                    lines.add("\u00a77Autonomie: \u00a7a" + secondsLeft + "s");
                }
            }
            drawHoveringText(lines, mx, my);
        }

        // Progress bar tooltip - v1.0.234 enrichi avec stats cuisson
        if (GuiUtils.inRect(mx, my, x + PROGRESS_HITBOX_X, y + PROGRESS_HITBOX_Y,
                   PROGRESS_HITBOX_W, PROGRESS_HITBOX_H)) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            lines.add("\u00a7eCuisson\u00a7r: " + pct + "%");
            // Temps de cuisson effectif (avec upgrades)
            int ticks = tile.getEffectiveMaxCookTime();
            float seconds = ticks / 20.0F;
            int speedCount = tile.getSpeedBoosterCount();
            int effCount = tile.getEfficiencyCount();
            String upgradesInfo = "";
            if (speedCount > 0 || effCount > 0) {
                upgradesInfo = " (" + speedCount + " SP";
                if (effCount > 0) upgradesInfo += ", " + effCount + " EF";
                upgradesInfo += ")";
            }
            lines.add(String.format("\u00a77Cuit en: \u00a7f%.2fs%s", seconds, upgradesInfo));
            // Taux : items par seconde
            float itemsPerSec = 20.0F / ticks;
            lines.add(String.format("\u00a77Taux: \u00a7f%.2f items/s", itemsPerSec));
            // Reste ticks avant fin cuisson (utile quand on voit la progression)
            if (tile.getCookProgress() > 0 && tile.getMaxCookTime() > 0) {
                int remaining = tile.getMaxCookTime() - tile.getCookProgress();
                lines.add("\u00a77Reste: \u00a7f" + remaining + " ticks");
            }
            drawHoveringText(lines, mx, my);
        }

        // Flame fuel tooltip (affiche pourcentage + ticks style vanilla)
        if (GuiUtils.inRect(mx, my, x + FUEL_FLAME_HITBOX_X, y + FUEL_FLAME_HITBOX_Y,
                   FUEL_FLAME_HITBOX_W, FUEL_FLAME_HITBOX_H)) {
            int ticks = tile.getFuelBurnTicks();
            int total = tile.getFuelTotalBurnTicks();
            String status;
            if (ticks > 0 && total > 0) {
                int pct = ticks * 100 / total;
                status = pct + "% (" + ticks + " ticks)";
            } else if (tile.isRFMode()
                && tile.getEnergyStored() > 0 && tile.getCookProgress() > 0) {
                status = "Mode RF";
            } else {
                status = "Vide";
            }
            drawHoveringText(Collections.singletonList(
                "Combustible: " + status), mx, my);
        }

        // Onglet Config (gauche)
        if (GuiUtils.inRect(mx, my, x - 13, y + TAB_Y, TAB_W, TAB_H)) {
            drawHoveringText(Collections.singletonList(
                configOpen ? "Fermer Config I/O" : "Ouvrir Config I/O"), mx, my);
        }

        // Onglet Upgrades (droite) - uniquement si enhanced
        if (tile.isEnhanced() && GuiUtils.inRect(mx, my, x + xSize - 2, y + TAB_Y, TAB_W, TAB_H)) {
            drawHoveringText(Collections.singletonList("Ouvrir Upgrades"), mx, my);
        }
    }

    // ======================================================================
    // PANNEAU CONFIG I/O (a GAUCHE du GUI, couleurs saturees)
    // ======================================================================

    // Dimensions panneau config (agrandi v1.0.199 : hauteur 175->185 pour eviter coupure)
    private static final int CONFIG_W = 150;
    private static final int CONFIG_H = 185;

    private void drawConfigPanel(int mx, int my) {
        int px = guiLeft - CONFIG_W - 6;  // A GAUCHE du GUI
        int py = guiTop + 10;

        // Fond du panneau (style Machine Humaine)
        drawRect(px - 2, py - 2, px + CONFIG_W + 2, py + CONFIG_H + 2, 0xFF5030A0);
        drawRect(px - 1, py - 1, px + CONFIG_W + 1, py + CONFIG_H + 1, 0xFFBB77FF);
        drawRect(px, py, px + CONFIG_W, py + CONFIG_H, 0xFF1A1030);

        // Barre de titre
        drawRect(px + 1, py + 1, px + CONFIG_W - 1, py + 16, 0xFF3A1F5E);
        fontRenderer.drawStringWithShadow("\u00A7d\u2699 Configuration I/O",
            px + 5, py + 4, 0xFFEEAAFF);
        drawRect(px + 3, py + 16, px + CONFIG_W - 3, py + 17, 0xFFBB77FF);

        // Boutons face en croix (plus gros : 28x28)
        SideConfig sc = tile.getSideConfig();
        int bs = 28, bg = 3;
        int totalW = bs * 3 + bg * 2;
        int cx = px + (CONFIG_W - totalW) / 2;
        int cy = py + 22;

        drawFaceBtn(cx + bs + bg, cy, bs, 1, sc, mx, my);                 // UP
        drawFaceBtn(cx, cy + bs + bg, bs, 4, sc, mx, my);                 // WEST
        drawFaceBtn(cx + bs + bg, cy + bs + bg, bs, 3, sc, mx, my);       // SOUTH (front)
        drawFaceBtn(cx + (bs + bg) * 2, cy + bs + bg, bs, 5, sc, mx, my); // EAST
        drawFaceBtn(cx, cy + (bs + bg) * 2, bs, 0, sc, mx, my);           // DOWN
        drawFaceBtn(cx + bs + bg, cy + (bs + bg) * 2, bs, 2, sc, mx, my); // NORTH

        // Instructions split sur 3 lignes (evite de couper)
        int helpY = cy + (bs + bg) * 3 + 6;
        fontRenderer.drawString("Clic : cycle None/In/",      px + 4, helpY,       0xFFAAAAAA);
        fontRenderer.drawString("              Out/Both",     px + 4, helpY + 10,  0xFFAAAAAA);
        fontRenderer.drawString("Maj+Clic : toggle Fuel IN",  px + 4, helpY + 22,  0xFFAAAAAA);

        // Legende couleurs (2 lignes de 2)
        int legY = helpY + 38;
        drawColorLegend(px + 4,  legY,      COL_IN,   "Input",  0xFF88CCFF);
        drawColorLegend(px + 78, legY,      COL_OUT,  "Output", 0xFFFFCC88);
        drawColorLegend(px + 4,  legY + 12, COL_BOTH, "Both",   0xFFEEAAFF);
        drawColorLegend(px + 78, legY + 12, COL_FUEL, "Fuel",   0xFFFFDD77);

        // Tooltips hover faces
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
                drawHoveringText(Arrays.asList(FACE_NAMES[face], status), mx, my);
            }
        }
    }

    private void drawColorLegend(int x, int y, int color, String label, int textColor) {
        drawRect(x, y, x + 8, y + 8, color);
        fontRenderer.drawString(label, x + 11, y, textColor);
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

        // Bordures biseautees
        drawRect(bx - 1, by - 1, bx + sz + 1, by, border);
        drawRect(bx - 1, by - 1, bx, by + sz + 1, border);
        drawRect(bx, by + sz, bx + sz + 1, by + sz + 1, 0xFF2A1540);
        drawRect(bx + sz, by, bx + sz + 1, by + sz + 1, 0xFF2A1540);

        // Corps gradient
        drawRect(bx, by, bx + sz, by + sz, color);
        int colorBright = GuiUtils.brighten(color, 0.4f);
        int colorDim = GuiUtils.darken(color, 0.3f);
        drawRect(bx + 2, by + 2, bx + sz - 2, by + sz - 2, colorBright);
        drawRect(bx + 5, by + 5, bx + sz - 5, by + sz - 5, color);
        // Ombre interne bas-droite
        drawRect(bx + sz - 4, by + sz - 4, bx + sz - 2, by + sz - 2, colorDim);

        // Label face
        int lw = fontRenderer.getStringWidth(FACE_LABELS[face]);
        fontRenderer.drawStringWithShadow(FACE_LABELS[face],
            bx + (sz - lw) / 2.0F, by + (sz - 8) / 2.0F, 0xFFFFFFFF);
    }

    // ======================================================================
    // SLOTS UPGRADE (planques hors-ecran dans le GUI Furnace principal)
    // ======================================================================
    // Les 4 slots upgrade du ContainerFurnaceNexus sont maintenant TOUJOURS
    // planques a (-1000, -1000) dans le GUI principal. Ils sont accessibles
    // via le GUI dedie GuiFurnaceUpgrades (ouvert par un clic sur l'onglet
    // Upgrades, pattern Mekanism).

    /**
     * Maintient les 4 slots upgrade du container toujours hors-ecran
     * dans ce GUI (ils sont visibles dans le GUI Upgrades dedie).
     */
    private void updateUpgradeSlotPositions() {
        for (int i = 0; i < 4; i++) {
            Slot slot = inventorySlots.inventorySlots.get(3 + i);
            slot.xPos = -1000;
            slot.yPos = -1000;
        }
    }

    // ======================================================================
    // SOURIS
    // ======================================================================

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int x = guiLeft;
        int y = guiTop;

        // 1. Clic onglet CONFIG (gauche)
        if (mx >= x - 13 && mx <= x + 2
            && my >= y + TAB_Y && my <= y + TAB_Y + TAB_H) {
            configOpen = !configOpen;
            return;
        }

        // 2. Clic onglet UPGRADES (droite) - uniquement si enhanced
        // Pattern Mekanism : ouvre un GUI dedie a la place d'un side-panel
        // v1.0.233 : on envoie un paquet enchantItem(100) au serveur via
        // playerController. Cote serveur le Container.enchantItem ouvre le
        // GUI avec EntityPlayerMP.openGui ce qui cree aussi le Container
        // serveur. mc.player.openGui cote client n'ouvrait que le GUI client
        // -> les slotClick du GUI Upgrades arrivaient au ContainerFurnaceNexus
        // (pas au ContainerFurnaceUpgrades) cote serveur, et ces clics
        // echouaient silencieusement.
        if (tile.isEnhanced()
            && mx >= x + xSize - 2 && mx <= x + xSize + 13
            && my >= y + TAB_Y && my <= y + TAB_Y + TAB_H) {
            mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 100);
            return;
        }

        // 3. Si config ouvert, check clics sur boutons faces
        if (configOpen) {
            int px = x - CONFIG_W - 6;
            int py = y + 10;
            int bs = 28, bg = 3;
            int totalW = bs * 3 + bg * 2;
            int cx = px + (CONFIG_W - totalW) / 2;
            int cy = py + 22;
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
                        sc.toggleFace(TileFurnaceNexus.SC_TYPE_FUEL_IN, face);
                        mc.playerController.sendEnchantPacket(
                            inventorySlots.windowId,
                            TileFurnaceNexus.SC_TYPE_FUEL_IN * 6 + face);
                    } else {
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
}
