package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * Overlay client-side qui teinte l'ecran selon la phase Manifoldine.
 *
 * IMPORTANT — Bug fix v1.0.327 : on s'abonne a HOTBAR (et pas ALL) parce que
 * RenderGameOverlayEvent.Post avec ElementType.ALL n'est pas garanti d'etre
 * appele en MC 1.12.2 / Forge. HOTBAR est le dernier element rendu donc en
 * dessinant Post-HOTBAR on est sur d'etre par dessus tout le HUD.
 *
 * PHASE 1 (0-4 min) — Plasma fractal DMT 4-couleurs :
 *   Grille 24x16, chaque cell calcule sa couleur via une fonction plasma
 *   (somme de 3 sinusoides 2D animees dans le temps). Le plasma value [0,4]
 *   indexe la palette DMT (magenta / cyan / lime / or). Alpha 0.55 pour
 *   un effet bien visible mais pas opaque.
 *
 * PHASE 2 (4-5 min) — Negatif total :
 *   Inverse les couleurs via blend GL_ONE_MINUS_DST_COLOR. Le trip vrille.
 */
@SideOnly(Side.CLIENT)
public class ManifoldOverlayHandler {

    /** Palette DMT scientifique : 4 couleurs a 90 deg sur le cercle chromatique. */
    private static final float[][] DMT_PALETTE = {
        {1.00f, 0.08f, 0.58f},  // #FF1493 magenta vif
        {0.00f, 1.00f, 1.00f},  // #00FFFF cyan pur
        {0.50f, 1.00f, 0.00f},  // #7FFF00 lime chartreuse
        {1.00f, 0.84f, 0.00f}   // #FFD700 or
    };

    private static final float ALPHA = 0.55f;

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        // FIX: HOTBAR (dernier element rendu en HUD), pas ALL
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        long now = player.world.getTotalWorldTime();
        int phase = ManifoldClientState.getCurrentPhase(now);
        if (phase == ManifoldEffectHandler.PHASE_NONE) return;
        if (phase == ManifoldEffectHandler.PHASE_FATIGUE) return;

        ScaledResolution res = new ScaledResolution(mc);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        if (phase == ManifoldEffectHandler.PHASE_ACTIVE) {
            renderPlasmaFractal(w, h, now);
        } else if (phase == ManifoldEffectHandler.PHASE_NEGATIVE) {
            renderNegative(w, h);
        }

        // Debug indicator — confirme que le packet est bien arrive
        renderDebugIndicator(mc.fontRenderer, phase);
    }

    /**
     * PLASMA FRACTAL DMT — algo classique de plasma effect (demo scene 90s).
     *
     * Pour chaque cell de la grille, on calcule :
     *   v = sin(x*fx1 + t*tx1) + sin(y*fy1 + t*ty1) + sin((x+y)*fxy + t*txy)
     *
     * v est dans [-3, 3], on le normalise [0, 4] pour indexer la palette DMT
     * (4 couleurs). On lerp entre les 2 couleurs adjacentes pour un gradient
     * lisse.
     */
    private void renderPlasmaFractal(int w, int h, long t) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        int cols = 24;
        int rows = 16;
        float cellW = w / (float) cols;
        float cellH = h / (float) rows;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double tt = t * 0.05;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // 3 sinusoides 2D animees → effet plasma fluide
                double v =
                    Math.sin(col * 0.45 + tt * 1.2) +
                    Math.sin(row * 0.55 + tt * 0.9) +
                    Math.sin((col + row) * 0.3 + tt * 1.5);
                // Normalise [-3, 3] → [0, 4]
                double norm = (v + 3.0) / 1.5;
                int idxA = ((int) norm) % 4;
                int idxB = (idxA + 1) % 4;
                double frac = norm - Math.floor(norm);

                // Lerp entre 2 couleurs adjacentes (gradient lisse)
                float[] cA = DMT_PALETTE[idxA];
                float[] cB = DMT_PALETTE[idxB];
                float r = (float) (cA[0] * (1.0 - frac) + cB[0] * frac);
                float g = (float) (cA[1] * (1.0 - frac) + cB[1] * frac);
                float b = (float) (cA[2] * (1.0 - frac) + cB[2] * frac);

                float x0 = col * cellW;
                float y0 = row * cellH;
                float x1 = x0 + cellW;
                float y1 = y0 + cellH;

                buf.pos(x0, y1, 0).color(r, g, b, ALPHA).endVertex();
                buf.pos(x1, y1, 0).color(r, g, b, ALPHA).endVertex();
                buf.pos(x1, y0, 0).color(r, g, b, ALPHA).endVertex();
                buf.pos(x0, y0, 0).color(r, g, b, ALPHA).endVertex();
            }
        }
        tess.draw();

        // Reset state
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * PHASE 2 : negatif total via blend ONE_MINUS_DST_COLOR.
     */
    private void renderNegative(int w, int h) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
            GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(0, h, 0).endVertex();
        buf.pos(w, h, 0).endVertex();
        buf.pos(w, 0, 0).endVertex();
        buf.pos(0, 0, 0).endVertex();
        tess.draw();

        // Reset
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Petit texte en haut a gauche pour confirmer que l'effet est actif.
     * Si tu vois ca → le packet est bien arrive et le handler s'execute.
     * Si tu vois pas ca → probleme de packet/handler.
     */
    private void renderDebugIndicator(FontRenderer font, int phase) {
        String label;
        int color;
        switch (phase) {
            case ManifoldEffectHandler.PHASE_ACTIVE:
                label = "MANIFOLDINE ACTIVE";
                color = 0xFF1493;  // magenta
                break;
            case ManifoldEffectHandler.PHASE_NEGATIVE:
                label = "MANIFOLDINE — VRILLE";
                color = 0x00FFFF;  // cyan (ironique : negatif rend en orange)
                break;
            default:
                return;
        }
        font.drawStringWithShadow(label, 8, 8, color);
    }
}
