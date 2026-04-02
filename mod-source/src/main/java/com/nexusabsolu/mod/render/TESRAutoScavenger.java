package com.nexusabsolu.mod.render;

import com.nexusabsolu.mod.init.ModItems;
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

    private static final float TWO_PI = (float)(Math.PI * 2.0);

    @Override
    public void render(TileAutoScavenger te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {

        float time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20.0F;
        boolean proc = te.isProcessing();
        EnumFacing facing = te.getFacing();

        // Full brightness
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);

        renderPickaxe(x, y, z, time, proc, facing);
        if (proc) {
            renderSparks(x, y, z, time, facing);
        }
        renderStatusLight(x, y, z, proc, facing);
    }

    // ==========================================================
    //  PICKAXE (swinging animation when processing)
    // ==========================================================

    private void renderPickaxe(double x, double y, double z,
                               float time, boolean proc, EnumFacing facing) {
        ItemStack pickaxe = new ItemStack(ModItems.PIOCHE_RENFORCEE);
        if (pickaxe.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.45, z + 0.5);

        // Rotate to face the block's front direction
        float yaw = getFacingYaw(facing);
        GlStateManager.rotate(yaw, 0, 1, 0);

        // Move pickaxe slightly toward front
        GlStateManager.translate(0, 0, -0.15);

        if (proc) {
            // Swing animation: oscillate around the mining angle
            float swingAngle = 25.0F * (float) Math.sin(time * 8.0);
            GlStateManager.rotate(-45.0F + swingAngle, 0, 0, 1);
            // Slight forward thrust on each swing
            float thrust = 0.03F * (float) Math.sin(time * 8.0);
            GlStateManager.translate(0, 0, thrust);
        } else {
            // Idle: pickaxe rests at an angle
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
    //  SPARKS (particles flying off the front when mining)
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

        // Direction offset toward front face
        float fx = facing.getFrontOffsetX() * 0.4F;
        float fz = facing.getFrontOffsetZ() * 0.4F;

        // 6 sparks at different phases
        for (int i = 0; i < 6; i++) {
            float phase = (time * 3.0F + i * 1.1F) % 1.0F;
            float sparkAlpha = 1.0F - phase;
            if (sparkAlpha <= 0) continue;

            // Spark flies outward + up from impact point
            float spreadX = (float) Math.sin(i * 2.3 + time * 5.0) * 0.2F * phase;
            float spreadZ = (float) Math.cos(i * 3.1 + time * 4.0) * 0.2F * phase;
            float sparkY = phase * 0.3F - 0.1F;
            float sx = fx + spreadX;
            float sz = fz + spreadZ;
            float size = 0.02F * (1.0F - phase);

            // Orange-yellow sparks
            float sr = 1.0F;
            float sg = 0.6F + 0.4F * (1.0F - phase);
            float sb = 0.1F;

            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(sx - size, sparkY - size, sz).color(sr, sg, sb, sparkAlpha * 0.8F).endVertex();
            buf.pos(sx + size, sparkY - size, sz).color(sr, sg, sb, sparkAlpha * 0.8F).endVertex();
            buf.pos(sx + size, sparkY + size, sz).color(sr, sg, sb, sparkAlpha * 0.8F).endVertex();
            buf.pos(sx - size, sparkY + size, sz).color(sr, sg, sb, sparkAlpha * 0.8F).endVertex();
            tess.draw();
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    // ==========================================================
    //  STATUS LIGHT (small LED on the front face)
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

        // Position the LED on the front face
        float offset = 0.501F;
        float fx = facing.getFrontOffsetX() * offset;
        float fz = facing.getFrontOffsetZ() * offset;
        float ledY = 0.35F; // near top of block
        float ledSize = 0.03F;

        float lr = proc ? 0.0F : 0.8F;
        float lg = proc ? 0.9F : 0.1F;

        // The LED is a small square on the front face
        if (facing.getAxis() == EnumFacing.Axis.Z) {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(-ledSize + fx, ledY - ledSize, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos( ledSize + fx, ledY - ledSize, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos( ledSize + fx, ledY + ledSize, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(-ledSize + fx, ledY + ledSize, fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            tess.draw();
        } else {
            buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            buf.pos(fx, ledY - ledSize, -ledSize + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY - ledSize,  ledSize + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY + ledSize,  ledSize + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            buf.pos(fx, ledY + ledSize, -ledSize + fz).color(lr, lg, 0.0F, 1.0F).endVertex();
            tess.draw();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    // ==========================================================
    //  UTIL
    // ==========================================================

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
