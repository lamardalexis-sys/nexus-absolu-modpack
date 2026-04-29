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
 * COUCHE 1 — Tunnel kaleidoscopique (zoom in/out) :
 *   Texture seamless 256x256 affichee en grille 3x3 avec scale qui pulse
 *   selon une courbe sinusoidale → effet "tunnel infini ou la camera
 *   avance/recule dans le motif". Mode blend additif pour faire briller
 *   les couleurs neon par dessus le monde.
 *
 * COUCHE 2 — Mandala fractal (background hero) :
 *   4 frames qui crossfade tous les 5s, rotation continue, mode lighten.
 *
 * COUCHE 3 — Plasma fractal (movement) :
 *   Grille 24x16 algorithmique, palette 8 couleurs DMT, alpha 0.4.
 *
 * COUCHE 4 — Entite centrale :
 *   Silhouette doree avec 2 yeux roses au centre de l'ecran. Pulse
 *   de scale et tourne lentement. Reference image 1.
 *
 * COUCHE 5 — Pulse central :
 *   Anneau expansif depuis le centre toutes les 2s.
 *
 * En PHASE 2 (negatif) : couleurs inversees, plus oppressant.
 *
 * Bug fix maintenu : ElementType.HOTBAR (pas ALL).
 */
@SideOnly(Side.CLIENT)
public class ManifoldOverlayHandler {

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

    private static final ResourceLocation TUNNEL_TILE =
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile.png");
    private static final ResourceLocation ENTITY_TEX =
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/entity.png");
    private static final ResourceLocation[] MANDALA_TEXTURES = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_1.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_2.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_3.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/mandala_4.png")
    };

    private static final int FRAME_DURATION = 100;  // 5s per mandala frame

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

        // === COUCHE 1 : Tunnel zoom in/out ===
        renderZoomTunnel(mc, w, h, now, negative);

        // === COUCHE 2 : Mandala texture rotative ===
        renderMandala(mc, w, h, now, negative);

        // === COUCHE 3 : Plasma fractal ===
        renderPlasma(w, h, now, negative);

        // === COUCHE 4 : Entite centrale ===
        renderEntity(mc, w, h, now, negative);

        // === COUCHE 5 : Pulse central ===
        renderCenterPulse(w, h, now, negative);

        // Debug
        renderDebugIndicator(mc.fontRenderer, phase);
    }

    /**
     * TUNNEL ZOOM IN/OUT — la camera avance/recule dans le motif.
     *
     * On affiche la texture seamless en grille 5x5 (assez pour couvrir
     * meme avec scale variable). La taille de chaque tile change selon
     * une courbe sinusoidale → ressenti "zoom".
     *
     * Astuce : on offset l'origine du grid avec scrollX/scrollY pour
     * suggerer un mouvement de camera. Et on rotate l'ensemble
     * legerement pour amplifier l'effet hallucinatoire.
     */
    private void renderZoomTunnel(Minecraft mc, int w, int h, long t, boolean negative) {
        // Cycle zoom : 6 secondes (120 ticks), va de 0.5x a 2.0x
        double zoomCycle = (t % 120) / 120.0;
        // Sin pour smooth in/out
        float scale = 0.6f + (float)(Math.sin(zoomCycle * 2 * Math.PI) * 0.5 + 0.5) * 1.4f;

        // Rotation lente
        float rotation = (t % 800) / 800.0f * 360.0f;

        // Taille de chaque tile (256 = base)
        float tileSize = 256.0f * scale * 0.5f;  // /2 pour avoir plus de tiles visibles

        // Combien de tiles sur l'ecran (avec marge pour rotation)
        int tilesX = (int)(w / tileSize) + 4;
        int tilesY = (int)(h / tileSize) + 4;

        // Offset scroll : suggere mouvement camera
        // On scroll inversement au zoom : quand on zoom in (scale grande),
        // on continue a "avancer" en scrollant
        float scrollOffset = (t % 240) / 240.0f * tileSize;

        mc.getTextureManager().bindTexture(TUNNEL_TILE);

        GlStateManager.enableBlend();
        // Blend additif : neon brillant par dessus le monde
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        if (negative) {
            GlStateManager.color(0.5f, 0.2f, 0.6f, 0.55f);
        } else {
            GlStateManager.color(1f, 1f, 1f, 0.45f);
        }

        // Centre rotation au milieu de l'ecran
        GlStateManager.pushMatrix();
        GlStateManager.translate(w / 2.0f, h / 2.0f, 0);
        GlStateManager.rotate(rotation, 0, 0, 1);
        GlStateManager.translate(-w / 2.0f, -h / 2.0f, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Origine grille : decalee pour scroll + centrage
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

        // Reset
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Mandala fractal : 2 textures crossfade + rotation lente.
     */
    private void renderMandala(Minecraft mc, int w, int h, long t, boolean negative) {
        int frame = (int)((t / FRAME_DURATION) % MANDALA_TEXTURES.length);
        int nextFrame = (frame + 1) % MANDALA_TEXTURES.length;
        float fadeFrac = (t % FRAME_DURATION) / (float) FRAME_DURATION;
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

        float alpha1 = (1.0f - fadeFrac) * (negative ? 0.45f : 0.55f);
        float alpha2 = fadeFrac * (negative ? 0.45f : 0.55f);

        drawRotatedTexture(mc, MANDALA_TEXTURES[frame], cx, cy, mandalaSize,
                           rotation, alpha1, negative);
        drawRotatedTexture(mc, MANDALA_TEXTURES[nextFrame], cx, cy, mandalaSize,
                           rotation + 12.0f, alpha2, negative);

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
    }

    private void drawRotatedTexture(Minecraft mc, ResourceLocation tex,
                                     float cx, float cy, float size,
                                     float rotation, float alpha, boolean negative) {
        mc.getTextureManager().bindTexture(tex);

        if (negative) {
            GlStateManager.color(0.4f, 0.15f, 0.55f, alpha);
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
     * Plasma fractal en grille 24x16, palette 8 couleurs.
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
        float alpha = negative ? 0.4f : 0.3f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double tt = t * 0.05;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double v =
                    Math.sin(col * 0.45 + tt * 1.2) +
                    Math.sin(row * 0.55 + tt * 0.9) +
                    Math.sin((col + row) * 0.3 + tt * 1.5);
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
     * Entite centrale : silhouette doree pulsante au centre de l'ecran.
     * Pulse de scale (1.0 → 1.15 → 1.0) + rotation tres lente.
     */
    private void renderEntity(Minecraft mc, int w, int h, long t, boolean negative) {
        // Pulse 0..1..0 sur 60 ticks (3s) — comme une respiration
        double breathe = Math.sin((t % 60) / 60.0 * 2 * Math.PI);
        float scale = 1.0f + (float) breathe * 0.08f;

        // Rotation tres lente (1 tour par 60s)
        float rotation = (t % 1200) / 1200.0f * 360.0f;

        float baseSize = Math.min(w, h) * 0.5f;  // 50% de la dimension la plus petite
        float entitySize = baseSize * scale;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        float alpha = negative ? 0.85f : 0.95f;
        if (negative) {
            GlStateManager.color(0.3f, 0.7f, 1f, alpha);  // bleu fantomatique en phase 2
        } else {
            GlStateManager.color(1f, 1f, 1f, alpha);
        }

        drawRotatedTexture(mc, ENTITY_TEX, w / 2.0f, h / 2.0f,
                           entitySize, rotation, alpha, negative);

        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Pulse central : anneau expansif depuis le centre.
     */
    private void renderCenterPulse(int w, int h, long t, boolean negative) {
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

        int colorIdx = (int)(t / 40) % 8;
        float[] c = DMT_PALETTE[colorIdx];
        float r = c[0], g = c[1], b = c[2];
        if (negative) {
            r = 1.0f - r; g = 1.0f - g; b = 1.0f - b;
        }

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
            buf.pos(cx + ca * rOuter, cy + sa * rOuter, 0).color(r, g, b, 0f).endVertex();
            buf.pos(cx + ca * rInner, cy + sa * rInner, 0).color(r, g, b, pulseAlpha).endVertex();
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
