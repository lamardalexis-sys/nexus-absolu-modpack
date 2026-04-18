package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

/**
 * GUI Furnaces Nexus, style Mekanism/Nexus Absolu (cf. GuiMachineHumaine).
 *
 * Pattern : texture PNG 256x256 en fond + fillBar pour indicateurs dynamiques
 * (progress, flamme, RF). Tooltips sur hover des slots upgrades et bars.
 *
 * Layout (xSize=176, ySize=166) :
 *  - Titre en haut
 *  - Slot INPUT (56, 17)
 *  - Slot FUEL (56, 53)
 *  - Flamme verticale (79-90, 38-49) indicateur fuel restant
 *  - Progress bar horizontale (94-114, 35-49) fleche qui avance
 *  - Slot OUTPUT (116, 35) avec cadre Mekanism-style
 *  - Barre RF verticale (140-148, 17-71)
 *  - 4 slots upgrades (152, 17/35/53/71)
 *  - Inventaire joueur + hotbar
 */
public class GuiFurnaceNexus extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_furnace.png");

    private final TileFurnaceNexus tile;

    public GuiFurnaceNexus(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceNexus(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    // === BACKGROUND (texture + barres dynamiques) ===

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft;
        int y = guiTop;

        // === FLAMME FUEL (verticale, fill du bas vers le haut) ===
        // Zone du tube = (79, 38) a (90, 49) = 11 wide x 11 tall
        int fuel = tile.getFuelRemaining();
        // Ratio base sur 10 operations max = 1 coal par defaut
        int fuelMax = 10;
        fillBarVertical(x + 79, y + 38, 11, 11, fuel, fuelMax,
                        0xFFCC3D10, 0xFFFF8830);

        // === PROGRESS BAR (horizontale, fleche qui avance) ===
        // Zone = (94, 35) a (114, 49) = 20 wide x 14 tall
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(20.0F * prog / maxP);
            // Couleur tier-colored
            int tierCol = getTierAccentColor(tile.getTier());
            int tierColBright = getTierAccentBrightColor(tile.getTier());
            drawRect(x + 94, y + 41, x + 94 + fillW, y + 44, tierCol);
            if (fillW > 4) {
                // Pointe de fleche plus brillante
                drawRect(x + 94 + fillW - 2, y + 39,
                         x + 94 + fillW + 1, y + 46, tierColBright);
            }
        }

        // === RF BAR (verticale, fond -> haut) ===
        // Zone = (140, 17) a (148, 71) = 8 wide x 54 tall
        int rf = tile.getEnergyStored();
        int rfMax = tile.getMaxEnergy();
        fillBarVertical(x + 140, y + 17, 8, 54, rf, rfMax,
                        0xFFCC4444, 0xFFFF6666);
    }

    // === FOREGROUND (titre + textes) ===

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Titre tier-colored
        FurnaceTier tier = tile.getTier();
        String title = getTierDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = getTierAccentBrightColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 4, titleColor);

        // Label "Inventaire"
        fontRenderer.drawStringWithShadow("Inventaire", 8, 71, 0xFF8866AA);

        // Label "Vitesse x.xx" sous la progress bar
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawStringWithShadow(speedStr, 94, 52, 0xFFDD88FF);

        // === TOOLTIPS sur hover ===
        drawTooltips(mx, my);
    }

    // === drawScreen pour tooltips sur bars (hors des slots) ===

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
    }

    private void drawTooltips(int mx, int my) {
        int rmx = mx - guiLeft;
        int rmy = my - guiTop;

        // RF bar tooltip
        if (inRect(rmx, rmy, 140, 17, 8, 54)) {
            drawHoveringText(Arrays.asList(
                "\u00A7eEnergie",
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"
            ), rmx, rmy);
        }

        // Flamme fuel tooltip
        if (inRect(rmx, rmy, 79, 38, 11, 11)) {
            String fuelStatus = tile.getFuelRemaining() > 0
                ? tile.getFuelRemaining() + " operations restantes"
                : "Vide";
            drawHoveringText(Arrays.asList(
                "\u00A76Fuel",
                fuelStatus
            ), rmx, rmy);
        }

        // Progress bar tooltip
        if (inRect(rmx, rmy, 94, 35, 20, 14)) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(Arrays.asList(
                "\u00A7dProgression",
                pct + "%",
                "\u00A77Vitesse x" + tile.getTier().speedMultiplier
            ), rmx, rmy);
        }

        // Upgrade slots tooltips (4 slots a 152, 17+18*i)
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            int sx = 152, sy = 17 + up.slotIndex * 18;
            if (inRect(rmx, rmy, sx, sy, 16, 16)) {
                // Ne dessine le tooltip que si le slot est VIDE
                // (sinon le tooltip de l'item prendra le dessus)
                if (tile.getStackInSlot(TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex).isEmpty()) {
                    drawHoveringText(Arrays.asList(
                        "\u00A7b" + getUpgradeLabel(up),
                        "\u00A77" + getUpgradeHint(up),
                        "\u00A78Max stack: " + up.maxStackSize
                    ), rmx, rmy);
                }
            }
        }
    }

    // === HELPERS ===

    /** Barre verticale qui se remplit du bas vers le haut (style Mekanism). */
    private void fillBarVertical(int bx, int by, int bw, int bh,
                                  int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillH = (int)(bh * ratio);
        drawRect(bx, by + bh - fillH, bx + bw, by + bh, color);
        // Highlight en haut du fill
        if (fillH > 2) {
            drawRect(bx, by + bh - fillH,
                     bx + bw, by + bh - fillH + 2, shine);
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

    /** Couleur sombre tier pour la barre de progression (corps). */
    private int getTierAccentColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFF808090;
            case GOLD:       return 0xFFC8A032;
            case INVAR:      return 0xFF96A596;
            case EMERADIC:   return 0xFF46AA5A;
            case VOSSIUM_IV: return 0xFF783CAA;
            default:         return 0xFF5050A0;
        }
    }

    /** Couleur claire tier pour la pointe de fleche + titre. */
    private int getTierAccentBrightColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFFBBBBCC;
            case GOLD:       return 0xFFFFDD60;
            case INVAR:      return 0xFFBBDDBB;
            case EMERADIC:   return 0xFF80E690;
            case VOSSIUM_IV: return 0xFFC070FF;
            default:         return 0xFF8888FF;
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
            case IO_EXPANSION:  return "Augmente slots in/out (requiert RF)";
            case SPEED_BOOSTER: return "+30% vitesse, +40% conso par stack";
            case EFFICIENCY:    return "-8% conso par stack";
            default:            return "";
        }
    }
}
