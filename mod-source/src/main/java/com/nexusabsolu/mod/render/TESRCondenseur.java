package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileCondenseur;
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
import org.lwjgl.opengl.GL11;

public class TESRCondenseur extends TileEntitySpecialRenderer<TileCondenseur> {

    // -- Screen coordinates (pixels on 32x32 texture) --
    private static final float SCR_LEFT   = 6.0F / 32.0F;
    private static final float SCR_RIGHT  = 26.0F / 32.0F;
    private static final float SCR_BOTTOM = 1.0F - (16.0F / 32.0F);
    private static final float SCR_TOP    = 1.0F - (4.0F / 32.0F);
    private static final float SCR_OFFSET = 0.001F;

    // -- Screen text layout --
    private static final float TEXT_LINE_H   = 0.04F;
    private static final float TEXT_LINE_GAP = 0.06F;
    private static final float TEXT_MARGIN   = 0.02F;
    private static final float CURSOR_WIDTH  = 0.03F;
    private static final int   TEXT_LINES    = 6;

    // -- LED indicator --
    private static final float LED_Y      = 0.28125F;
    private static final float LED_H1     = 25.0F / 32.0F;
    private static final float LED_H2     = 27.0F / 32.0F;
    private static final float LED_HEIGHT = 0.03F;

    // -- Item rendering --
    private static final float ITEM_BASE_Y = 1.45F;
    private static final float ITEM_SPREAD = 0.3F;
    private static final float ITEM_SCALE  = 0.35F;

    // -- Fluid rendering --
    private static final float FLUID_INSET  = 0.15F;
    private static final float FLUID_FLOOR  = 1.02F;
    private static final float FLUID_HEIGHT = 0.85F;

    // -- Vortex --
    private static final float VORTEX_START    = 0.3F;
    private static final int   VORTEX_RINGS    = 3;
    private static final int   VORTEX_SEGMENTS = 16;
    private static final float VORTEX_WIDTH    = 0.018F;

    // -- Animation thresholds --
    private static final float SPIRAL_THRESHOLD = 0.6F;
    private static final float FLASH_THRESHOLD  = 0.9F;
    private static final float TWO_PI = (float)(Math.PI * 2.0);

    @Override
    public boolean isGlobalRenderer(TileCondenseur te) {
        return true;
    }

    @Override
    public void render(TileCondenseur te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isStructureValid()) return;

        // Game-time animation: pauses when game pauses, smooth with partialTicks
        float time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20.0F;
        int dx = te.getMultiDX();
        int dz = te.getMultiDZ();
        float cx = 0.5F + dx * 0.5F;
        float cz = 0.5F + dz * 0.5F;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderScreen(te, x, y, z, time, dx, dz);
        renderItems(te, x, y, z, time, cx, cz);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    // ======================================================================
    //  HELPERS
    // ======================================================================

    /** Set lightmap to full brightness for glowing effects. */
    private void setFullBrightness() {
        OpenGlHelper.setLightmapTextureCoords(
            OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    /**
     * Emit a vertex on either an X-face or Z-face.
     * xAxis=true: facePos=X constant, h=Z, v=Y.
     * xAxis=false: facePos=Z constant, h=X, v=Y.
     */
    private void addFaceVertex(BufferBuilder buf, boolean xAxis, float facePos,
                               float h, float v, float r, float g, float b, float a) {
        if (xAxis) {
            buf.pos(facePos, v, h).color(r, g, b, a).endVertex();
        } else {
            buf.pos(h, v, facePos).color(r, g, b, a).endVertex();
        }
    }

    // ======================================================================
    //  EFFECT 7: DYNAMIC SCREEN (idle + processing, on BOTH faces)
    // ======================================================================

    private void renderScreen(TileCondenseur te, double x, double y, double z,
                              float time, int dx, int dz) {
        boolean processing = te.isProcessing();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        setFullBrightness();

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // X-facing outward side
        float xFacePos = dx > 0 ? -SCR_OFFSET : 1.0F + SCR_OFFSET;
        renderScreenFace(buf, tess, processing, time, true, xFacePos, dx > 0);

        // Z-facing outward side
        float zFacePos = dz > 0 ? -SCR_OFFSET : 1.0F + SCR_OFFSET;
        renderScreenFace(buf, tess, processing, time, false, zFacePos, dz < 0);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    /**
     * Render one screen face (background + content + LED).
     * Shared by both X-face and Z-face to eliminate duplication.
     */
    private void renderScreenFace(BufferBuilder buf, Tessellator tess,
                                  boolean processing, float time,
                                  boolean xAxis, float facePos, boolean flipH) {
        float hLeft  = flipH ? 1.0F - SCR_LEFT  : SCR_LEFT;
        float hRight = flipH ? 1.0F - SCR_RIGHT : SCR_RIGHT;
        float hMin = Math.min(hLeft, hRight);
        float hMax = Math.max(hLeft, hRight);

        // Background
        float bgR, bgG, bgB;
        if (processing) {
            bgR = 0.0F; bgG = 0.08F; bgB = 0.0F;
        } else {
            bgR = 0.02F; bgG = 0.02F; bgB = 0.04F;
        }
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFaceVertex(buf, xAxis, facePos, hMin, SCR_BOTTOM, bgR, bgG, bgB, 0.95F);
        addFaceVertex(buf, xAxis, facePos, hMax, SCR_BOTTOM, bgR, bgG, bgB, 0.95F);
        addFaceVertex(buf, xAxis, facePos, hMax, SCR_TOP, bgR * 1.5F, bgG * 1.5F, bgB * 1.5F, 0.95F);
        addFaceVertex(buf, xAxis, facePos, hMin, SCR_TOP, bgR * 1.5F, bgG * 1.5F, bgB * 1.5F, 0.95F);
        tess.draw();

        if (processing) {
            renderScreenProcessing(buf, tess, time, xAxis, facePos, flipH, hMin, hMax);
        } else {
            renderScreenIdle(buf, tess, time, xAxis, facePos, hMin, hMax);
        }

        // LED indicator (always visible)
        float ledH1 = flipH ? 1.0F - LED_H1 : LED_H1;
        float ledH2 = flipH ? 1.0F - LED_H2 : LED_H2;
        float ledMin = Math.min(ledH1, ledH2);
        float ledMax = Math.max(ledH1, ledH2);
        float ledR = processing ? 0.0F : 0.8F;
        float ledG = processing ? 0.9F : 0.1F;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFaceVertex(buf, xAxis, facePos, ledMin, LED_Y, ledR, ledG, 0.0F, 1.0F);
        addFaceVertex(buf, xAxis, facePos, ledMax, LED_Y, ledR, ledG, 0.0F, 1.0F);
        addFaceVertex(buf, xAxis, facePos, ledMax, LED_Y + LED_HEIGHT, ledR, ledG, 0.0F, 1.0F);
        addFaceVertex(buf, xAxis, facePos, ledMin, LED_Y + LED_HEIGHT, ledR, ledG, 0.0F, 1.0F);
        tess.draw();
    }

    /** Processing screen: scrolling green text lines + blinking cursor. */
    private void renderScreenProcessing(BufferBuilder buf, Tessellator tess, float time,
                                        boolean xAxis, float facePos, boolean flipH,
                                        float hMin, float hMax) {
        float scrollOffset = (time * 0.8F) % 1.0F;
        float textStart = flipH ? hMax - TEXT_MARGIN : hMin + TEXT_MARGIN;

        for (int line = 0; line < TEXT_LINES; line++) {
            float ly = SCR_BOTTOM + TEXT_MARGIN + line * TEXT_LINE_GAP + scrollOffset * TEXT_LINE_GAP;
            if (ly < SCR_BOTTOM || ly + TEXT_LINE_H > SCR_TOP) continue;

            float lw = 0.3F + 0.15F * (float) Math.sin(line * 2.7 + time * 0.5);
            float lineEnd = flipH ? textStart - lw : textStart + lw;
            float lMin = Math.min(textStart, lineEnd);
            float lMax = Math.max(textStart, lineEnd);

            float bright = 0.4F + 0.3F * (float) Math.sin(time * 3.0 + line * 1.5);
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            addFaceVertex(buf, xAxis, facePos, lMin, ly, 0.0F, bright, 0.0F, 0.8F);
            addFaceVertex(buf, xAxis, facePos, lMax, ly, 0.0F, bright, 0.0F, 0.8F);
            addFaceVertex(buf, xAxis, facePos, lMax, ly + TEXT_LINE_H, 0.0F, bright * 0.7F, 0.0F, 0.8F);
            addFaceVertex(buf, xAxis, facePos, lMin, ly + TEXT_LINE_H, 0.0F, bright * 0.7F, 0.0F, 0.8F);
            tess.draw();
        }

        // Blinking cursor
        if ((int) (time * 2) % 2 == 0) {
            float curY = SCR_BOTTOM + TEXT_MARGIN + 5 * TEXT_LINE_GAP + scrollOffset * TEXT_LINE_GAP;
            if (curY + TEXT_LINE_H < SCR_TOP) {
                float curEnd = flipH ? textStart - CURSOR_WIDTH : textStart + CURSOR_WIDTH;
                float cMin = Math.min(textStart, curEnd);
                float cMax = Math.max(textStart, curEnd);
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                addFaceVertex(buf, xAxis, facePos, cMin, curY, 0.0F, 0.9F, 0.0F, 1.0F);
                addFaceVertex(buf, xAxis, facePos, cMax, curY, 0.0F, 0.9F, 0.0F, 1.0F);
                addFaceVertex(buf, xAxis, facePos, cMax, curY + TEXT_LINE_H, 0.0F, 0.9F, 0.0F, 1.0F);
                addFaceVertex(buf, xAxis, facePos, cMin, curY + TEXT_LINE_H, 0.0F, 0.9F, 0.0F, 1.0F);
                tess.draw();
            }
        }
    }

    /** Idle screen: slow scanline + pulsating violet center dot. */
    private void renderScreenIdle(BufferBuilder buf, Tessellator tess, float time,
                                  boolean xAxis, float facePos,
                                  float hMin, float hMax) {
        // Scanline sweeping up
        float scanY = SCR_BOTTOM + ((time * 0.15F) % 1.0F) * (SCR_TOP - SCR_BOTTOM);
        float scanL = hMin + TEXT_MARGIN;
        float scanR = hMax - TEXT_MARGIN;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFaceVertex(buf, xAxis, facePos, scanL, scanY, 0.05F, 0.05F, 0.12F, 0.6F);
        addFaceVertex(buf, xAxis, facePos, scanR, scanY, 0.05F, 0.05F, 0.12F, 0.6F);
        addFaceVertex(buf, xAxis, facePos, scanR, scanY + 0.02F, 0.08F, 0.08F, 0.18F, 0.4F);
        addFaceVertex(buf, xAxis, facePos, scanL, scanY + 0.02F, 0.08F, 0.08F, 0.18F, 0.4F);
        tess.draw();

        // Pulsating violet dot at center
        float dotY = (SCR_BOTTOM + SCR_TOP) / 2.0F;
        float dotH = (hMin + hMax) / 2.0F;
        float pulse = 0.03F + 0.015F * (float) Math.sin(time * 1.5);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        addFaceVertex(buf, xAxis, facePos, dotH - pulse, dotY - pulse, 0.15F, 0.05F, 0.25F, 0.7F);
        addFaceVertex(buf, xAxis, facePos, dotH + pulse, dotY - pulse, 0.15F, 0.05F, 0.25F, 0.7F);
        addFaceVertex(buf, xAxis, facePos, dotH + pulse, dotY + pulse, 0.15F, 0.05F, 0.25F, 0.7F);
        addFaceVertex(buf, xAxis, facePos, dotH - pulse, dotY + pulse, 0.15F, 0.05F, 0.25F, 0.7F);
        tess.draw();
    }

    // ======================================================================
    //  EFFECTS 1-2-3: FLOATING ITEMS + BATHTUB SPIRAL + SINK INTO HOLE
    // ======================================================================

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        float[][] startPos = {
            { -ITEM_SPREAD, -ITEM_SPREAD },
            {  ITEM_SPREAD, -ITEM_SPREAD },
            { -ITEM_SPREAD,  ITEM_SPREAD },
            {  ITEM_SPREAD,  ITEM_SPREAD },
        };

        setFullBrightness();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = startPos[i][0];
            float oz = startPos[i][1];
            float itemY = ITEM_BASE_Y;

            if (processing) {
                // Phase 1 (0-60%): items spiral inward toward center
                float spiralPhase = Math.min(proc / SPIRAL_THRESHOLD, 1.0F);
                // Phase 2 (60-100%): items sink down into the hole
                float sinkPhase = Math.max((proc - SPIRAL_THRESHOLD) / (1.0F - SPIRAL_THRESHOLD), 0.0F);

                float radius = ITEM_SPREAD * (1.0F - spiralPhase * 0.85F);
                float baseAngle = (float) Math.atan2(startPos[i][1], startPos[i][0]);
                float spiralSpeed = 3.0F + spiralPhase * 8.0F;
                float angle = baseAngle + time * spiralSpeed * (0.3F + spiralPhase);

                ox = radius * (float) Math.cos(angle);
                oz = radius * (float) Math.sin(angle);
                itemY = ITEM_BASE_Y - sinkPhase * 1.2F;
            }

            float bob = processing ? 0 : (float) Math.sin(time * 1.5 + i * 1.5) * 0.04F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx + ox, y + itemY + bob, z + cz + oz);

            float spinSpeed = processing ? 80.0F + proc * 200.0F : 35.0F;
            float spinAngle = (time * spinSpeed + i * 90.0F) % 360.0F;
            GlStateManager.rotate(spinAngle, 0, 1, 0);

            float scale = ITEM_SCALE;
            if (processing) {
                scale *= (1.0F - proc * 0.6F);
            }
            GlStateManager.scale(scale, scale, scale);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        // Output item with glow effect
        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            float outBob = (float) Math.sin(time * 1.2) * 0.05F;
            float outY = ITEM_BASE_Y + 0.15F + outBob;

            // Glow disc behind the output item
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
            setFullBrightness();

            float glowSize = 0.25F + 0.06F * (float) Math.sin(time * 2.0);
            float glowAlpha = 0.35F + 0.15F * (float) Math.sin(time * 1.5);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(cx - glowSize, outY - 0.02, cz - glowSize).color(0.55F, 0.1F, 0.85F, glowAlpha).endVertex();
            buf.pos(cx + glowSize, outY - 0.02, cz - glowSize).color(0.55F, 0.1F, 0.85F, glowAlpha).endVertex();
            buf.pos(cx + glowSize, outY - 0.02, cz + glowSize).color(0.55F, 0.1F, 0.85F, glowAlpha).endVertex();
            buf.pos(cx - glowSize, outY - 0.02, cz + glowSize).color(0.55F, 0.1F, 0.85F, glowAlpha).endVertex();
            tess.draw();

            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();

            // The item itself
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx, y + outY, z + cz);
            GlStateManager.rotate((time * 20.0F) % 360.0F, 0, 1, 0);
            GlStateManager.rotate(8.0F, 1, 0, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            setFullBrightness();
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                output, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    // ======================================================================
    //  EFFECTS 4-5-6: VIOLET FLUID + COLOR SHIFT + VORTEX RINGS
    //  EFFECT 8: COMPLETION FLASH
    // ======================================================================

    private void renderFluid(TileCondenseur te, double x, double y, double z,
                             float time, int dx, int dz) {
        if (!te.isProcessing()) return;

        float percent = te.getProcessPercent() / 100.0F;
        float minX = Math.min(0, dx) + FLUID_INSET;
        float maxX = Math.max(0, dx) + 1 - FLUID_INSET;
        float minZ = Math.min(0, dz) + FLUID_INSET;
        float maxZ = Math.max(0, dz) + 1 - FLUID_INSET;

        float topY = FLUID_FLOOR + percent * FLUID_HEIGHT;

        // Wave displacement
        float wave1 = 0.03F * (float) Math.sin(time * 2.5);
        float wave2 = 0.03F * (float) Math.sin(time * 3.1 + 1.5);
        float wave3 = 0.02F * (float) Math.sin(time * 4.0 + 3.0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        setFullBrightness();

        // Color: vibrant deep purple -> electric magenta
        float r = 0.4F + percent * 0.5F;
        float g = 0.02F + percent * 0.08F;
        float b = 0.75F - percent * 0.1F;
        float a = 0.65F + 0.1F * (float) Math.sin(time * 2.0);
        // Glow intensifies with progress
        r += percent * 0.15F;
        float sa = a * 0.7F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Top surface with waves
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave3, maxZ).color(r, g, b, a).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, a).endVertex();
        tess.draw();

        // Front face (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, FLUID_FLOOR, minZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sa).endVertex();
        buf.pos(maxX, FLUID_FLOOR, minZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Back face (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, FLUID_FLOOR, maxZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sa).endVertex();
        buf.pos(minX, FLUID_FLOOR, maxZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave3, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Left face (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, FLUID_FLOOR, maxZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sa).endVertex();
        buf.pos(minX, FLUID_FLOOR, minZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Right face (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, FLUID_FLOOR, minZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sa).endVertex();
        buf.pos(maxX, FLUID_FLOOR, maxZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sa).endVertex();
        buf.pos(maxX, topY + wave3, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Bottom face
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, FLUID_FLOOR, minZ).color(r * 0.4F, g * 0.4F, b * 0.4F, sa).endVertex();
        buf.pos(minX, FLUID_FLOOR, maxZ).color(r * 0.4F, g * 0.4F, b * 0.4F, sa).endVertex();
        buf.pos(maxX, FLUID_FLOOR, maxZ).color(r * 0.4F, g * 0.4F, b * 0.4F, sa).endVertex();
        buf.pos(maxX, FLUID_FLOOR, minZ).color(r * 0.4F, g * 0.4F, b * 0.4F, sa).endVertex();
        tess.draw();

        // Vortex rings (solid quads, visible at 30%+)
        if (percent > VORTEX_START) {
            float vortexIntensity = (percent - VORTEX_START) / (1.0F - VORTEX_START);
            float centerFX = (minX + maxX) / 2.0F;
            float centerFZ = (minZ + maxZ) / 2.0F;
            float vr = Math.min(r + 0.2F, 1.0F);
            float vg = 0.05F + percent * 0.15F;
            float vb = Math.min(b + 0.1F, 1.0F);

            for (int ring = 0; ring < VORTEX_RINGS; ring++) {
                float outerRadius = (0.15F + ring * 0.2F) * (1.0F - vortexIntensity * 0.5F);
                float innerRadius = Math.max(outerRadius - VORTEX_WIDTH, 0.01F);
                float ringAlpha = 0.3F + vortexIntensity * 0.4F;
                float ringY = topY + 0.01F + ring * 0.005F;
                float ringSpeed = time * (2.0F + ring) + ring * 1.5F;

                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                for (int s = 0; s < VORTEX_SEGMENTS; s++) {
                    float t1 = s / (float) VORTEX_SEGMENTS;
                    float t2 = (s + 1) / (float) VORTEX_SEGMENTS;
                    float ang1 = t1 * TWO_PI + ringSpeed;
                    float ang2 = t2 * TWO_PI + ringSpeed;

                    float cos1 = (float) Math.cos(ang1);
                    float sin1 = (float) Math.sin(ang1);
                    float cos2 = (float) Math.cos(ang2);
                    float sin2 = (float) Math.sin(ang2);

                    float o1x = centerFX + outerRadius * cos1;
                    float o1z = centerFZ + outerRadius * sin1;
                    float o2x = centerFX + outerRadius * cos2;
                    float o2z = centerFZ + outerRadius * sin2;
                    float i1x = centerFX + innerRadius * cos1;
                    float i1z = centerFZ + innerRadius * sin1;
                    float i2x = centerFX + innerRadius * cos2;
                    float i2z = centerFZ + innerRadius * sin2;

                    buf.pos(o1x, ringY, o1z).color(vr, vg, vb, ringAlpha).endVertex();
                    buf.pos(o2x, ringY, o2z).color(vr, vg, vb, ringAlpha).endVertex();
                    buf.pos(i2x, ringY, i2z).color(vr, vg, vb, ringAlpha * 0.3F).endVertex();
                    buf.pos(i1x, ringY, i1z).color(vr, vg, vb, ringAlpha * 0.3F).endVertex();
                }
                tess.draw();
            }
        }

        // Completion flash (last 10% of processing)
        if (percent > FLASH_THRESHOLD) {
            float flashPower = (percent - FLASH_THRESHOLD) / (1.0F - FLASH_THRESHOLD);
            float flashAlpha = flashPower * flashPower * 0.5F;
            float pulse = 0.5F + 0.5F * (float) Math.sin(time * 8.0);
            flashAlpha *= (0.7F + 0.3F * pulse);
            float flashR = 0.8F + flashPower * 0.2F;
            float flashG = 0.5F + flashPower * 0.4F;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            // Top glow
            buf.pos(minX, topY + 0.05, minZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            buf.pos(maxX, topY + 0.05, minZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            buf.pos(maxX, topY + 0.05, maxZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            buf.pos(minX, topY + 0.05, maxZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            // Front glow
            buf.pos(minX, FLUID_FLOOR, minZ).color(flashR, flashG, 1.0F, flashAlpha * 0.4F).endVertex();
            buf.pos(maxX, FLUID_FLOOR, minZ).color(flashR, flashG, 1.0F, flashAlpha * 0.4F).endVertex();
            buf.pos(maxX, topY + 0.05, minZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            buf.pos(minX, topY + 0.05, minZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            // Back glow
            buf.pos(maxX, FLUID_FLOOR, maxZ).color(flashR, flashG, 1.0F, flashAlpha * 0.4F).endVertex();
            buf.pos(minX, FLUID_FLOOR, maxZ).color(flashR, flashG, 1.0F, flashAlpha * 0.4F).endVertex();
            buf.pos(minX, topY + 0.05, maxZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            buf.pos(maxX, topY + 0.05, maxZ).color(flashR, flashG, 1.0F, flashAlpha).endVertex();
            tess.draw();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
