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
    public void render(TileCondenseur te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (!te.isStructureValid()) return;

        float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;
        int mdx = te.getMultiDX();
        int mdz = te.getMultiDZ();
        float centerX = 0.5F + mdx * 0.5F;
        float centerZ = 0.5F + mdz * 0.5F;

        renderArms(te, x, y, z, time, centerX, centerZ, mdx, mdz);
        renderItems(te, x, y, z, time, centerX, centerZ);
        renderFluid(te, x, y, z, time, mdx, mdz);
    }

    private void renderArms(TileCondenseur te, double x, double y, double z,
                            float time, float centerX, float centerZ, int mdx, int mdz) {

        float processPercent = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();
        boolean hasItems = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty()
                        || !te.getStackInSlot(2).isEmpty() || !te.getStackInSlot(3).isEmpty();

        // 3 arm positions - at the 3 glass block corners (not the wall corner)
        // Glass blocks are at positions 4,5,6 in the top layer
        // Arms are anchored at the top of the bottom blocks, reaching into glass area
        float[][] armBases = {
            {0.15F, 0.15F},   // corner near master
            {mdx - 0.15F * mdx / Math.abs(mdx), 0.15F},   // along X
            {0.15F, mdz - 0.15F * mdz / Math.abs(mdz)},   // along Z
        };

        // Simplify arm base positions
        float ab0x = 0.2F;
        float ab0z = 0.2F;
        float ab1x = mdx * 0.8F;
        float ab1z = 0.2F;
        float ab2x = 0.2F;
        float ab2z = mdz * 0.8F;

        // Arm 0: handles compacts (slots 0,1) - near master corner
        // Arm 1: handles key (slot 2) - along X
        // Arm 2: handles catalyst (slot 3) - along Z

        float idleSwing = (float)Math.sin(time * 0.8) * 15.0F;
        float armSpeed = processing ? 3.0F : 0.8F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        // Arm 0 - compacts handler
        float angle0 = processing ? processPercent * 90.0F : idleSwing;
        boolean hasCompact = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        if (hasCompact) angle0 = 30.0F + (processing ? processPercent * 60.0F : 0);
        drawArm(centerX + ab0x, 1.05F, centerZ + ab0z, angle0, time, 0.5F,
                hasCompact ? 0.8F : 0.4F);

        // Arm 1 - key handler
        float angle1 = processing ? processPercent * 90.0F + 120.0F : idleSwing + 120.0F;
        boolean hasKey = !te.getStackInSlot(2).isEmpty();
        if (hasKey) angle1 = 150.0F + (processing ? processPercent * 60.0F : 0);
        drawArm(centerX + ab1x, 1.05F, centerZ + ab1z, angle1, time, 0.45F,
                hasKey ? 0.8F : 0.4F);

        // Arm 2 - catalyst handler
        float angle2 = processing ? processPercent * 90.0F + 240.0F : idleSwing + 240.0F;
        boolean hasCat = !te.getStackInSlot(3).isEmpty();
        if (hasCat) angle2 = 270.0F + (processing ? processPercent * 60.0F : 0);
        drawArm(centerX + ab2x, 1.05F, centerZ + ab2z, angle2, time, 0.45F,
                hasCat ? 0.8F : 0.4F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawArm(float px, float py, float pz, float angle, float time,
                         float length, float brightness) {
        // Arm segment dimensions
        float w = 0.04F;  // width
        float h = 0.03F;  // height

        GlStateManager.pushMatrix();
        GlStateManager.translate(px, py, pz);

        // Pivot joint (small cube)
        drawBox(-0.03F, 0, -0.03F, 0.03F, 0.06F, 0.03F,
                0.3F * brightness, 0.3F * brightness, 0.35F * brightness, 1.0F);

        // Upper arm
        GlStateManager.rotate(angle, 0, 1, 0);
        GlStateManager.translate(0, 0.03F, 0);
        drawBox(-w, 0, -w, w, length * 0.5F, w,
                0.25F * brightness, 0.25F * brightness, 0.3F * brightness, 1.0F);

        // Elbow joint
        GlStateManager.translate(0, length * 0.5F, 0);
        float elbowAngle = 30.0F + 20.0F * (float)Math.sin(time * 1.5);
        drawBox(-0.025F, 0, -0.025F, 0.025F, 0.04F, 0.025F,
                0.35F * brightness, 0.2F * brightness, 0.45F * brightness, 1.0F);

        // Forearm (angled down toward center)
        GlStateManager.rotate(elbowAngle, 1, 0, 0);
        GlStateManager.translate(0, 0.02F, 0);
        drawBox(-w * 0.8F, 0, -w * 0.8F, w * 0.8F, length * 0.4F, w * 0.8F,
                0.2F * brightness, 0.2F * brightness, 0.25F * brightness, 1.0F);

        // Claw/gripper at end
        GlStateManager.translate(0, length * 0.4F, 0);
        float clawOpen = 0.02F + 0.01F * (float)Math.sin(time * 2.0);
        drawBox(-clawOpen - 0.01F, 0, -0.015F, -clawOpen + 0.005F, 0.04F, 0.015F,
                0.4F * brightness, 0.15F * brightness, 0.5F * brightness, 1.0F);
        drawBox(clawOpen - 0.005F, 0, -0.015F, clawOpen + 0.01F, 0.04F, 0.015F,
                0.4F * brightness, 0.15F * brightness, 0.5F * brightness, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Top
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1, y2, z1).color(r*1.1F, g*1.1F, b*1.1F, a).endVertex();
        buf.pos(x2, y2, z1).color(r*1.1F, g*1.1F, b*1.1F, a).endVertex();
        buf.pos(x2, y2, z2).color(r*1.1F, g*1.1F, b*1.1F, a).endVertex();
        buf.pos(x1, y2, z2).color(r*1.1F, g*1.1F, b*1.1F, a).endVertex();
        tess.draw();

        // Bottom
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1, y1, z2).color(r*0.6F, g*0.6F, b*0.6F, a).endVertex();
        buf.pos(x2, y1, z2).color(r*0.6F, g*0.6F, b*0.6F, a).endVertex();
        buf.pos(x2, y1, z1).color(r*0.6F, g*0.6F, b*0.6F, a).endVertex();
        buf.pos(x1, y1, z1).color(r*0.6F, g*0.6F, b*0.6F, a).endVertex();
        tess.draw();

        // Front (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1, y1, z1).color(r*0.8F, g*0.8F, b*0.8F, a).endVertex();
        buf.pos(x2, y1, z1).color(r*0.8F, g*0.8F, b*0.8F, a).endVertex();
        buf.pos(x2, y2, z1).color(r, g, b, a).endVertex();
        buf.pos(x1, y2, z1).color(r, g, b, a).endVertex();
        tess.draw();

        // Back (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x2, y1, z2).color(r*0.8F, g*0.8F, b*0.8F, a).endVertex();
        buf.pos(x1, y1, z2).color(r*0.8F, g*0.8F, b*0.8F, a).endVertex();
        buf.pos(x1, y2, z2).color(r, g, b, a).endVertex();
        buf.pos(x2, y2, z2).color(r, g, b, a).endVertex();
        tess.draw();

        // Left (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1, y1, z2).color(r*0.7F, g*0.7F, b*0.7F, a).endVertex();
        buf.pos(x1, y1, z1).color(r*0.7F, g*0.7F, b*0.7F, a).endVertex();
        buf.pos(x1, y2, z1).color(r*0.9F, g*0.9F, b*0.9F, a).endVertex();
        buf.pos(x1, y2, z2).color(r*0.9F, g*0.9F, b*0.9F, a).endVertex();
        tess.draw();

        // Right (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x2, y1, z1).color(r*0.7F, g*0.7F, b*0.7F, a).endVertex();
        buf.pos(x2, y1, z2).color(r*0.7F, g*0.7F, b*0.7F, a).endVertex();
        buf.pos(x2, y2, z2).color(r*0.9F, g*0.9F, b*0.9F, a).endVertex();
        buf.pos(x2, y2, z1).color(r*0.9F, g*0.9F, b*0.9F, a).endVertex();
        tess.draw();
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float centerX, float centerZ) {
        float baseY = 1.4F;
        float processPercent = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        // Items float near center, pushed by arms
        float[][] offsets = {
            {-0.2F, -0.2F}, { 0.2F, -0.2F},
            {-0.2F,  0.2F}, { 0.2F,  0.2F},
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = offsets[i][0];
            float oz = offsets[i][1];
            if (processing) {
                float pull = processPercent * 0.95F;
                ox *= (1.0F - pull);
                oz *= (1.0F - pull);
            }

            float bob = (float)Math.sin(time * 1.5 + i * 1.5) * 0.03F;
            // During processing, items sink down toward the hole
            float sinkY = processing ? -processPercent * 0.8F : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + centerX + ox, y + baseY + bob + sinkY, z + centerZ + oz);
            float angle = (time * 40.0F + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);
            float scale = 0.28F;
            if (processing) scale *= (1.0F - processPercent * 0.5F);
            GlStateManager.scale(scale, scale, scale);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        // Output item
        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + centerX, y + baseY + 0.1, z + centerZ);
            GlStateManager.rotate((time * 25.0F) % 360.0F, 0, 1, 0);
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
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
        float inset = 0.12F;
        float minX = Math.min(0, dx) + inset;
        float maxX = Math.max(0, dx) + 1 - inset;
        float minZ = Math.min(0, dz) + inset;
        float maxZ = Math.max(0, dz) + 1 - inset;
        float floorY = 1.02F;
        float topY = floorY + percent * 0.9F;
        float wave1 = 0.02F * (float)Math.sin(time * 2.5);
        float wave2 = 0.02F * (float)Math.sin(time * 3.1 + 1.5);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        float r = 0.4F + percent * 0.5F;
        float g = 0.02F + percent * 0.08F;
        float b = 0.6F - percent * 0.35F;
        float a = 0.5F + 0.1F * (float)Math.sin(time * 2.0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Top
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, topY+wave1, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY+wave2, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY+wave1, maxZ).color(r, g, b, a).endVertex();
        buf.pos(minX, topY+wave2, maxZ).color(r, g, b, a).endVertex();
        tess.draw();
        // Front
        float sa = a * 0.7F;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX,floorY,minZ).color(r*0.7F,g*0.7F,b*0.7F,sa).endVertex();
        buf.pos(maxX,floorY,minZ).color(r*0.7F,g*0.7F,b*0.7F,sa).endVertex();
        buf.pos(maxX,topY+wave2,minZ).color(r,g,b,sa).endVertex();
        buf.pos(minX,topY+wave1,minZ).color(r,g,b,sa).endVertex();
        tess.draw();
        // Back
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX,floorY,maxZ).color(r*0.7F,g*0.7F,b*0.7F,sa).endVertex();
        buf.pos(minX,floorY,maxZ).color(r*0.7F,g*0.7F,b*0.7F,sa).endVertex();
        buf.pos(minX,topY+wave2,maxZ).color(r,g,b,sa).endVertex();
        buf.pos(maxX,topY+wave1,maxZ).color(r,g,b,sa).endVertex();
        tess.draw();
        // Left
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX,floorY,maxZ).color(r*0.6F,g*0.6F,b*0.6F,sa).endVertex();
        buf.pos(minX,floorY,minZ).color(r*0.6F,g*0.6F,b*0.6F,sa).endVertex();
        buf.pos(minX,topY+wave1,minZ).color(r,g,b,sa).endVertex();
        buf.pos(minX,topY+wave2,maxZ).color(r,g,b,sa).endVertex();
        tess.draw();
        // Right
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX,floorY,minZ).color(r*0.6F,g*0.6F,b*0.6F,sa).endVertex();
        buf.pos(maxX,floorY,maxZ).color(r*0.6F,g*0.6F,b*0.6F,sa).endVertex();
        buf.pos(maxX,topY+wave1,maxZ).color(r,g,b,sa).endVertex();
        buf.pos(maxX,topY+wave2,minZ).color(r,g,b,sa).endVertex();
        tess.draw();
        // Bottom
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX,floorY,minZ).color(r*0.5F,g*0.5F,b*0.5F,sa).endVertex();
        buf.pos(minX,floorY,maxZ).color(r*0.5F,g*0.5F,b*0.5F,sa).endVertex();
        buf.pos(maxX,floorY,maxZ).color(r*0.5F,g*0.5F,b*0.5F,sa).endVertex();
        buf.pos(maxX,floorY,minZ).color(r*0.5F,g*0.5F,b*0.5F,sa).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
