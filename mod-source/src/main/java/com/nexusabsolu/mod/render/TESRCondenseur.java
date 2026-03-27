package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class TESRCondenseur extends TileEntitySpecialRenderer<TileCondenseur> {

    @Override
    public void render(TileCondenseur te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        if (!te.isStructureValid()) return;

        float time = (float)(System.currentTimeMillis() % 100000L) / 1000.0F;

        renderItems(te, x, y, z, time, partialTicks);
        renderLiquid(te, x, y, z, time, partialTicks);
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float partialTicks) {

        // Item positions inside the glass area (top layer, center of 2x2)
        // Offset from master block position to center of multibloc
        float centerX = 1.0F;
        float centerZ = 1.0F;
        float baseY = 1.4F; // inside the glass layer

        float[][] offsets = {
            {-0.25F, 0.0F, -0.25F},  // slot 0
            { 0.25F, 0.0F, -0.25F},  // slot 1
            {-0.25F, 0.0F,  0.25F},  // slot 2
            { 0.25F, 0.0F,  0.25F},  // slot 3
        };

        float processPercent = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            float ox = offsets[i][0];
            float oy = offsets[i][1];
            float oz = offsets[i][2];

            // When processing, items drift toward center
            if (processing) {
                float pull = processPercent * 0.8F;
                ox *= (1.0F - pull);
                oz *= (1.0F - pull);
                // Rise slightly as they converge
                oy += processPercent * 0.2F;
            }

            // Gentle hover bob
            float bob = (float)Math.sin(time * 1.5 + i * 1.5) * 0.05F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(
                x + centerX + ox,
                y + baseY + oy + bob,
                z + centerZ + oz
            );

            // Spin slowly, each item offset
            float angle = (time * 40.0F + i * 90.0F) % 360.0F;
            GlStateManager.rotate(angle, 0, 1, 0);

            // Items shrink as they merge during processing
            float scale = 0.35F;
            if (processing) {
                scale *= (1.0F - processPercent * 0.6F);
            }
            GlStateManager.scale(scale, scale, scale);

            // Render the item
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();

            GlStateManager.popMatrix();
        }

        // Render output item (slot 4) if present - larger, glowing
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
                              float time, float partialTicks) {

        if (!te.isProcessing()) return;

        float percent = te.getProcessPercent() / 100.0F;

        // Liquid rises from bottom of glass area during processing
        // Glass area is y+1 to y+2, inset a bit
        float liquidY = 1.05F + percent * 0.85F;
        float inset = 0.1F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        // Pulsing alpha
        float pulse = 0.4F + 0.15F * (float)Math.sin(time * 3.0);
        // Color: purple-to-red gradient as processing progresses
        float r = 0.3F + percent * 0.4F;
        float g = 0.0F + percent * 0.05F;
        float b = 0.5F - percent * 0.2F;

        GL11.glColor4f(r, g, b, pulse);

        // Draw liquid quad (flat plane covering the 2x2 area)
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(inset, liquidY, inset);
        GL11.glVertex3f(2.0F - inset, liquidY, inset);
        GL11.glVertex3f(2.0F - inset, liquidY, 2.0F - inset);
        GL11.glVertex3f(inset, liquidY, 2.0F - inset);
        GL11.glEnd();

        // Second layer slightly above for depth effect
        float wave = 0.03F * (float)Math.sin(time * 2.0);
        GL11.glColor4f(r + 0.1F, g, b + 0.1F, pulse * 0.5F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(inset + 0.1F, liquidY + 0.05F + wave, inset + 0.1F);
        GL11.glVertex3f(2.0F - inset - 0.1F, liquidY + 0.05F + wave, inset + 0.1F);
        GL11.glVertex3f(2.0F - inset - 0.1F, liquidY + 0.05F + wave, 2.0F - inset - 0.1F);
        GL11.glVertex3f(inset + 0.1F, liquidY + 0.05F + wave, 2.0F - inset - 0.1F);
        GL11.glEnd();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }
}
