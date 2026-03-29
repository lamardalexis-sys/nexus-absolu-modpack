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

        renderItems(te, x, y, z, time, cx, cz);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        // Items float in the glass layer (y+1.0 to y+2.0)
        float baseY = 1.45F;
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        // 4 input slots spread in the glass area
        float spread = 0.3F;
        float[][] off = {
            {-spread, -spread},  // slot 0: compact 1
            { spread, -spread},  // slot 1: compact 2
            {-spread,  spread},  // slot 2: key
            { spread,  spread},  // slot 3: catalyst
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = off[i][0];
            float oz = off[i][1];

            // During processing: items converge to center and sink into the hole
            if (processing) {
                ox *= (1.0F - proc * 0.95F);
                oz *= (1.0F - proc * 0.95F);
            }

            float bob = (float)Math.sin(time * 1.5 + i * 1.5) * 0.04F;
            float sink = processing ? -proc * 0.8F : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx + ox, y + baseY + bob + sink, z + cz + oz);

            // Spin
            float angle = (time * 35.0F + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);

            // Shrink during processing
            float scale = 0.35F;
            if (processing) scale *= (1.0F - proc * 0.5F);
            GlStateManager.scale(scale, scale, scale);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        // Output item - floats at center, bigger
        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + cx, y + baseY + 0.1, z + cz);
            GlStateManager.rotate((time * 20.0F) % 360.0F, 0, 1, 0);
            GlStateManager.rotate(10.0F, 1, 0, 0);
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
        float inset = 0.15F;
        float minX = Math.min(0, dx) + inset;
        float maxX = Math.max(0, dx) + 1 - inset;
        float minZ = Math.min(0, dz) + inset;
        float maxZ = Math.max(0, dz) + 1 - inset;

        // Fluid in the glass layer
        float floorY = 1.02F;
        float fillHeight = percent * 0.85F;
        float topY = floorY + fillHeight;

        // Waves
        float wave1 = 0.02F * (float)Math.sin(time * 2.5);
        float wave2 = 0.02F * (float)Math.sin(time * 3.1 + 1.5);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        // Color: purple -> red as progress increases
        float r = 0.4F + percent * 0.5F;
        float g = 0.02F + percent * 0.08F;
        float b = 0.6F - percent * 0.35F;
        float a = 0.55F + 0.1F * (float)Math.sin(time * 2.0);
        float sa = a * 0.7F;

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        // Top surface with waves
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, a).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, a).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, a).endVertex();
        tess.draw();

        // Front face (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, minZ).color(r*0.7F, g*0.7F, b*0.7F, sa).endVertex();
        buf.pos(maxX, floorY, minZ).color(r*0.7F, g*0.7F, b*0.7F, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Back face (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, maxZ).color(r*0.7F, g*0.7F, b*0.7F, sa).endVertex();
        buf.pos(minX, floorY, maxZ).color(r*0.7F, g*0.7F, b*0.7F, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Left face (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, maxZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(minX, floorY, minZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(minX, topY + wave1, minZ).color(r, g, b, sa).endVertex();
        buf.pos(minX, topY + wave2, maxZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Right face (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(maxX, floorY, minZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r*0.6F, g*0.6F, b*0.6F, sa).endVertex();
        buf.pos(maxX, topY + wave1, maxZ).color(r, g, b, sa).endVertex();
        buf.pos(maxX, topY + wave2, minZ).color(r, g, b, sa).endVertex();
        tess.draw();

        // Bottom face
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(minX, floorY, minZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(minX, floorY, maxZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(maxX, floorY, maxZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        buf.pos(maxX, floorY, minZ).color(r*0.5F, g*0.5F, b*0.5F, sa).endVertex();
        tess.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
