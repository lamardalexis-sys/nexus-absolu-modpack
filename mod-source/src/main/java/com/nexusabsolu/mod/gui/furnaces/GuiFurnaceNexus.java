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

    /** Texture vanilla tier 0 (1 slot, sans carte IO). */
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_furnace.png");

    /** v1.0.261 : Textures dediees par tier IO (carte IO I/II/III/IV installee). */
    private static final ResourceLocation[] TEXTURE_IO = {
        new ResourceLocation(Reference.MOD_ID, "textures/gui/gui_furnace_io1.png"),  // 3+3 slots
        new ResourceLocation(Reference.MOD_ID, "textures/gui/gui_furnace_io2.png"),  // 5+5 slots
        new ResourceLocation(Reference.MOD_ID, "textures/gui/gui_furnace_io3.png"),  // 7+7 slots
        new ResourceLocation(Reference.MOD_ID, "textures/gui/gui_furnace_io4.png"),  // 9+9 slots
    };

    /** Retourne la bonne texture selon le tier IO installe. */
    private ResourceLocation getTexture() {
        int n = tile.getIOSlotCount();
        // n = 1 (pas de carte), 3, 5, 7 ou 9 => ioTier = 0, 1, 2, 3, 4
        int ioTier = Math.max(0, (n - 1) / 2);
        if (ioTier == 0) return TEXTURE;
        return TEXTURE_IO[ioTier - 1];
    }

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

    // v1.0.288 (Alexis) : labels relatifs au four, plus intuitifs pour le joueur.
    //   Avant : B/H/N/S/O/E (conventions cardinales Minecraft)
    //   Apres : Ba/H/Ar/Av/Ga/Dr (positions relatives au four)
    //
    // Mapping (index = EnumFacing.getIndex()) :
    //   [0] DOWN  -> "Ba" (Bas, face au sol)
    //   [1] UP    -> "H"  (Haut)
    //   [2] NORTH -> "Ar" (Arriere, face derriere le four)
    //   [3] SOUTH -> "Av" (Avant, face devant le four / front)
    //   [4] WEST  -> "Ga" (Gauche)
    //   [5] EAST  -> "Dr" (Droite)
    //
    // NOTE design : le layout GUI place DOWN en bas-gauche et NORTH en bas-centre
    // du panneau. Chaque label reste rigoureusement associe a sa face Minecraft
    // reelle : cliquer 'Ba' change bien la face DU SOL du block, cliquer 'Ar'
    // change bien la face arriere.
    private static final String[] FACE_LABELS = {"Ba", "H", "Ar", "Av", "Ga", "Dr"};
    private static final String[] FACE_NAMES = {
        "Bas", "Haut", "Arriere", "Avant", "Gauche", "Droite"
    };

    // ======================================================================
    // LAYOUT : coordonnees et tailles des elements GUI, centralises ici pour
    // qu'on n'ait pas a chasser les nombres magiques dans chaque methode.
    // Les coordonnees sont en LOCAL (relatives a guiLeft/guiTop).
    // ======================================================================

    /** Jauge RF verticale (cote droit). 
     *  v1.0.270 : texture vanilla regeneree avec RF bar a x=132 (matche le style
     *  des textures IO : xSize-44 = 176-44 = 132). Zone (132..141, 12..84) = 10x72 fill. */
    private static final int RF_BAR_X = 132;
    private static final int RF_BAR_Y = 12;
    private static final int RF_BAR_W = 10;
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

    /** Bouton Auto-Sort sous l'onglet CONFIG. 15x15. v1.0.256 */
    private static final int SORT_BTN_X = -13;        // meme x que tab CONFIG
    private static final int SORT_BTN_Y = 40;          // sous le tab CONFIG
    private static final int SORT_BTN_W = 15;
    private static final int SORT_BTN_H = 15;

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        // Layout horizontal Mekanism-Factory-style : xSize dynamique, ySize fixe.
        //   xSize = max(176, N*18 + 50) ou N = getIOSlotCount()
        //   Les slots sont centres sur la ZONE MACHINE (0..xSize-40) pour eviter
        //   le chevauchement avec la RF bar a droite.
        //   - Tier 0/I/II/III (1/3/5/7 slots) : 176 (tient)
        //   - Tier IV (9 slots)               : 212 (elargi)
        int visibleSlots = tile.getIOSlotCount();
        this.xSize = Math.max(176, visibleSlots * 18 + 58);
        this.ySize = 186;  // inchange quel que soit le tier
    }

    // ======================================================================
    // BACKGROUND
    // ======================================================================

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        // Planque les 4 slots upgrade hors-ecran (accessibles via GUI dedie)
        updateUpgradeSlotPositions();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        ResourceLocation tex = getTexture();
        mc.getTextureManager().bindTexture(tex);

        int x = guiLeft;
        int y = guiTop;
        int visibleSlots = tile.getIOSlotCount();

        if (visibleSlots == 1) {
            // === Tier 0 : texture vanilla integrale ===
            drawTexturedModalRect(x, y, 0, 0, 176, ySize);
            // En mode RF : masque le slot fuel vanilla avec un rect violet
            if (tile.isRFMode()) {
                drawRect(x + 40, y + 50, x + 58, y + 68, 0xFF312A3E);
            }
        } else {
            // === Tier >= I : utilise la texture IO dediee (v1.0.261) ===
            // La texture contient deja : cadre, slots, progress arrow,
            // fuel, inventaire et layout complet a la bonne taille.
            drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
            // En mode RF : masque le slot fuel dessine dans la texture
            // (position 20, 73, meme que code Container).
            if (tile.isRFMode()) {
                drawRect(x + 19, y + 72, x + 37, y + 90, 0xFF1B0E2A);
            }
        }

        // === RF BAR VERTICALE a droite ===
        // Tier 0 : texture vanilla (v1.0.270) a RF bar dessinee a x=132.
        //   On dessine la JAUGE par-dessus a la meme position pour que
        //   le fill remplace le fond rouge statique de la texture.
        // Tier >= I : xSize peut etre plus large que 176, on dessine a xSize-44
        //   (memaligne que les textures IO).
        int rfBarX, rfFillX;
        if (visibleSlots == 1) {
            rfBarX = RF_BAR_X;   // 132 (matche texture vanilla v1.0.270)
            rfFillX = RF_FILL_X; // 133
        } else {
            rfBarX = xSize - 44;
            rfFillX = rfBarX + 1;
        }
        if (tile.isRFMode()) {
            drawRect(x + rfBarX, y + RF_BAR_Y,
                     x + rfBarX + RF_BAR_W, y + RF_BAR_Y + RF_BAR_H, 0xFF1A1A1A);
            drawRect(x + rfFillX, y + RF_FILL_Y,
                     x + rfFillX + RF_FILL_W, y + RF_FILL_Y + RF_FILL_H, 0xFF3D0A0A);
            GuiUtils.fillBarVertical(x + rfFillX, y + RF_FILL_Y, RF_FILL_W, RF_FILL_H,
                tile.getEnergyStored(), tile.getMaxEnergy(),
                0xFFB22222, 0xFFFF8A3C);
            int fillH = tile.getMaxEnergy() > 0
                ? (int)(RF_FILL_H * (float)tile.getEnergyStored() / tile.getMaxEnergy()) : 0;
            if (fillH > 0) {
                int fillBottom = y + RF_FILL_Y + RF_FILL_H;
                drawRect(x + rfFillX, fillBottom - fillH,
                         x + rfFillX + 1, fillBottom, 0xFFFFAA44);
            }
        }

        // === FLAMME fuel indicator ===
        // Tier 0 : position vanilla (69, 56) sous la progress arrow
        // Tier >= I : a droite du fuel slot a x=20 -> flamme a x=40, y=79
        // En mode RF + tier >= I : pas de flamme (slot fuel est cache, flamme inutile)
        int flameX, flameY;
        boolean showFlame = true;
        if (visibleSlots == 1) {
            flameX = FUEL_FLAME_X;
            flameY = FUEL_FLAME_Y;
            // Tier 0 + mode RF : cache la flamme aussi (slot fuel masque)
            if (tile.isRFMode()) showFlame = false;
        } else {
            flameX = 40;  // fuel.x (20) + slot_width (18) + 2 marge
            flameY = 79;  // vertical center du slot fuel y=73..89
            if (tile.isRFMode()) showFlame = false;
        }

        int fuelBurnTicks = tile.getFuelBurnTicks();
        int fuelTotal = tile.getFuelTotalBurnTicks();

        if (showFlame && fuelBurnTicks > 0 && fuelTotal > 0) {
            GuiUtils.fillBarHorizontal(x + flameX, y + flameY,
                FUEL_FLAME_W, FUEL_FLAME_H,
                fuelBurnTicks, fuelTotal,
                0xFFCC3D10, 0xFFFF8830);
        }

        // === PROGRESS fleche : centree horizontalement entre input et output rows ===
        // Tier 0 : position vanilla (64, 29)
        // Tier >= I : centree sur la ZONE MACHINE (hors RF bar a droite)
        //   zone = 0..xSize-40 (meme logique que titre)
        int progX, progY;
        if (visibleSlots == 1) {
            progX = PROGRESS_X;
            progY = PROGRESS_Y;
        } else {
            // v1.0.263b : progress centree sur xSize entier avec clamp
            // pour eviter RF bar a xSize-44 (stop a xSize-46)
            int idealProgX = (xSize - PROGRESS_W) / 2;
            int maxProgEndX = xSize - 46;
            if (idealProgX + PROGRESS_W > maxProgEndX) {
                progX = maxProgEndX - PROGRESS_W;
            } else {
                progX = idealProgX;
            }
            // Progress y = entre input (y=inputY, h=16) et output (y=outputY)
            ContainerFurnaceNexus ct = (ContainerFurnaceNexus) inventorySlots;
            int gapMiddle = (ct.getInputRowY() + 16 + ct.getOutputRowY() - PROGRESS_H) / 2;
            progY = gapMiddle;
        }

        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(PROGRESS_W * (float)prog / maxP);
            int tierCol = FurnaceTierStyle.getProgressColor(tile.getTier());
            int tierBright = FurnaceTierStyle.getProgressBright(tile.getTier());
            drawRect(x + progX, y + progY,
                     x + progX + fillW, y + progY + PROGRESS_H, tierCol);
            if (fillW > 2) {
                drawRect(x + progX, y + progY,
                         x + progX + fillW, y + progY + 1, tierBright);
            }
        }

        // === ONGLETS LATERAUX ===
        // v1.0.261 : tier 0 utilise la texture vanilla (onglets UV a 176).
        // Tier >= I utilise la texture IO dediee (onglets UV a 224).
        int tabU = (visibleSlots == 1) ? 176 : 224;
        // Les onglets restent dessines avec la MEME texture que le fond (deja bind)
        drawTexturedModalRect(x - 13, y + TAB_Y, tabU, 0, TAB_W, TAB_H);
        if (tile.isEnhanced()) {
            drawTexturedModalRect(x + xSize - 2, y + TAB_Y, tabU, 17, TAB_W, TAB_H);
        }

        // === BOUTON AUTO-SORT (sous onglet CONFIG, tier >= I uniquement) ===
        // Dessin manuel : carre avec bordure + lettre 'S' + couleur selon etat
        // Vert si actif, gris si desactive.
        if (tile.getIOSlotCount() > 1) {
            int btnX = x + SORT_BTN_X;
            int btnY = y + SORT_BTN_Y;
            boolean enabled = tile.isAutoSortEnabled();
            // Fond : vert sature si actif, gris sombre si desactive
            int bgColor = enabled ? 0xFF3AA638 : 0xFF444444;
            int borderColor = enabled ? 0xFF7AEB78 : 0xFF888888;
            // Bordure
            drawRect(btnX, btnY, btnX + SORT_BTN_W, btnY + SORT_BTN_H, borderColor);
            // Fond interieur (1px de marge pour la bordure)
            drawRect(btnX + 1, btnY + 1, btnX + SORT_BTN_W - 1, btnY + SORT_BTN_H - 1, bgColor);
        }
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

        // Titre : centre sur la zone machine (hors RF bar a droite)
        // v1.0.265 : maxTextEndX passe de xSize-46 a xSize-54 (marge 16px
        // au lieu de 8px) pour eviter 'Fourneau Vossium IVRF' (capture Alexis).
        String title = FurnaceTierStyle.getDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = FurnaceTierStyle.getTitleColor(tier);
        int maxTextEndX = xSize - 54;
        float titleX = (xSize - tw) / 2.0F;
        if (titleX + tw > maxTextEndX) titleX = maxTextEndX - tw;
        fontRenderer.drawStringWithShadow(title, titleX, 6, titleColor);

        // Vitesse : position differente selon tier
        // Tier 0 (vanilla) : y=40 a droite de la progress arrow (style vanilla)
        // Tier >= I : a DROITE de la progress arrow, meme y, pour ne pas
        //   chevaucher les lignes input/output qui sont tres proches
        //   (en mode RF notamment : input y=27, output y=63, progress y=49)
        // v1.0.289 : utilise getEffectiveSpeedMultiplier() qui inclut les
        //   Speed Boosters installes, au lieu de tier.speedMultiplier (base fixe).
        //   Le nombre se met maintenant a jour live quand on ajoute/retire un booster.
        //   Locale.US pour forcer le format "x20.6" (point decimal) au lieu de "x20,6".
        String speedStr = String.format(java.util.Locale.US, "x%.1f", tile.getEffectiveSpeedMultiplier());
        int sw = fontRenderer.getStringWidth(speedStr);
        float speedX, speedY;
        if (tile.getIOSlotCount() == 1) {
            speedX = 68;
            speedY = 40;
        } else {
            ContainerFurnaceNexus ct = (ContainerFurnaceNexus) inventorySlots;
            int progY2 = (ct.getInputRowY() + 16 + ct.getOutputRowY() - 7) / 2;
            // Progress arrow : centre xSize, largeur 32
            int idealProgX = (xSize - 32) / 2;
            int maxProgEndX2 = xSize - 46;
            int progressXPos;
            if (idealProgX + 32 > maxProgEndX2) progressXPos = maxProgEndX2 - 32;
            else progressXPos = idealProgX;
            // Speed label : 4px a DROITE de la progress arrow, meme hauteur
            speedX = progressXPos + 32 + 4;
            speedY = progY2 - 1;  // vertical-align avec la progress arrow
            // Clamp : si depasse la RF bar, mettre a gauche de la progress
            if (speedX + sw > maxTextEndX) {
                speedX = progressXPos - sw - 4;
            }
        }
        fontRenderer.drawStringWithShadow(speedStr, speedX, speedY, 0xFF8866AA);

        // Label Inventaire a y=93 (inchange, layout horizontal ne decale plus l'inv)
        // Recentrer horizontalement si xSize > 176 pour matcher l'inv centre
        int invTextureX = (xSize - 176) / 2;
        fontRenderer.drawStringWithShadow("Inventaire", 8 + invTextureX, 93, 0xFF8866AA);

        // Label RF au-dessus de la barre verticale - uniquement si RF Converter place
        if (tile.isRFMode()) {
            // Position label RF : tier 0 aligne sur RF bar x=132 (v1.0.270),
            // tier >= I sur xSize-47. 'RF' = 13px large, bar = 10px, donc on
            // centre : rfBarX + 5 (milieu bar) - 6 (demi texte) = rfBarX - 1
            int rfLabelX = (tile.getIOSlotCount() == 1) ? (RF_BAR_X - 1) : (xSize - 47);
            fontRenderer.drawStringWithShadow("RF", rfLabelX, 4, 0xFFCC4444);
        }

        // === BOUTON AUTO-SORT : lettre 'S' + label On/Off (tier >= I) ===
        // Foreground coords sont locales (origine a guiLeft/guiTop).
        // Le bouton est dessine en background a SORT_BTN_X=-13, SORT_BTN_Y=40.
        if (tile.getIOSlotCount() > 1) {
            boolean enabled = tile.isAutoSortEnabled();
            // Lettre 'S' centree dans le bouton 15x15
            // btnCenterX = -13 + 15/2 = -5.5 -> charWidth 'S' = 5 -> x = -13 + 5 = -8
            fontRenderer.drawStringWithShadow("S", SORT_BTN_X + 5, SORT_BTN_Y + 4,
                enabled ? 0xFFFFFFFF : 0xFFCCCCCC);
            // Label On/Off sous le bouton
            String stateLabel = enabled ? "On" : "Off";
            int labelColor = enabled ? 0xFF88FF88 : 0xFFAAAAAA;
            fontRenderer.drawStringWithShadow(stateLabel,
                SORT_BTN_X + 1, SORT_BTN_Y + SORT_BTN_H + 2, labelColor);
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
        // v1.0.271 : hitbox dynamique selon tier (tier 0 = RF_BAR_X, tier >= I = xSize-44)
        // v1.0.285 : ajout conso max + autonomie corrigee selon etat autoSort.
        // v1.0.286 : la 'Conso max' utilise maintenant le multiplier tier IO
        //            (1.5/2/4/6) au lieu du nombre brut de slots (3/5/7/9).
        //            Cf. TileFurnaceNexus.getAutoSortConsumptionMultiplier().
        int rfHitboxX = (tile.getIOSlotCount() == 1) ? RF_BAR_X : (xSize - 44);
        if (tile.isRFMode() && GuiUtils.inRect(mx, my, x + rfHitboxX, y + RF_BAR_Y, RF_BAR_W, RF_BAR_H)) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add("\u00a7eEnergie\u00a7r: " + GuiUtils.formatRf(tile.getEnergyStored())
                + " / " + GuiUtils.formatRf(tile.getMaxEnergy()) + " RF");
            int rfPerPair = tile.getEffectiveRfPerTick();
            int maxSlots = tile.getIOSlotCount();
            boolean autoSort = tile.isAutoSortEnabled();

            // Multiplier reellement applique si autoSort ON (1.5, 2.0, 4.0, 6.0)
            // peu importe combien de paires sont actives actuellement.
            // Si carte IO absente (maxSlots==1), multiplier vaut 1.0.
            int ioTier = Math.max(0, (maxSlots - 1) / 2);
            float multiplierIfAutoSort;
            switch (ioTier) {
                case 1:  multiplierIfAutoSort = 1.5F; break;
                case 2:  multiplierIfAutoSort = 2.0F; break;
                case 3:  multiplierIfAutoSort = 4.0F; break;
                case 4:  multiplierIfAutoSort = 6.0F; break;
                default: multiplierIfAutoSort = 1.0F; break;  // tier 0
            }
            int rfMaxConso = (int)(rfPerPair * multiplierIfAutoSort);

            // Ligne 1 : conso par paire (= base * upgrades Speed/Efficiency)
            lines.add("\u00a77Conso/paire: \u00a7f" + rfPerPair + " RF/t");

            // Ligne 2 (tier >= I uniquement) : conso max si autoSort active
            if (maxSlots > 1) {
                // Couleur : rouge si autoSort ON (valeur actuellement appliquee),
                // gris si autoSort OFF (indicatif, serait la conso si active)
                String color = autoSort ? "\u00a7c" : "\u00a78";
                lines.add("\u00a77Conso max: " + color + GuiUtils.formatRf(rfMaxConso) + " RF/t");
            }

            // Autonomie : calculee sur la conso effectivement appliquee maintenant
            //   autoSort ON  : rfForAutonomie = rfMaxConso
            //   autoSort OFF : rfForAutonomie = rfPerPair (sequentiel)
            int rfForAutonomie = autoSort ? rfMaxConso : rfPerPair;
            if (rfForAutonomie > 0 && tile.getEnergyStored() > 0) {
                int secondsLeft = tile.getEnergyStored() / (rfForAutonomie * 20);
                String autonomieLabel = (maxSlots > 1)
                    ? (autoSort ? "Autonomie (auto-sort)" : "Autonomie (sequentiel)")
                    : "Autonomie";
                if (secondsLeft > 60) {
                    lines.add("\u00a77" + autonomieLabel + ": \u00a7a" + (secondsLeft / 60) + "m " + (secondsLeft % 60) + "s");
                } else {
                    lines.add("\u00a77" + autonomieLabel + ": \u00a7a" + secondsLeft + "s");
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
            // v1.0.290 : getEffectiveMaxCookTime() retourne maintenant un float
            // (peut etre < 1 tick pour les tres hauts tiers avec boosters).
            float ticks = tile.getEffectiveMaxCookTime();
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

        // Bouton Auto-Sort (sous Config, tier >= I uniquement)
        if (tile.getIOSlotCount() > 1
            && GuiUtils.inRect(mx, my, x + SORT_BTN_X, y + SORT_BTN_Y, SORT_BTN_W, SORT_BTN_H)) {
            java.util.List<String> lines = new java.util.ArrayList<>();
            boolean enabled = tile.isAutoSortEnabled();
            lines.add((enabled ? "\u00A7aAuto-Sort: On" : "\u00A77Auto-Sort: Off"));
            lines.add("\u00A77Distribue les stacks equitablement");
            lines.add("\u00A77entre tous les inputs actifs.");
            lines.add("\u00A78Clic : " + (enabled ? "desactiver" : "activer"));
            drawHoveringText(lines, mx, my);
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
     * Maintient les 4 slots upgrade du container toujours hors-ecran dans ce GUI
     * (ils sont visibles dans le GUI Upgrades dedie).
     *
     * Avec le nouveau layout v1.0.252 :
     *   Indices container : 9 inputs (0-8), 1 fuel (9), 9 outputs (10-18),
     *                       4 upgrades (19-22)
     *
     * Cache aussi le SLOT FUEL en mode RF (v1.0.255) : quand le four tourne a
     * l'energie RF, le coal est inutile donc on cache son slot.
     */
    private void updateUpgradeSlotPositions() {
        // Cache les 4 slots upgrade (indices 19-22)
        for (int i = 0; i < 4; i++) {
            Slot slot = inventorySlots.inventorySlots.get(19 + i);
            slot.xPos = -1000;
            slot.yPos = -1000;
        }

        // Cache le slot FUEL (indice 9) si mode RF
        Slot fuelSlot = inventorySlots.inventorySlots.get(9);
        if (tile.isRFMode()) {
            fuelSlot.xPos = -1000;
            fuelSlot.yPos = -1000;
        } else {
            // Restaure position selon tier
            int visibleSlots = tile.getIOSlotCount();
            if (visibleSlots == 1) {
                fuelSlot.xPos = 41; fuelSlot.yPos = 51;
            } else {
                fuelSlot.xPos = 20; fuelSlot.yPos = 73;
            }
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

        // 1b. Clic bouton AUTO-SORT (sous CONFIG, tier >= I uniquement)
        if (tile.getIOSlotCount() > 1
            && mx >= x + SORT_BTN_X && mx <= x + SORT_BTN_X + SORT_BTN_W
            && my >= y + SORT_BTN_Y && my <= y + SORT_BTN_Y + SORT_BTN_H) {
            // Envoi packet serveur : enchantItem(200) -> Container.toggleAutoSort
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, 200);
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
