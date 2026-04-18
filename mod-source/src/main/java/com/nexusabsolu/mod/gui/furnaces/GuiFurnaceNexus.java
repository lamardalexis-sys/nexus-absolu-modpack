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
import java.util.Collections;

/**
 * GUI Furnaces Nexus - pattern KRDA (texture PNG complete + fillBar overlay).
 *
 * Layout (xSize=176, ySize=166) :
 *   INPUT slot    (55, 16)  18x18
 *   FUEL slot     (55, 52)  18x18
 *   FLAME tube    (79, 37)  12x13  -- indicateur combustible
 *   PROGRESS zone (94, 38)  20x10  -- fleche se remplit
 *   OUTPUT slot   (116, 30) 26x26
 *   RF tube       (152, 15) 6x58
 *   UPGRADES      (161, 15/33/51/69) 18x18 x 4
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

    // === BACKGROUND ===

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft;
        int y = guiTop;

        // === BARRE RF (verticale, remplit du bas) ===
        // Tube : x=152, y=15, 6x58 dans la texture
        fillBarVertical(x + 152, y + 15, 6, 58,
            tile.getEnergyStored(), tile.getMaxEnergy(),
            0xFFCC4444, 0xFFFF6666);

        // === INDICATEUR FLAMME (vertical, fuel restant) ===
        // Tube : x=79, y=37, 12x13
        int fuel = tile.getFuelRemaining();
        int fuelMax = Math.max(1, fuel);  // dynamique (dernier coal donne le max)
        if (fuel > 0) {
            fillBarVertical(x + 79, y + 37, 12, 13, fuel, fuelMax,
                0xFFCC3D10, 0xFFFF8830);
        } else if (tile.getEnergyStored() > 0 && tile.getCookProgress() > 0) {
            // Mode RF actif : flamme bleue subtile indiquant que ca tourne
            fillBarVertical(x + 79, y + 37, 12, 13, 1, 1,
                0xFF4455CC, 0xFF7788FF);
        }

        // === PROGRESS (fleche horizontale qui avance) ===
        // Zone : x=94, y=38, 20x10
        int prog = tile.getCookProgress();
        int maxP = tile.getMaxCookTime();
        if (maxP > 0 && prog > 0) {
            int fillW = (int)(20.0F * prog / maxP);
            // Couleur tier
            int tierCol = getTierProgressColor(tile.getTier());
            int tierBright = getTierProgressBright(tile.getTier());
            // Corps
            drawRect(x + 94, y + 41, x + 94 + fillW, y + 45, tierCol);
            // Highlight
            if (fillW > 2) {
                drawRect(x + 94, y + 41, x + 94 + fillW, y + 42, tierBright);
            }
        }
    }

    // === FOREGROUND (titre + labels + % cuisson) ===

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        FurnaceTier tier = tile.getTier();

        // Titre centre
        String title = getTierDisplayName(tier);
        int tw = fontRenderer.getStringWidth(title);
        int titleColor = getTierTitleColor(tier);
        fontRenderer.drawStringWithShadow(title, (xSize - tw) / 2.0F, 4, titleColor);

        // Label E (energie) en haut du tube RF
        fontRenderer.drawStringWithShadow("E", 151, 6, 0xFFFF6666);

        // Label "Inventaire" (coherent avec KRDA)
        fontRenderer.drawStringWithShadow("Inventaire", 8, 83, 0xFF8866AA);

        // Vitesse sous la progress bar
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawStringWithShadow(speedStr, 96, 52, 0xFF8866AA);

        // Arrows decoratives (style KRDA) autour de progress
        fontRenderer.drawStringWithShadow("\u00bb", 86, 40, titleColor);
        fontRenderer.drawStringWithShadow("\u00bb", 114, 40, titleColor);
    }

    // === drawScreen pour tooltips custom ===

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

        // RF bar tooltip
        if (inRect(mx, my, x + 152, y + 15, 6, 58)) {
            drawHoveringText(Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"), mx, my);
        }

        // Flamme fuel tooltip
        if (inRect(mx, my, x + 79, y + 37, 12, 13)) {
            String status = tile.getFuelRemaining() > 0
                ? tile.getFuelRemaining() + " operations"
                : (tile.getEnergyStored() > 0 ? "Mode RF" : "Vide");
            drawHoveringText(Collections.singletonList(
                "Combustible: " + status), mx, my);
        }

        // Progress bar tooltip
        if (inRect(mx, my, x + 94, y + 38, 20, 10)) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(Collections.singletonList(
                "Cuisson: " + pct + "%"), mx, my);
        }

        // Upgrades tooltips (seulement si vide)
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            int sx = x + 161;
            int sy = y + 15 + up.slotIndex * 18;
            if (inRect(mx, my, sx, sy, 18, 18)) {
                int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
                if (tile.getStackInSlot(slotIdx).isEmpty()) {
                    drawHoveringText(Arrays.asList(
                        "\u00A7b" + getUpgradeLabel(up),
                        "\u00A77" + getUpgradeHint(up)
                    ), mx, my);
                }
            }
        }
    }

    // === HELPERS (identiques au pattern KRDA) ===

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

    // === Customisation par tier ===

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
