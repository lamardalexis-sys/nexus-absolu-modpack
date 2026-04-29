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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

/**
 * Overlay client-side : composite multi-couches pour effet DMT.
 *
 * COUCHE 1 — Mandala fractal (textures preconçues 512x512 ultra saturees) :
 *   4 frames qui fadent l'une vers l'autre toutes les 5s. Rotation continue
 *   du mandala. Render en mode "lighten" (= max(src, dst)) pour faire briller
 *   les couleurs neon sur le fond.
 *
 * COUCHE 2 — Plasma fractal (algo) :
 *   Grille 24x16 calculee live, palette 8-couleurs DMT, alpha 0.4 pour
 *   superposition harmonieuse. Donne le mouvement organique fluide.
 *
 * COUCHE 3 — Pulse central :
 *   Cercle expansif depuis le centre toutes les ~2s (battement de coeur).
 *
 * En PHASE 2 (negatif) : tout est inverse via blend ONE_MINUS_DST_COLOR.
 *
 * Bug fix : ElementType.HOTBAR (pas ALL) car ALL n'est pas appele en Post.
 */
@SideOnly(Side.CLIENT)
public class ManifoldOverlayHandler {

    /** Palette DMT 8 couleurs ULTRA NEON pour le plasma. */
    private static final float[][] DMT_PALETTE = {
        {1.00f, 0.00f, 0.78f},  // magenta
        {1.00f, 0.39f, 0.00f},  // orange
        {1.00f, 0.86f, 0.00f},  // or
        {0.55f, 1.00f, 0.00f},  // lime
        {0.00f, 1.00f, 0.78f},  // turquoise
        {0.00f, 0.86f, 1.00f},  // cyan
        {0.71f, 0.20f, 1.00f},  // violet
        {1.00f, 0.00f, 0.39f}   // rose
    };

    /** Textures mandala. */
    private static final ResourceLocation[] MANDALA_TEXTURES = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_3.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_4.png")
    };

    /** Duree d'un frame mandala (en ticks). */
    private static final int FRAME_DURATION = 100;  // 5 secondes

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
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

        boolean negative = (phase == ManifoldEffectHandler.PHASE_NEGATIVE);

        // === COUCHE 1 : Mandala texture ===
        renderMandala(mc, w, h, now, negative);

        // === COUCHE 2 : Plasma fractal par dessus ===
        renderPlasma(w, h, now, negative);

        // === COUCHE 3 : Pulse central ===
        renderCenterPulse(w, h, now, negative);

        // === DEBUG : indicateur ===
        renderDebugIndicator(mc.fontRenderer, phase);
    }

    /**
     * Mandala fractal : 2 textures qui fadent en crossfade + rotation continue.
     * Rendu en "blend additive" (src + dst) pour faire briller les couleurs
     * neon par dessus le monde sombre.
     */
    private void renderMandala(Minecraft mc, int w, int h, long t, boolean negative) {
        // Frame index avec crossfade
        int frame = (int)((t / FRAME_DURATION) % MANDALA_TEXTURES.length);
        int nextFrame = (frame + 1) % MANDALA_TEXTURES.length;
        float fadeFrac = (t % FRAME_DURATION) / (float) FRAME_DURATION;

        // Rotation : 1 tour toutes les 30s (600 ticks)
        float rotation = (t % 600) / 600.0f * 360.0f;

        // Taille du mandala : couvre l'ecran avec marge (sqrt(2) du diagonale
        // pour qu'apres rotation 45deg ca couvre toujours)
        float mandalaSize = (float) Math.max(w, h) * 1.5f;
        float cx = w / 2.0f;
        float cy = h / 2.0f;

        GlStateManager.enableBlend();
        // Blend additive — les couleurs neon "brillent" par dessus le monde sombre
        // mode "lighten" : dst = max(src, dst)
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        float alpha1 = (1.0f - fadeFrac) * (negative ? 0.55f : 0.65f);
        float alpha2 = fadeFrac * (negative ? 0.55f : 0.65f);

        // Render frame courant
        drawRotatedTexture(mc, MANDALA_TEXTURES[frame],
            cx, cy, mandalaSize, rotation, alpha1, negative);

        // Render frame suivant en crossfade
        drawRotatedTexture(mc, MANDALA_TEXTURES[nextFrame],
            cx, cy, mandalaSize, rotation + 12.0f, alpha2, negative);

        // Reset
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
    }

    /**
     * Dessine une texture en quad rotated autour de (cx, cy).
     */
    private void drawRotatedTexture(Minecraft mc, ResourceLocation tex,
                                     float cx, float cy, float size,
                                     float rotation, float alpha, boolean negative) {
        mc.getTextureManager().bindTexture(tex);

        if (negative) {
            // En negatif : couleur inversee
            GlStateManager.color(0.3f, 0.1f, 0.5f, alpha);
        } else {
            GlStateManager.color(1f, 1f, 1f, alpha);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, 0);
        GlStateManager.rotate(rotation, 0, 0, 1);

        float half = size / 2.0f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-half,  half, 0).tex(0, 1).endVertex();
        buf.pos( half,  half, 0).tex(1, 1).endVertex();
        buf.pos( half, -half, 0).tex(1, 0).endVertex();
        buf.pos(-half, -half, 0).tex(0, 0).endVertex();
        tess.draw();

        GlStateManager.popMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Plasma fractal en grille 24x16, palette 8 couleurs, alpha 0.4.
     */
    private void renderPlasma(int w, int h, long t, boolean negative) {
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
        float alpha = negative ? 0.5f : 0.4f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double tt = t * 0.05;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Plasma : 3 sinusoides 2D
                double v =
                    Math.sin(col * 0.45 + tt * 1.2) +
                    Math.sin(row * 0.55 + tt * 0.9) +
                    Math.sin((col + row) * 0.3 + tt * 1.5);
                // Map [-3, 3] -> [0, 8] pour 8 couleurs
                double norm = (v + 3.0) * (8.0 / 6.0);
                int idxA = ((int) norm) % 8;
                int idxB = (idxA + 1) % 8;
                double frac = norm - Math.floor(norm);

                float[] cA = DMT_PALETTE[idxA];
                float[] cB = DMT_PALETTE[idxB];
                float r = (float) (cA[0] * (1.0 - frac) + cB[0] * frac);
                float g = (float) (cA[1] * (1.0 - frac) + cB[1] * frac);
                float b = (float) (cA[2] * (1.0 - frac) + cB[2] * frac);

                if (negative) {
                    r = 1.0f - r; g = 1.0f - g; b = 1.0f - b;
                }

                float x0 = col * cellW;
                float y0 = row * cellH;
                float x1 = x0 + cellW;
                float y1 = y0 + cellH;

                buf.pos(x0, y1, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x1, y1, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x1, y0, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x0, y0, 0).color(r, g, b, alpha).endVertex();
            }
        }
        tess.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Pulse central : cercle expansif depuis le centre, ~2s par cycle.
     * Cree un "battement de coeur" visuel.
     */
    private void renderCenterPulse(int w, int h, long t, boolean negative) {
        // Cycle 40 ticks (2s)
        float cycle = (t % 40) / 40.0f;
        float radius = cycle * Math.max(w, h) * 0.7f;
        float pulseAlpha = (1.0f - cycle) * 0.4f;

        if (pulseAlpha < 0.02f) return;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        float cx = w / 2.0f;
        float cy = h / 2.0f;

        // Couleur pulse : choisi selon le temps dans la palette
        int colorIdx = (int)(t / 40) % 8;
        float[] c = DMT_PALETTE[colorIdx];
        float r = c[0], g = c[1], b = c[2];
        if (negative) {
            r = 1.0f - r; g = 1.0f - g; b = 1.0f - b;
        }

        // Anneau circulaire (2 cercles concentriques)
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        int segments = 64;
        float thickness = 8.0f;
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
            float ca = (float) Math.cos(angle);
            float sa = (float) Math.sin(angle);
            float rOuter = radius + thickness;
            float rInner = radius - thickness;
            buf.pos(cx + ca * rOuter, cy + sa * rOuter, 0)
                .color(r, g, b, 0f).endVertex();
            buf.pos(cx + ca * rInner, cy + sa * rInner, 0)
                .color(r, g, b, pulseAlpha).endVertex();
        }
        tess.draw();

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderDebugIndicator(FontRenderer font, int phase) {
        String label;
        int color;
        switch (phase) {
            case ManifoldEffectHandler.PHASE_ACTIVE:
                label = "MANIFOLDINE ACTIVE";
                color = 0xFF1493;
                break;
            case ManifoldEffectHandler.PHASE_NEGATIVE:
                label = "MANIFOLDINE — VRILLE";
                color = 0x00FFFF;
                break;
            default:
                return;
        }
        font.drawStringWithShadow(label, 8, 8, color);
    }
}
