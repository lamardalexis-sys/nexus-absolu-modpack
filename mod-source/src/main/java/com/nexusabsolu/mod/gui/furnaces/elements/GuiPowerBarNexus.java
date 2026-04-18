package com.nexusabsolu.mod.gui.furnaces.elements;

import com.nexusabsolu.mod.Reference;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Barre d'energie verticale (RF) + mini-tube flamme pour l'indicateur fuel.
 * Inspire de mekanism.client.gui.element.GuiPowerBar.
 *
 * Sprite sheet textures/gui/elements/guipowerbar_nexus.png :
 *   POWER_BAR_EMPTY  (x=0,  y=0, 14x54)
 *   POWER_BAR_FILLED (x=14, y=0, 14x54) - degrade rouge (bas) -> jaune (haut)
 *   FLAME_EMPTY      (x=28, y=0, 14x13)
 *   FLAME_FILLED     (x=42, y=0, 14x13)
 */
@SideOnly(Side.CLIENT)
public class GuiPowerBarNexus {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/elements/guipowerbar_nexus.png");

    public static final int BAR_WIDTH = 14;
    public static final int BAR_HEIGHT = 54;
    public static final int FLAME_WIDTH = 14;
    public static final int FLAME_HEIGHT = 13;

    /**
     * Dessine la barre RF verticale (vide + remplie selon ratio).
     * La partie remplie croit du bas vers le haut.
     *
     * @param gui instance Gui
     * @param x position x ABSOLUE
     * @param y position y ABSOLUE (haut de la barre)
     * @param energy energie actuelle
     * @param maxEnergy capacite max
     */
    public static void drawPowerBar(Gui gui, int x, int y, int energy, int maxEnergy) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // 1. Silhouette vide
        gui.drawTexturedModalRect(x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT);

        // 2. Partie remplie (du bas vers le haut)
        if (maxEnergy > 0 && energy > 0) {
            float ratio = Math.min(1.0F, (float) energy / maxEnergy);
            int fillH = (int) (BAR_HEIGHT * ratio);
            if (fillH > 0) {
                // Offset dans le sprite sheet : partie filled est a x=14
                // Comme on remplit du bas, on drawTexture avec y ajuste
                int yOffset = BAR_HEIGHT - fillH;
                gui.drawTexturedModalRect(
                    x, y + yOffset,
                    BAR_WIDTH, yOffset,   // offset dans le sheet
                    BAR_WIDTH, fillH);
            }
        }
    }

    /**
     * Dessine l'indicateur flamme (mini-tube vertical, plein si fuel > 0).
     *
     * @param gui instance Gui
     * @param x position x ABSOLUE
     * @param y position y ABSOLUE
     * @param fuelActive true si un combustible brule (flamme visible), false sinon
     */
    public static void drawFlame(Gui gui, int x, int y, boolean fuelActive) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int sheetX = fuelActive ? 42 : 28;
        gui.drawTexturedModalRect(x, y, sheetX, 0, FLAME_WIDTH, FLAME_HEIGHT);
    }
}
