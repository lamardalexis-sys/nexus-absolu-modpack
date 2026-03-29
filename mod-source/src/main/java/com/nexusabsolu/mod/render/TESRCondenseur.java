package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class TESRCondenseur extends TileEntitySpecialRenderer<TileCondenseur> {

    @Override
    public boolean isGlobalRenderer(TileCondenseur te) {
        return true;
    }

    @Override
    public void render(TileCondenseur te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isStructureValid()) return;
        float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
        int dx = te.getMultiDX();
        int dz = te.getMultiDZ();
        float cx = 0.5F + dx * 0.5F;
        float cz = 0.5F + dz * 0.5F;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        renderScreen(te, x, y, z, time, dx, dz);
        renderItems(te, x, y, z, time, cx, cz);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    private void renderScreen(TileCondenseur te, double x, double y, double z,
                              float time, int dx, int dz) {
        if (!te.isStructureValid()) return;

        boolean processing = te.isProcessing();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();

        // Screen area on texture: pixels 6-25 x, 5-15 y on 32x32
        // In block coords: x=0.1875-0.78125, y=0.53125-0.84375 (from bottom)
        float scrLeft = 6.0F / 32.0F;
        float scrRight = 26.0F / 32.0F;
        float scrBottom = 1.0F - (16.0F / 32.0F); // 0.5
        float scrTop = 1.0F - (4.0F / 32.0F);     // 0.875

        // Determine outward faces of master block
        // dx>0: west face visible, dx<0: east face visible
        // dz>0: north face visible, dz<0: south face visible

        float offset = 0.001F;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Render on the X-facing outward side
        {
            float faceX = dx > 0 ? -offset : 1.0F + offset;
            float flipL = dx > 0 ? 1.0F - scrLeft : scrLeft;
            float flipR = dx > 0 ? 1.0F - scrRight : scrRight;

            // Screen background - always visible
            float bgR, bgG, bgB;
            if (processing) {
                bgR = 0.0F; bgG = 0.08F; bgB = 0.0F;
            } else {
                bgR = 0.02F; bgG = 0.02F; bgB = 0.04F; // dim blue-grey when off
            }
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(faceX, scrBottom, flipR).color(bgR, bgG, bgB, 0.95F).endVertex();
            buf.pos(faceX, scrBottom, flipL).color(bgR, bgG, bgB, 0.95F).endVertex();
            buf.pos(faceX, scrTop, flipL).color(bgR * 1.5F, bgG * 1.5F, bgB * 1.5F, 0.95F).endVertex();
            buf.pos(faceX, scrTop, flipR).color(bgR * 1.5F, bgG * 1.5F, bgB * 1.5F, 0.95F).endVertex();
            tess.draw();

            if (processing) {

                // Scrolling green "text" lines
                float scrollOffset = (time * 0.8F) % 1.0F;
                float lineH = 0.04F;
                float lineGap = 0.06F;
                for (int line = 0; line < 6; line++) {
                    float ly = scrBottom + 0.02F + line * lineGap + scrollOffset * lineGap;
                    if (ly < scrBottom || ly + lineH > scrTop) continue;

                    // Vary line width to look like different text
                    float lw = 0.3F + 0.15F * (float)Math.sin(line * 2.7 + time * 0.5);
                    float lineL = dx > 0 ? 1.0F - scrLeft - 0.02F : scrLeft + 0.02F;
                    float lineR = dx > 0 ? lineL - lw : lineL + lw;

                    float bright = 0.4F + 0.3F * (float)Math.sin(time * 3.0 + line * 1.5);
                    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    buf.pos(faceX, ly, Math.min(lineL, lineR)).color(0.0F, bright, 0.0F, 0.8F).endVertex();
                    buf.pos(faceX, ly, Math.max(lineL, lineR)).color(0.0F, bright, 0.0F, 0.8F).endVertex();
                    buf.pos(faceX, ly + lineH, Math.max(lineL, lineR)).color(0.0F, bright * 0.7F, 0.0F, 0.8F).endVertex();
                    buf.pos(faceX, ly + lineH, Math.min(lineL, lineR)).color(0.0F, bright * 0.7F, 0.0F, 0.8F).endVertex();
                    tess.draw();
                }

                // Blinking cursor
                if ((int)(time * 2) % 2 == 0) {
                    float curY = scrBottom + 0.02F + 5 * lineGap + scrollOffset * lineGap;
                    if (curY + lineH < scrTop) {
                        float curL = dx > 0 ? 1.0F - scrLeft - 0.02F : scrLeft + 0.02F;
                        float curR = dx > 0 ? curL - 0.03F : curL + 0.03F;
                        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                        buf.pos(faceX, curY, Math.min(curL, curR)).color(0.0F, 0.9F, 0.0F, 1.0F).endVertex();
                        buf.pos(faceX, curY, Math.max(curL, curR)).color(0.0F, 0.9F, 0.0F, 1.0F).endVertex();
                        buf.pos(faceX, curY + lineH, Math.max(curL, curR)).color(0.0F, 0.9F, 0.0F, 1.0F).endVertex();
                        buf.pos(faceX, curY + lineH, Math.min(curL, curR)).color(0.0F, 0.9F, 0.0F, 1.0F).endVertex();
                        tess.draw();
                    }
                }
            } else {
                // IDLE screen: slow scanning line + dim "standby" bar
                float scanY = scrBottom + ((time * 0.15F) % 1.0F) * (scrTop - scrBottom);
                float scanL = Math.min(flipL, flipR) + 0.02F;
                float scanR = Math.max(flipL, flipR) - 0.02F;
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(faceX, scanY, scanL).color(0.05F, 0.05F, 0.12F, 0.6F).endVertex();
                buf.pos(faceX, scanY, scanR).color(0.05F, 0.05F, 0.12F, 0.6F).endVertex();
                buf.pos(faceX, scanY + 0.02F, scanR).color(0.08F, 0.08F, 0.18F, 0.4F).endVertex();
                buf.pos(faceX, scanY + 0.02F, scanL).color(0.08F, 0.08F, 0.18F, 0.4F).endVertex();
                tess.draw();

                // Dim center dot (standby indicator)
                float dotY = (scrBottom + scrTop) / 2.0F;
                float dotZ = (Math.min(flipL, flipR) + Math.max(flipL, flipR)) / 2.0F;
                float pulse = 0.03F + 0.015F * (float)Math.sin(time * 1.5);
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(faceX, dotY - pulse, dotZ - pulse).color(0.15F, 0.05F, 0.25F, 0.7F).endVertex();
                buf.pos(faceX, dotY - pulse, dotZ + pulse).color(0.15F, 0.05F, 0.25F, 0.7F).endVertex();
                buf.pos(faceX, dotY + pulse, dotZ + pulse).color(0.15F, 0.05F, 0.25F, 0.7F).endVertex();
                buf.pos(faceX, dotY + pulse, dotZ - pulse).color(0.15F, 0.05F, 0.25F, 0.7F).endVertex();
                tess.draw();
            }
            float ledY = 0.28125F; // pixel 23 from top = (32-23)/32
            float ledX1 = dx > 0 ? 1.0F - 25.0F/32.0F : 25.0F/32.0F;
            float ledX2 = dx > 0 ? 1.0F - 27.0F/32.0F : 27.0F/32.0F;
            float ledR = processing ? 0.0F : 0.8F;
            float ledG = processing ? 0.9F : 0.1F;
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(faceX, ledY, Math.min(ledX1, ledX2)).color(ledR, ledG, 0.0F, 1.0F).endVertex();
            buf.pos(faceX, ledY, Math.max(ledX1, ledX2)).color(ledR, ledG, 0.0F, 1.0F).endVertex();
            buf.pos(faceX, ledY + 0.03F, Math.max(ledX1, ledX2)).color(ledR, ledG, 0.0F, 1.0F).endVertex();
            buf.pos(faceX, ledY + 0.03F, Math.min(ledX1, ledX2)).color(ledR, ledG, 0.0F, 1.0F).endVertex();
            tess.draw();
        }

        // Render on the Z-facing outward side
        {
            float faceZ = dz > 0 ? -offset : 1.0F + offset;
            float flipL = dz > 0 ? scrLeft : 1.0F - scrLeft;
            float flipR = dz > 0 ? scrRight : 1.0F - scrRight;

            // Screen background - always visible
            float bgR2, bgG2, bgB2;
            if (processing) {
                bgR2 = 0.0F; bgG2 = 0.08F; bgB2 = 0.0F;
            } else {
                bgR2 = 0.02F; bgG2 = 0.02F; bgB2 = 0.04F;
            }
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(Math.min(flipL, flipR), scrBottom, faceZ).color(bgR2, bgG2, bgB2, 0.95F).endVertex();
            buf.pos(Math.max(flipL, flipR), scrBottom, faceZ).color(bgR2, bgG2, bgB2, 0.95F).endVertex();
            buf.pos(Math.max(flipL, flipR), scrTop, faceZ).color(bgR2*1.5F, bgG2*1.5F, bgB2*1.5F, 0.95F).endVertex();
            buf.pos(Math.min(flipL, flipR), scrTop, faceZ).color(bgR2*1.5F, bgG2*1.5F, bgB2*1.5F, 0.95F).endVertex();
            tess.draw();

            if (processing) {
                float scrollOffset = (time * 0.8F) % 1.0F;
                float lineH = 0.04F;
                float lineGap = 0.06F;
                for (int line = 0; line < 6; line++) {
                    float ly = scrBottom + 0.02F + line * lineGap + scrollOffset * lineGap;
                    if (ly < scrBottom || ly + lineH > scrTop) continue;
                    float lw = 0.3F + 0.15F * (float)Math.sin(line * 2.7 + time * 0.5);
                    float lineStart = dz > 0 ? scrLeft + 0.02F : 1.0F - scrLeft - 0.02F;
                    float lineEnd = dz > 0 ? lineStart + lw : lineStart - lw;
                    float bright = 0.4F + 0.3F * (float)Math.sin(time * 3.0 + line * 1.5);
                    buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    buf.pos(Math.min(lineStart, lineEnd), ly, faceZ).color(0.0F, bright, 0.0F, 0.8F).endVertex();
                    buf.pos(Math.max(lineStart, lineEnd), ly, faceZ).color(0.0F, bright, 0.0F, 0.8F).endVertex();
                    buf.pos(Math.max(lineStart, lineEnd), ly + lineH, faceZ).color(0.0F, bright * 0.7F, 0.0F, 0.8F).endVertex();
                    buf.pos(Math.min(lineStart, lineEnd), ly + lineH, faceZ).color(0.0F, bright * 0.7F, 0.0F, 0.8F).endVertex();
                    tess.draw();
                }
            } else {
                // Idle scanline
                float scanY = scrBottom + ((time * 0.15F) % 1.0F) * (scrTop - scrBottom);
                float scanL2 = Math.min(flipL, flipR) + 0.02F;
                float scanR2 = Math.max(flipL, flipR) - 0.02F;
                buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                buf.pos(scanL2, scanY, faceZ).color(0.05F, 0.05F, 0.12F, 0.6F).endVertex();
                buf.pos(scanR2, scanY, faceZ).color(0.05F, 0.05F, 0.12F, 0.6F).endVertex();
                buf.pos(scanR2, scanY + 0.02F, faceZ).color(0.08F, 0.08F, 0.18F, 0.4F).endVertex();
                buf.pos(scanL2, scanY + 0.02F, faceZ).color(0.08F, 0.08F, 0.18F, 0.4F).endVertex();
                tess.draw();
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        float baseY = 1.45F;
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        // 4 input items spread in the glass area
        float spread = 0.3F;
        float[][] startPos = {
            {-spread, -spread},  // slot 0: compact 1
            { spread, -spread},  // slot 1: compact 2
            {-spread,  spread},  // slot 2: key
            { spread,  spread},  // slot 3: catalyst
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = startPos[i][0];
            float oz = startPos[i][1];
            float itemY = baseY;

            if (processing) {
                // BATHTUB DRAIN EFFECT:
                // Phase 1 (0-60%): items spiral inward, getting closer to center
                // Phase 2 (60-100%): items sink down into the hole
                float spiralPhase = Math.min(proc / 0.6F, 1.0F);
                float sinkPhase = Math.max((proc - 0.6F) / 0.4F, 0.0F);

                // Spiral: items orbit around center, radius shrinks
                float radius = spread * (1.0F - spiralPhase * 0.85F);
                float baseAngle = (float)Math.atan2(startPos[i][1], startPos[i][0]);
                float spiralSpeed = 3.0F + spiralPhase * 8.0F; // faster as they get closer
                float angle = baseAngle + time * spiralSpeed * (0.3F + spiralPhase);

                ox = radius * (float)Math.cos(angle);
                oz = radius * (float)Math.sin(angle);

                // Sink into the hole during phase 2
                itemY = baseY - sinkPhase * 1.2F;
            }

            float bob = processing ? 0 : (float)Math.sin(time * 1.5 + i * 1.5) * 0.04F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx + ox, y + itemY + bob, z + cz + oz);

            // Spin faster during processing
            float spinSpeed = processing ? 80.0F + proc * 200.0F : 35.0F;
            float angle = (time * spinSpeed + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);

            // Shrink as they approach the hole
            float scale = 0.35F;
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

        // Output item - rises up from the hole with a glow effect
        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            float outBob = (float)Math.sin(time * 1.2) * 0.05F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx, y + baseY + 0.15 + outBob, z + cz);
            GlStateManager.rotate((time * 20.0F) % 360.0F, 0, 1, 0);
            GlStateManager.rotate(8.0F, 1, 0, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                output, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    private void renderFluid(TileCondenseur te, double x, double y, double z,
                             float time, int dx, int dz) {
        if (!te.isProcessing()) return;

        float percent = te.getProcessPercent() / 100.0F;
        float inset = 0.15F;
        float minX = Math.min(0, dx) + inset;
        float maxX = Math.max(0, dx) + 1 - inset;
        float minZ = Math.min(0, dz) + inset;
        float maxZ = Math.max(0, dz) + 1 - inset;

        float floorY = 1.02F;
        float fillHeight = percent * 0.85F;
        float topY = floorY + fillHeight;

        // Waves
        float wave1 = 0.03F * (float)Math.sin(time * 2.5);
        float wave2 = 0.03F * (float)Math.sin(time * 3.1 + 1.5);
        float wave3 = 0.02F * (float)Math.sin(time * 4.0 + 3.0);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        // Color: deep purple -> bright violet -> hot magenta
        // NOT brown! Vibrant purple energy
        float r = 0.35F + percent * 0.45F;
        float g = 0.02F;
        float b = 0.65F - percent * 0.15F;
        float a = 0.6F + 0.1F * (float)Math.sin(time * 2.0);
        float sa = a * 0.7F;

        // Glow intensity increases with progress
        float glow = percent * 0.2F;
        r += glow;

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
        buf.pos(minX, floorY, minZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(maxX, floorY, minZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Back face (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, maxZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(minX, floorY, maxZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave3, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Left face (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, maxZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(minX, floorY, minZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Right face (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, minZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(maxX, topY + wave3, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Bottom face
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, minZ).color(r*0.4F, g*0.4F, b*0.4F, sa).endVertex();
        buf.pos(minX, floorY, maxZ).color(r*0.4F, g*0.4F, b*0.4F, sa).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r*0.4F, g*0.4F, b*0.4F, sa).endVertex();
        buf.pos(maxX, floorY, minZ).color(r*0.4F, g*0.4F, b*0.4F, sa).endVertex();
        tess.draw();

        // VORTEX EFFECT: spiral lines on the surface near the center
        if (percent > 0.3F) {
            float vortexIntensity = (percent - 0.3F) / 0.7F;
            float centerFX = (minX + maxX) / 2.0F;
            float centerFZ = (minZ + maxZ) / 2.0F;

            for (int ring = 0; ring < 3; ring++) {
                float ringRadius = 0.15F + ring * 0.2F;
                ringRadius *= (1.0F - vortexIntensity * 0.5F);
                int segments = 12;
                float ringAlpha = 0.3F + vortexIntensity * 0.4F;
                float ringY = topY + 0.01F + ring * 0.005F;

                buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
                for (int s = 0; s <= segments; s++) {
                    float t = s / (float) segments;
                    float ang = t * (float)(Math.PI * 2) + time * (2.0F + ring) + ring * 1.5F;
                    float rx = centerFX + ringRadius * (float)Math.cos(ang);
                    float rz = centerFZ + ringRadius * (float)Math.sin(ang);
                    buf.pos(rx, ringY, rz).color(r + 0.2F, g + 0.1F, b + 0.1F, ringAlpha).endVertex();
                }
                tess.draw();
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
