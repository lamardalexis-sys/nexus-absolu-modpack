package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TESRCondenseurT2 extends TileEntitySpecialRenderer<TileCondenseurT2> {

    // Shell textures (48x48 connected textures for each face)
    private static final ResourceLocation SHELL_FRONT  = new ResourceLocation("nexusabsolu", "textures/blocks/shell_front.png");
    private static final ResourceLocation SHELL_SIDE   = new ResourceLocation("nexusabsolu", "textures/blocks/shell_side.png");
    private static final ResourceLocation SHELL_SIDE_IN  = new ResourceLocation("nexusabsolu", "textures/blocks/shell_side_input.png");
    private static final ResourceLocation SHELL_SIDE_OUT = new ResourceLocation("nexusabsolu", "textures/blocks/shell_side_output.png");
    private static final ResourceLocation SHELL_TOP    = new ResourceLocation("nexusabsolu", "textures/blocks/shell_top.png");
    private static final ResourceLocation SHELL_BOTTOM = new ResourceLocation("nexusabsolu", "textures/blocks/shell_bottom.png");
    private static final ResourceLocation SHELL_BACK   = new ResourceLocation("nexusabsolu", "textures/blocks/shell_back.png");

    private static final float TWO_PI = (float)(Math.PI * 2.0);
    private static final float HALF_PI = (float)(Math.PI * 0.5);

    // -- Chamber geometry --
    private static final float FLUID_INSET   = 0.25F;
    private static final float FLUID_FLOOR   = 0.55F;
    private static final float FLUID_HEIGHT  = 1.6F;

    // -- Items --
    private static final float ITEM_BASE_Y   = 1.4F;
    private static final float ITEM_ORBIT_R  = 0.55F;
    private static final float ITEM_SCALE    = 0.32F;
    private static final float HELIX_AMPLITUDE = 0.25F;

    // -- Energy column --
    private static final float COL_RADIUS    = 0.06F;
    private static final int   COL_SIDES     = 8;
    private static final float COL_FLOOR     = 0.3F;
    private static final float COL_TOP       = 2.2F;

    // -- Vortex --
    private static final int   VORTEX_RINGS    = 4;
    private static final int   VORTEX_SEGMENTS = 20;
    private static final float VORTEX_WIDTH    = 0.02F;

    // -- Orbital rings --
    private static final int   ORBITAL_COUNT    = 3;
    private static final int   ORBITAL_SEGMENTS = 24;
    private static final float ORBITAL_WIDTH    = 0.015F;

    // -- Sphere --
    private static final int   SPHERE_RINGS   = 6;
    private static final int   SPHERE_SEGS    = 16;

    // -- Thresholds --
    private static final float SPIRAL_THRESHOLD = 0.6F;
    private static final float VORTEX_START     = 0.2F;
    private static final float FLASH_START      = 0.88F;

    @Override
    public boolean isGlobalRenderer(TileCondenseurT2 te) {
        return true;
    }

    @Override
    public void render(TileCondenseurT2 te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isStructureValid()) return;

        float time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20.0F;
        float pct = te.getProcessPercent() / 100.0F;
        boolean proc = te.isProcessing();

        // Structure center (Vossium2 block) in render coords
        BlockPos center = te.getStructureCenter();
        double cx = x + (center.getX() - te.getPos().getX()) + 0.5;
        double cy = y + (center.getY() - te.getPos().getY());
        double cz = z + (center.getZ() - te.getPos().getZ()) + 0.5;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        renderShell(te, x, y, z);
        renderScreen(te, x, y, z, time, pct, proc);

        renderEnergyColumn(cx, cy, cz, time, pct, proc);
        renderFluid(cx, cy, cz, time, pct, proc);
        renderItems(te, cx, cy, cz, time, pct, proc);
        if (proc && pct > VORTEX_START) {
            renderDoubleVortex(cx, cy, cz, time, pct);
        }
        if (proc && pct > 0.15F) {
            renderOrbitalRings(cx, cy, cz, time, pct);
        }
        if (proc) {
            renderEnergySphere(cx, cy, cz, time, pct);
        }
        if (proc && pct > FLASH_START) {
            renderShockwave(cx, cy, cz, time, pct);
        }
    }

    // ======================================================================
    //  HELPER
    // ======================================================================

    private void setFullBrightness() {
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    private void beginTranslucent() {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        setFullBrightness();
    }

    private void endTranslucent() {
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    // ======================================================================
    //  SHELL: Connected machine casing rendered over the multiblock
    // ======================================================================

    private void renderShell(TileCondenseurT2 te, double x, double y, double z) {
        int[] bounds = te.getStructureBounds();
        int[] frontDir = te.getFrontDirection();
        BlockPos masterPos = te.getPos();

        // Bounding box in render space
        double x0 = x + (bounds[0] - masterPos.getX());
        double y0 = y + (bounds[1] - masterPos.getY());
        double z0 = z + (bounds[2] - masterPos.getZ());
        double x1 = x + (bounds[3] - masterPos.getX());
        double y1 = y + (bounds[4] - masterPos.getY());
        double z1 = z + (bounds[5] - masterPos.getZ());

        // Tiny offset to render OVER block faces (avoid z-fighting)
        double o = 0.001;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        setFullBrightness();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Determine which face is front, back, left, right based on frontDir
        // frontDir: {dx, dz} — direction the front face points toward

        // Top face (always UP)
        bindTexture(SHELL_TOP);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x0, y1 + o, z0).tex(0, 0).endVertex();
        buf.pos(x0, y1 + o, z1).tex(0, 1).endVertex();
        buf.pos(x1, y1 + o, z1).tex(1, 1).endVertex();
        buf.pos(x1, y1 + o, z0).tex(1, 0).endVertex();
        tess.draw();

        // Bottom face
        bindTexture(SHELL_BOTTOM);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x0, y0 - o, z1).tex(0, 0).endVertex();
        buf.pos(x0, y0 - o, z0).tex(0, 1).endVertex();
        buf.pos(x1, y0 - o, z0).tex(1, 1).endVertex();
        buf.pos(x1, y0 - o, z1).tex(1, 0).endVertex();
        tess.draw();

        // Front face (direction depends on rotation)
        if (frontDir[0] == 1) {
            // R0: Front faces +X, width=-1 is -Z (input), width=+1 is +Z (output)
            renderFace(buf, tess, SHELL_FRONT, x1+o, y0, z0, x1+o, y1, z1, 0);
            renderFace(buf, tess, SHELL_BACK, x0-o, y0, z0, x0-o, y1, z1, 1);
            renderFace(buf, tess, SHELL_SIDE_IN, x0, y0, z0-o, x1, y1, z0-o, 2);
            renderFace(buf, tess, SHELL_SIDE_OUT, x0, y0, z1+o, x1, y1, z1+o, 3);
        } else if (frontDir[0] == -1) {
            // R1: Front faces -X, width=-1 is +Z (input), width=+1 is -Z (output)
            renderFace(buf, tess, SHELL_FRONT, x0-o, y0, z0, x0-o, y1, z1, 1);
            renderFace(buf, tess, SHELL_BACK, x1+o, y0, z0, x1+o, y1, z1, 0);
            renderFace(buf, tess, SHELL_SIDE_OUT, x0, y0, z0-o, x1, y1, z0-o, 3);
            renderFace(buf, tess, SHELL_SIDE_IN, x0, y0, z1+o, x1, y1, z1+o, 2);
        } else if (frontDir[1] == 1) {
            // R2: Front faces +Z, width=-1 is +X (input), width=+1 is -X (output)
            renderFace(buf, tess, SHELL_FRONT, x0, y0, z1+o, x1, y1, z1+o, 3);
            renderFace(buf, tess, SHELL_BACK, x0, y0, z0-o, x1, y1, z0-o, 2);
            renderFace(buf, tess, SHELL_SIDE_OUT, x0-o, y0, z0, x0-o, y1, z1, 1);
            renderFace(buf, tess, SHELL_SIDE_IN, x1+o, y0, z0, x1+o, y1, z1, 0);
        } else {
            // R3: Front faces -Z, width=-1 is -X (input), width=+1 is +X (output)
            renderFace(buf, tess, SHELL_FRONT, x0, y0, z0-o, x1, y1, z0-o, 2);
            renderFace(buf, tess, SHELL_BACK, x0, y0, z1+o, x1, y1, z1+o, 3);
            renderFace(buf, tess, SHELL_SIDE_IN, x0-o, y0, z0, x0-o, y1, z1, 0);
            renderFace(buf, tess, SHELL_SIDE_OUT, x1+o, y0, z0, x1+o, y1, z1, 1);
        }

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  SCREEN: Dynamic display on front face
    // ======================================================================

    private void renderScreen(TileCondenseurT2 te, double x, double y, double z,
                              float time, float pct, boolean proc) {
        int[] bounds = te.getStructureBounds();
        int[] frontDir = te.getFrontDirection();
        BlockPos mp = te.getPos();

        double bx0 = x + (bounds[0] - mp.getX());
        double by0 = y + (bounds[1] - mp.getY());
        double bz0 = z + (bounds[2] - mp.getZ());
        double bx1 = x + (bounds[3] - mp.getX());
        double by1 = y + (bounds[4] - mp.getY());
        double bz1 = z + (bounds[5] - mp.getZ());

        double sBot = by1 - 0.75;
        double sTop = by1 - 0.15;
        double off = 0.005;

        GlStateManager.pushMatrix();
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float sr = proc ? 0.05F : 0.02F;
        float sg = proc ? 0.35F : 0.02F;
        float sb = proc ? 0.08F : 0.08F;
        float sa = proc ? 0.9F : 0.8F;

        // Draw screen + progress bar on the front face
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (frontDir[0] == 1) {
            double fx = bx1 + off;
            buf.pos(fx, sBot, bz0+0.9).color(sr,sg,sb,sa).endVertex();
            buf.pos(fx, sBot, bz1-0.9).color(sr,sg,sb,sa).endVertex();
            buf.pos(fx, sTop, bz1-0.9).color(sr,sg,sb,sa*0.7F).endVertex();
            buf.pos(fx, sTop, bz0+0.9).color(sr,sg,sb,sa*0.7F).endVertex();
        } else if (frontDir[0] == -1) {
            double fx = bx0 - off;
            buf.pos(fx, sBot, bz1-0.9).color(sr,sg,sb,sa).endVertex();
            buf.pos(fx, sBot, bz0+0.9).color(sr,sg,sb,sa).endVertex();
            buf.pos(fx, sTop, bz0+0.9).color(sr,sg,sb,sa*0.7F).endVertex();
            buf.pos(fx, sTop, bz1-0.9).color(sr,sg,sb,sa*0.7F).endVertex();
        } else if (frontDir[1] == 1) {
            double fz = bz1 + off;
            buf.pos(bx0+0.9, sBot, fz).color(sr,sg,sb,sa).endVertex();
            buf.pos(bx1-0.9, sBot, fz).color(sr,sg,sb,sa).endVertex();
            buf.pos(bx1-0.9, sTop, fz).color(sr,sg,sb,sa*0.7F).endVertex();
            buf.pos(bx0+0.9, sTop, fz).color(sr,sg,sb,sa*0.7F).endVertex();
        } else {
            double fz = bz0 - off;
            buf.pos(bx1-0.9, sBot, fz).color(sr,sg,sb,sa).endVertex();
            buf.pos(bx0+0.9, sBot, fz).color(sr,sg,sb,sa).endVertex();
            buf.pos(bx0+0.9, sTop, fz).color(sr,sg,sb,sa*0.7F).endVertex();
            buf.pos(bx1-0.9, sTop, fz).color(sr,sg,sb,sa*0.7F).endVertex();
        }
        tess.draw();

        // Progress bar (green fill from bottom)
        if (proc && pct > 0.01F) {
            float fillH = pct * (float)(sTop - sBot - 0.1);
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            float pr = 0.1F + pct * 0.5F, pg = 0.7F, pb = 0.15F, pa = 0.85F;
            double fy = sBot + 0.05 + fillH;
            if (frontDir[0] == 1) {
                double fx = bx1 + off + 0.001;
                buf.pos(fx, sBot+0.05, bz0+1.0).color(pr,pg,pb,pa).endVertex();
                buf.pos(fx, sBot+0.05, bz1-1.0).color(pr,pg,pb,pa).endVertex();
                buf.pos(fx, fy, bz1-1.0).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
                buf.pos(fx, fy, bz0+1.0).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
            } else if (frontDir[0] == -1) {
                double fx = bx0 - off - 0.001;
                buf.pos(fx, sBot+0.05, bz1-1.0).color(pr,pg,pb,pa).endVertex();
                buf.pos(fx, sBot+0.05, bz0+1.0).color(pr,pg,pb,pa).endVertex();
                buf.pos(fx, fy, bz0+1.0).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
                buf.pos(fx, fy, bz1-1.0).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
            } else if (frontDir[1] == 1) {
                double fz = bz1 + off + 0.001;
                buf.pos(bx0+1.0, sBot+0.05, fz).color(pr,pg,pb,pa).endVertex();
                buf.pos(bx1-1.0, sBot+0.05, fz).color(pr,pg,pb,pa).endVertex();
                buf.pos(bx1-1.0, fy, fz).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
                buf.pos(bx0+1.0, fy, fz).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
            } else {
                double fz = bz0 - off - 0.001;
                buf.pos(bx1-1.0, sBot+0.05, fz).color(pr,pg,pb,pa).endVertex();
                buf.pos(bx0+1.0, sBot+0.05, fz).color(pr,pg,pb,pa).endVertex();
                buf.pos(bx0+1.0, fy, fz).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
                buf.pos(bx1-1.0, fy, fz).color(pr*0.6F,pg*0.6F,pb,pa*0.5F).endVertex();
            }
            tess.draw();
        }

        endTranslucent();
        GlStateManager.popMatrix();
    }

    private void renderFace(BufferBuilder buf, Tessellator tess,
                            ResourceLocation tex, double fx, double y0, double fz,
                            double fx2, double y1, double fz2, int orient) {
        bindTexture(tex);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        switch (orient) {
            case 0: // +X face (plane at x=fx)
                buf.pos(fx, y0, fz ).tex(1, 1).endVertex();
                buf.pos(fx, y0, fz2).tex(0, 1).endVertex();
                buf.pos(fx, y1, fz2).tex(0, 0).endVertex();
                buf.pos(fx, y1, fz ).tex(1, 0).endVertex();
                break;
            case 1: // -X face
                buf.pos(fx, y0, fz2).tex(1, 1).endVertex();
                buf.pos(fx, y0, fz ).tex(0, 1).endVertex();
                buf.pos(fx, y1, fz ).tex(0, 0).endVertex();
                buf.pos(fx, y1, fz2).tex(1, 0).endVertex();
                break;
            case 2: // -Z face (north)
                buf.pos(fx2, y0, fz).tex(1, 1).endVertex();
                buf.pos(fx,  y0, fz).tex(0, 1).endVertex();
                buf.pos(fx,  y1, fz).tex(0, 0).endVertex();
                buf.pos(fx2, y1, fz).tex(1, 0).endVertex();
                break;
            case 3: // +Z face (south)
                buf.pos(fx,  y0, fz).tex(1, 1).endVertex();
                buf.pos(fx2, y0, fz).tex(0, 1).endVertex();
                buf.pos(fx2, y1, fz).tex(0, 0).endVertex();
                buf.pos(fx,  y1, fz).tex(1, 0).endVertex();
                break;
        }
        tess.draw();
    }

    // ======================================================================
    //  EFFECT 1: ENERGY COLUMN (vertical beam of light at center)
    // ======================================================================

    private void renderEnergyColumn(double cx, double cy, double cz,
                                    float time, float pct, boolean proc) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, cz);
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float baseAlpha = proc ? 0.3F + pct * 0.4F : 0.08F;
        float pulse = 1.0F + 0.15F * (float) Math.sin(time * 3.0);
        float radius = COL_RADIUS * pulse;
        float coreR = proc ? 0.5F + pct * 0.4F : 0.15F;
        float coreG = proc ? 0.1F + pct * 0.2F : 0.05F;
        float coreB = proc ? 0.8F - pct * 0.1F : 0.25F;

        // Core column (solid center)
        buf.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= COL_SIDES; i++) {
            float ang = (i / (float) COL_SIDES) * TWO_PI;
            float px = radius * (float) Math.cos(ang);
            float pz = radius * (float) Math.sin(ang);
            buf.pos(px, COL_FLOOR, pz).color(coreR, coreG, coreB, baseAlpha * 0.3F).endVertex();
            buf.pos(px, COL_TOP, pz).color(coreR, coreG, coreB, baseAlpha).endVertex();
        }
        tess.draw();

        // Outer glow (larger, dimmer)
        float glowR = radius * 3.5F;
        float glowAlpha = baseAlpha * 0.25F;
        buf.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= COL_SIDES; i++) {
            float ang = (i / (float) COL_SIDES) * TWO_PI;
            float px = glowR * (float) Math.cos(ang);
            float pz = glowR * (float) Math.sin(ang);
            buf.pos(px, COL_FLOOR, pz).color(coreR, coreG, coreB, 0.0F).endVertex();
            buf.pos(px, COL_TOP, pz).color(coreR, coreG, coreB, glowAlpha).endVertex();
        }
        tess.draw();

        // Rising particle streaks along the column
        if (proc) {
            int streaks = 4;
            for (int s = 0; s < streaks; s++) {
                float streakAng = (s / (float) streaks) * TWO_PI + time * 2.5F;
                float sr = radius * 1.8F;
                float sx = sr * (float) Math.cos(streakAng);
                float sz = sr * (float) Math.sin(streakAng);
                float streakY = COL_FLOOR + ((time * 1.5F + s * 0.4F) % 1.0F) * (COL_TOP - COL_FLOOR);
                float streakH = 0.15F;
                float sa = baseAlpha * 0.6F;

                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(sx - 0.02, streakY, sz).color(1.0F, 0.8F, 1.0F, sa).endVertex();
                buf.pos(sx + 0.02, streakY, sz).color(1.0F, 0.8F, 1.0F, sa).endVertex();
                buf.pos(sx + 0.02, streakY + streakH, sz).color(1.0F, 0.8F, 1.0F, 0.0F).endVertex();
                buf.pos(sx - 0.02, streakY + streakH, sz).color(1.0F, 0.8F, 1.0F, 0.0F).endVertex();
                tess.draw();
            }
        }

        endTranslucent();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  EFFECT 2: FLUID (violet liquid rising with waves)
    // ======================================================================

    private void renderFluid(double cx, double cy, double cz,
                             float time, float pct, boolean proc) {
        if (!proc) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, cz);
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float halfW = 1.0F - FLUID_INSET;
        float topY = FLUID_FLOOR + pct * FLUID_HEIGHT;

        float w1 = 0.04F * (float) Math.sin(time * 2.5);
        float w2 = 0.04F * (float) Math.sin(time * 3.1 + 1.5);
        float w3 = 0.03F * (float) Math.sin(time * 4.0 + 3.0);
        float w4 = 0.03F * (float) Math.sin(time * 3.5 + 2.2);

        // Color: deep violet -> bright magenta
        float r = 0.45F + pct * 0.45F;
        float g = 0.03F + pct * 0.1F;
        float b = 0.78F - pct * 0.08F;
        float a = 0.55F + 0.1F * (float) Math.sin(time * 2.0);
        r += pct * 0.12F; // extra glow
        float sa = a * 0.6F;

        // Top surface
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-halfW, topY + w1, -halfW).color(r, g, b, a).endVertex();
        buf.pos( halfW, topY + w2, -halfW).color(r, g, b, a).endVertex();
        buf.pos( halfW, topY + w3,  halfW).color(r, g, b, a).endVertex();
        buf.pos(-halfW, topY + w4,  halfW).color(r, g, b, a).endVertex();
        tess.draw();

        // 4 side walls
        float dr = r * 0.55F, dg = g * 0.55F, db = b * 0.55F;
        // -Z
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-halfW, FLUID_FLOOR, -halfW).color(dr, dg, db, sa).endVertex();
        buf.pos( halfW, FLUID_FLOOR, -halfW).color(dr, dg, db, sa).endVertex();
        buf.pos( halfW, topY + w2, -halfW).color(r, g, b, sa).endVertex();
        buf.pos(-halfW, topY + w1, -halfW).color(r, g, b, sa).endVertex();
        tess.draw();
        // +Z
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos( halfW, FLUID_FLOOR,  halfW).color(dr, dg, db, sa).endVertex();
        buf.pos(-halfW, FLUID_FLOOR,  halfW).color(dr, dg, db, sa).endVertex();
        buf.pos(-halfW, topY + w4,  halfW).color(r, g, b, sa).endVertex();
        buf.pos( halfW, topY + w3,  halfW).color(r, g, b, sa).endVertex();
        tess.draw();
        // -X
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-halfW, FLUID_FLOOR,  halfW).color(dr, dg, db, sa).endVertex();
        buf.pos(-halfW, FLUID_FLOOR, -halfW).color(dr, dg, db, sa).endVertex();
        buf.pos(-halfW, topY + w1, -halfW).color(r, g, b, sa).endVertex();
        buf.pos(-halfW, topY + w4,  halfW).color(r, g, b, sa).endVertex();
        tess.draw();
        // +X
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos( halfW, FLUID_FLOOR, -halfW).color(dr, dg, db, sa).endVertex();
        buf.pos( halfW, FLUID_FLOOR,  halfW).color(dr, dg, db, sa).endVertex();
        buf.pos( halfW, topY + w3,  halfW).color(r, g, b, sa).endVertex();
        buf.pos( halfW, topY + w2, -halfW).color(r, g, b, sa).endVertex();
        tess.draw();
        // Bottom
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-halfW, FLUID_FLOOR, -halfW).color(dr * 0.7F, dg * 0.7F, db * 0.7F, sa).endVertex();
        buf.pos(-halfW, FLUID_FLOOR,  halfW).color(dr * 0.7F, dg * 0.7F, db * 0.7F, sa).endVertex();
        buf.pos( halfW, FLUID_FLOOR,  halfW).color(dr * 0.7F, dg * 0.7F, db * 0.7F, sa).endVertex();
        buf.pos( halfW, FLUID_FLOOR, -halfW).color(dr * 0.7F, dg * 0.7F, db * 0.7F, sa).endVertex();
        tess.draw();

        endTranslucent();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  EFFECT 3: ITEMS (helix orbit + bathtub sink + output glow)
    // ======================================================================

    private void renderItems(TileCondenseurT2 te, double cx, double cy, double cz,
                             float time, float pct, boolean proc) {
        setFullBrightness();

        float[][] basePos = {
            { ITEM_ORBIT_R,  0,  0},
            {-ITEM_ORBIT_R,  0,  0},
            { 0,  0,  ITEM_ORBIT_R},
            { 0,  0, -ITEM_ORBIT_R},
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getInputStack(i);
            if (stack.isEmpty()) continue;

            float ox = basePos[i][0];
            float oy = ITEM_BASE_Y;
            float oz = basePos[i][2];

            if (proc) {
                float spiralPhase = Math.min(pct / SPIRAL_THRESHOLD, 1.0F);
                float sinkPhase = Math.max((pct - SPIRAL_THRESHOLD) / (1.0F - SPIRAL_THRESHOLD), 0.0F);

                // Helix: items spiral inward + oscillate vertically
                float radius = ITEM_ORBIT_R * (1.0F - spiralPhase * 0.9F);
                float baseAng = (float) Math.atan2(basePos[i][2], basePos[i][0]);
                float speed = 3.0F + spiralPhase * 10.0F;
                float ang = baseAng + time * speed * (0.3F + spiralPhase);

                ox = radius * (float) Math.cos(ang);
                oz = radius * (float) Math.sin(ang);
                // Helix vertical oscillation
                oy = ITEM_BASE_Y + HELIX_AMPLITUDE * (float) Math.sin(ang * 2.0F) * (1.0F - spiralPhase);
                // Sink
                oy -= sinkPhase * 1.5F;
            }

            float bob = proc ? 0 : (float) Math.sin(time * 1.2 + i * 1.8) * 0.05F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(cx + ox, cy + oy + bob, cz + oz);

            float spinSpeed = proc ? 90.0F + pct * 250.0F : 30.0F;
            GlStateManager.rotate((time * spinSpeed + i * 90.0F) % 360.0F, 0, 1, 0);

            float scale = ITEM_SCALE * (proc ? (1.0F - pct * 0.65F) : 1.0F);
            GlStateManager.scale(scale, scale, scale);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        // Output item with glow disc
        ItemStack output = te.getOutputStack();
        if (!output.isEmpty()) {
            float outBob = (float) Math.sin(time * 1.0) * 0.06F;
            float outY = ITEM_BASE_Y + 0.3F + outBob;

            // Glow disc
            GlStateManager.pushMatrix();
            GlStateManager.translate(cx, cy, cz);
            beginTranslucent();
            float gs = 0.3F + 0.08F * (float) Math.sin(time * 2.0);
            float ga = 0.4F + 0.2F * (float) Math.sin(time * 1.5);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(-gs, outY - 0.03, -gs).color(0.6F, 0.15F, 0.9F, ga).endVertex();
            buf.pos( gs, outY - 0.03, -gs).color(0.6F, 0.15F, 0.9F, ga).endVertex();
            buf.pos( gs, outY - 0.03,  gs).color(0.6F, 0.15F, 0.9F, ga).endVertex();
            buf.pos(-gs, outY - 0.03,  gs).color(0.6F, 0.15F, 0.9F, ga).endVertex();
            tess.draw();
            endTranslucent();
            GlStateManager.popMatrix();

            // Item
            GlStateManager.pushMatrix();
            GlStateManager.translate(cx, cy + outY, cz);
            GlStateManager.rotate((time * 25.0F) % 360.0F, 0, 1, 0);
            GlStateManager.rotate(10.0F, 1, 0, 0);
            GlStateManager.scale(0.55F, 0.55F, 0.55F);
            setFullBrightness();
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                output, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    // ======================================================================
    //  EFFECT 4: DOUBLE VORTEX (ascending + descending ring pairs)
    // ======================================================================

    private void renderDoubleVortex(double cx, double cy, double cz,
                                    float time, float pct) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy, cz);
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float intensity = (pct - VORTEX_START) / (1.0F - VORTEX_START);
        float topY = FLUID_FLOOR + pct * FLUID_HEIGHT;

        float r = 0.6F + pct * 0.3F;
        float g = 0.08F + pct * 0.15F;
        float b = 0.85F - pct * 0.05F;

        // Ascending vortex (from fluid surface upward)
        for (int ring = 0; ring < VORTEX_RINGS; ring++) {
            float outerR = (0.12F + ring * 0.18F) * (1.0F - intensity * 0.4F);
            float innerR = Math.max(outerR - VORTEX_WIDTH, 0.01F);
            float ringAlpha = 0.25F + intensity * 0.35F;
            float ringY = topY + 0.02F + ring * 0.015F;
            float speed = time * (2.5F + ring * 0.8F) + ring * 1.2F;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int s = 0; s < VORTEX_SEGMENTS; s++) {
                float t1 = s / (float) VORTEX_SEGMENTS;
                float t2 = (s + 1) / (float) VORTEX_SEGMENTS;
                float a1 = t1 * TWO_PI + speed;
                float a2 = t2 * TWO_PI + speed;
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);
                buf.pos(outerR * c1, ringY, outerR * s1).color(r, g, b, ringAlpha).endVertex();
                buf.pos(outerR * c2, ringY, outerR * s2).color(r, g, b, ringAlpha).endVertex();
                buf.pos(innerR * c2, ringY, innerR * s2).color(r, g, b, ringAlpha * 0.2F).endVertex();
                buf.pos(innerR * c1, ringY, innerR * s1).color(r, g, b, ringAlpha * 0.2F).endVertex();
            }
            tess.draw();
        }

        // Descending vortex (mirror, spinning opposite direction)
        for (int ring = 0; ring < VORTEX_RINGS; ring++) {
            float outerR = (0.12F + ring * 0.18F) * (1.0F - intensity * 0.4F);
            float innerR = Math.max(outerR - VORTEX_WIDTH, 0.01F);
            float ringAlpha = 0.18F + intensity * 0.25F;
            float ringY = topY - 0.02F - ring * 0.015F;
            float speed = -time * (2.5F + ring * 0.8F) + ring * 2.0F;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int s = 0; s < VORTEX_SEGMENTS; s++) {
                float t1 = s / (float) VORTEX_SEGMENTS;
                float t2 = (s + 1) / (float) VORTEX_SEGMENTS;
                float a1 = t1 * TWO_PI + speed;
                float a2 = t2 * TWO_PI + speed;
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);
                buf.pos(outerR * c1, ringY, outerR * s1).color(r * 0.7F, g, b * 0.9F, ringAlpha).endVertex();
                buf.pos(outerR * c2, ringY, outerR * s2).color(r * 0.7F, g, b * 0.9F, ringAlpha).endVertex();
                buf.pos(innerR * c2, ringY, innerR * s2).color(r * 0.7F, g, b * 0.9F, ringAlpha * 0.2F).endVertex();
                buf.pos(innerR * c1, ringY, innerR * s1).color(r * 0.7F, g, b * 0.9F, ringAlpha * 0.2F).endVertex();
            }
            tess.draw();
        }

        endTranslucent();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  EFFECT 5: ORBITAL RINGS (3D tilted rings like electron shells)
    // ======================================================================

    private void renderOrbitalRings(double cx, double cy, double cz,
                                    float time, float pct) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy + 1.2, cz);
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float intensity = Math.min(pct / 0.5F, 1.0F);
        float oAlpha = 0.2F + intensity * 0.35F;

        // Each orbital ring has a different tilt and rotation speed
        float[][] tilts = {
            {30.0F, 0.0F},    // ring 0: 30deg X tilt
            {-25.0F, 90.0F},  // ring 1: -25deg X, 90deg Y offset
            {0.0F, 45.0F},    // ring 2: 0deg X, 45deg Y offset (equatorial)
        };
        float[] radii = {0.7F, 0.85F, 0.55F};
        float[] speeds = {1.2F, -0.9F, 1.8F};
        float[][] colors = {
            {0.7F, 0.2F, 1.0F},  // purple
            {0.3F, 0.5F, 1.0F},  // blue
            {0.9F, 0.3F, 0.8F},  // magenta
        };

        for (int o = 0; o < ORBITAL_COUNT; o++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(tilts[o][1] + time * speeds[o] * 20.0F, 0, 1, 0);
            GlStateManager.rotate(tilts[o][0], 1, 0, 0);

            float outerR = radii[o];
            float innerR = outerR - ORBITAL_WIDTH * 2.0F;
            float cr = colors[o][0], cg = colors[o][1], cb = colors[o][2];

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int s = 0; s < ORBITAL_SEGMENTS; s++) {
                float t1 = s / (float) ORBITAL_SEGMENTS;
                float t2 = (s + 1) / (float) ORBITAL_SEGMENTS;
                float a1 = t1 * TWO_PI;
                float a2 = t2 * TWO_PI;

                // Ring lies in the XZ plane (Y=0), will be tilted by the matrix
                float o1x = outerR * (float) Math.cos(a1);
                float o1z = outerR * (float) Math.sin(a1);
                float o2x = outerR * (float) Math.cos(a2);
                float o2z = outerR * (float) Math.sin(a2);
                float i1x = innerR * (float) Math.cos(a1);
                float i1z = innerR * (float) Math.sin(a1);
                float i2x = innerR * (float) Math.cos(a2);
                float i2z = innerR * (float) Math.sin(a2);

                buf.pos(o1x, 0, o1z).color(cr, cg, cb, oAlpha).endVertex();
                buf.pos(o2x, 0, o2z).color(cr, cg, cb, oAlpha).endVertex();
                buf.pos(i2x, 0, i2z).color(cr, cg, cb, oAlpha * 0.3F).endVertex();
                buf.pos(i1x, 0, i1z).color(cr, cg, cb, oAlpha * 0.3F).endVertex();
            }
            tess.draw();

            GlStateManager.popMatrix();
        }

        endTranslucent();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  EFFECT 6: ENERGY SPHERE (pulsing wireframe sphere at center)
    // ======================================================================

    private void renderEnergySphere(double cx, double cy, double cz,
                                    float time, float pct) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy + 1.2, cz);
        beginTranslucent();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float baseR = 0.2F + pct * 0.35F;
        float pulse = 1.0F + 0.15F * (float) Math.sin(time * 4.0);
        float radius = baseR * pulse;
        float sAlpha = 0.15F + pct * 0.25F;

        float r = 0.55F + pct * 0.35F;
        float g = 0.1F + pct * 0.15F;
        float b = 0.85F;

        // Horizontal rings at different latitudes
        for (int ring = 0; ring < SPHERE_RINGS; ring++) {
            float lat = -HALF_PI + (ring + 1) / (float)(SPHERE_RINGS + 1) * (float) Math.PI;
            float ringR = radius * (float) Math.cos(lat);
            float ringY = radius * (float) Math.sin(lat);
            float innerR = Math.max(ringR - 0.01F, 0.005F);

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int s = 0; s < SPHERE_SEGS; s++) {
                float t1 = s / (float) SPHERE_SEGS;
                float t2 = (s + 1) / (float) SPHERE_SEGS;
                float a1 = t1 * TWO_PI + time * 1.5F;
                float a2 = t2 * TWO_PI + time * 1.5F;
                float c1 = (float) Math.cos(a1), s1f = (float) Math.sin(a1);
                float c2 = (float) Math.cos(a2), s2f = (float) Math.sin(a2);

                buf.pos(ringR * c1, ringY, ringR * s1f).color(r, g, b, sAlpha).endVertex();
                buf.pos(ringR * c2, ringY, ringR * s2f).color(r, g, b, sAlpha).endVertex();
                buf.pos(innerR * c2, ringY, innerR * s2f).color(r, g, b, sAlpha * 0.3F).endVertex();
                buf.pos(innerR * c1, ringY, innerR * s1f).color(r, g, b, sAlpha * 0.3F).endVertex();
            }
            tess.draw();
        }

        endTranslucent();
        GlStateManager.popMatrix();
    }

    // ======================================================================
    //  EFFECT 7: SHOCKWAVE (expanding ring at completion)
    // ======================================================================

    private void renderShockwave(double cx, double cy, double cz,
                                 float time, float pct) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(cx, cy + 1.2, cz);
        beginTranslucent();

        // Additive blending for shockwave glow
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float power = (pct - FLASH_START) / (1.0F - FLASH_START);
        float pulse = 0.5F + 0.5F * (float) Math.sin(time * 10.0);
        float waveAlpha = power * power * 0.6F * (0.6F + 0.4F * pulse);

        // Expanding ring
        float waveR = 0.3F + power * 1.2F;
        float innerR = Math.max(waveR - 0.08F, 0.05F);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        int segs = 24;
        for (int s = 0; s < segs; s++) {
            float t1 = s / (float) segs;
            float t2 = (s + 1) / (float) segs;
            float a1 = t1 * TWO_PI;
            float a2 = t2 * TWO_PI;
            float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
            float c2 = (float) Math.cos(a2), s2 = (float) Math.sin(a2);

            buf.pos(waveR * c1, 0, waveR * s1).color(0.9F, 0.7F, 1.0F, waveAlpha).endVertex();
            buf.pos(waveR * c2, 0, waveR * s2).color(0.9F, 0.7F, 1.0F, waveAlpha).endVertex();
            buf.pos(innerR * c2, 0, innerR * s2).color(1.0F, 0.9F, 1.0F, waveAlpha * 0.5F).endVertex();
            buf.pos(innerR * c1, 0, innerR * s1).color(1.0F, 0.9F, 1.0F, waveAlpha * 0.5F).endVertex();
        }
        tess.draw();

        // Vertical flash column at center
        float flashAlpha = power * 0.4F * (0.7F + 0.3F * pulse);
        float flashR = 0.15F + power * 0.1F;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-flashR, -0.8, -flashR).color(1.0F, 0.8F, 1.0F, flashAlpha * 0.3F).endVertex();
        buf.pos( flashR, -0.8, -flashR).color(1.0F, 0.8F, 1.0F, flashAlpha * 0.3F).endVertex();
        buf.pos( flashR,  1.2,  flashR).color(1.0F, 0.8F, 1.0F, flashAlpha).endVertex();
        buf.pos(-flashR,  1.2,  flashR).color(1.0F, 0.8F, 1.0F, flashAlpha).endVertex();
        tess.draw();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(-flashR, -0.8,  flashR).color(1.0F, 0.8F, 1.0F, flashAlpha * 0.3F).endVertex();
        buf.pos( flashR, -0.8,  flashR).color(1.0F, 0.8F, 1.0F, flashAlpha * 0.3F).endVertex();
        buf.pos( flashR,  1.2, -flashR).color(1.0F, 0.8F, 1.0F, flashAlpha).endVertex();
        buf.pos(-flashR,  1.2, -flashR).color(1.0F, 0.8F, 1.0F, flashAlpha).endVertex();
        tess.draw();

        // Restore normal blending
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        endTranslucent();
        GlStateManager.popMatrix();
    }
}
