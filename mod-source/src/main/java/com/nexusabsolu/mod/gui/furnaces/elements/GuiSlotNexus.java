package com.nexusabsolu.mod.gui.furnaces.elements;

import com.nexusabsolu.mod.Reference;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Element GUI reutilisable pour dessiner un slot au-dessus de la texture de fond.
 * Inspire directement de mekanism.client.gui.element.GuiSlot (Mekanism 1.12).
 *
 * Layout de la sprite sheet (textures/gui/elements/guislot_nexus.png) :
 *   NORMAL     (x=0,   y=0, 18x18)
 *   POWER      (x=18,  y=0, 18x18)
 *   INPUT      (x=36,  y=0, 18x18)
 *   FUEL       (x=54,  y=0, 18x18)
 *   UPGRADE    (x=72,  y=0, 18x18)
 *   OUTPUT_LG  (x=90,  y=0, 26x26)
 *
 * Overlays (y=18) :
 *   POWER_ICON (x=36, y=18, 18x18)
 *   FLAME_ICON (x=54, y=18, 18x18)
 */
@SideOnly(Side.CLIENT)
public class GuiSlotNexus {

    public static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/elements/guislot_nexus.png");

    public enum SlotType {
        NORMAL(18, 18, 0, 0),
        POWER(18, 18, 18, 0),
        INPUT(18, 18, 36, 0),
        FUEL(18, 18, 54, 0),
        UPGRADE(18, 18, 72, 0),
        OUTPUT_LARGE(26, 26, 90, 0);

        public final int width, height, textureX, textureY;
        SlotType(int w, int h, int tx, int ty) {
            this.width = w; this.height = h;
            this.textureX = tx; this.textureY = ty;
        }
    }

    public enum SlotOverlay {
        POWER(18, 18, 36, 18),
        FLAME(18, 18, 54, 18);

        public final int width, height, textureX, textureY;
        SlotOverlay(int w, int h, int tx, int ty) {
            this.width = w; this.height = h;
            this.textureX = tx; this.textureY = ty;
        }
    }

    /**
     * Draws a slot at screen position (x, y).
     * @param gui tout Gui instance (utilise pour bindTexture)
     * @param type type de slot (forme + couleur)
     * @param overlay overlay optionnel (icone power/flame), null = aucun
     * @param x position x ABSOLUE (guiLeft + offset)
     * @param y position y ABSOLUE (guiTop + offset)
     */
    public static void draw(Gui gui, SlotType type, SlotOverlay overlay, int x, int y) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // Slot de base
        gui.drawTexturedModalRect(x, y, type.textureX, type.textureY, type.width, type.height);
        // Overlay eventuel (centre sur le slot)
        if (overlay != null) {
            int ox = x + (type.width - overlay.width) / 2;
            int oy = y + (type.height - overlay.height) / 2;
            gui.drawTexturedModalRect(ox, oy, overlay.textureX, overlay.textureY,
                                       overlay.width, overlay.height);
        }
    }

    /** Version sans overlay. */
    public static void draw(Gui gui, SlotType type, int x, int y) {
        draw(gui, type, null, x, y);
    }
}
