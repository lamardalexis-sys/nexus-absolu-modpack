package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.client.Minecraft;
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
 * Overlay client-side qui teinte l'ecran selon la phase Manifoldine en cours.
 *
 *   PHASE 1 (0-4 min)   : violet/cyan figé (couleurs Manifoldine)
 *                         Pulsation lente entre violet et cyan.
 *   PHASE 2 (4-5 min)   : NEGATIF total (le trip vrille)
 *                         Inverse les couleurs via blend mode GL_ONE_MINUS_DST_COLOR.
 *   PHASE 3 (fatigue)   : pas d'overlay (juste les potions)
 *
 * S'appuie sur PERSISTED_NBT_TAG du player (sync auto avec le serveur).
 */
@SideOnly(Side.CLIENT)
public class ManifoldOverlayHandler {

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        // Seulement apres le HUD (sinon on dessine sous les barres)
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) return;

        int phase = getPhase(player);
        if (phase == ManifoldEffectHandler.PHASE_NONE) return;
        if (phase == ManifoldEffectHandler.PHASE_FATIGUE) return;

        ScaledResolution res = new ScaledResolution(mc);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();

        long t = player.world.getTotalWorldTime();

        if (phase == ManifoldEffectHandler.PHASE_ACTIVE) {
            renderTrippyTint(w, h, t);
        } else if (phase == ManifoldEffectHandler.PHASE_NEGATIVE) {
            renderNegativeOverlay(w, h);
        }
    }

    /**
     * PHASE 1 : palette DMT 4-couleurs ("Chrysanthemum veil") qui pulse
     * et ondule en grille (effet entoptique documente dans la litterature DMT).
     *
     * Couleurs scientifiquement basees sur les rapports phenomenologiques DMT
     * (Scientific Reports, 2022 — analyse de 3778 experiences r/DMT) :
     *   Magenta vif #FF1493 (deep pink)
     *   Cyan pur   #00FFFF
     *   Lime       #7FFF00 (chartreuse)
     *   Or         #FFD700 (gold)
     * Ces 4 couleurs sont a 90 deg les unes des autres sur le cercle chromatique
     * → contraste perceptuel maximum.
     *
     * Implementation : on dessine une GRILLE de quads (8x8) ou chaque cellule
     * a sa propre couleur de la palette, qui change en fonction du temps et
     * de la position. Effet "mosaique animee" qui rend bien meme sans shader.
     */
    private void renderTrippyTint(int w, int h, long t) {
        // Palette DMT (4 couleurs RGB)
        float[][] DMT_PALETTE = {
            {1.00f, 0.08f, 0.58f},  // #FF1493 magenta
            {0.00f, 1.00f, 1.00f},  // #00FFFF cyan
            {0.50f, 1.00f, 0.00f},  // #7FFF00 lime
            {1.00f, 0.84f, 0.00f}   // #FFD700 gold
        };

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);

        // === GRILLE DE QUADS COLORES ===
        // 8 colonnes × 6 rangees, chaque cell prend sa couleur de la palette
        // selon (col + row + temps) % 4 → cycling 4-couleurs
        int cols = 8;
        int rows = 6;
        float cellW = w / (float) cols;
        float cellH = h / (float) rows;
        float baseAlpha = 0.14f;  // assez subtil pour pas occlure la vue

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Phase de cycle (couleurs qui scrollent dans la grille)
        double phaseShift = (t * 0.15) % 4.0;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                // Index couleur dans la palette : "wave distortion"
                // Ondulation diagonale + rotation temporelle = pattern qui bouge
                double waveX = Math.sin((col + t * 0.05) * 0.7);
                double waveY = Math.cos((row + t * 0.04) * 0.7);
                int colorIdx = (int)((col + row + phaseShift + waveX + waveY) % 4 + 4) % 4;
                float[] c = DMT_PALETTE[colorIdx];

                // Alpha module par sin pour faire pulser chaque cell independamment
                double pulse = (Math.sin(t * 0.1 + col * 0.5 + row * 0.3) + 1.0) * 0.5;
                float a = baseAlpha + (float)(pulse * 0.06);

                float x0 = col * cellW;
                float y0 = row * cellH;
                float x1 = x0 + cellW;
                float y1 = y0 + cellH;

                buf.pos(x0, y1, 0).color(c[0], c[1], c[2], a).endVertex();
                buf.pos(x1, y1, 0).color(c[0], c[1], c[2], a).endVertex();
                buf.pos(x1, y0, 0).color(c[0], c[1], c[2], a).endVertex();
                buf.pos(x0, y0, 0).color(c[0], c[1], c[2], a).endVertex();
            }
        }
        tess.draw();

        // === VIGNETTE : 4 couleurs alternees aux 4 coins ===
        // Effet "chrysanthemum veil" : les bords de l'ecran tirent vers les
        // 4 couleurs DMT en mode plus sature
        renderDMTVignette(w, h, DMT_PALETTE, t);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    /**
     * Vignette 4-couleurs aux coins de l'ecran (chrysanthemum veil).
     * Chaque coin a sa propre couleur, rotation temporelle de l'attribution.
     */
    private void renderDMTVignette(int w, int h, float[][] palette, long t) {
        int border = Math.min(w, h) / 5;
        float maxAlpha = 0.42f;
        int rotOffset = (int)((t / 30) % 4);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Top-left, Top-right, Bot-right, Bot-left → 4 coins, 4 couleurs
        float[] tl = palette[(0 + rotOffset) % 4];
        float[] tr = palette[(1 + rotOffset) % 4];
        float[] br = palette[(2 + rotOffset) % 4];
        float[] bl = palette[(3 + rotOffset) % 4];

        // Top band : gradient TL → TR
        buf.pos(0, 0, 0).color(tl[0], tl[1], tl[2], maxAlpha).endVertex();
        buf.pos(w, 0, 0).color(tr[0], tr[1], tr[2], maxAlpha).endVertex();
        buf.pos(w, border, 0).color(tr[0], tr[1], tr[2], 0f).endVertex();
        buf.pos(0, border, 0).color(tl[0], tl[1], tl[2], 0f).endVertex();
        // Bottom band : gradient BL → BR
        buf.pos(0, h - border, 0).color(bl[0], bl[1], bl[2], 0f).endVertex();
        buf.pos(w, h - border, 0).color(br[0], br[1], br[2], 0f).endVertex();
        buf.pos(w, h, 0).color(br[0], br[1], br[2], maxAlpha).endVertex();
        buf.pos(0, h, 0).color(bl[0], bl[1], bl[2], maxAlpha).endVertex();
        // Left band
        buf.pos(0, 0, 0).color(tl[0], tl[1], tl[2], maxAlpha).endVertex();
        buf.pos(border, 0, 0).color(tl[0], tl[1], tl[2], 0f).endVertex();
        buf.pos(border, h, 0).color(bl[0], bl[1], bl[2], 0f).endVertex();
        buf.pos(0, h, 0).color(bl[0], bl[1], bl[2], maxAlpha).endVertex();
        // Right band
        buf.pos(w - border, 0, 0).color(tr[0], tr[1], tr[2], 0f).endVertex();
        buf.pos(w, 0, 0).color(tr[0], tr[1], tr[2], maxAlpha).endVertex();
        buf.pos(w, h, 0).color(br[0], br[1], br[2], maxAlpha).endVertex();
        buf.pos(w - border, h, 0).color(br[0], br[1], br[2], 0f).endVertex();

        tess.draw();
    }

    /**
     * PHASE 2 : negatif total via blend ONE_MINUS_DST_COLOR.
     * Effet : tout ce qui etait clair devient sombre et vice-versa.
     * Couleur des yeux qui basculent dans le mauvais trip.
     */
    private void renderNegativeOverlay(int w, int h) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        // Blend mode "negatif" : DST = (1-DST) * SRC
        // Avec SRC = blanc, on a : DST_new = 1 - DST_old (inversion parfaite)
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

        // Restore
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private static int getPhase(EntityPlayer player) {
        long now = player.world.getTotalWorldTime();
        return ManifoldClientState.getCurrentPhase(now);
    }
}
