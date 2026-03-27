package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
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

        // Center of the 2x2 footprint relative to master block
        float centerX = 0.5F + dx * 0.5F;
        float centerZ = 0.5F + dz * 0.5F;

        renderItems(te, x, y, z, time, centerX, centerZ);
        renderLiquid(te, x, y, z, time, centerX, centerZ, dx, dz);
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float centerX, float centerZ) {

        float baseY = 1.4F;

        float[][] offsets = {
            {-0.25F, -0.25F},
            { 0.25F, -0.25F},
            {-0.25F,  0.25F},
            { 0.25F,  0.25F},
        };

        float processPercent = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = offsets[i][0];
            float oz = offsets[i][1];

            if (processing) {
                float pull = processPercent * 0.8F;
                ox *= (1.0F - pull);
                oz *= (1.0F - pull);
            }

            float bob = (float)Math.sin(time * 1.5 + i * 1.5) * 0.05F;
            float rise = processing ? processPercent * 0.2F : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                x + centerX + ox,
                y + baseY + bob + rise,
                z + centerZ + oz
            );

            float angle = (time * 40.0F + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);

            float scale = 0.35F;
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

        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + centerX, y + baseY + 0.1, z + centerZ);

            float angle = (time * 25.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);
            GlStateManager.rotate(15.0F, 1, 0, 0);
            GlStateManager.scale(0.5F, 0.5F, 0.5F);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                output, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }
    }

    private void renderLiquid(TileCondenseur te, double x, double y, double z,
                              float time, float centerX, float centerZ, int dx, int dz) {

        if (!te.isProcessing()) return;

        float percent = te.getProcessPercent() / 100.0F;
        float inset = 0.15F;

        float minX = Math.min(0, dx) + inset;
        float maxX = Math.max(0, dx) + 1 - inset;
        float minZ = Math.min(0, dz) + inset;
        float maxZ = Math.max(0, dz) + 1 - inset;

        float liquidY = 1.05F + percent * 0.85F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        float pulse = 0.4F + 0.15F * (float)Math.sin(time * 3.0);
        float r = 0.3F + percent * 0.4F;
        float g = 0.0F + percent * 0.05F;
        float b = 0.5F - percent * 0.2F;

        GL11.glColor4f(r, g, b, pulse);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(minX, liquidY, minZ);
        GL11.glVertex3f(maxX, liquidY, minZ);
        GL11.glVertex3f(maxX, liquidY, maxZ);
        GL11.glVertex3f(minX, liquidY, maxZ);
        GL11.glEnd();

        float wave = 0.03F * (float)Math.sin(time * 2.0);
        GL11.glColor4f(r + 0.1F, g, b + 0.1F, pulse * 0.5F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(minX + 0.1F, liquidY + 0.05F + wave, minZ + 0.1F);
        GL11.glVertex3f(maxX - 0.1F, liquidY + 0.05F + wave, minZ + 0.1F);
        GL11.glVertex3f(maxX - 0.1F, liquidY + 0.05F + wave, maxZ - 0.1F);
        GL11.glVertex3f(minX + 0.1F, liquidY + 0.05F + wave, maxZ - 0.1F);
        GL11.glEnd();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }
}
