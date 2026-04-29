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
    // v1.0.330 (Etape 2 visuel ultime) -- 16 mandalas 1024x1024 supersampled
    // (vs 4 mandalas 512x512 avant). Le cycle complet dure 16 * MANDALA_FRAME_DURATION
    // = 80s a 100 ticks/frame, soit ~6 cycles sur le trip de 8 min.
    private static final ResourceLocation[] MANDALA_TEX = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_3.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_4.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_5.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_6.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_7.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_8.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_9.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_10.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_11.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_12.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_13.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_14.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_15.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_16.png")
    };
    // v1.0.331 (Etape 3 visuel ultime) -- 16 frames de morphing :
    //   0-3  : iris cyan/magenta qui grossit
    //   4-7  : crack effect (lignes blanches + contour visage qui apparait)
    //   8-11 : 3 visages superposes (separation chromatique cyan/or/magenta)
    //   12-15: entite Salviadroid finale qui respire
    private static final ResourceLocation[] ENTITY_FRAMES = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_0.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_3.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_4.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_5.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_6.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_7.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_8.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_9.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_10.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_11.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_12.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_13.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_14.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity_15.png")
    };

    private static final int MANDALA_FRAME_DURATION = 100;  // 5s per mandala frame
    private static final int ENTITY_FRAME_DURATION = 14;    // ~1 frame per beat (84 BPM)

    // v1.0.334 (Etape 6 visuel ultime) : cosmic dust precompute.
    // 80 etoiles fixes en coordonnees normalisees [0,1] avec couleur DMT.
    // Init seede dans static block pour reproductibilite (meme positions a
    // chaque demarrage du jeu).
    private static final int N_STARS = 80;
    private static final float[] STAR_X = new float[N_STARS];
    private static final float[] STAR_Y = new float[N_STARS];
    private static final int[] STAR_COLOR = new int[N_STARS];
    private static final float[] STAR_SIZE = new float[N_STARS];
    static {
        java.util.Random rng = new java.util.Random(42L);
        for (int i = 0; i < N_STARS; i++) {
            STAR_X[i] = rng.nextFloat();
            STAR_Y[i] = rng.nextFloat();
            STAR_COLOR[i] = rng.nextInt(8);
            STAR_SIZE[i] = 1.0f + rng.nextFloat() * 2.0f;  // 1.0 - 3.0 px
        }
    }

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

        // === COUCHE 0 -- NEW Etape 6 : cosmic dust en fond, des Stage 2 ===
        // Intensite combinee Stages 2-5 (max des 4) -> apparait avec couleurs,
        // disparait apres le PEAK comme les autres effets.
        float dustIntensity = Math.max(Math.max(intensities[1], intensities[2]),
                                       Math.max(intensities[3], intensities[4]));
        if (dustIntensity > 0.01f) {
            renderCosmicDust(w, h, now, dustIntensity);
        }

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

        // === COUCHE 6 -- NEW Etape 6 : waveform bars BPM-sync en bas ===
        // Visible des Stage 2 jusqu'au PEAK (intensite combinee).
        float wavIntensity = Math.max(Math.max(intensities[1], intensities[2]),
                                      Math.max(intensities[3], intensities[4]));
        if (wavIntensity > 0.01f) {
            renderWaveformBars(w, h, now, wavIntensity);
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
     * Tunnel zoom in/out -- 3 couches parallax avec scales/rotations/vitesses
     * differentes et acceleration progressive (effet "on accelere dans le temps").
     *
     * v1.0.332 (Etape 4 visuel ultime) :
     *   - Couche fond  : scale 0.30x, rot lente, zoom lent (parallax background)
     *   - Couche moyen : scale 0.60x, rot moyenne, zoom moyen
     *   - Couche avant : scale 1.20x, rot rapide, zoom rapide (parallax foreground)
     *   - Acceleration : monte 1->4 sur Stage 4, max 4 sur Stage 5 PEAK,
     *     redescend 4->1 sur Stage 4'.
     */
    private void renderZoomTunnel(Minecraft mc, int w, int h, long t, float intensity) {
        float progress = ManifoldClientState.getTripProgress(t);
        float accel = getTunnelAcceleration(progress);

        mc.getTextureManager().bindTexture(TUNNEL_TILE);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        // 3 couches, du fond vers l'avant. Alpha cumulee ~= 0.47 (proche
        // de l'intensite originale 0.45) en blend additif.
        renderTunnelLayer(mc, w, h, t, intensity * 0.10f, 0.30f, 0.5f, 0.5f * accel);
        renderTunnelLayer(mc, w, h, t, intensity * 0.15f, 0.60f, 1.0f, 1.0f * accel);
        renderTunnelLayer(mc, w, h, t, intensity * 0.22f, 1.20f, 1.8f, 1.8f * accel);

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Rend une seule couche du tunnel a alpha/scale/rotation/vitesse donnes.
     * Le caller doit avoir setup le blend, bind la texture et reset le state.
     */
    private void renderTunnelLayer(Minecraft mc, int w, int h, long t,
                                    float alpha, float scaleMul,
                                    float rotSpeed, float zoomSpeed) {
        // Zoom cycle 6s a vitesse normale, accelere par zoomSpeed
        double zoomPhase = (((double) t) * zoomSpeed) % 120.0;
        double zoomCycle = zoomPhase / 120.0;
        float scale = (0.6f + (float)(Math.sin(zoomCycle * 2 * Math.PI) * 0.5 + 0.5) * 1.4f) * scaleMul;
        float rotation = (float)((((double) t) * rotSpeed) % 800.0 / 800.0 * 360.0);

        float tileSize = 256.0f * scale * 0.5f;
        if (tileSize < 8.0f) tileSize = 8.0f; // securite : pas de tiles micro
        int tilesX = (int)(w / tileSize) + 4;
        int tilesY = (int)(h / tileSize) + 4;
        float scrollOffset = (float)((((double) t) * zoomSpeed) % 240.0 / 240.0 * tileSize);

        GlStateManager.color(1f, 1f, 1f, alpha);

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
    }

    /**
     * Acceleration du tunnel selon la phase du trip.
     *   Stage 4 aller [0.3125, 0.5]    : accel monte 1.0 -> 4.0
     *   Stage 5 PEAK  [0.5, 0.6875]    : accel max = 4.0
     *   Stage 4 retour [0.6875, 0.8125]: accel descend 4.0 -> 1.0
     *   Hors de cette plage : 1.0 (defaut, pas d'acceleration)
     */
    private float getTunnelAcceleration(float progress) {
        final float S4_START = ManifoldEffectHandler.STAGE_3_GEOMETRIC_END;     // 0.3125
        final float S5_START = ManifoldEffectHandler.STAGE_4_HYPERSPACE_END;    // 0.5
        final float S5_END   = ManifoldEffectHandler.STAGE_5_PEAK_END;          // 0.6875
        final float S4R_END  = ManifoldEffectHandler.STAGE_4R_HYPERSPACE_END;   // 0.8125

        if (progress < S4_START || progress >= S4R_END) return 1.0f;

        if (progress < S5_START) {
            float t = (progress - S4_START) / (S5_START - S4_START);
            return 1.0f + 3.0f * t;
        }
        if (progress < S5_END) {
            return 4.0f;
        }
        float t = (progress - S5_END) / (S4R_END - S5_END);
        return 4.0f - 3.0f * t;
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
     *
     * v1.0.336 BUGFIX :
     *   - CROSSFADE entre frames adjacentes (avant : changement sec, ressemblait
     *     a un diaporama). Maintenant frame N et N+1 sont blendees selon la
     *     fraction temporelle, comme les mandalas.
     *   - Force `enableAlpha()` pour garantir le rendu correct des zones
     *     transparentes des PNG.
     *
     * v1.0.331 (Etape 3 visuel ultime) : MORPHING progressif.
     *   - Premieres 30s du PEAK -> 16 frames de morphing en sequence
     *     (iris -> crack -> 3 visages -> Salviadroid)
     *   - 60s suivantes -> boucle BPM-sync sur frames 12-15 (Salviadroid)
     *
     * Pulse de scale (respiration) + rotation tres lente, conserves.
     */
    private void renderEntity(Minecraft mc, int w, int h, long t, float intensity, float beat) {
        float frameFloat = computeEntityFrameFloat(t);
        int frameA = (int) Math.floor(frameFloat);
        int frameB = (frameA + 1) % ENTITY_FRAMES.length;
        float fade = frameFloat - frameA;     // 0..1 fraction crossfade
        if (frameA < 0) frameA = 0;
        if (frameA >= ENTITY_FRAMES.length) frameA = ENTITY_FRAMES.length - 1;

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
        GlStateManager.enableAlpha();  // v1.0.336 : zones transparentes PNG

        float baseAlpha = intensity * 0.95f;
        float alphaA = (1.0f - fade) * baseAlpha;
        float alphaB = fade * baseAlpha;

        // Frame courante (alpha decroit du fait du crossfade)
        if (alphaA > 0.01f) {
            drawRotatedTexture(mc, ENTITY_FRAMES[frameA], w / 2.0f, h / 2.0f,
                               size, rotation, alphaA);
        }
        // Frame suivante (alpha croit)
        if (alphaB > 0.01f) {
            drawRotatedTexture(mc, ENTITY_FRAMES[frameB], w / 2.0f, h / 2.0f,
                               size, rotation, alphaB);
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Selection de la frame d'entite (en float pour crossfade) selon le
     * peakProgress.
     *
     * v1.0.336 : retourne un FLOAT au lieu d'un int -> permet le crossfade
     * fluide entre frame N et N+1 dans renderEntity.
     *
     * Logique :
     *   - PEAK = [0.5, 0.6875] du trip (~1.5 min sur 8 min)
     *   - 1ere tranche (0.5..0.5625, soit 30s) -> morphing : valeur retournee
     *     varie lineairement de 0.0 a 16.0 (les 16 frames defilent en
     *     sequence avec crossfade)
     *   - 2eme tranche (0.5625..0.6875, soit 60s) -> loop des frames 12-15
     *     (Salviadroid qui respire) BPM-sync, valeur 12.0..16.0 cyclique
     *   - Hors PEAK -> 12.0 (defaut Salviadroid statique)
     */
    private float computeEntityFrameFloat(long t) {
        float progress = ManifoldClientState.getTripProgress(t);
        final float PEAK_START = ManifoldEffectHandler.STAGE_4_HYPERSPACE_END; // 0.5
        final float PEAK_END = ManifoldEffectHandler.STAGE_5_PEAK_END;         // 0.6875
        final float MORPH_END = PEAK_START + (PEAK_END - PEAK_START) / 3.0f;   // ~0.5625

        if (progress < PEAK_START || progress >= PEAK_END) {
            return 12.0f;
        }

        if (progress < MORPH_END) {
            // Phase morphing : map progress lineairement sur 0..16 frames
            float morphFrac = (progress - PEAK_START) / (MORPH_END - PEAK_START);
            return morphFrac * ENTITY_FRAMES.length;
        }

        // Phase loop : frames 12..16 BPM-sync, en float pour crossfade
        // Une frame complete = ENTITY_FRAME_DURATION ticks
        long ticksSinceMorphEnd = t - (long) (PEAK_START * ManifoldEffectHandler.TRIP_DURATION);
        float subTicks = (float) (ticksSinceMorphEnd % ENTITY_FRAME_DURATION) / ENTITY_FRAME_DURATION;
        int loopFrame = (int) ((t / ENTITY_FRAME_DURATION) % 4);
        return 12.0f + loopFrame + subTicks;
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

    /**
     * Cosmic dust : 80 etoiles fixes (positions seedees) qui scintillent
     * independamment. Apparait en fond a partir du Stage 2 et reste visible
     * jusqu'au PEAK pour donner une ambiance "espace cosmique".
     *
     * v1.0.334 (Etape 6 visuel ultime).
     */
    private void renderCosmicDust(int w, int h, long t, float intensity) {
        if (intensity < 0.01f) return;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < N_STARS; i++) {
            float sx = STAR_X[i] * w;
            float sy = STAR_Y[i] * h;
            float[] c = DMT_PALETTE[STAR_COLOR[i]];

            // Twinkle : alpha pulse avec frequence variable par etoile (0.5..2.5 Hz)
            float twinkleFreq = 0.5f + (i % 5) * 0.4f;
            float twinklePhase = i * 0.7f;
            float twinkle = 0.5f + 0.5f * (float) Math.sin(t / 20.0 * twinkleFreq + twinklePhase);
            float alpha = intensity * 0.7f * twinkle;
            if (alpha < 0.02f) continue;

            float size = STAR_SIZE[i];
            buf.pos(sx - size, sy - size, 0).color(c[0], c[1], c[2], alpha).endVertex();
            buf.pos(sx + size, sy - size, 0).color(c[0], c[1], c[2], alpha).endVertex();
            buf.pos(sx + size, sy + size, 0).color(c[0], c[1], c[2], alpha).endVertex();
            buf.pos(sx - size, sy + size, 0).color(c[0], c[1], c[2], alpha).endVertex();
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Waveform reactive : 3 barres BPM-sync en bas de l'ecran (bass, mid, high).
     * Pas de FFT temps reel disponible -- utilise le beat phase pour simuler
     * une reaction musicale plausible.
     *
     * v1.0.334 (Etape 6 visuel ultime).
     */
    private void renderWaveformBars(int w, int h, long t, float intensity) {
        if (intensity < 0.01f) return;

        float beatPhase = ManifoldClientState.getBeatPhase(t);

        // 3 barres : bass (kick) / mid (envelope) / high (haute frequence simulee)
        float bassEnv = (1.0f - beatPhase);                                            // fort sur kick, decroit
        float midEnv  = (float) Math.sin(beatPhase * Math.PI);                          // peak au milieu du beat
        float highEnv = 0.5f + 0.5f * (float) Math.sin(beatPhase * 4.0 * Math.PI);     // 2 oscillations par beat

        // Petite normalisation pour eviter d'avoir des barres a zero
        bassEnv = 0.2f + 0.8f * bassEnv;
        midEnv  = 0.2f + 0.8f * midEnv;
        highEnv = 0.2f + 0.8f * highEnv;

        // 3 couleurs DMT : magenta (bass), or (mid), cyan (high)
        float[] cBass = DMT_PALETTE[0];   // magenta
        float[] cMid  = DMT_PALETTE[2];   // or
        float[] cHigh = DMT_PALETTE[5];   // cyan

        // Geometrie : 3 barres centrees, espacees en bas
        float barWidth = w * 0.10f;
        float barSpacing = w * 0.06f;
        float totalWidth = barWidth * 3 + barSpacing * 2;
        float startX = (w - totalWidth) / 2.0f;
        float baseY = h - 12;        // y du sol des barres
        float maxBarH = h * 0.08f;   // hauteur max d'une barre

        float bassH = maxBarH * bassEnv;
        float midH  = maxBarH * midEnv;
        float highH = maxBarH * highEnv;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        float alpha = intensity * 0.55f;

        drawBarVertex(buf, startX,                                  baseY, barWidth, bassH, cBass, alpha);
        drawBarVertex(buf, startX + barWidth + barSpacing,          baseY, barWidth, midH,  cMid,  alpha);
        drawBarVertex(buf, startX + (barWidth + barSpacing) * 2,    baseY, barWidth, highH, cHigh, alpha);

        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Helper : ajoute les 4 vertices d'une barre verticale dans le buffer.
     * baseY est le bas, hauteur monte vers le haut.
     */
    private void drawBarVertex(BufferBuilder buf, float x, float baseY,
                                float width, float height,
                                float[] color, float alpha) {
        float top = baseY - height;
        // Gradient subtle : alpha plus fort en bas, plus faible en haut
        float aBottom = alpha;
        float aTop = alpha * 0.4f;
        buf.pos(x,         baseY, 0).color(color[0], color[1], color[2], aBottom).endVertex();
        buf.pos(x + width, baseY, 0).color(color[0], color[1], color[2], aBottom).endVertex();
        buf.pos(x + width, top,   0).color(color[0], color[1], color[2], aTop).endVertex();
        buf.pos(x,         top,   0).color(color[0], color[1], color[2], aTop).endVertex();
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
