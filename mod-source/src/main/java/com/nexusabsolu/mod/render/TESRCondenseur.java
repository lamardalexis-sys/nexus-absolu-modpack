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
        int dx = te.getMultiDX();
        int dz = te.getMultiDZ();

        // Center of the 2x2 footprint relative to master
        float centerX = 0.5F + dx * 0.5F;
        float centerZ = 0.5F + dz * 0.5F;

        renderItems(te, x, y, z, time, centerX, centerZ);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float centerX, float centerZ) {

        // Render INSIDE the glass layer: y+1.1 to y+1.9
        float baseY = 1.5F;

        float[][] offsets = {
            {-0.3F, -0.3F},
            { 0.3F, -0.3F},
            {-0.3F,  0.3F},
            { 0.3F,  0.3F},
        };

        float processPercent = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = offsets[i][0];
            float oz = offsets[i][1];

            if (processing) {
                float pull = processPercent * 0.9F;
                ox *= (1.0F - pull);
                oz *= (1.0F - pull);
            }

            float bob = (float)Math.sin(time * 1.5 + i * 1.5) * 0.04F;
            float rise = processing ? processPercent * 0.15F : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                x + centerX + ox,
                y + baseY + bob + rise,
                z + centerZ + oz
            );

            float angle = (time * 40.0F + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);

            float scale = 0.3F;
            if (processing) {
                scale *= (1.0F - processPercent * 0.6F);
            }
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

            float angle = (time * 25.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.rotate(15.0F, 1, 0, 0);
            GlStateManager.scale(0.45F, 0.45F, 0.45F);

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

        // Bounds of the 2x2 area
        float minX = Math.min(0, dx) + inset;
        float maxX = Math.max(0, dx) + 1 - inset;
        float minZ = Math.min(0, dz) + inset;
        float maxZ = Math.max(0, dz) + 1 - inset;

        // Fluid fills the glass layer from bottom to top
        float floorY = 1.02F;
        float fillHeight = percent * 0.9F;
        float topY = floorY + fillHeight;

        // Wave on surface
        float wave1 = 0.02F * (float)Math.sin(time * 2.5);
        float wave2 = 0.02F * (float)Math.sin(time * 3.1 + 1.5);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        // Color shifts from purple to hot red as it progresses
        float r = 0.4F + percent * 0.5F;
        float g = 0.02F + percent * 0.08F;
        float b = 0.6F - percent * 0.35F;
        float a = 0.55F + 0.1F * (float)Math.sin(time * 2.0);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // === TOP SURFACE (with waves) ===
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, a).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, a).endVertex();
        tess.draw();

        // === FRONT FACE (-Z) ===
        float sideA = a * 0.7F;
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, minZ).color(r * 0.7F, g * 0.7F, b * 0.7F, sideA).endVertex();
        buf.pos(maxX, floorY, minZ).color(r * 0.7F, g * 0.7F, b * 0.7F, sideA).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sideA).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sideA).endVertex();
        tess.draw();

        // === BACK FACE (+Z) ===
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, maxZ).color(r * 0.7F, g * 0.7F, b * 0.7F, sideA).endVertex();
        buf.pos(minX, floorY, maxZ).color(r * 0.7F, g * 0.7F, b * 0.7F, sideA).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sideA).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, sideA).endVertex();
        tess.draw();

        // === LEFT FACE (-X) ===
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, maxZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sideA).endVertex();
        buf.pos(minX, floorY, minZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sideA).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sideA).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sideA).endVertex();
        tess.draw();

        // === RIGHT FACE (+X) ===
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, minZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sideA).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r * 0.6F, g * 0.6F, b * 0.6F, sideA).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, sideA).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sideA).endVertex();
        tess.draw();

        // === BOTTOM FACE ===
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, minZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sideA).endVertex();
        buf.pos(minX, floorY, maxZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sideA).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sideA).endVertex();
        buf.pos(maxX, floorY, minZ).color(r * 0.5F, g * 0.5F, b * 0.5F, sideA).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }
}
