package com.nexusabsolu.mod.archives.render;

import com.nexusabsolu.mod.archives.tiles.TileArchiveController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * TESR Archives Voss : rend la shell metallique du multiblock 5x2x1.
 *
 * <p>Design : quand structureFormed=true, dessine une enveloppe metallique
 * continue qui couvre la forme podium (5 blocs sol + 3 dessus, coins manquants).
 *
 * <p>Les 8 blocs physiques (Frame, Thermal, Controller, ItemInput, ItemOutput)
 * restent presents pour la logique mais leur rendu visuel est supprime via
 * Block.getRenderType = INVISIBLE quand le Controller est forme.
 *
 * <p><b>Geometrie du multiblock</b> (WEST_EAST axis, Controller origin 0,0,0) :
 * <pre>
 *   Couche 2 (y = 0)  :       [-1,0] [0,0] [+1,0]       <- Frame, Controller, Frame
 *   Couche 1 (y = -1) : [-2,-1][-1,-1][0,-1][+1,-1][+2,-1]  <- IN, Thermal, Thermal, Thermal, OUT
 * </pre>
 *
 * <p>En coords de RENDU (x,y,z translation donnee a render()) :
 * <ul>
 *   <li>x,y,z = coords de l'origine Controller relative a la camera</li>
 *   <li>La shell doit etre dessinee relative a cette origine, avec les
 *       offsets des blocs du multiblock</li>
 * </ul>
 *
 * <p>7 textures utilisees :
 * <ul>
 *   <li>archives_shell_front_bottom.png (80x16) - face avant couche 1</li>
 *   <li>archives_shell_front_top.png (48x16) - face avant couche 2</li>
 *   <li>archives_shell_top.png (48x16) - dessus (couche 2)</li>
 *   <li>archives_shell_bottom.png (80x16) - dessous (couche 1)</li>
 *   <li>archives_shell_side.png (80x32) - faces gauche et droite (vue de cote)</li>
 *   <li>archives_shell_end_in.png (16x32) - bout west (port IN)</li>
 *   <li>archives_shell_end_out.png (16x32) - bout east (port OUT)</li>
 * </ul>
 *
 * @since v1.0.306 (Archives Voss Sprint 1 polish)
 */
public class TESRArchiveController extends TileEntitySpecialRenderer<TileArchiveController> {

    private static final ResourceLocation TEX_FRONT_BOTTOM =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_front_bottom.png");
    private static final ResourceLocation TEX_FRONT_TOP =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_front_top.png");
    private static final ResourceLocation TEX_TOP =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_top.png");
    private static final ResourceLocation TEX_BOTTOM =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_bottom.png");
    private static final ResourceLocation TEX_SIDE =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_side.png");
    private static final ResourceLocation TEX_END_IN =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_end_in.png");
    private static final ResourceLocation TEX_END_OUT =
        new ResourceLocation("nexusabsolu", "textures/blocks/archives_shell_end_out.png");

    @Override
    public boolean isGlobalRenderer(TileArchiveController te) {
        return true;
    }

    @Override
    public void render(TileArchiveController te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isStructureFormed()) return;

        TileArchiveController.Axis axis = te.getStructureAxis();
        if (axis == null) return;

        GlStateManager.pushMatrix();
        // Se place en origine Controller (coin du bloc, pas centre)
        GlStateManager.translate(x, y, z);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();  // shell opaque
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);

        // Rotation selon axe pour orienter correctement la shell
        // WEST_EAST : axis X, pas de rotation
        // NORTH_SOUTH : on rotate 90 degres autour de Y
        if (axis == TileArchiveController.Axis.NORTH_SOUTH) {
            GlStateManager.translate(0.5, 0.0, 0.5);  // pivot centre bloc Controller
            GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5, 0.0, -0.5);
        }

        // Maintenant on est dans le referentiel WEST_EAST :
        //   x axis = longueur du multiblock (5 blocs, de -2 a +2)
        //   y axis = hauteur (couche 1 a y=-1 a 0, couche 2 a y=0 a 1)
        //   z axis = profondeur (1 bloc d'epaisseur, z=0 a 1)

        renderShell();

        GlStateManager.popMatrix();
    }

    /**
     * Dessine les 6 "sections" de la shell en utilisant les textures adaptees.
     *
     * <p>Convention de coords (apres translate/rotate au point de Controller) :
     * <ul>
     *   <li>x longitudinal : -2 (west/IN) a +3 (east/OUT exclusif)</li>
     *   <li>y vertical : -1 (sol couche 1) a +1 (toit couche 2)</li>
     *   <li>z profondeur : 0 (back) a 1 (front)</li>
     * </ul>
     */
    private void renderShell() {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // --- FACE AVANT (z = 1.0, front) ---
        // Couche 1 : x de -2 a +3, y de -1 a 0, z = 1
        bindTexture(TEX_FRONT_BOTTOM);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-2.0, -1.0, 1.0).tex(0, 1).endVertex();  // bas gauche
        buf.pos( 3.0, -1.0, 1.0).tex(1, 1).endVertex();  // bas droite
        buf.pos( 3.0,  0.0, 1.0).tex(1, 0).endVertex();  // haut droite
        buf.pos(-2.0,  0.0, 1.0).tex(0, 0).endVertex();  // haut gauche
        tess.draw();

        // Couche 2 : x de -1 a +2, y de 0 a 1, z = 1
        bindTexture(TEX_FRONT_TOP);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1.0, 0.0, 1.0).tex(0, 1).endVertex();
        buf.pos( 2.0, 0.0, 1.0).tex(1, 1).endVertex();
        buf.pos( 2.0, 1.0, 1.0).tex(1, 0).endVertex();
        buf.pos(-1.0, 1.0, 1.0).tex(0, 0).endVertex();
        tess.draw();

        // --- FACE ARRIERE (z = 0, back) ---
        // Meme textures mais UV miroir horizontal
        bindTexture(TEX_FRONT_BOTTOM);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos( 3.0, -1.0, 0.0).tex(0, 1).endVertex();
        buf.pos(-2.0, -1.0, 0.0).tex(1, 1).endVertex();
        buf.pos(-2.0,  0.0, 0.0).tex(1, 0).endVertex();
        buf.pos( 3.0,  0.0, 0.0).tex(0, 0).endVertex();
        tess.draw();

        bindTexture(TEX_FRONT_TOP);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos( 2.0, 0.0, 0.0).tex(0, 1).endVertex();
        buf.pos(-1.0, 0.0, 0.0).tex(1, 1).endVertex();
        buf.pos(-1.0, 1.0, 0.0).tex(1, 0).endVertex();
        buf.pos( 2.0, 1.0, 0.0).tex(0, 0).endVertex();
        tess.draw();

        // --- DESSUS (y = 1.0, top) ---
        // Seulement couche 2 : x de -1 a +2, z de 0 a 1
        bindTexture(TEX_TOP);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1.0, 1.0, 0.0).tex(0, 0).endVertex();
        buf.pos( 2.0, 1.0, 0.0).tex(1, 0).endVertex();
        buf.pos( 2.0, 1.0, 1.0).tex(1, 1).endVertex();
        buf.pos(-1.0, 1.0, 1.0).tex(0, 1).endVertex();
        tess.draw();

        // --- DESSOUS (y = -1.0, bottom) ---
        // Couche 1 : x de -2 a +3, z de 0 a 1
        bindTexture(TEX_BOTTOM);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-2.0, -1.0, 1.0).tex(0, 0).endVertex();
        buf.pos( 3.0, -1.0, 1.0).tex(1, 0).endVertex();
        buf.pos( 3.0, -1.0, 0.0).tex(1, 1).endVertex();
        buf.pos(-2.0, -1.0, 0.0).tex(0, 1).endVertex();
        tess.draw();

        // --- MARCHES (transitions couche 1 / couche 2) ---
        // Entre IN (x=-2..-1) et Frame gauche (x=-1..0), il y a une marche au y=0
        // Cette marche est visible par le dessus (petite portion)
        // Surface superieure de IN : y=0, x=-2 a -1, z=0 a 1
        bindTexture(TEX_TOP);  // meme texture, sera visible sur 1 bloc
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-2.0, 0.0, 0.0).tex(0, 0).endVertex();
        buf.pos(-1.0, 0.0, 0.0).tex(0.333, 0).endVertex();
        buf.pos(-1.0, 0.0, 1.0).tex(0.333, 1).endVertex();
        buf.pos(-2.0, 0.0, 1.0).tex(0, 1).endVertex();
        tess.draw();

        // Surface superieure de OUT : y=0, x=2 a 3, z=0 a 1
        bindTexture(TEX_TOP);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos( 2.0, 0.0, 0.0).tex(0.666, 0).endVertex();
        buf.pos( 3.0, 0.0, 0.0).tex(1, 0).endVertex();
        buf.pos( 3.0, 0.0, 1.0).tex(1, 1).endVertex();
        buf.pos( 2.0, 0.0, 1.0).tex(0.666, 1).endVertex();
        tess.draw();

        // Face verticale west de la couche 2 (mur vertical entre marche IN et frame gauche)
        // De y=0 a y=1, x=-1, z=0 a 1
        bindTexture(TEX_END_IN);  // reutilise end_in pour ce mur
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1.0, 0.0, 0.0).tex(0, 1).endVertex();
        buf.pos(-1.0, 0.0, 1.0).tex(1, 1).endVertex();
        buf.pos(-1.0, 1.0, 1.0).tex(1, 0).endVertex();
        buf.pos(-1.0, 1.0, 0.0).tex(0, 0).endVertex();
        tess.draw();

        // Face verticale east de la couche 2 (mur vertical entre frame droite et marche OUT)
        bindTexture(TEX_END_OUT);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(2.0, 0.0, 1.0).tex(0, 1).endVertex();
        buf.pos(2.0, 0.0, 0.0).tex(1, 1).endVertex();
        buf.pos(2.0, 1.0, 0.0).tex(1, 0).endVertex();
        buf.pos(2.0, 1.0, 1.0).tex(0, 0).endVertex();
        tess.draw();

        // --- FACE COTE WEST (x = -2.0, bout IN, juste couche 1) ---
        // y de -1 a 0, z de 0 a 1
        bindTexture(TEX_END_IN);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-2.0, -1.0, 1.0).tex(0, 1).endVertex();
        buf.pos(-2.0, -1.0, 0.0).tex(1, 1).endVertex();
        buf.pos(-2.0,  0.0, 0.0).tex(1, 0.5).endVertex();  // on utilise juste moitie bas de texture
        buf.pos(-2.0,  0.0, 1.0).tex(0, 0.5).endVertex();
        tess.draw();

        // --- FACE COTE EAST (x = +3.0, bout OUT, juste couche 1) ---
        bindTexture(TEX_END_OUT);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(3.0, -1.0, 0.0).tex(0, 1).endVertex();
        buf.pos(3.0, -1.0, 1.0).tex(1, 1).endVertex();
        buf.pos(3.0,  0.0, 1.0).tex(1, 0.5).endVertex();
        buf.pos(3.0,  0.0, 0.0).tex(0, 0.5).endVertex();
        tess.draw();

        // --- FACES LATERALES (bande continue sur longueur) ---
        // Cote LEFT (z = 0, dans referentiel local = nord si WEST_EAST)
        bindTexture(TEX_SIDE);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        // Couche 1 : x=-2 a +3, y=-1 a 0
        buf.pos(-2.0, -1.0, 0.0).tex(0, 1).endVertex();
        buf.pos( 3.0, -1.0, 0.0).tex(1, 1).endVertex();
        buf.pos( 3.0,  0.0, 0.0).tex(1, 0.5).endVertex();
        buf.pos(-2.0,  0.0, 0.0).tex(0, 0.5).endVertex();
        tess.draw();

        // Couche 2 sur le meme cote : x=-1 a +2, y=0 a 1
        // On utilise la moitie haute de la texture SIDE
        bindTexture(TEX_SIDE);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1.0, 0.0, 0.0).tex(0.2, 0.5).endVertex();
        buf.pos( 2.0, 0.0, 0.0).tex(0.8, 0.5).endVertex();
        buf.pos( 2.0, 1.0, 0.0).tex(0.8, 0).endVertex();
        buf.pos(-1.0, 1.0, 0.0).tex(0.2, 0).endVertex();
        tess.draw();

        // Cote RIGHT (z = 1, miroir du left)
        bindTexture(TEX_SIDE);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos( 3.0, -1.0, 1.0).tex(0, 1).endVertex();
        buf.pos(-2.0, -1.0, 1.0).tex(1, 1).endVertex();
        buf.pos(-2.0,  0.0, 1.0).tex(1, 0.5).endVertex();
        buf.pos( 3.0,  0.0, 1.0).tex(0, 0.5).endVertex();
        tess.draw();

        // Couche 2 cote right
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos( 2.0, 0.0, 1.0).tex(0.2, 0.5).endVertex();
        buf.pos(-1.0, 0.0, 1.0).tex(0.8, 0.5).endVertex();
        buf.pos(-1.0, 1.0, 1.0).tex(0.8, 0).endVertex();
        buf.pos( 2.0, 1.0, 1.0).tex(0.2, 0).endVertex();
        tess.draw();
    }

    private void bindTexture(ResourceLocation tex) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}
