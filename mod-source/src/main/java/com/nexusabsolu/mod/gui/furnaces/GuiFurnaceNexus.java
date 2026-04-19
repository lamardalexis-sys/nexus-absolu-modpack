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

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 186;  // HAUTEUR AUGMENTEE de 166 -> 186
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
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft;
        int y = guiTop;

        // === RF BAR VERTICALE a droite (v7) ===
        // Texture zone : (140, 12) a (150, 84) = 10x72 max fill
        fillBarVertical(x + 141, y + 13, 8, 70,
            tile.getEnergyStored(), tile.getMaxEnergy(),
            0xFFCC4444, 0xFFFF6666);

        // === FLAMME fuel indicator style vanilla (descend avec le fuel restant) ===
        // Zone : (68, 55) a (91, 62) = 24x8
        int fuelBurnTicks = tile.getFuelBurnTicks();
        int fuelTotal = tile.getFuelTotalBurnTicks();
        boolean rfActive = tile.getEnergyStored() > 0 && tile.getCookProgress() > 0;

        if (fuelBurnTicks > 0 && fuelTotal > 0) {
            // Flamme orange proportionnelle au fuel restant (ratio descend dans le temps)
            fillBarHorizontal(x + 69, y + 56, 22, 6,
                fuelBurnTicks, fuelTotal,
                0xFFCC3D10, 0xFFFF8830);
        } else if (rfActive) {
            // Mode RF : barre pleine bleue constante (l'energie est continue)
            fillBarHorizontal(x + 69, y + 56, 22, 6, 1, 1,
                0xFF4455CC, 0xFF7788FF);
        }

        // === PROGRESS fleche qui avance ===
        // Zone : (68, 27) a (91, 36) = 24x10
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(24.0F * prog / maxP);
            int tierCol = getTierProgressColor(tile.getTier());
            int tierBright = getTierProgressBright(tile.getTier());
            drawRect(x + 68, y + 30, x + 68 + fillW, y + 34, tierCol);
            if (fillW > 2) {
                drawRect(x + 68, y + 30, x + 68 + fillW, y + 31, tierBright);
            }
        }

        // === ONGLETS LATERAUX ===
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Onglet CONFIG a GAUCHE (depasse a gauche, x=-13)
        drawTexturedModalRect(x - 13, y + 18, 176, 0, 15, 17);
        // Onglet UPGRADES a DROITE (depasse a droite, x=xSize-2)
        drawTexturedModalRect(x + xSize - 2, y + 18, 176, 17, 15, 17);
    }

    // ======================================================================
    // FOREGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        FurnaceTier tier = tile.getTier();

        // Titre centre
        String title = getTierDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = getTierTitleColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 6, titleColor);

        // Vitesse sous la progress arrow
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawStringWithShadow(speedStr, 68, 40, 0xFF8866AA);

        // Label Inventaire (ajuste a ySize=186 -> inv a y=93+)
        fontRenderer.drawStringWithShadow("Inventaire", 8, 93, 0xFF8866AA);

        // Label RF au-dessus de la barre verticale
        fontRenderer.drawStringWithShadow("RF", 137, 4, 0xFFCC4444);
    }

    // ======================================================================
    // drawScreen : tooltips + panels
    // ======================================================================

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);

        // TRICK : pour le panneau Config (a gauche), agrandir xSize/guiLeft
        // temporairement pour que Minecraft calcule correctement la position
        // du tooltip (evite debordement sur le panneau).
        int realXSize = this.xSize;
        int realGuiLeft = this.guiLeft;
        if (configOpen) {
            this.guiLeft -= (CONFIG_W + 6);
            this.xSize += (CONFIG_W + 6);
        }

        renderHoveredToolTip(mx, my);

        // RESTORE pour le reste
        this.xSize = realXSize;
        this.guiLeft = realGuiLeft;

        drawCustomTooltips(mx, my);
        if (configOpen) drawConfigPanel(mx, my);
    }

    private void drawCustomTooltips(int mx, int my) {
        int x = guiLeft;
        int y = guiTop;

        // RF bar tooltip (zone verticale (140,12)-(150,84))
        if (inRect(mx, my, x + 140, y + 12, 10, 72)) {
            drawHoveringText(Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"), mx, my);
        }

        // Progress bar tooltip
        if (inRect(mx, my, x + 68, y + 27, 24, 10)) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(Collections.singletonList(
                "Cuisson: " + pct + "%"), mx, my);
        }

        // Flame fuel tooltip (affiche pourcentage + ticks style vanilla)
        if (inRect(mx, my, x + 68, y + 55, 24, 8)) {
            int ticks = tile.getFuelBurnTicks();
            int total = tile.getFuelTotalBurnTicks();
            String status;
            if (ticks > 0 && total > 0) {
                int pct = ticks * 100 / total;
                status = pct + "% (" + ticks + " ticks)";
            } else if (tile.getEnergyStored() > 0 && tile.getCookProgress() > 0) {
                status = "Mode RF";
            } else {
                status = "Vide";
            }
            drawHoveringText(Collections.singletonList(
                "Combustible: " + status), mx, my);
        }

        // Onglet Config (gauche)
        if (inRect(mx, my, x - 13, y + 18, 15, 17)) {
            drawHoveringText(Collections.singletonList(
                configOpen ? "Fermer Config I/O" : "Ouvrir Config I/O"), mx, my);
        }

        // Onglet Upgrades (droite) - ouvre le GUI dedie
        if (inRect(mx, my, x + xSize - 2, y + 18, 15, 17)) {
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
        int colorBright = brighten(color, 0.4f);
        int colorDim = darken(color, 0.3f);
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

        // 1. Clic onglet CONFIG (gauche, x=-13, 15x17)
        if (mx >= x - 13 && mx <= x + 2 && my >= y + 18 && my <= y + 35) {
            configOpen = !configOpen;
            return;
        }

        // 2. Clic onglet UPGRADES (droite, x=xSize-2, 15x17)
        // Pattern Mekanism : ouvre un GUI dedie a la place d'un side-panel
        if (mx >= x + xSize - 2 && mx <= x + xSize + 13
            && my >= y + 18 && my <= y + 35) {
            net.minecraft.util.math.BlockPos pos = tile.getPos();
            mc.player.openGui(
                com.nexusabsolu.mod.NexusAbsoluMod.instance,
                com.nexusabsolu.mod.gui.GuiHandler.FURNACE_UPGRADES_GUI,
                mc.world,
                pos.getX(), pos.getY(), pos.getZ()
            );
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

    /** Barre verticale qui se remplit du BAS vers le HAUT avec degrade rouge->jaune. */
    private void fillBarVertical(int bx, int by, int bw, int bh,
                                  int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillH = (int)(bh * ratio);
        if (fillH <= 0) return;

        // Degrade rouge (bas) -> jaune (haut) pixel par pixel
        for (int dy = 0; dy < fillH; dy++) {
            // dy = 0 = tout en bas du remplissage = rouge fonce
            // dy = fillH-1 = haut du remplissage = jaune vif
            float t = (float) dy / Math.max(1, fillH - 1);
            int r = 255;
            int g = (int)(40 + (220 - 40) * t);   // 40 -> 220
            int b = (int)(40 * (1 - t));           // 40 -> 0
            int col = 0xFF000000 | (r << 16) | (g << 8) | b;
            int py = by + bh - 1 - dy;   // dessine du bas vers le haut
            drawRect(bx, py, bx + bw, py + 1, col);
        }

        // Shine brillant au sommet du remplissage
        if (fillH > 2) {
            int topY = by + bh - fillH;
            drawRect(bx, topY, bx + bw, topY + 1, 0xFFFFFFAA);
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

    private int darken(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) * (1 - factor));
        int g = (int)(((color >> 8) & 0xFF) * (1 - factor));
        int b = (int)((color & 0xFF) * (1 - factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
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
