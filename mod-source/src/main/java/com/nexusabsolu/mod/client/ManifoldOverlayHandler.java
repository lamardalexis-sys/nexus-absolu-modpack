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
     * PHASE 1 : teinte violet/cyan qui pulse lentement.
     * Alpha modere (~0.18) pour pas occlure l'image, juste la coloriser.
     */
    private void renderTrippyTint(int w, int h, long t) {
        // Pulsation 0..1 a periode 80 ticks (4 secondes)
        double pulse = (Math.sin(t * 2.0 * Math.PI / 80.0) + 1.0) * 0.5;

        // Couleurs Manifoldine : violet (0x9C27B0) et cyan (0x00E5FF)
        // pulse=0 -> violet, pulse=1 -> cyan
        float r = (float) lerp(0x9C / 255.0, 0x00 / 255.0, pulse);
        float g = (float) lerp(0x27 / 255.0, 0xE5 / 255.0, pulse);
        float b = (float) lerp(0xB0 / 255.0, 0xFF / 255.0, pulse);
        float a = 0.18f;

        // === Render quad plein ecran ===
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO);
        GlStateManager.color(r, g, b, a);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(0, h, 0).endVertex();
        buf.pos(w, h, 0).endVertex();
        buf.pos(w, 0, 0).endVertex();
        buf.pos(0, 0, 0).endVertex();
        tess.draw();

        // Vignette aux bords (plus sature en peripherie)
        renderVignette(w, h, r, g, b, 0.35f);

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
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

    /**
     * Vignette : rectangle plein avec alpha forte aux bords, faible au centre.
     * Implementation low-tech : 4 trapezes (haut/bas/gauche/droite).
     */
    private void renderVignette(int w, int h, float r, float g, float b, float maxAlpha) {
        int border = Math.min(w, h) / 6;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        // Top
        buf.pos(0, 0, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(w, 0, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(w, border, 0).color(r, g, b, 0f).endVertex();
        buf.pos(0, border, 0).color(r, g, b, 0f).endVertex();
        // Bottom
        buf.pos(0, h - border, 0).color(r, g, b, 0f).endVertex();
        buf.pos(w, h - border, 0).color(r, g, b, 0f).endVertex();
        buf.pos(w, h, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(0, h, 0).color(r, g, b, maxAlpha).endVertex();
        // Left
        buf.pos(0, 0, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(border, 0, 0).color(r, g, b, 0f).endVertex();
        buf.pos(border, h, 0).color(r, g, b, 0f).endVertex();
        buf.pos(0, h, 0).color(r, g, b, maxAlpha).endVertex();
        // Right
        buf.pos(w - border, 0, 0).color(r, g, b, 0f).endVertex();
        buf.pos(w, 0, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(w, h, 0).color(r, g, b, maxAlpha).endVertex();
        buf.pos(w - border, h, 0).color(r, g, b, 0f).endVertex();

        tess.draw();
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static int getPhase(EntityPlayer player) {
        long now = player.world.getTotalWorldTime();
        return ManifoldClientState.getCurrentPhase(now);
    }
}
