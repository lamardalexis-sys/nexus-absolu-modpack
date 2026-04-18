package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.furnaces.elements.GuiPowerBarNexus;
import com.nexusabsolu.mod.gui.furnaces.elements.GuiProgressNexus;
import com.nexusabsolu.mod.gui.furnaces.elements.GuiSlotNexus;
import com.nexusabsolu.mod.gui.furnaces.elements.GuiSlotNexus.SlotOverlay;
import com.nexusabsolu.mod.gui.furnaces.elements.GuiSlotNexus.SlotType;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

/**
 * GUI Furnaces Nexus - refactor style Mekanism (v3, texture decomposee).
 *
 * Pattern:
 *  1. Texture de fond minimaliste (vanilla gris beige + zone inventaire)
 *  2. Elements superposes : GuiSlotNexus, GuiProgressNexus, GuiPowerBarNexus
 *  3. Foreground : titre + tooltips
 *
 * Positions slots (coordonnees container, sans guiLeft/guiTop) :
 *   INPUT   (56, 17)
 *   FUEL    (56, 53)
 *   OUTPUT  (111, 30) - 26x26 (OUTPUT_LARGE)
 *   UPGRADES (152, 17/35/53/71) - 4 verticaux
 *   PROGRESS ARROW (79, 35) - 28x11
 *   POWER BAR (140, 15) - 14x54
 *   FLAME indicator (77, 37) - 14x13 entre input et fuel
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

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        int x = guiLeft;
        int y = guiTop;

        // === 1. Texture de fond (vanilla gris + inventaire) ===
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // === 2. Slots par-dessus (style Mekanism : decomposes) ===
        // Input slot
        GuiSlotNexus.draw(this, SlotType.INPUT, x + 55, y + 16);
        // Fuel slot avec overlay flamme
        GuiSlotNexus.draw(this, SlotType.FUEL, SlotOverlay.FLAME, x + 55, y + 52);
        // Output large slot (26x26)
        GuiSlotNexus.draw(this, SlotType.OUTPUT_LARGE, x + 111, y + 30);
        // 4 Upgrade slots a droite avec cadre violet Nexus
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            GuiSlotNexus.draw(this, SlotType.UPGRADE,
                x + 151, y + 16 + up.slotIndex * 18);
        }

        // === 3. Power bar (RF) verticale ===
        GuiPowerBarNexus.drawPowerBar(this, x + 139, y + 15,
            tile.getEnergyStored(), tile.getMaxEnergy());

        // === 4. Flamme indicator (au-dessus fuel slot) ===
        boolean fuelActive = tile.getFuelRemaining() > 0
            || (tile.getEnergyStored() > 0 && tile.getCookProgress() > 0);
        GuiPowerBarNexus.drawFlame(this, x + 81, y + 38, fuelActive);

        // === 5. Progress arrow (entre fuel et output) ===
        float progress = 0;
        if (tile.getMaxCookTime() > 0) {
            progress = (float) tile.getCookProgress() / tile.getMaxCookTime();
        }
        GuiProgressNexus.draw(this, tile.getTier(), x + 79, y + 35, progress);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        FurnaceTier tier = tile.getTier();
        String title = getTierDisplayName(tier);
        int titleColor = 0x404040;  // style vanilla Mekanism (sombre)

        // Titre centre
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawString(title, (xSize - tw) / 2, 6, titleColor);

        // Label "Inventaire" en bas
        fontRenderer.drawString("Inventaire", 8, ySize - 96 + 2, titleColor);

        // Vitesse sous la progress bar
        String speedStr = "x" + tier.speedMultiplier;
        fontRenderer.drawString(speedStr, 79, 50, 0x606060);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        drawTooltips(mx, my);
    }

    private void drawTooltips(int mx, int my) {
        int rmx = mx - guiLeft;
        int rmy = my - guiTop;

        // Power bar tooltip (14x54 at 139, 15)
        if (inRect(rmx, rmy, 139, 15, 14, 54)) {
            drawHoveringText(Arrays.asList(
                "\u00A7eEnergie",
                tile.getEnergyStored() + " / " + tile.getMaxEnergy() + " RF"
            ), rmx, rmy);
        }

        // Flame indicator tooltip (14x13 at 81, 38)
        if (inRect(rmx, rmy, 81, 38, 14, 13)) {
            String status = tile.getFuelRemaining() > 0
                ? tile.getFuelRemaining() + " operations restantes"
                : (tile.getEnergyStored() > 0 ? "Mode RF actif" : "Vide");
            drawHoveringText(Arrays.asList(
                "\u00A76Combustible",
                "\u00A77" + status
            ), rmx, rmy);
        }

        // Progress arrow tooltip (28x11 at 79, 35)
        if (inRect(rmx, rmy, 79, 35, 28, 11)) {
            int pct = tile.getMaxCookTime() > 0
                ? tile.getCookProgress() * 100 / tile.getMaxCookTime() : 0;
            drawHoveringText(Arrays.asList(
                "\u00A7dProgression",
                pct + "%",
                "\u00A77Vitesse x" + tile.getTier().speedMultiplier
            ), rmx, rmy);
        }

        // Upgrade slots tooltips (18x18 at 151, 16+18*i)
        for (FurnaceUpgrade up : FurnaceUpgrade.values()) {
            int sx = 151, sy = 16 + up.slotIndex * 18;
            if (inRect(rmx, rmy, sx, sy, 18, 18)) {
                int slotIdx = TileFurnaceNexus.SLOT_UPGRADE_BASE + up.slotIndex;
                // Tooltip seulement si slot vide (sinon tooltip de l'item)
                if (tile.getStackInSlot(slotIdx).isEmpty()) {
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
            case SPEED_BOOSTER: return "+30% vitesse, +40% conso / stack";
            case EFFICIENCY:    return "-8% conso / stack";
            default:            return "";
        }
    }
}
