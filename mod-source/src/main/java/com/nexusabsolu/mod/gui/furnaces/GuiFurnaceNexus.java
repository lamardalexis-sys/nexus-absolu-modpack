package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * GUI pour les Furnaces Nexus. Rendu programmatique (pas de PNG externe),
 * pour avoir une teinte par tier facilement.
 *
 * Layout (176 x 166) :
 *  - Titre (tier + nom) en haut
 *  - Slot input (56, 17)
 *  - Slot fuel (56, 53)
 *  - Flamme animee entre input et fuel
 *  - Barre progress (horizontale) input -> output, centre 79, 34
 *  - Slot output (116, 35)
 *  - 4 slots upgrades (152, 17 a 71) verticaux a droite
 *  - Barre energie RF (verticale, a cote des upgrades)
 *  - Inventaire joueur en bas
 */
public class GuiFurnaceNexus extends GuiContainer {

    private final TileFurnaceNexus tile;

    // Dimensions du "chrome" de la GUI
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int INV_START_Y = 84;  // ou l'inventaire joueur commence

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    // === BACKGROUND (dessine avant les slots) ===

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = guiLeft;
        int y = guiTop;

        // Couleur de fond tier-colored
        int[] tierColor = getTierColor(tile.getTier());
        int bgColor = 0xFF000000 | (tierColor[0] << 16) | (tierColor[1] << 8) | tierColor[2];
        int bgDark = darken(bgColor, 0.4f);
        int bgLight = lighten(bgColor, 0.3f);

        // Fond principal
        drawRect(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, bgDark);

        // Bordure biseautee (3 couches)
        drawBeveledRect(x, y, GUI_WIDTH, GUI_HEIGHT, bgLight, bgColor, darken(bgDark, 0.3f));

        // === Zone inventaire joueur (fond plus sombre) ===
        drawRect(x + 7, y + 82, x + GUI_WIDTH - 7, y + GUI_HEIGHT - 7, 0xFF1A1A22);
        drawBeveledRect(x + 7, y + 82, GUI_WIDTH - 14, GUI_HEIGHT - 89,
                       0xFF33333F, 0xFF222228, 0xFF111117);

        // === Cadres des slots (3 machine + 4 upgrades + 27 inv + 9 hotbar) ===
        // Machine slots
        drawSlotFrame(x + 55, y + 16);   // input
        drawSlotFrame(x + 55, y + 52);   // fuel
        drawSlotFrame(x + 115, y + 34);  // output
        // Upgrade slots
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            drawSlotFrame(x + 151, y + 16 + up.slotIndex * 18);
        }
        // Inventaire joueur
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotFrame(x + 7 + col * 18, y + 94 + row * 18);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            drawSlotFrame(x + 7 + col * 18, y + 152);
        }

        // === Barre de progression (input -> output) ===
        int progressX = x + 79;
        int progressY = y + 34;
        int progressW = 24;
        int progressH = 17;
        drawRect(progressX, progressY, progressX + progressW, progressY + progressH, 0xFF0A0A10);
        if (tile.getMaxCookTime() > 0 && tile.getCookProgress() > 0) {
            int fillW = progressW * tile.getCookProgress() / tile.getMaxCookTime();
            // Fleche tier-coloree qui avance
            drawRect(progressX + 1, progressY + 7,
                     progressX + 1 + fillW, progressY + 10, bgLight);
            // Pointe de fleche
            if (fillW > 4) {
                drawRect(progressX + fillW - 2, progressY + 5,
                         progressX + fillW + 1, progressY + 12, bgLight);
            }
        }

        // === Indicateur flamme fuel ===
        int flameX = x + 57;
        int flameY = y + 37;
        int flameH = 13;
        int fuelRatio = Math.min(100, tile.getFuelRemaining() * 10);  // approx 10% par op
        int fillH = flameH * fuelRatio / 100;
        drawRect(flameX, flameY, flameX + 14, flameY + flameH, 0xFF0A0A10);
        if (fillH > 0) {
            // Flamme orange/rouge
            drawRect(flameX + 2, flameY + flameH - fillH,
                     flameX + 12, flameY + flameH, 0xFFE84510);
            drawRect(flameX + 4, flameY + flameH - fillH,
                     flameX + 10, flameY + flameH - fillH / 2, 0xFFFFDD40);
        }

        // === Barre RF (verticale a droite) ===
        int rfX = x + 134;
        int rfY = y + 17;
        int rfW = 8;
        int rfH = 72;
        drawRect(rfX, rfY, rfX + rfW, rfY + rfH, 0xFF0A0A10);
        if (tile.getMaxEnergy() > 0 && tile.getEnergyStored() > 0) {
            int eFillH = rfH * tile.getEnergyStored() / tile.getMaxEnergy();
            // Degradé rouge en bas -> jaune en haut
            for (int iy = 0; iy < eFillH; iy++) {
                float t = (float) iy / rfH;
                int r = 255;
                int g = (int)(40 + 200 * t);
                int b = 40;
                int color = 0xFF000000 | (r << 16) | (Math.min(255, g) << 8) | b;
                drawHorizontalLine(rfX + 1, rfX + rfW - 2, rfY + rfH - 1 - iy, color);
            }
        }
    }

    // === FOREGROUND (dessine apres les slots) ===

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Titre du tier
        String title = getTierDisplayName(tile.getTier());
        int titleColor = 0xFFFFFF;
        fontRenderer.drawString(title, 8, 6, titleColor);

        // Texte "Inventaire"
        fontRenderer.drawString(I18n.format("container.inventory"), 8, 72, 0xAAAAAA);

        // Tooltip sur barre RF
        int rfLocalX = 134;
        int rfLocalY = 17;
        if (mx - guiLeft >= rfLocalX && mx - guiLeft <= rfLocalX + 8
            && my - guiTop >= rfLocalY && my - guiTop <= rfLocalY + 72) {
            drawHoveringText(java.util.Arrays.asList(
                "Energie",
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"),
                mx - guiLeft, my - guiTop);
        }

        // Tooltip sur progression
        int progLocalX = 79, progLocalY = 34;
        if (mx - guiLeft >= progLocalX && mx - guiLeft <= progLocalX + 24
            && my - guiTop >= progLocalY && my - guiTop <= progLocalY + 17) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(java.util.Arrays.asList(
                "Progression",
                pct + "%",
                "Vitesse " + tile.getTier().speedMultiplier + "x"),
                mx - guiLeft, my - guiTop);
        }

        // Tooltips sur les slots upgrades
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            int sx = 151, sy = 16 + up.slotIndex * 18;
            if (mx - guiLeft >= sx && mx - guiLeft <= sx + 17
                && my - guiTop >= sy && my - guiTop <= sy + 17) {
                drawHoveringText(java.util.Arrays.asList(
                    getUpgradeLabel(up),
                    "\u00A77" + getUpgradeHint(up),
                    "\u00A78Max stack: " + up.maxStackSize),
                    mx - guiLeft, my - guiTop);
            }
        }
    }

    // === HELPERS ===

    private int[] getTierColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return new int[]{130, 130, 140};
            case GOLD:       return new int[]{200, 160, 50};
            case INVAR:      return new int[]{150, 165, 150};
            case EMERADIC:   return new int[]{70, 170, 90};
            case VOSSIUM_IV: return new int[]{120, 60, 170};
            default:         return new int[]{80, 80, 90};
        }
    }

    private String getTierDisplayName(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return "Fourneau de Fer";
            case GOLD:       return "Fourneau d'Or";
            case INVAR:      return "Fourneau d'Invar";
            case EMERADIC:   return "Fourneau de Cristal Emeradic";
            case VOSSIUM_IV: return "Fourneau de Vossium IV";
            default:         return "Fourneau " + tier.registryName;
        }
    }

    private String getUpgradeLabel(FurnaceUpgrade up) {
        switch (up) {
            case RF_CONVERTER:  return "Convertisseur RF";
            case IO_EXPANSION:  return "Extension I/O";
            case SPEED_BOOSTER: return "Accelerateur";
            case EFFICIENCY:    return "Carte d'Efficience";
            default:            return up.registrySuffix;
        }
    }

    private String getUpgradeHint(FurnaceUpgrade up) {
        switch (up) {
            case RF_CONVERTER:  return "Convertit coal en RF (+5% vitesse)";
            case IO_EXPANSION:  return "Augmente slots in/out (necessite RF)";
            case SPEED_BOOSTER: return "+30% vitesse, +40% conso par stack";
            case EFFICIENCY:    return "-8% conso par stack (multiplicatif)";
            default:            return "";
        }
    }

    private int darken(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) * (1 - factor));
        int g = (int)(((color >> 8) & 0xFF) * (1 - factor));
        int b = (int)((color & 0xFF) * (1 - factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int lighten(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) + (255 - ((color >> 16) & 0xFF)) * factor);
        int g = (int)(((color >> 8) & 0xFF) + (255 - ((color >> 8) & 0xFF)) * factor);
        int b = (int)((color & 0xFF) + (255 - (color & 0xFF)) * factor);
        return 0xFF000000 | (Math.min(255, r) << 16) | (Math.min(255, g) << 8) | Math.min(255, b);
    }

    private void drawBeveledRect(int x, int y, int w, int h, int topLeftColor, int midColor, int bottomRightColor) {
        // Top edge (clair)
        drawHorizontalLine(x, x + w - 1, y, topLeftColor);
        drawHorizontalLine(x, x + w - 2, y + 1, midColor);
        // Left edge (clair)
        drawVerticalLine(x, y, y + h - 1, topLeftColor);
        drawVerticalLine(x + 1, y + 1, y + h - 2, midColor);
        // Right edge (sombre)
        drawVerticalLine(x + w - 1, y, y + h - 1, bottomRightColor);
        drawVerticalLine(x + w - 2, y + 1, y + h - 2, midColor);
        // Bottom edge (sombre)
        drawHorizontalLine(x, x + w - 1, y + h - 1, bottomRightColor);
        drawHorizontalLine(x + 1, x + w - 2, y + h - 2, midColor);
    }

    private void drawSlotFrame(int x, int y) {
        // Slot noir encadre, 18x18 (bordures 1px)
        drawRect(x, y, x + 18, y + 18, 0xFF000000);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF3D3D48);
    }
}
