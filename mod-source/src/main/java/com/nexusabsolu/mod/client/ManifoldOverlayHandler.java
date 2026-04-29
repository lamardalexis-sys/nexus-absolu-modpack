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
 * Overlay client multi-couches, BPM-sync (84 BPM), 9 stages progressifs.
 *
 * Chaque LAYER a son intensite independante calculee depuis le trip progress :
 *
 *   Stage 1 ONSET       Tint subtil (luminosity boost via overlay blanc transparent)
 *   Stage 2 SATURATION  Plasma + Mandala 50%
 *   Stage 3 GEOMETRIC   Mandala 100% + Plasma 100% + Tunnel 50%
 *   Stage 4 HYPERSPACE  + Tunnel 100% + Pulse central 100%
 *   Stage 5 PEAK        + Entite centrale animee + intensite max partout
 *
 * Pendant le retour (5→1') chaque layer descend dans l'ordre inverse →
 * effet de "calmer" graduellement.
 *
 * BPM SYNC :
 *   - alpha mandala/tunnel pulse sur le beat (BeatKick)
 *   - rotation des layers acceleree sur les downbeats (toutes les 4 beats)
 *   - pulse central battement = 1 par beat
 *
 * Bug fix maintenu : ElementType.HOTBAR (pas ALL).
 */
@SideOnly(Side.CLIENT)
public class ManifoldOverlayHandler {

    private static final float[][] DMT_PALETTE = {
        {1.00f, 0.00f, 0.78f}, {1.00f, 0.39f, 0.00f},
        {1.00f, 0.86f, 0.00f}, {0.55f, 1.00f, 0.00f},
        {0.00f, 1.00f, 0.78f}, {0.00f, 0.86f, 1.00f},
        {0.71f, 0.20f, 1.00f}, {1.00f, 0.00f, 0.39f}
    };

    private static final ResourceLocation TUNNEL_TILE =
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile.png");
    private static final ResourceLocation[] MANDALA_TEX = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_3.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_4.png")
    };
    // Entite avec 4 frames d'animation
    private static final ResourceLocation[] ENTITY_FRAMES = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_0.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_3.png")
    };

    private static final int MANDALA_FRAME_DURATION = 100;  // 5s per mandala frame
    private static final int ENTITY_FRAME_DURATION = 14;    // ~1 frame per beat (84 BPM)

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.HOTBAR) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        long now = player.world.getTotalWorldTime();
        float progress = ManifoldClientState.getTripProgress(now);
        if (progress < 0) {
            // Stage fatigue ou inactif
            int stage = ManifoldClientState.getCurrentStage(now);
            if (stage == ManifoldEffectHandler.STAGE_FATIGUE) {
                renderFatigueOverlay(mc);
            }
            return;
        }

        ScaledResolution res = new ScaledResolution(mc);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        // Calcul des intensites par stage [s1, s2, s3, s4, s5]
        float[] intensities = ManifoldClientState.getLayerIntensities(progress);

        // Pulsation BPM-sync
        float beatKick = ManifoldClientState.getBeatKick(now);

        // === COUCHE 1 — Onset : subtle white tint (luminosity boost) ===
        if (intensities[0] > 0.01f) {
            renderOnsetTint(w, h, intensities[0], beatKick);
        }

        // === COUCHE 2 — Saturation : plasma fractal couleurs vives ===
        if (intensities[1] > 0.01f) {
            renderPlasma(w, h, now, intensities[1] * (0.7f + 0.3f * beatKick));
        }

        // === COUCHE 3 — Geometric : mandala fractal rotatif ===
        if (intensities[2] > 0.01f) {
            renderMandala(mc, w, h, now, intensities[2] * (0.8f + 0.2f * beatKick));
        }

        // === COUCHE 4 — Hyperspace : tunnel zoom + pulse central ===
        if (intensities[3] > 0.01f) {
            renderZoomTunnel(mc, w, h, now, intensities[3]);
            renderCenterPulse(w, h, now, intensities[3], beatKick);
        }

        // === COUCHE 5 — PEAK : entite animee + bande sombre top/bottom ===
        if (intensities[4] > 0.01f) {
            renderEntity(mc, w, h, now, intensities[4], beatKick);
            renderPeakLetterbox(w, h, intensities[4]);
        }

        // Debug
        renderDebugIndicator(mc.fontRenderer, ManifoldClientState.getCurrentStage(now), progress);
    }

    /**
     * Stage 1 : tint subtil blanc + leger boost luminosite.
     * Le monde "s'allege" — vignette interne brillante.
     */
    private void renderOnsetTint(int w, int h, float intensity, float beat) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        // Vignette inverse : blanc au centre fade vers bord
        float alpha = intensity * 0.10f * (0.7f + 0.3f * beat);
        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float radius = Math.max(w, h) * 0.6f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        // Centre brillant
        buf.pos(cx, cy, 0).color(1f, 1f, 1f, alpha).endVertex();
        // Bord transparent
        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
            float ex = cx + (float) Math.cos(angle) * radius;
            float ey = cy + (float) Math.sin(angle) * radius;
            buf.pos(ex, ey, 0).color(1f, 1f, 1f, 0f).endVertex();
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

    /**
     * Plasma fractal 8 couleurs DMT.
     */
    private void renderPlasma(int w, int h, long t, float intensity) {
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
        float alpha = intensity * 0.4f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double tt = t * 0.05;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double v = Math.sin(col * 0.45 + tt * 1.2)
                         + Math.sin(row * 0.55 + tt * 0.9)
                         + Math.sin((col + row) * 0.3 + tt * 1.5);
                double norm = (v + 3.0) * (8.0 / 6.0);
                int idxA = ((int) norm) % 8;
                int idxB = (idxA + 1) % 8;
                double frac = norm - Math.floor(norm);

                float[] cA = DMT_PALETTE[idxA];
                float[] cB = DMT_PALETTE[idxB];
                float r = (float) (cA[0] * (1.0 - frac) + cB[0] * frac);
                float g = (float) (cA[1] * (1.0 - frac) + cB[1] * frac);
                float b = (float) (cA[2] * (1.0 - frac) + cB[2] * frac);

                float x0 = col * cellW;
                float y0 = row * cellH;
                buf.pos(x0, y0 + cellH, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x0 + cellW, y0 + cellH, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x0 + cellW, y0, 0).color(r, g, b, alpha).endVertex();
                buf.pos(x0, y0, 0).color(r, g, b, alpha).endVertex();
            }
        }
        tess.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Mandala fractal rotatif avec crossfade entre 4 frames.
     */
    private void renderMandala(Minecraft mc, int w, int h, long t, float intensity) {
        int frame = (int)((t / MANDALA_FRAME_DURATION) % MANDALA_TEX.length);
        int nextFrame = (frame + 1) % MANDALA_TEX.length;
        float fadeFrac = (t % MANDALA_FRAME_DURATION) / (float) MANDALA_FRAME_DURATION;
        float rotation = (t % 600) / 600.0f * 360.0f;

        float mandalaSize = (float) Math.max(w, h) * 1.5f;
        float cx = w / 2.0f;
        float cy = h / 2.0f;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        float a1 = (1.0f - fadeFrac) * intensity * 0.55f;
        float a2 = fadeFrac * intensity * 0.55f;

        drawRotatedTexture(mc, MANDALA_TEX[frame], cx, cy, mandalaSize, rotation, a1);
        drawRotatedTexture(mc, MANDALA_TEX[nextFrame], cx, cy, mandalaSize, rotation + 12.0f, a2);

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
    }

    private void drawRotatedTexture(Minecraft mc, ResourceLocation tex,
                                     float cx, float cy, float size,
                                     float rotation, float alpha) {
        mc.getTextureManager().bindTexture(tex);
        GlStateManager.color(1f, 1f, 1f, alpha);

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
     * Tunnel zoom in/out — texture seamless en grille avec scale animee.
     */
    private void renderZoomTunnel(Minecraft mc, int w, int h, long t, float intensity) {
        // Zoom cycle 6s
        double zoomCycle = (t % 120) / 120.0;
        float scale = 0.6f + (float)(Math.sin(zoomCycle * 2 * Math.PI) * 0.5 + 0.5) * 1.4f;
        float rotation = (t % 800) / 800.0f * 360.0f;
        float tileSize = 256.0f * scale * 0.5f;
        int tilesX = (int)(w / tileSize) + 4;
        int tilesY = (int)(h / tileSize) + 4;
        float scrollOffset = (t % 240) / 240.0f * tileSize;

        mc.getTextureManager().bindTexture(TUNNEL_TILE);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, intensity * 0.45f);

        GlStateManager.pushMatrix();
        GlStateManager.translate(w / 2.0f, h / 2.0f, 0);
        GlStateManager.rotate(rotation, 0, 0, 1);
        GlStateManager.translate(-w / 2.0f, -h / 2.0f, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float gridOriginX = -tileSize * 2 - scrollOffset;
        float gridOriginY = -tileSize * 2 - scrollOffset;

        for (int gy = 0; gy < tilesY; gy++) {
            for (int gx = 0; gx < tilesX; gx++) {
                float x0 = gridOriginX + gx * tileSize;
                float y0 = gridOriginY + gy * tileSize;
                float x1 = x0 + tileSize;
                float y1 = y0 + tileSize;
                buf.pos(x0, y1, 0).tex(0, 1).endVertex();
                buf.pos(x1, y1, 0).tex(1, 1).endVertex();
                buf.pos(x1, y0, 0).tex(1, 0).endVertex();
                buf.pos(x0, y0, 0).tex(0, 0).endVertex();
            }
        }
        tess.draw();

        GlStateManager.popMatrix();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Pulse central : 1 anneau par BEAT (84 BPM).
     */
    private void renderCenterPulse(int w, int h, long t, float intensity, float beat) {
        // L'anneau commence quand beat=0 et grandit jusqu'a beat=1
        float phase = ManifoldClientState.getBeatPhase(t);
        float radius = phase * Math.max(w, h) * 0.7f;
        float pulseAlpha = (1.0f - phase) * 0.4f * intensity;
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
        int colorIdx = (int)(t / 14) % 8;
        float[] c = DMT_PALETTE[colorIdx];

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
            buf.pos(cx + ca * rOuter, cy + sa * rOuter, 0).color(c[0], c[1], c[2], 0f).endVertex();
            buf.pos(cx + ca * rInner, cy + sa * rInner, 0).color(c[0], c[1], c[2], pulseAlpha).endVertex();
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

    /**
     * Entite animee Salviadroid au PEAK (stage 5).
     * 4 frames qui cyclent au BPM (1 frame par beat).
     * Pulse de scale (respiration) + rotation tres lente.
     */
    private void renderEntity(Minecraft mc, int w, int h, long t, float intensity, float beat) {
        // Frame index sync au beat
        int frame = (int)((t / ENTITY_FRAME_DURATION) % ENTITY_FRAMES.length);

        // Pulse scale beat-sync : grandit sur kick
        float scale = 1.0f + beat * 0.12f;

        // Rotation tres lente (1 tour / 90s)
        float rotation = (t % 1800) / 1800.0f * 360.0f;

        float baseSize = Math.min(w, h) * 0.6f;
        float size = baseSize * scale;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        drawRotatedTexture(mc, ENTITY_FRAMES[frame], w / 2.0f, h / 2.0f,
                           size, rotation, intensity * 0.95f);

        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Letterbox cinematique au PEAK : bandes sombres haut/bas pour effet
     * "entree dans une autre dimension".
     */
    private void renderPeakLetterbox(int w, int h, float intensity) {
        float barHeight = h * 0.12f * intensity;
        if (barHeight < 1f) return;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0f, 0f, 0f, intensity * 0.85f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // Top
        buf.pos(0, barHeight, 0).endVertex();
        buf.pos(w, barHeight, 0).endVertex();
        buf.pos(w, 0, 0).endVertex();
        buf.pos(0, 0, 0).endVertex();
        // Bottom
        buf.pos(0, h, 0).endVertex();
        buf.pos(w, h, 0).endVertex();
        buf.pos(w, h - barHeight, 0).endVertex();
        buf.pos(0, h - barHeight, 0).endVertex();
        tess.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Overlay subtil pendant la fatigue : gris desature.
     */
    private void renderFatigueOverlay(Minecraft mc) {
        ScaledResolution res = new ScaledResolution(mc);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.2f, 0.2f, 0.25f, 0.25f);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(0, h, 0).endVertex();
        buf.pos(w, h, 0).endVertex();
        buf.pos(w, 0, 0).endVertex();
        buf.pos(0, 0, 0).endVertex();
        tess.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderDebugIndicator(FontRenderer font, int stage, float progress) {
        String label;
        int color;
        switch (stage) {
            case ManifoldEffectHandler.STAGE_1_ONSET:
                label = "1. ONSET — le monde s'allege";
                color = 0xCCCCCC;
                break;
            case ManifoldEffectHandler.STAGE_2_SATURATION:
                label = "2. SATURATION — couleurs vives";
                color = 0xFF1493;
                break;
            case ManifoldEffectHandler.STAGE_3_GEOMETRIC:
                label = "3. GEOMETRIC — fractales";
                color = 0x00FFFF;
                break;
            case ManifoldEffectHandler.STAGE_4_HYPERSPACE:
                label = "4. HYPERSPACE — tunnel";
                color = 0xFFD700;
                break;
            case ManifoldEffectHandler.STAGE_5_PEAK:
                label = "5. PEAK — entity contact";
                color = 0xFF00FF;
                break;
            case ManifoldEffectHandler.STAGE_FATIGUE:
                label = "CRASH — fatigue";
                color = 0x808080;
                break;
            default:
                return;
        }
        // Affichage en haut a gauche : nom du stage + progress
        int pct = Math.max(0, Math.min(100, (int)(progress * 100)));
        font.drawStringWithShadow(label + " (" + pct + "%)", 8, 8, color);
    }
}
