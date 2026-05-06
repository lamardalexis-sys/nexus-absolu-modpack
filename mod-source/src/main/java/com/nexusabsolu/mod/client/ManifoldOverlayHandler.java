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

    // v1.0.337 (Etape 7) -- 4 variantes hyperspace qui se succedent au Stage 4.
    // tile_a : grille hexagonale neon DMT (auto)
    // tile_b : eclats cristallins prismatiques (auto)
    // tile_c : runes Voss + circuits (Voss lore)
    // tile_d : blueprint fractal Voss (Voss lore)
    // tunnel_tile.png (legacy) reste sur le disque, copie de tile_a, mais
    // n'est plus reference dans le code Java.
    private static final ResourceLocation[] TUNNEL_TILES = {
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile_a.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile_b.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile_c.png"),
        new ResourceLocation("nexusabsolu", "textures/gui/manifold/tunnel_tile_d.png")
    };
    // Couleur de teinte par variante (multipliee sur le RGB de la texture)
    private static final float[][] TUNNEL_TINTS = {
        {1.00f, 1.00f, 1.00f},   // A : neutre
        {1.00f, 0.85f, 1.00f},   // B : leger violet
        {1.00f, 0.95f, 0.70f},   // C : dore (lore Voss)
        {0.70f, 0.95f, 1.00f}    // D : cyan dimensionnel
    };
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
    // v1.0.342 (Etape 9) -- 50 frames de morphing pour vraie video :
    //   0-5   (6)  : IRIS qui grossit
    //   6-17  (12) : CRACK fissures de 0% a 100%
    //   18-29 (12) : FACES 3 visages superposes puis fusion
    //   30-41 (12) : METAMORPHOSE visage or -> entite humanoide verte
    //   42-49 (8)  : ENTITY_LOOP entite finale + etoile pulse
    //   ENTITY MORPHING - 300 frames sur 6 phases (effet quasi-video) :
    //   IRIS        : 0-31    (32)  - oeil semi-realiste qui s'ouvre
    //   CRACK       : 32-87   (56)  - cracks rouge sang autour de l'oeil
    //   FACES       : 88-143  (56)  - visages organiques chair sombre
    //   METAMORPHOSE: 144-199 (56)  - tentacules sortent + entite se forme
    //   ENTITY_LOOP : 200-239 (40)  - entite finale (loop ~30s du PEAK)
    //   NDE         : 240-299 (60)  - Near-Death Experience one-shot (~30s)
    //                                 5 sous-phases : decorporation, hyperspace,
    //                                 crystal_palace, past_lives, blackout.
    //                                 Effets painterly ULTRA appliques (distortion,
    //                                 cosmic noise, chromatic aberration, grain).
    private static final ResourceLocation[] ENTITY_FRAMES = new ResourceLocation[300];
    static {
        for (int i = 0; i < 300; i++) {
            ENTITY_FRAMES[i] = new ResourceLocation(
                "nexusabsolu", "textures/gui/manifold/entity_" + i + ".png");
        }
    }
    private static final int N_ENTITY_MORPH_FRAMES = 200; // frames 0..199 = morphing
    private static final int N_ENTITY_LOOP_FRAMES = 40;   // frames 200..239 = loop entity
    private static final int N_NDE_FRAMES = 60;           // frames 240..299 = NDE one-shot

    private static final int MANDALA_FRAME_DURATION = 100;  // 5s per mandala frame
    private static final int ENTITY_FRAME_DURATION = 3;    // ~150ms par frame, 6.6 fps brut
                                                            // + crossfade Java entre frames
                                                            // adjacentes = effet quasi-video.
                                                            // 240 frames * 150ms = 36s (loop ~6s)

    // v1.0.355 : 4 fresques 4K DMT pour le tunnel infini avant le PEAK.
    // Chaque fresque = mandala fractal multi-echelles dense, kaleidoscope DMT.
    // Elles sont scaled de 1.0 a 16.0 (zoom progressif) pour effet 'tunnel infini'.
    // Crossfade entre fresques en boucle pour variete sans repetition visible.
    // Generees par scripts-tools/generate_fractal_fresques_v2.py --all
    private static final int N_FRESQUES = 4;
    private static final ResourceLocation[] FRESQUE_TEX = new ResourceLocation[N_FRESQUES];
    static {
        for (int i = 0; i < N_FRESQUES; i++) {
            FRESQUE_TEX[i] = new ResourceLocation(
                "nexusabsolu", "textures/manifold/trip/fresque_" + i + ".png");
        }
    }
    // Cycle de zoom : 1.0 -> 16.0 sur 600 ticks = 30 secondes par fresque
    private static final int FRESQUE_CYCLE_TICKS = 600;
    private static final float FRESQUE_ZOOM_MIN = 1.0f;
    private static final float FRESQUE_ZOOM_MAX = 16.0f;

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

        // === COUCHE 3 — Geometric : mandala fractal MULTI-COUCHE rotatif ===
        if (intensities[2] > 0.01f) {
            renderMandala(mc, w, h, now, intensities[2], beatKick);
        }

        // === COUCHE 4 — Hyperspace : tunnel zoom + pulse central ===
        if (intensities[3] > 0.01f) {
            renderZoomTunnel(mc, w, h, now, intensities[3]);
            renderCenterPulse(w, h, now, intensities[3], beatKick);
        }

        // === COUCHE 4.5 — TUNNEL INFINI FRESQUE 4K (v1.0.355) ===
        // 30 dernieres secondes du HYPERSPACE (3:30 -> 4:00 du trip = 0.4375 -> 0.5).
        // 4 fresques DMT 4K en boucle, zoom progressif 1x -> 16x sur 30s par fresque,
        // crossfade entre fresques pour variete sans repetition visible.
        // Effet : on plonge dans une oeuvre d'art DMT infiniment detaillee juste
        // avant l'apparition de l'entite.
        // (variable 'progress' deja declaree plus haut dans la methode, on la reutilise)
        float fresqueIntensity = computeFresqueIntensity(progress);
        if (fresqueIntensity > 0.01f) {
            renderInfiniteFresqueTunnel(mc, w, h, now, fresqueIntensity);
        }

        // === COUCHE 5 -- PEAK : LE TRIP BOUFFE TOUT L'ECRAN ===
        // v1.0.346 : Au PEAK on met le paquet pour que le joueur ne voie plus
        // le jeu du tout (sauf vignette de bordure). Sequence :
        //   1. Fond opaque psychedelique qui recouvre l'ecran
        //   2. Mandala geant rotation rapide derriere l'entite
        //   3. Entite humanoide enorme (1.0x au lieu de 0.6x screen)
        //   4. Letterbox renforce (bandes noires plus grosses)
        if (intensities[4] > 0.01f) {
            renderPeakOccluder(w, h, now, intensities[4], beatKick);
            renderPeakBigMandala(mc, w, h, now, intensities[4], beatKick);
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
     * Stage 1 : 2 sous-phases internes pour creer une vraie progression.
     *
     *   Sub-phase INTRO    (intensity 0.0..0.7) : presence subtile
     *     - Vignette sombre rouge sang qui respire au beat
     *     - Quelques particules eparses qui clignotent
     *     - Pulse central tres leger
     *     - But : "le joueur sent qu'il se passe quelque chose"
     *
     *   Sub-phase ONSET    (intensity 0.3..1.0) : couleurs apparaissent
     *     - Vignette coloree (cyan -> magenta progressif)
     *     - Saturation boost progressive
     *     - Pulse central colore beat-sync
     *     - But : "les couleurs deviennent saturees et belles"
     *
     *   Cross-fade entre 0.3 et 0.7 pour transition douce.
     */
    private void renderOnsetTint(int w, int h, float intensity, float beat) {
        // Calcul des intensites des 2 sous-phases (cross-fade)
        // Intro : visible de 0 a 0.7, max plein de 0 a 0.5, fade 0.5 a 0.7
        float introI = Math.min(1.0f, Math.max(0f, intensity / 0.5f));
        if (intensity > 0.5f) {
            introI *= Math.max(0f, 1.0f - (intensity - 0.5f) / 0.2f);
        }
        // Onset color : commence a 0.3, max a 1.0
        float onsetI = Math.max(0f, (intensity - 0.3f) / 0.7f);

        if (introI > 0.01f) {
            renderIntroPresence(w, h, introI, beat);
        }
        if (onsetI > 0.01f) {
            renderOnsetColors(w, h, onsetI, beat);
        }
    }

    /**
     * Sub-phase INTRO : presence subtile mais perceptible.
     * Le joueur ne voit rien d'evident mais ressent que ca commence.
     */
    private void renderIntroPresence(int w, int h, float intensity, float beat) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        float cx = w / 2.0f;
        float cy = h / 2.0f;

        // === 1. Vignette sombre qui respire (assombrit les bords avec teinte rouge tres faible) ===
        float vignetteAlpha = intensity * 0.15f * (0.7f + 0.3f * beat);
        float radius = Math.max(w, h) * 0.55f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        // Centre transparent (pas d'assombrissement au centre)
        buf.pos(cx, cy, 0).color(0.15f, 0.05f, 0.08f, 0f).endVertex();
        // Bord assombri rouge tres tres sombre
        int segments = 32;
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
            float ex = cx + (float) Math.cos(angle) * radius;
            float ey = cy + (float) Math.sin(angle) * radius;
            buf.pos(ex, ey, 0).color(0.15f, 0.05f, 0.08f, vignetteAlpha).endVertex();
        }
        tess.draw();

        // === 2. Particules eparses qui clignotent (8 points fixes pseudo-aleatoires) ===
        float[][] particles = {
            {0.15f, 0.20f}, {0.85f, 0.15f}, {0.10f, 0.75f}, {0.90f, 0.80f},
            {0.30f, 0.50f}, {0.70f, 0.45f}, {0.50f, 0.10f}, {0.50f, 0.90f}
        };

        long timeFrames = (System.currentTimeMillis() / 100) % 1000;
        for (int i = 0; i < particles.length; i++) {
            // Clignotement pseudo-aleatoire base sur l'index et le temps
            float blink = (float) Math.sin((timeFrames * 0.05) + i * 1.7) * 0.5f + 0.5f;
            float pAlpha = intensity * blink * 0.5f * (0.6f + 0.4f * beat);
            if (pAlpha < 0.02f) continue;

            float px = particles[i][0] * w;
            float py = particles[i][1] * h;
            float psize = 4.0f + 2.0f * blink;

            // Cercle simple
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(px, py, 0).color(1.0f, 0.9f, 0.7f, pAlpha).endVertex();
            for (int k = 0; k <= 16; k++) {
                float a = (k / 16f) * 2.0f * (float) Math.PI;
                buf.pos(px + (float) Math.cos(a) * psize,
                        py + (float) Math.sin(a) * psize, 0)
                   .color(1.0f, 0.9f, 0.7f, 0f).endVertex();
            }
            tess.draw();
        }

        // === 3. Pulse central tres leger (oscillation luminosite) ===
        float pulseAlpha = intensity * 0.06f * beat;
        if (pulseAlpha > 0.005f) {
            float pulseRadius = Math.min(w, h) * 0.15f;
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(cx, cy, 0).color(1.0f, 0.95f, 0.85f, pulseAlpha).endVertex();
            for (int k = 0; k <= 24; k++) {
                float a = (k / 24f) * 2.0f * (float) Math.PI;
                buf.pos(cx + (float) Math.cos(a) * pulseRadius,
                        cy + (float) Math.sin(a) * pulseRadius, 0)
                   .color(1.0f, 0.95f, 0.85f, 0f).endVertex();
            }
            tess.draw();
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Sub-phase ONSET coloree : les couleurs apparaissent et se saturent.
     * Cyan -> magenta progressif, vignette coloree, pulse central colore.
     */
    private void renderOnsetColors(int w, int h, float intensity, float beat) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        // ADD blend pour que les couleurs s'ajoutent (effet boost luminosite)
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float radius = Math.max(w, h) * 0.65f;

        // === Couleur centrale : interpolation cyan -> magenta selon intensity ===
        // intensity 0 = cyan froid, intensity 1 = magenta chaud psychedélique
        float r_color = 0.0f + intensity * 1.0f;       // 0 -> 1 (rouge)
        float g_color = 0.7f - intensity * 0.7f;       // 0.7 -> 0 (vert disparait)
        float b_color = 1.0f - intensity * 0.2f;       // 1 -> 0.8 (bleu reste)
        float alpha_center = intensity * 0.18f * (0.7f + 0.3f * beat);

        // === 1. Vignette colorée centrale (centre brillant, bords transparents) ===
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(cx, cy, 0).color(r_color, g_color, b_color, alpha_center).endVertex();
        int segments = 48;
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
            float ex = cx + (float) Math.cos(angle) * radius;
            float ey = cy + (float) Math.sin(angle) * radius;
            buf.pos(ex, ey, 0).color(r_color, g_color, b_color, 0f).endVertex();
        }
        tess.draw();

        // === 2. Vignette de couleur opposee aux bords (effet contraste) ===
        // Si centre = magenta, bords = cyan. Effet psychedelique.
        float r_edge = 1.0f - r_color;
        float g_edge = 1.0f - g_color;
        float b_edge = 1.0f - b_color;
        float alpha_edge = intensity * 0.10f * (0.7f + 0.3f * beat);
        float edgeRadiusInner = Math.max(w, h) * 0.45f;
        float edgeRadiusOuter = Math.max(w, h) * 0.75f;

        buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
            float ca = (float) Math.cos(angle);
            float sa = (float) Math.sin(angle);
            // Inner ring : transparent
            buf.pos(cx + ca * edgeRadiusInner, cy + sa * edgeRadiusInner, 0)
               .color(r_edge, g_edge, b_edge, 0f).endVertex();
            // Outer ring : colore
            buf.pos(cx + ca * edgeRadiusOuter, cy + sa * edgeRadiusOuter, 0)
               .color(r_edge, g_edge, b_edge, alpha_edge).endVertex();
        }
        tess.draw();

        // === 3. Pulse central colore beat-sync (gros flash discret) ===
        float pulseAlpha = intensity * 0.20f * beat;
        if (pulseAlpha > 0.01f) {
            float pulseRadius = Math.min(w, h) * (0.20f + 0.05f * beat);
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(cx, cy, 0).color(r_color, g_color, b_color, pulseAlpha).endVertex();
            for (int k = 0; k <= 32; k++) {
                float a = (k / 32f) * 2.0f * (float) Math.PI;
                buf.pos(cx + (float) Math.cos(a) * pulseRadius,
                        cy + (float) Math.sin(a) * pulseRadius, 0)
                   .color(r_color, g_color, b_color, 0f).endVertex();
            }
            tess.draw();
        }

        // Restore blend mode normal
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
        // v1.0.356 : alpha 0.65 -> 0.85 (encore plus opaque)
        float alpha = intensity * 0.85f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        double tt = t * 0.05;

        // v1.0.356 : Pre-compute couleur aux NOEUDS de la grille (cols+1 x rows+1)
        // pour que chaque cellule ait des couleurs LISSEES aux 4 coins.
        // OpenGL interpolera automatiquement entre les coins -> plus de
        // grille rectangulaire visible (cellules carrees disparaissent).
        float[][] gridR = new float[cols + 1][rows + 1];
        float[][] gridG = new float[cols + 1][rows + 1];
        float[][] gridB = new float[cols + 1][rows + 1];
        
        for (int col = 0; col <= cols; col++) {
            for (int row = 0; row <= rows; row++) {
                double v = Math.sin(col * 0.45 + tt * 1.2)
                         + Math.sin(row * 0.55 + tt * 0.9)
                         + Math.sin((col + row) * 0.3 + tt * 1.5);
                double norm = (v + 3.0) * (8.0 / 6.0);
                int idxA = ((int) norm) % 8;
                int idxB = (idxA + 1) % 8;
                double frac = norm - Math.floor(norm);
                float[] cA = DMT_PALETTE[idxA];
                float[] cB = DMT_PALETTE[idxB];
                gridR[col][row] = (float) (cA[0] * (1.0 - frac) + cB[0] * frac);
                gridG[col][row] = (float) (cA[1] * (1.0 - frac) + cB[1] * frac);
                gridB[col][row] = (float) (cA[2] * (1.0 - frac) + cB[2] * frac);
            }
        }

        // Dessine les cellules avec couleurs interpolees aux 4 coins
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float x0 = col * cellW;
                float y0 = row * cellH;
                // 4 coins : top-left (col,row), top-right (col+1,row),
                //           bottom-right (col+1,row+1), bottom-left (col,row+1)
                // OpenGL fait l'interpolation lineaire entre les 4 -> gradient lisse
                buf.pos(x0, y0 + cellH, 0)
                   .color(gridR[col][row + 1], gridG[col][row + 1],
                          gridB[col][row + 1], alpha).endVertex();
                buf.pos(x0 + cellW, y0 + cellH, 0)
                   .color(gridR[col + 1][row + 1], gridG[col + 1][row + 1],
                          gridB[col + 1][row + 1], alpha).endVertex();
                buf.pos(x0 + cellW, y0, 0)
                   .color(gridR[col + 1][row], gridG[col + 1][row],
                          gridB[col + 1][row], alpha).endVertex();
                buf.pos(x0, y0, 0)
                   .color(gridR[col][row], gridG[col][row],
                          gridB[col][row], alpha).endVertex();
            }
        }
        tess.draw();

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
    /**
     * Stage 3 GEOMETRIC : mandala fractal MULTI-COUCHE pour effet impressionnant.
     *
     *   Couche XL (2.5x screen)  : mandala lent en arriere, alpha 25%, rotation +
     *   Couche M  (1.5x screen)  : mandala central principal, alpha 45%, rotation INVERSE
     *   Couche S  (0.6x screen)  : focal point au centre, alpha 30%, rotation rapide +
     *
     * Effet :
     *   - Rotations contraires entre couches = hypnotique
     *   - Vitesses differentes = profondeur fractale
     *   - Pulsation scale beat-sync (zoom in/out 8% au BPM)
     *   - Glow central au beat fort = focal point qui pulse
     *
     * Crossfade preserve sur chaque couche entre frame N et frame N+1.
     */
    private void renderMandala(Minecraft mc, int w, int h, long t, float intensity, float beat) {
        int frame = (int)((t / MANDALA_FRAME_DURATION) % MANDALA_TEX.length);
        int nextFrame = (frame + 1) % MANDALA_TEX.length;
        float fadeFrac = (t % MANDALA_FRAME_DURATION) / (float) MANDALA_FRAME_DURATION;

        // Pulsation scale beat-sync (toutes les couches respirent ensemble)
        float beatScale = 1.0f + beat * 0.08f;

        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float baseSize = (float) Math.max(w, h);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        // === COUCHE XL : mandala lent en arriere ===
        // Rotation : 1 tour en 60s (1200 ticks)
        float rotXL = (t % 1200) / 1200.0f * 360.0f;
        float sizeXL = baseSize * 2.5f * beatScale;
        // v1.0.352 : alpha 0.25 -> 0.40 (moins transparent)
        float aXL = intensity * 0.40f;
        drawRotatedTexture(mc, MANDALA_TEX[frame], cx, cy, sizeXL, rotXL,
                           aXL * (1.0f - fadeFrac));
        drawRotatedTexture(mc, MANDALA_TEX[nextFrame], cx, cy, sizeXL,
                           rotXL + 12.0f, aXL * fadeFrac);

        // === COUCHE M : mandala central principal (rotation INVERSE) ===
        // Rotation : 1 tour en 30s (600 ticks) DANS L'AUTRE SENS
        float rotM = -(t % 600) / 600.0f * 360.0f;
        float sizeM = baseSize * 1.5f * beatScale;
        // v1.0.352 : alpha 0.45 -> 0.70 (mandala principal plus visible)
        float aM = intensity * 0.70f;
        // Frame offset de +4 pour que la couche M ne soit pas la meme que XL
        int frameM = (frame + 4) % MANDALA_TEX.length;
        int nextFrameM = (nextFrame + 4) % MANDALA_TEX.length;
        drawRotatedTexture(mc, MANDALA_TEX[frameM], cx, cy, sizeM, rotM,
                           aM * (1.0f - fadeFrac));
        drawRotatedTexture(mc, MANDALA_TEX[nextFrameM], cx, cy, sizeM,
                           rotM - 12.0f, aM * fadeFrac);

        // === COUCHE S : focal point au centre (rotation rapide) ===
        // Rotation : 1 tour en 15s (300 ticks)
        float rotS = (t % 300) / 300.0f * 360.0f;
        float sizeS = baseSize * 0.6f * beatScale;
        // v1.0.352 : alpha 0.30 -> 0.50
        float aS = intensity * 0.50f;
        // Frame offset de +8 pour diversite
        int frameS = (frame + 8) % MANDALA_TEX.length;
        int nextFrameS = (nextFrame + 8) % MANDALA_TEX.length;
        drawRotatedTexture(mc, MANDALA_TEX[frameS], cx, cy, sizeS, rotS,
                           aS * (1.0f - fadeFrac));
        drawRotatedTexture(mc, MANDALA_TEX[nextFrameS], cx, cy, sizeS,
                           rotS + 12.0f, aS * fadeFrac);

        // === GLOW CENTRAL beat-sync (apparait au beat fort) ===
        if (beat > 0.3f) {
            GlStateManager.disableTexture2D();
            float glowAlpha = intensity * beat * 0.35f;
            float glowRadius = baseSize * 0.08f * (1.0f + beat * 0.3f);

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            // Centre brillant : couleur qui cycle dans la palette DMT selon t
            int paletteIdx = (int)((t / 60) % DMT_PALETTE.length);
            float[] glowColor = DMT_PALETTE[paletteIdx];
            buf.pos(cx, cy, 0).color(glowColor[0], glowColor[1], glowColor[2], glowAlpha).endVertex();
            int segments = 32;
            for (int i = 0; i <= segments; i++) {
                float angle = (i / (float) segments) * 2.0f * (float) Math.PI;
                float ex = cx + (float) Math.cos(angle) * glowRadius;
                float ey = cy + (float) Math.sin(angle) * glowRadius;
                buf.pos(ex, ey, 0).color(glowColor[0], glowColor[1], glowColor[2], 0f).endVertex();
            }
            tess.draw();
            GlStateManager.enableTexture2D();
        }

        // Restore blend mode normal pour les couches suivantes
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
    }

    private void drawRotatedTexture(Minecraft mc, ResourceLocation tex,
                                     float cx, float cy, float size,
                                     float rotation, float alpha) {
        // v1.0.346 BUGFIX : force le bon blend mode CHAQUE FOIS qu'on dessine
        // une texture. Avant : si la couche precedente (ex: tunnel 3D additive)
        // avait un blend func different, le PNG s'affichait avec un carre opaque
        // autour des zones transparentes. Force GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA
        // = blend alpha standard qui respecte la transparence du PNG.
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

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
        GlStateManager.enableDepth();
    }

    /**
     * Tunnel zoom in/out -- 3 couches parallax avec scales/rotations/vitesses
     * differentes et acceleration progressive (effet "on accelere dans le temps").
     *
     * v1.0.337 (Etape 7 visuel ultime) :
     *   - 4 VARIANTES de tile qui se succedent pendant Stage 4 (1:30) :
     *     A (hex DMT) -> B (cristaux) -> C (runes Voss) -> D (blueprint Voss)
     *     Crossfade entre variantes successives sur ~3s.
     *   - NOUVELLE COUCHE 3D QUADS (faux 3D) en plus des 3 couches parallax :
     *     ~32 quads disperses en profondeur Z [0.5..50], scale = K/Z donc
     *     les quads loin sont petits, ceux proches sont enormes. Z avance
     *     vers la camera a chaque tick -> effet hyperspace Star Wars.
     *
     * v1.0.332 (Etape 4) :
     *   - 3 couches parallax (fond/moyen/avant) avec scales/rotations/vitesses
     *     differentes + acceleration progressive sur le PEAK.
     */
    private void renderZoomTunnel(Minecraft mc, int w, int h, long t, float intensity) {
        float progress = ManifoldClientState.getTripProgress(t);
        float accel = getTunnelAcceleration(progress);

        // Selection des 2 variantes a blend (A-B-C-D) selon le progress
        int[] variantPair = computeVariantPair(progress);
        int vA = variantPair[0];
        int vB = variantPair[1];
        float vFade = Float.intBitsToFloat(variantPair[2]);  // hack pour passer un float

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.enableTexture2D();

        // === 3 couches parallax 2D (background, comme avant) ===
        // v1.0.352 : alphas 0.10/0.15/0.22 -> 0.18/0.25/0.35 (moins transparent)
        // Variante A avec alpha (1 - vFade) + variante B avec alpha vFade
        // Couche fond
        renderTunnelLayer(mc, w, h, t, intensity * 0.18f * (1f - vFade), 0.30f, 0.5f, 0.5f * accel, vA);
        if (vFade > 0.01f) {
            renderTunnelLayer(mc, w, h, t, intensity * 0.18f * vFade, 0.30f, 0.5f, 0.5f * accel, vB);
        }
        // Couche moyenne
        renderTunnelLayer(mc, w, h, t, intensity * 0.25f * (1f - vFade), 0.60f, 1.0f, 1.0f * accel, vA);
        if (vFade > 0.01f) {
            renderTunnelLayer(mc, w, h, t, intensity * 0.25f * vFade, 0.60f, 1.0f, 1.0f * accel, vB);
        }
        // Couche avant
        renderTunnelLayer(mc, w, h, t, intensity * 0.35f * (1f - vFade), 1.20f, 1.8f, 1.8f * accel, vA);
        if (vFade > 0.01f) {
            renderTunnelLayer(mc, w, h, t, intensity * 0.35f * vFade, 1.20f, 1.8f, 1.8f * accel, vB);
        }

        // === NOUVEAU : couche 3D quads en perspective ===
        renderHyperspace3D(mc, w, h, t, intensity, accel, vA, vB, vFade);

        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * Selection des variantes A/B/C/D selon le progress du trip.
     * Stage 4 aller : 0.3125 -> 0.5 (1:30 = 4 segments de 22.5s)
     * Stage 4 retour : 0.6875 -> 0.8125 (mirror, 4 segments)
     * Hors Stage 4 : variante A par defaut (couches parallax restent
     * subtiles en Stages 2-3 et 5).
     *
     * Returns {indexA, indexB, fadeAB_as_floatBits}
     */
    private int[] computeVariantPair(float progress) {
        final float S4_START = ManifoldEffectHandler.STAGE_3_GEOMETRIC_END;     // 0.3125
        final float S5_START = ManifoldEffectHandler.STAGE_4_HYPERSPACE_END;    // 0.5
        final float S5_END   = ManifoldEffectHandler.STAGE_5_PEAK_END;          // 0.6875
        final float S4R_END  = ManifoldEffectHandler.STAGE_4R_HYPERSPACE_END;   // 0.8125
        final float CROSSFADE_FRAC = 0.50f; // 50% de chaque segment = crossfade long
                                            // (etait 0.15f, augmente pour transitions
                                            // tres douces entre variantes A->B->C->D)

        // Hors Stage 4 : variante A pure
        if (progress < S4_START || (progress >= S5_START && progress < S5_END) || progress >= S4R_END) {
            return new int[]{0, 0, Float.floatToIntBits(0f)};
        }

        // Determination de la "phase" dans Stage 4 (aller ou retour)
        float localProg;
        if (progress < S5_START) {
            // Aller : 0.3125..0.5 -> 0..1
            localProg = (progress - S4_START) / (S5_START - S4_START);
        } else {
            // Retour : 0.6875..0.8125 -> 0..1 mais en MIRROR (D->A au lieu de A->D)
            localProg = 1f - (progress - S5_END) / (S4R_END - S5_END);
        }

        // 4 segments de longueur 0.25 chacun
        float segIdx = localProg * 4f;        // 0..4
        int seg = (int) Math.floor(segIdx);   // 0..3
        if (seg > 3) seg = 3;
        float fracInSeg = segIdx - seg;       // 0..1 dans le segment

        // Crossfade dans les derniers CROSSFADE_FRAC du segment vers le suivant
        if (fracInSeg < (1f - CROSSFADE_FRAC) || seg == 3) {
            return new int[]{seg, seg, Float.floatToIntBits(0f)};
        }
        float fade = (fracInSeg - (1f - CROSSFADE_FRAC)) / CROSSFADE_FRAC;
        return new int[]{seg, seg + 1, Float.floatToIntBits(fade)};
    }

    /**
     * Rend une seule couche 2D du tunnel.
     * v1.0.337 : accepte variantIdx pour bind la bonne texture + tinte.
     */
    private void renderTunnelLayer(Minecraft mc, int w, int h, long t,
                                    float alpha, float scaleMul,
                                    float rotSpeed, float zoomSpeed,
                                    int variantIdx) {
        if (alpha < 0.005f) return;

        mc.getTextureManager().bindTexture(TUNNEL_TILES[variantIdx]);
        float[] tint = TUNNEL_TINTS[variantIdx];

        // Zoom cycle 6s a vitesse normale, accelere par zoomSpeed
        double zoomPhase = (((double) t) * zoomSpeed) % 120.0;
        double zoomCycle = zoomPhase / 120.0;
        float scale = (0.6f + (float)(Math.sin(zoomCycle * 2 * Math.PI) * 0.5 + 0.5) * 1.4f) * scaleMul;
        float rotation = (float)((((double) t) * rotSpeed) % 800.0 / 800.0 * 360.0);

        float tileSize = 256.0f * scale * 0.5f;
        if (tileSize < 8.0f) tileSize = 8.0f;
        // v1.0.352 FIX : grille doit couvrir la DIAGONALE (pas juste w x h) sinon
        // apres rotation les coins de l'ecran sortent de la grille -> carres noirs.
        // Diagonale de 1920x1080 = 2200 pixels, donc on dimensionne la grille
        // pour couvrir un carre de cote = diagonale.
        // v1.0.358 : marge +4 -> +12 tiles pour eliminer les coins noirs residuels
        // qui apparaissent avec scroll offset + rotation continue.
        float diagonal = (float)Math.sqrt(w * w + h * h);
        int tilesPerSide = (int)Math.ceil(diagonal / tileSize) + 12;
        int tilesX = tilesPerSide;
        int tilesY = tilesPerSide;
        float scrollOffset = (float)((((double) t) * zoomSpeed) % 240.0 / 240.0 * tileSize);

        GlStateManager.color(tint[0], tint[1], tint[2], alpha);

        GlStateManager.pushMatrix();
        GlStateManager.translate(w / 2.0f, h / 2.0f, 0);
        GlStateManager.rotate(rotation, 0, 0, 1);
        GlStateManager.translate(-w / 2.0f, -h / 2.0f, 0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Centrer la grille sur le centre de l'ecran (apres rotation = toujours centre)
        float gridOriginX = w / 2.0f - tilesX * tileSize / 2.0f - scrollOffset;
        float gridOriginY = h / 2.0f - tilesY * tileSize / 2.0f - scrollOffset;

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

    // === FAUX 3D : 80 quads precomputes en perspective (boost x2.5 vs avant) ===
    // Init seede dans static block pour reproductibilite.
    private static final int N_HYPER_QUADS = 80;
    private static final float[] HYPER_X = new float[N_HYPER_QUADS];   // [-1..1]
    private static final float[] HYPER_Y = new float[N_HYPER_QUADS];   // [-1..1]
    private static final float[] HYPER_Z = new float[N_HYPER_QUADS];   // [HYPER_Z_NEAR..HYPER_Z_FAR]
    private static final float[] HYPER_ROT = new float[N_HYPER_QUADS]; // rotation initiale
    private static final float HYPER_Z_NEAR = 0.5f;
    private static final float HYPER_Z_FAR = 100.0f;  // 50 -> 100 : 2x plus de profondeur
    private static final float HYPER_FOCAL = 250.0f; // distance focale (pixels) -- joue sur la taille
    static {
        java.util.Random rng = new java.util.Random(2026L);
        for (int i = 0; i < N_HYPER_QUADS; i++) {
            HYPER_X[i] = rng.nextFloat() * 2.0f - 1.0f;
            HYPER_Y[i] = rng.nextFloat() * 2.0f - 1.0f;
            HYPER_Z[i] = HYPER_Z_NEAR + rng.nextFloat() * (HYPER_Z_FAR - HYPER_Z_NEAR);
            HYPER_ROT[i] = rng.nextFloat() * 360.0f;
        }
    }

    // === ANNEAUX STARGATE : 6 anneaux concentriques en perspective ===
    // v1.0.358 : reduit de 12 a 6 pour moins de scintillement
    // (user feedback : 'plein de ronds qui pop et depop ca fait mal')
    private static final int N_HYPER_RINGS = 6;
    private static final float[] RING_Z = new float[N_HYPER_RINGS];
    private static final float[] RING_ROT = new float[N_HYPER_RINGS];
    static {
        java.util.Random ringRng = new java.util.Random(2027L);
        // Distribution uniforme de Z pour avoir des anneaux a toutes les profondeurs
        for (int i = 0; i < N_HYPER_RINGS; i++) {
            RING_Z[i] = HYPER_Z_NEAR + (i + 0.5f) / N_HYPER_RINGS *
                        (HYPER_Z_FAR - HYPER_Z_NEAR);
            RING_ROT[i] = ringRng.nextFloat() * 360.0f;
        }
    }

    // === GHOST FIGURES : 6 silhouettes humaines en perspective 3D ===
    // v1.0.358 : reduit de 12 a 6 pour moins de scintillement
    private static final int N_GHOST_FIGURES = 6;
    private static final float[] GHOST_X = new float[N_GHOST_FIGURES];
    private static final float[] GHOST_Y = new float[N_GHOST_FIGURES];
    private static final float[] GHOST_Z = new float[N_GHOST_FIGURES];
    private static final int[] GHOST_ERA = new int[N_GHOST_FIGURES]; // 0..5 era
    // Couleurs par era : caveman / antique / medieval / modern / future / cosmic
    private static final float[][] GHOST_ERA_COLORS = {
        {1.0f, 0.55f, 0.15f},  // 0 caveman orange chaud
        {1.0f, 0.85f, 0.30f},  // 1 antique gold
        {1.0f, 0.40f, 0.70f},  // 2 medieval rose
        {0.20f, 0.85f, 1.0f},  // 3 modern cyan
        {0.40f, 1.0f, 0.40f},  // 4 future green acid
        {0.75f, 0.25f, 1.0f},  // 5 cosmic violet
    };
    static {
        java.util.Random ghostRng = new java.util.Random(2028L);
        for (int i = 0; i < N_GHOST_FIGURES; i++) {
            // Dispersion plus large que les quads (angles plus aux bords)
            GHOST_X[i] = (ghostRng.nextFloat() * 2.0f - 1.0f) * 1.2f;
            GHOST_Y[i] = (ghostRng.nextFloat() * 2.0f - 1.0f) * 1.2f;
            GHOST_Z[i] = HYPER_Z_NEAR + ghostRng.nextFloat() *
                        (HYPER_Z_FAR - HYPER_Z_NEAR);
            GHOST_ERA[i] = ghostRng.nextInt(6);
        }
    }

    /**
     * Rend la couche 3D faux-perspective : N quads disperses en profondeur,
     * Z avance vers la camera -> effet hyperspace Star Wars.
     *
     * v1.0.337 (Etape 7).
     *
     * Algo :
     *   - chaque quad a ses coords (xN, yN, z) ou xN/yN sont en [-1,1]
     *   - le Z courant = (HYPER_Z[i] - speed * t) wrapped dans [Z_NEAR, Z_FAR]
     *   - position 2D = (cx + xN * focal/z, cy + yN * focal/z)
     *   - taille 2D = baseSize * focal/z  -> tiles loin petits, pres enormes
     *   - alpha fade aux 2 extremites (apparait en Z_FAR, disparait en Z_NEAR)
     *   - rotation propre par quad + drift global (effet vortex)
     */
    /**
     * v1.0.355 : Calcule l'intensite du tunnel infini fresque.
     * Active sur les 30 dernieres secondes du HYPERSPACE (0.4375 -> 0.5)
     * + courte transition au debut du PEAK pour fade out doux.
     * 
     *   progress < 0.4375           : 0 (pas encore actif)
     *   0.4375 < progress < 0.4500  : fade in (smoothstep)
     *   0.4500 < progress < 0.5000  : 1.0 (tunnel infini plein regime)
     *   0.5000 < progress < 0.5125  : fade out smooth (entree PEAK)
     *   progress > 0.5125           : 0 (PEAK pur)
     */
    private float computeFresqueIntensity(float progress) {
        float fadeInStart = 0.4375f;   // 3:30 du trip (8 min)
        float fadeInEnd = 0.4500f;     // 3:36
        float fadeOutStart = 0.5000f;  // 4:00 (entree PEAK)
        float fadeOutEnd = 0.5125f;    // 4:06
        
        if (progress < fadeInStart || progress > fadeOutEnd) return 0f;
        
        if (progress < fadeInEnd) {
            float t = (progress - fadeInStart) / (fadeInEnd - fadeInStart);
            return t * t * (3f - 2f * t);  // smoothstep
        }
        if (progress < fadeOutStart) return 1f;
        
        // Fade out
        float t = (progress - fadeOutStart) / (fadeOutEnd - fadeOutStart);
        float s = t * t * (3f - 2f * t);
        return 1f - s;
    }

    /**
     * v1.0.355 : Tunnel infini avec fresques 4K mandala fractal multi-echelles.
     * 
     * Concept : 4 fresques DMT 4K, chacune scaled de 1.0 a 16.0 sur 30 secondes
     * (cycle log : zoom apparent constant). Quand une fresque finit son cycle
     * (zoom 16x), crossfade vers la suivante. En boucle, donc on ne sait jamais
     * quelle est la prochaine fresque -> sensation de plongee infinie dans
     * une oeuvre d'art.
     * 
     * Le scale logarithmique fait que le ressenti de zoom est CONSTANT (pas
     * d'acceleration vers la fin), ce qui est crucial pour l'effet 'tunnel'.
     */
    private void renderInfiniteFresqueTunnel(Minecraft mc, int w, int h, long t,
                                              float intensity) {
        if (intensity < 0.01f) return;
        
        // Cycle courant + fraction dans le cycle
        long inCycle = t % FRESQUE_CYCLE_TICKS;
        float cycleFrac = inCycle / (float) FRESQUE_CYCLE_TICKS;
        
        // Index fresque courante
        int currFresque = (int)((t / FRESQUE_CYCLE_TICKS) % N_FRESQUES);
        int nextFresque = (currFresque + 1) % N_FRESQUES;
        
        // Crossfade entre fresques sur les 5% derniers du cycle
        float crossfade = 0f;
        if (cycleFrac > 0.95f) {
            float fadeT = (cycleFrac - 0.95f) / 0.05f;
            crossfade = fadeT * fadeT * (3f - 2f * fadeT);
        }
        
        // Scale exponentiel : 1.0 -> 16.0 (logaritmique = ressenti constant)
        // scale(t=0) = 1, scale(t=1) = 16, scale(t=0.5) = 4 (sqrt(16))
        float scaleCurr = (float) Math.pow(FRESQUE_ZOOM_MAX / FRESQUE_ZOOM_MIN,
                                            cycleFrac);
        // Pour la fresque suivante (qui prend la suite quand crossfade > 0),
        // elle est en debut de cycle (scale = 1.0)
        float scaleNext = 1.0f;
        
        // Taille de base : couvre toute la diagonale pour eviter coins vides
        float baseSize = (float) Math.sqrt(w * w + h * h) * 1.1f;
        
        float cx = w / 2.0f;
        float cy = h / 2.0f;
        
        // Rotation lente pour effet vortex (1 tour en 4 minutes)
        float rotation = ((t % 4800) / 4800.0f) * 360.0f;
        
        // Dessin fresque courante (alpha 1.0 - crossfade)
        if (intensity * (1f - crossfade) > 0.01f) {
            float sizeCurr = baseSize * scaleCurr;
            drawRotatedTexture(mc, FRESQUE_TEX[currFresque], cx, cy, sizeCurr,
                               rotation, intensity * (1f - crossfade));
        }
        
        // Dessin fresque suivante (en crossfade quand on approche de la fin)
        if (intensity * crossfade > 0.01f) {
            float sizeNext = baseSize * scaleNext;
            drawRotatedTexture(mc, FRESQUE_TEX[nextFresque], cx, cy, sizeNext,
                               -rotation * 0.7f, intensity * crossfade);
        }
    }

    private void renderHyperspace3D(Minecraft mc, int w, int h, long t,
                                     float intensity, float accel,
                                     int vA, int vB, float vFade) {
        // v1.0.351 : Vitesse de defilement Z par tick BOOST (0.20 -> 0.30, +50%)
        float zSpeed = 0.30f * accel;
        float zRange = HYPER_Z_FAR - HYPER_Z_NEAR;

        // Drift de rotation global (effet vortex)
        float globalRot = (float)(((double) t * 0.5) % 360.0);

        // v1.0.353 : Detection du PEAK pour attenuer tunnel + supprimer rings/ghosts
        // Pendant le PEAK le focus doit etre sur l'entite, pas sur les elements
        // du tunnel qui creeraient un chaos visuel.
        float progress = ManifoldClientState.getTripProgress(t);
        float peakStart = ManifoldEffectHandler.STAGE_4_HYPERSPACE_END;
        // Scale tunnel : 1.0 hors PEAK, descend smoothly a 0.4 pendant les
        // premieres 5% du trip apres entree en PEAK
        float peakDamp = 1.0f;
        if (progress >= peakStart) {
            float t01 = (progress - peakStart) / 0.05f;
            if (t01 > 1.0f) t01 = 1.0f;
            // Smootherstep de 1.0 vers 0.4
            float s = t01 * t01 * (3f - 2f * t01);
            peakDamp = 1.0f - s * 0.6f; // 1.0 -> 0.4
        }

        float baseAlpha = intensity * 0.55f * peakDamp;
        if (baseAlpha < 0.01f) return;

        float cx = w / 2.0f;
        float cy = h / 2.0f;

        // Pour eviter trop de bind texture changes, on fait 2 passes :
        // une avec variant A, une avec variant B (si crossfade actif).
        renderHyperspace3DPass(mc, w, h, t, cx, cy, zSpeed, zRange, globalRot,
                                baseAlpha * (1f - vFade), vA);
        if (vFade > 0.01f) {
            renderHyperspace3DPass(mc, w, h, t, cx, cy, zSpeed, zRange, globalRot,
                                    baseAlpha * vFade, vB);
        }

        // v1.0.353 : RINGS + GHOSTS SEULEMENT pendant HYPERSPACE PUR (pas PEAK).
        // Ces elements creent du chaos visuel pendant le PEAK ou on veut juste
        // l'entite + occluder. Fade smooth a 0 sur les premieres 5% du PEAK.
        float ringsGhostScale = 1.0f;
        if (progress >= peakStart) {
            float t01 = (progress - peakStart) / 0.05f;
            if (t01 > 1.0f) t01 = 1.0f;
            float s = t01 * t01 * (3f - 2f * t01);
            ringsGhostScale = 1.0f - s; // 1.0 -> 0.0
        }
        if (ringsGhostScale > 0.01f) {
            // === ANNEAUX STARGATE : 12 anneaux concentriques en perspective ===
            renderHyperspaceRings(w, h, t, intensity * ringsGhostScale,
                                   zSpeed, zRange, globalRot);
            // === GHOST FIGURES : silhouettes humaines = vies infinies ===
            renderGhostFigures(w, h, t, intensity * ringsGhostScale,
                                zSpeed * 1.3f, zRange);
        }
    }

    private void renderHyperspace3DPass(Minecraft mc, int w, int h, long t,
                                         float cx, float cy,
                                         float zSpeed, float zRange,
                                         float globalRot, float passAlpha,
                                         int variantIdx) {
        if (passAlpha < 0.01f) return;

        mc.getTextureManager().bindTexture(TUNNEL_TILES[variantIdx]);
        float[] tint = TUNNEL_TINTS[variantIdx];

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

        for (int i = 0; i < N_HYPER_QUADS; i++) {
            // Z courant : Z_initial - speed*t, wrapped
            float zRaw = HYPER_Z[i] - (float)((zSpeed * t) % zRange);
            // Wrap dans [Z_NEAR, Z_FAR]
            while (zRaw < HYPER_Z_NEAR) zRaw += zRange;
            while (zRaw > HYPER_Z_FAR) zRaw -= zRange;

            // Skip si trop pres (eviter division par 0 et tile gigantesque)
            if (zRaw < HYPER_Z_NEAR + 0.05f) continue;

            // Projection : focal / z
            float persp = HYPER_FOCAL / zRaw;
            float px = cx + HYPER_X[i] * w * 0.5f * persp / 4.0f;
            float py = cy + HYPER_Y[i] * h * 0.5f * persp / 4.0f;
            // Taille du quad : 64px * persp / 4 (pour avoir des tiles raisonnables)
            float halfSize = 32.0f * persp / 4.0f;
            if (halfSize > 800f) halfSize = 800f; // clamp anti-explosion

            // Alpha fade aux extremites de Z
            float zAlpha = 1.0f;
            if (zRaw < HYPER_Z_NEAR + 6.0f) {
                zAlpha = (zRaw - HYPER_Z_NEAR) / 6.0f;
            } else if (zRaw > HYPER_Z_FAR - 5.0f) {
                zAlpha = (HYPER_Z_FAR - zRaw) / 5.0f;
            }
            float a = passAlpha * Math.max(0f, Math.min(1f, zAlpha));
            if (a < 0.005f) continue;

            // Rotation propre + globale
            float rot = (HYPER_ROT[i] + globalRot) * (float) Math.PI / 180.0f;
            float c = (float) Math.cos(rot);
            float s = (float) Math.sin(rot);

            // 4 coins du quad apres rotation 2D
            float[][] corners = {
                {-halfSize, -halfSize}, { halfSize, -halfSize},
                { halfSize,  halfSize}, {-halfSize,  halfSize}
            };
            float[][] uvs = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};

            // R/G/B selon tint, alpha selon a
            int rByte = (int)(tint[0] * 255f);
            int gByte = (int)(tint[1] * 255f);
            int bByte = (int)(tint[2] * 255f);
            int aByte = (int)(a * 255f);

            for (int k = 0; k < 4; k++) {
                float vx = corners[k][0] * c - corners[k][1] * s + px;
                float vy = corners[k][0] * s + corners[k][1] * c + py;
                buf.pos(vx, vy, 0).tex(uvs[k][0], uvs[k][1]).color(rByte, gByte, bByte, aByte).endVertex();
            }
        }
        tess.draw();
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
     * v1.0.351 : ANNEAUX STARGATE en perspective 3D pendant Stage 4 HYPERSPACE.
     *
     * 12 anneaux concentriques distribues en Z [Z_NEAR..Z_FAR]. Chaque anneau
     * avance vers la camera avec zSpeed, wrap a Z_FAR quand il passe Z_NEAR.
     * Le rayon visible = focal/Z (perspective) -> anneau loin = petit, anneau
     * proche = enorme. Effet 'on traverse des portes Stargate sans fin'.
     *
     * Couleurs cyclees dans la palette DMT, rotation propre + drift global.
     */
    private void renderHyperspaceRings(int w, int h, long t, float intensity,
                                        float zSpeed, float zRange, float globalRot) {
        if (intensity < 0.01f) return;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float baseSize = (float) Math.max(w, h) * 0.5f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        for (int i = 0; i < N_HYPER_RINGS; i++) {
            // Calc Z courant avec wrapping
            float zRaw = RING_Z[i] - (float)((zSpeed * t) % zRange);
            while (zRaw < HYPER_Z_NEAR) zRaw += zRange;
            while (zRaw > HYPER_Z_FAR) zRaw -= zRange;
            if (zRaw < HYPER_Z_NEAR + 0.05f) continue;

            // Rayon visible = baseSize * focal / Z
            float persp = HYPER_FOCAL / zRaw;
            float radius = baseSize * persp / 4.0f;
            if (radius < 5f || radius > 4000f) continue;

            // Alpha fade aux extremites
            float zAlpha = 1.0f;
            if (zRaw < HYPER_Z_NEAR + 6.0f) {
                zAlpha = (zRaw - HYPER_Z_NEAR) / 6.0f;
            } else if (zRaw > HYPER_Z_FAR - 10.0f) {
                zAlpha = (HYPER_Z_FAR - zRaw) / 10.0f;
            }
            float a = intensity * zAlpha * 0.45f;
            if (a < 0.01f) continue;

            // Couleur cyclee dans la palette DMT
            int paletteIdx = (i + (int)((t / 30) % DMT_PALETTE.length)) % DMT_PALETTE.length;
            float[] color = DMT_PALETTE[paletteIdx];

            // Rotation propre + globale
            float ringRot = (RING_ROT[i] + globalRot * 1.5f) * (float) Math.PI / 180.0f;

            // Dessine anneau via TRIANGLE_STRIP (couronne d'anneau)
            int segments = 36;
            float thickness = radius * 0.08f;
            if (thickness < 2f) thickness = 2f;
            float rInner = radius - thickness / 2.0f;
            float rOuter = radius + thickness / 2.0f;

            buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int k = 0; k <= segments; k++) {
                float angle = (k / (float) segments) * 2.0f * (float) Math.PI + ringRot;
                float ca = (float) Math.cos(angle);
                float sa = (float) Math.sin(angle);
                buf.pos(cx + ca * rInner, cy + sa * rInner, 0)
                   .color(color[0], color[1], color[2], a).endVertex();
                buf.pos(cx + ca * rOuter, cy + sa * rOuter, 0)
                   .color(color[0], color[1], color[2], a * 0.5f).endVertex();
            }
            tess.draw();
        }

        // === RESTORE GL STATE pour eviter leak vers les methodes suivantes ===
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
    }

    /**
     * v1.0.351 : GHOST FIGURES = silhouettes humaines en perspective 3D dans
     * le tunnel hyperspace. Representent les "vies infinies" qui defilent
     * pendant que le joueur plonge dans le tunnel DMT.
     *
     * 12 silhouettes (1 par 'epoque humaine' avec couleurs distinctes) qui
     * avancent vers la camera. Quand l'une atteint Z_NEAR elle wrap a Z_FAR.
     * Defilement legerement plus rapide que les quads (zSpeed * 1.3) pour
     * effet 'vies qui passent vite'.
     *
     * Chaque silhouette = tete (cercle) + corps (trapeze) + bras (lignes).
     */
    private void renderGhostFigures(int w, int h, long t, float intensity,
                                     float zSpeed, float zRange) {
        if (intensity < 0.01f) return;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        float cx = w / 2.0f;
        float cy = h / 2.0f;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        for (int i = 0; i < N_GHOST_FIGURES; i++) {
            // Calc Z courant avec wrapping
            float zRaw = GHOST_Z[i] - (float)((zSpeed * t) % zRange);
            while (zRaw < HYPER_Z_NEAR) zRaw += zRange;
            while (zRaw > HYPER_Z_FAR) zRaw -= zRange;
            if (zRaw < HYPER_Z_NEAR + 0.05f) continue;

            // Projection
            float persp = HYPER_FOCAL / zRaw;
            float px = cx + GHOST_X[i] * w * 0.5f * persp / 4.0f;
            float py = cy + GHOST_Y[i] * h * 0.5f * persp / 4.0f;
            float scale = persp / 4.0f;
            if (scale > 4.0f) scale = 4.0f;
            if (scale < 0.05f) continue;

            // Alpha fade aux extremites Z
            float zAlpha = 1.0f;
            if (zRaw < HYPER_Z_NEAR + 6.0f) {
                zAlpha = (zRaw - HYPER_Z_NEAR) / 6.0f;
            } else if (zRaw > HYPER_Z_FAR - 10.0f) {
                zAlpha = (HYPER_Z_FAR - zRaw) / 10.0f;
            }
            float a = intensity * zAlpha * 0.65f;
            if (a < 0.01f) continue;

            float[] color = GHOST_ERA_COLORS[GHOST_ERA[i]];
            float r_ = color[0], g_ = color[1], b_ = color[2];

            // Tailles relatives a scale
            float headR = 18.0f * scale;
            float bodyW = 22.0f * scale;
            float bodyH = 50.0f * scale;
            float armLen = 28.0f * scale;
            float headY = py - bodyH * 0.6f;

            // === TETE : cercle plein ===
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(px, headY, 0).color(r_, g_, b_, a).endVertex();
            int hSeg = 16;
            for (int k = 0; k <= hSeg; k++) {
                float angle = (k / (float) hSeg) * 2.0f * (float) Math.PI;
                buf.pos(px + (float) Math.cos(angle) * headR,
                        headY + (float) Math.sin(angle) * headR, 0)
                   .color(r_, g_, b_, a * 0.7f).endVertex();
            }
            tess.draw();

            // === CORPS : trapeze ===
            float bodyTopY = headY + headR * 0.8f;
            float bodyBotY = bodyTopY + bodyH;
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(px - bodyW * 0.5f, bodyTopY, 0).color(r_, g_, b_, a).endVertex();
            buf.pos(px + bodyW * 0.5f, bodyTopY, 0).color(r_, g_, b_, a).endVertex();
            buf.pos(px + bodyW * 0.7f, bodyBotY, 0).color(r_, g_, b_, a * 0.5f).endVertex();
            buf.pos(px - bodyW * 0.7f, bodyBotY, 0).color(r_, g_, b_, a * 0.5f).endVertex();
            tess.draw();

            // === BRAS : 2 quads fins (gauche + droite) ===
            float armY = bodyTopY + bodyH * 0.20f;
            float armW = 5.0f * scale;
            // Bras gauche (descendant)
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(px - bodyW * 0.5f - armW, armY, 0).color(r_, g_, b_, a * 0.6f).endVertex();
            buf.pos(px - bodyW * 0.5f, armY, 0).color(r_, g_, b_, a * 0.6f).endVertex();
            buf.pos(px - bodyW * 0.5f - armW * 0.5f, armY + armLen, 0).color(r_, g_, b_, a * 0.4f).endVertex();
            buf.pos(px - bodyW * 0.5f - armW * 1.5f, armY + armLen, 0).color(r_, g_, b_, a * 0.4f).endVertex();
            tess.draw();
            // Bras droit
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(px + bodyW * 0.5f, armY, 0).color(r_, g_, b_, a * 0.6f).endVertex();
            buf.pos(px + bodyW * 0.5f + armW, armY, 0).color(r_, g_, b_, a * 0.6f).endVertex();
            buf.pos(px + bodyW * 0.5f + armW * 1.5f, armY + armLen, 0).color(r_, g_, b_, a * 0.4f).endVertex();
            buf.pos(px + bodyW * 0.5f + armW * 0.5f, armY + armLen, 0).color(r_, g_, b_, a * 0.4f).endVertex();
            tess.draw();
        }

        // === RESTORE GL STATE pour eviter leak vers les methodes suivantes ===
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.enableTexture2D();
    }

    /**
     * Pulse central : 1 anneau par BEAT (84 BPM).
     * v1.0.358 : alpha 0.4 -> 0.18 (-55% scintillement, user feedback)
     */
    private void renderCenterPulse(int w, int h, long t, float intensity, float beat) {
        // L'anneau commence quand beat=0 et grandit jusqu'a beat=1
        float phase = ManifoldClientState.getBeatPhase(t);
        float radius = phase * Math.max(w, h) * 0.7f;
        float pulseAlpha = (1.0f - phase) * 0.18f * intensity;
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
        float fade = frameFloat - frameA;
        if (frameA < 0) frameA = 0;
        if (frameA >= ENTITY_FRAMES.length) frameA = ENTITY_FRAMES.length - 1;

        // v1.0.346 : pulse scale beat-sync PLUS FORT (0.12 -> 0.20)
        float scale = 1.0f + beat * 0.20f;

        float rotation = (t % 1800) / 1800.0f * 360.0f;

        // v1.0.357 FIX 16/9 : etait Math.min(w, h) = 1080 sur 1920x1080
        // -> entite dessinee dans un CARRE 1080x1080 centre, on voyait les
        // cotes gauche/droit avec plasma/occluder qui depassaient.
        // FIX : utiliser la DIAGONALE * 1.15 pour couvrir TOUT l'ecran
        // meme apres rotation (diagonale 1920x1080 = 2200, * 1.15 = 2530).
        // Resultat : l'entite + son halo couvrent largeur+hauteur+rotation.
        float baseSize = (float) Math.sqrt(w * w + h * h) * 1.15f;
        float size = baseSize * scale;

        float baseAlpha = intensity * 0.95f;
        float alphaA = (1.0f - fade) * baseAlpha;
        float alphaB = fade * baseAlpha;

        if (alphaA > 0.01f) {
            drawRotatedTexture(mc, ENTITY_FRAMES[frameA], w / 2.0f, h / 2.0f,
                               size, rotation, alphaA);
        }
        if (alphaB > 0.01f) {
            drawRotatedTexture(mc, ENTITY_FRAMES[frameB], w / 2.0f, h / 2.0f,
                               size, rotation, alphaB);
        }

        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * v1.0.346 -- Couche occludante au PEAK qui recouvre 100% de l'ecran
     * avec un fond psychedelique anime. But : pendant le PEAK le joueur
     * ne voit plus le jeu, juste le trip a fond.
     *
     * Effet : grille de quads colores qui changent de teinte avec le temps
     * et qui pulsent au beat. Alpha varie de 0 (debut/fin du PEAK) a ~0.85
     * (milieu du PEAK).
     */
    private void renderPeakOccluder(int w, int h, long t, float intensity, float beat) {
        // Alpha module par intensity et un peu par le beat
        float alpha = intensity * 0.85f + beat * 0.10f;
        if (alpha < 0.05f) return;
        if (alpha > 0.95f) alpha = 0.95f;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Grille 16x9 de quads colores qui changent de teinte avec t
        int cols = 16;
        int rows = 9;
        float cellW = (float) w / cols;
        float cellH = (float) h / rows;

        // v1.0.357 FIX 16/9 : Pre-compute couleur + alpha aux NOEUDS de la grille
        // (cols+1 x rows+1) pour que chaque cellule ait gradient lisse aux 4 coins.
        // Plus de "carres rectangulaires" visibles -> gradient continu DMT.
        float[][] gridR = new float[cols + 1][rows + 1];
        float[][] gridG = new float[cols + 1][rows + 1];
        float[][] gridB = new float[cols + 1][rows + 1];
        float[][] gridA = new float[cols + 1][rows + 1];
        for (int gx = 0; gx <= cols; gx++) {
            for (int gy = 0; gy <= rows; gy++) {
                int colorIdx = (gx + gy + (int)(t / 8)) % DMT_PALETTE.length;
                float[] c = DMT_PALETTE[colorIdx];
                float cellPhase = ((gx * 7 + gy * 13) % 16) / 16.0f;
                float cellPulse = (float) Math.sin((t + cellPhase * 100) * 0.05);
                float cellAlpha = alpha * (0.7f + 0.3f * cellPulse);
                gridR[gx][gy] = c[0];
                gridG[gx][gy] = c[1];
                gridB[gx][gy] = c[2];
                gridA[gx][gy] = cellAlpha;
            }
        }

        for (int gy = 0; gy < rows; gy++) {
            for (int gx = 0; gx < cols; gx++) {
                float x0 = gx * cellW;
                float y0 = gy * cellH;
                float x1 = x0 + cellW;
                float y1 = y0 + cellH;
                // 4 coins avec couleurs interpolees -> OpenGL fait gradient
                buf.pos(x0, y1, 0).color(gridR[gx][gy + 1], gridG[gx][gy + 1],
                    gridB[gx][gy + 1], gridA[gx][gy + 1]).endVertex();
                buf.pos(x1, y1, 0).color(gridR[gx + 1][gy + 1], gridG[gx + 1][gy + 1],
                    gridB[gx + 1][gy + 1], gridA[gx + 1][gy + 1]).endVertex();
                buf.pos(x1, y0, 0).color(gridR[gx + 1][gy], gridG[gx + 1][gy],
                    gridB[gx + 1][gy], gridA[gx + 1][gy]).endVertex();
                buf.pos(x0, y0, 0).color(gridR[gx][gy], gridG[gx][gy],
                    gridB[gx][gy], gridA[gx][gy]).endVertex();
            }
        }
        tess.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
    }

    /**
     * v1.0.346 -- Mandala geant rotation rapide derriere l'entite au PEAK.
     * Taille = 1.4x l'ecran (deborde des bords) -> aucune zone vide visible.
     */
    private void renderPeakBigMandala(Minecraft mc, int w, int h, long t,
                                       float intensity, float beat) {
        // Selection mandala : un different par beat fort
        int mandalaIdx = (int)((t / 40) % MANDALA_TEX.length);
        float rotation = (t % 600) / 600.0f * 360.0f;  // 1 tour / 30s
        // Pulse zoom synchro beat (taille pulse 1.4x -> 1.55x sur le kick)
        float scale = 1.4f + beat * 0.15f;
        // v1.0.357 FIX 16/9 : Math.max(w,h) * 1.4 = 2688 sur 1920x1080,
        // mais avec rotation les coins peuvent etre vides. Diagonale * scale
        // garantit couverture totale.
        float size = (float) Math.sqrt(w * w + h * h) * scale;

        // Alpha : se voit derriere l'occluder mais reste vivant
        float alpha = intensity * 0.55f + beat * 0.15f;
        if (alpha > 0.85f) alpha = 0.85f;

        if (alpha > 0.02f) {
            drawRotatedTexture(mc, MANDALA_TEX[mandalaIdx],
                               w / 2.0f, h / 2.0f,
                               size, rotation, alpha);
        }
    }

    /**
     * Selection de la frame d'entite (en float pour crossfade) selon le
     * peakProgress.
     *
     * v1.0.343 : morphing raccourci 45s -> 30s pour densifier l'animation.
     * v1.0.350 : ajout phase NDE (60 frames one-shot) en fin de PEAK.
     *
     * Logique :
     *   - PEAK = [0.5, 0.6875] du trip (~90s)
     *   - 1ere tranche (0.5..0.5625, ~30s)    -> 200 frames de morphing one-shot
     *   - 2eme tranche (0.5625..0.625, ~30s)  -> 40 frames loop entity (cycle 6s)
     *   - 3eme tranche (0.625..0.6875, ~30s)  -> 60 frames NDE one-shot (240..299)
     *   - Hors PEAK -> N_ENTITY_MORPH_FRAMES (defaut entite statique)
     */
    private float computeEntityFrameFloat(long t) {
        float progress = ManifoldClientState.getTripProgress(t);
        final float PEAK_START = ManifoldEffectHandler.STAGE_4_HYPERSPACE_END; // 0.5
        final float PEAK_END = ManifoldEffectHandler.STAGE_5_PEAK_END;         // 0.6875
        // 30s morphing = 1/3 du PEAK (90s total)
        final float MORPH_END = PEAK_START + (PEAK_END - PEAK_START) / 3.0f;   // ~0.5625
        // 30s entity loop = 1/3 du PEAK
        final float LOOP_END = PEAK_START + (PEAK_END - PEAK_START) * 2.0f / 3.0f; // ~0.625

        if (progress < PEAK_START || progress >= PEAK_END) {
            return (float) N_ENTITY_MORPH_FRAMES;  // 1ere frame du loop entity = defaut
        }

        if (progress < MORPH_END) {
            // Phase morphing : map progress lineairement sur 0..N_ENTITY_MORPH_FRAMES
            float morphFrac = (progress - PEAK_START) / (MORPH_END - PEAK_START);
            return morphFrac * N_ENTITY_MORPH_FRAMES;
        }

        if (progress < LOOP_END) {
            // Phase loop entity : frames 200..239 BPM-sync, en float pour crossfade
            long ticksSinceMorphEnd = t - (long) (PEAK_START * ManifoldEffectHandler.TRIP_DURATION);
            float subTicks = (float) (ticksSinceMorphEnd % ENTITY_FRAME_DURATION) / ENTITY_FRAME_DURATION;
            int loopFrame = (int) ((t / ENTITY_FRAME_DURATION) % N_ENTITY_LOOP_FRAMES);
            return N_ENTITY_MORPH_FRAMES + loopFrame + subTicks;
        }

        // Phase NDE : map progress lineairement sur N_ENTITY_MORPH_FRAMES + N_ENTITY_LOOP_FRAMES
        // ..N_ENTITY_MORPH_FRAMES + N_ENTITY_LOOP_FRAMES + N_NDE_FRAMES (= 240..299)
        // One-shot : pas de loop, defile lineairement vers le blackout final.
        float ndeFrac = (progress - LOOP_END) / (PEAK_END - LOOP_END);
        return (float) (N_ENTITY_MORPH_FRAMES + N_ENTITY_LOOP_FRAMES) + ndeFrac * N_NDE_FRAMES;
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
