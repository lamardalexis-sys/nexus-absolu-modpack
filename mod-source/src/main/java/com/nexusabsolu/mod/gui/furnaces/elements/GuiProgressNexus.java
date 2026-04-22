package com.nexusabsolu.mod.gui.furnaces.elements;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Composant GUI pour la fleche progress bar, variantes par tier.
 * Inspire de mekanism.client.gui.element.GuiProgress.
 *
 * Sprite sheet textures/gui/elements/guiprogress_nexus.png :
 *   Row 0 (y=0):  IRON      (empty x=0-27, filled x=28-55)
 *   Row 1 (y=11): GOLD
 *   Row 2 (y=22): INVARIUM  (v1.0.268 : renomme de INVAR)
 *   Row 3 (y=33): EMERADIC
 *   Row 4 (y=44): VOSSIUM_IV
 *
 * Chaque fleche fait 28x11 px.
 *
 * v1.0.268 : fichier legacy conserve pour compatibilite. Le code actuel
 * dessine la progress arrow directement via drawRect dans GuiFurnaceNexus,
 * donc cette classe n'est plus appelee. Conservee au cas ou un autre GUI
 * voudrait l'utiliser plus tard (ex: condenseur T2).
 */
@SideOnly(Side.CLIENT)
public class GuiProgressNexus {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/elements/guiprogress_nexus.png");

    public static final int ARROW_WIDTH = 28;
    public static final int ARROW_HEIGHT = 11;

    /**
     * Dessine la fleche progress (vide + partie remplie selon progress).
     *
     * @param gui instance Gui pour drawTexturedModalRect
     * @param tier tier du furnace (selectionne la couleur de la fleche)
     * @param x position x ABSOLUE
     * @param y position y ABSOLUE
     * @param progress 0.0 - 1.0 (fraction de la fleche remplie)
     */
    public static void draw(Gui gui, FurnaceTier tier, int x, int y, float progress) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int texY = getTierRowY(tier);

        // 1. Dessine la silhouette vide (partie gauche du sprite sheet)
        gui.drawTexturedModalRect(x, y, 0, texY, ARROW_WIDTH, ARROW_HEIGHT);

        // 2. Dessine la partie remplie par-dessus (partie droite du sheet = x=28 dans le sheet)
        if (progress > 0) {
            int fillWidth = (int) (ARROW_WIDTH * Math.min(1.0F, progress));
            if (fillWidth > 0) {
                gui.drawTexturedModalRect(x, y, ARROW_WIDTH, texY, fillWidth, ARROW_HEIGHT);
            }
        }
    }

    private static int getTierRowY(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0;
            case GOLD:       return 11;
            case INVARIUM:   return 22;
            case EMERADIC:   return 33;
            case VOSSIUM_IV: return 44;
            default:         return 0;
        }
    }
}
