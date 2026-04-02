package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.tiles.TileAutoScavenger;
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
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

public class TESRAutoScavenger extends TileEntitySpecialRenderer<TileAutoScavenger> {

    @Override
    public void render(TileAutoScavenger te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        float time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20.0F;
        boolean proc = te.isProcessing();
        EnumFacing facing = te.getFacing();

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        // Render the actual pickaxe from the slot
        ItemStack pickaxe = te.getPickaxeStack();
        if (!pickaxe.isEmpty()) {
            renderPickaxe(x, y, z, time, proc, facing, pickaxe);
        }

        if (proc) {
            renderSparks(x, y, z, time, facing);
        }
        renderStatusLight(x, y, z, proc, facing);
    }

    // ==========================================================
    //  PICKAXE from the actual slot
    // ==========================================================

    private void renderPickaxe(double x, double y, double z,
                               float time, boolean proc, EnumFacing facing,
                               ItemStack pickaxe) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.45, z + 0.5);

        // Rotate to face front
        GlStateManager.rotate(getFacingYaw(facing), 0, 1, 0);

        // Move toward front
        GlStateManager.translate(0, 0, -0.15);

        if (proc) {
            // Swing: oscillate mining angle
            float swing = 25.0F * (float) Math.sin(time * 8.0);
            GlStateManager.rotate(-45.0F + swing, 0, 0, 1);
            // Forward thrust on each swing
            float thrust = 0.04F * (float) Math.sin(time * 8.0);
            GlStateManager.translate(0, 0, thrust);
        } else {
            // Idle: resting angle
            GlStateManager.rotate(-30.0F, 0, 0, 1);
        }

        GlStateManager.scale(0.5F, 0.5F, 0.5F);

        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(
            pickaxe, ItemCameraTransforms.TransformType.FIXED);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.popMatrix();
    }

    // ==========================================================
    //  SPARKS flying off the front during mining
    // ==========================================================

    private void renderSparks(double x, double y, double z,
                              float time, EnumFacing facing) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.depthMask(false);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float fx = facing.getFrontOffsetX() * 0.45F;
        float fz = facing.getFrontOffsetZ() * 0.45F;

        for (int i = 0; i < 6; i++) {
            float phase = (time * 3.0F + i * 1.1F) % 1.0F;
            float sparkAlpha = (1.0F - phase) * 0.9F;
            if (sparkAlpha <= 0.05F) continue;

            float spreadX = (float) Math.sin(i * 2.3 + time * 5.0) * 0.2F * phase;
            float spreadZ = (float) Math.cos(i * 3.1 + time * 4.0) * 0.2F * phase;
            float sparkY = phase * 0.35F - 0.1F;
            float sx = fx + spreadX;
            float sz = fz + spreadZ;
            float size = 0.025F * (1.0F - phase);

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(sx - size, sparkY - size, sz).color(1.0F, 0.65F, 0.1F, sparkAlpha).endVertex();
            buf.pos(sx + size, sparkY - size, sz).color(1.0F, 0.65F, 0.1F, sparkAlpha).endVertex();
            buf.pos(sx + size, sparkY + size, sz).color(1.0F, 0.85F, 0.3F, sparkAlpha * 0.5F).endVertex();
            buf.pos(sx - size, sparkY + size, sz).color(1.0F, 0.85F, 0.3F, sparkAlpha * 0.5F).endVertex();
            tess.draw();
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    // ==========================================================
    //  STATUS LED on front face
    // ==========================================================

    private void renderStatusLight(double x, double y, double z,
                                   boolean proc, EnumFacing facing) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        float offset = 0.501F;
        float fx = facing.getFrontOffsetX() * offset;
        float fz = facing.getFrontOffsetZ() * offset;
        float ledY = 0.35F;
        float s = 0.03F;
        float lr = proc ? 0.0F : 0.8F;
        float lg = proc ? 0.9F : 0.1F;

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        if (facing.getAxis() == EnumFacing.Axis.Z) {
            buf.pos(-s + fx, ledY - s, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos( s + fx, ledY - s, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos( s + fx, ledY + s, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(-s + fx, ledY + s, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
        } else {
            buf.pos(fx, ledY - s, -s + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY - s,  s + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY + s,  s + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY + s, -s + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
        }
        tess.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private float getFacingYaw(EnumFacing facing) {
        switch (facing) {
            case SOUTH: return 0.0F;
            case WEST:  return 90.0F;
            case NORTH: return 180.0F;
            case EAST:  return 270.0F;
            default:    return 0.0F;
        }
    }
}
