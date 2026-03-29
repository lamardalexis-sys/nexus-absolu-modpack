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
        float centerX = 0.5F + dx * 0.5F;
        float centerZ = 0.5F + dz * 0.5F;

        renderArms(te, x, y, z, time, centerX, centerZ, dx, dz);
        renderItems(te, x, y, z, time, centerX, centerZ);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    private void renderArms(TileCondenseur te, double x, double y, double z,
                            float time, float cx, float cz, int dx, int dz) {
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();
        float sdx = dx > 0 ? 1.0F : -1.0F;
        float sdz = dz > 0 ? 1.0F : -1.0F;

        // Arm Y position: middle of the glass layer
        float armY = 1.35F;

        // 3 arms anchored at the OUTER corner of each glass block
        // Glass 1: above master at (0, y+1, 0) -> outer corner away from center
        float a1x = 0.5F - sdx * 0.4F;
        float a1z = 0.5F - sdz * 0.4F;

        // Glass 2: above (dx, y+1, 0) -> outer corner
        float a2x = dx + 0.5F + sdx * 0.4F;
        float a2z = 0.5F - sdz * 0.4F;

        // Glass 3: above (0, y+1, dz) -> outer corner
        float a3x = 0.5F - sdx * 0.4F;
        float a3z = dz + 0.5F + sdz * 0.4F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        // Arm 1 - compacts (slots 0,1)
        boolean has1 = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        float swing1 = processing ? 0 : (float)Math.sin(time * 0.6) * 6.0F;
        float reach1 = has1 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.35F;
        drawHorizontalArm(a1x, armY, a1z, cx, cz, swing1, reach1, time, has1 ? 1.0F : 0.5F);

        // Arm 2 - key (slot 2)
        boolean has2 = !te.getStackInSlot(2).isEmpty();
        float swing2 = processing ? 0 : (float)Math.sin(time * 0.6 + 2.1) * 6.0F;
        float reach2 = has2 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.35F;
        drawHorizontalArm(a2x, armY, a2z, cx, cz, swing2, reach2, time, has2 ? 1.0F : 0.5F);

        // Arm 3 - catalyst (slot 3)
        boolean has3 = !te.getStackInSlot(3).isEmpty();
        float swing3 = processing ? 0 : (float)Math.sin(time * 0.6 + 4.2) * 6.0F;
        float reach3 = has3 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.35F;
        drawHorizontalArm(a3x, armY, a3z, cx, cz, swing3, reach3, time, has3 ? 1.0F : 0.5F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawHorizontalArm(float anchorX, float anchorY, float anchorZ,
                                    float targetX, float targetZ,
                                    float swingOffset, float reach, float time, float brightness) {
        // Calculate angle from anchor to target center
        float deltaX = targetX - anchorX;
        float deltaZ = targetZ - anchorZ;
        float yawAngle = (float)Math.toDegrees(Math.atan2(deltaX, deltaZ));

        // Arm dimensions - MUCH bigger
        float armWidth = 0.06F;
        float armHeight = 0.07F;
        float seg1 = reach * 0.55F;
        float seg2 = reach * 0.5F;

        // Colors
        float baseR = 0.22F * brightness, baseG = 0.22F * brightness, baseB = 0.28F * brightness;
        float jointR = 0.4F * brightness, jointG = 0.15F * brightness, jointB = 0.5F * brightness;
        float clawR = 0.45F * brightness, clawG = 0.15F * brightness, clawB = 0.55F * brightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(anchorX, anchorY, anchorZ);

        // Base mount - visible cube at anchor point
        drawBox(-0.08F, -0.08F, -0.08F, 0.08F, 0.08F, 0.08F,
                0.18F * brightness, 0.18F * brightness, 0.22F * brightness, 1.0F);

        // Rotate toward center + swing
        GlStateManager.rotate(-yawAngle + swingOffset, 0, 1, 0);

        // === Segment 1: upper arm ===
        drawBox(-armWidth, -armHeight, 0, armWidth, armHeight, seg1,
                baseR, baseG, baseB, 1.0F);

        // === Elbow joint ===
        GlStateManager.translate(0, 0, seg1);
        drawBox(-0.07F, -0.07F, -0.03F, 0.07F, 0.07F, 0.03F,
                jointR, jointG, jointB, 1.0F);

        // Elbow bends slightly downward
        float elbowBend = 8.0F + 4.0F * (float)Math.sin(time * 1.0);
        GlStateManager.rotate(elbowBend, 1, 0, 0);

        // === Segment 2: forearm ===
        drawBox(-armWidth * 0.8F, -armHeight * 0.8F, 0,
                armWidth * 0.8F, armHeight * 0.8F, seg2,
                baseR * 0.9F, baseG * 0.9F, baseB * 0.9F, 1.0F);

        // === Gripper at end ===
        GlStateManager.translate(0, 0, seg2);
        float clawOpen = 0.04F + 0.015F * (float)Math.sin(time * 2.0);

        // Left claw finger
        drawBox(-clawOpen - 0.02F, -0.03F, 0, -clawOpen + 0.005F, 0.03F, 0.07F,
                clawR, clawG, clawB, 1.0F);
        // Right claw finger
        drawBox(clawOpen - 0.005F, -0.03F, 0, clawOpen + 0.02F, 0.03F, 0.07F,
                clawR, clawG, clawB, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        r = Math.min(1.0F, r); g = Math.min(1.0F, g); b = Math.min(1.0F, b);

        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // Top
        buf.pos(x1,y2,z1).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x2,y2,z1).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x2,y2,z2).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x1,y2,z2).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        // Bottom
        buf.pos(x1,y1,z2).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x2,y1,z2).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x2,y1,z1).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x1,y1,z1).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        // Front (-Z)
        buf.pos(x1,y1,z1).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x2,y1,z1).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x2,y2,z1).color(r,g,b,a).endVertex();
        buf.pos(x1,y2,z1).color(r,g,b,a).endVertex();
        // Back (+Z)
        buf.pos(x2,y1,z2).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x1,y1,z2).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x1,y2,z2).color(r,g,b,a).endVertex();
        buf.pos(x2,y2,z2).color(r,g,b,a).endVertex();
        // Left (-X)
        buf.pos(x1,y1,z2).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x1,y1,z1).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x1,y2,z1).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
        buf.pos(x1,y2,z2).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
        // Right (+X)
        buf.pos(x2,y1,z1).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x2,y1,z2).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x2,y2,z2).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
        buf.pos(x2,y2,z1).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
        tess.draw();
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        float baseY = 1.35F;
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();
        float[][] offsets = {{-0.25F,-0.25F},{0.25F,-0.25F},{-0.25F,0.25F},{0.25F,0.25F}};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            float ox = offsets[i][0], oz = offsets[i][1];
            if (processing) { ox *= (1-proc*0.95F); oz *= (1-proc*0.95F); }
            float bob = (float)Math.sin(time*1.5+i*1.5)*0.03F;
            float sinkY = processing ? -proc * 0.7F : 0;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x+cx+ox, y+baseY+bob+sinkY, z+cz+oz);
            GlStateManager.rotate((time*40+i*90)%360, 0, 1, 0);
            float s = 0.3F; if (processing) s *= (1-proc*0.5F);
            GlStateManager.scale(s, s, s);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        ItemStack output = te.getStackInSlot(4);
        if (!output.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x+cx, y+baseY+0.1, z+cz);
            GlStateManager.rotate((time*25)%360, 0, 1, 0);
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(output, ItemCameraTransforms.TransformType.FIXED);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }

    private void renderFluid(TileCondenseur te, double x, double y, double z,
                             float time, int dx, int dz) {
        if (!te.isProcessing()) return;
        float p = te.getProcessPercent() / 100.0F;
        float ins = 0.12F;
        float mnX=Math.min(0,dx)+ins, mxX=Math.max(0,dx)+1-ins;
        float mnZ=Math.min(0,dz)+ins, mxZ=Math.max(0,dz)+1-ins;
        float fY=1.02F, tY=fY+p*0.9F;
        float w1=0.02F*(float)Math.sin(time*2.5), w2=0.02F*(float)Math.sin(time*3.1+1.5);
        float r=0.4F+p*0.5F, g=0.02F+p*0.08F, b=0.6F-p*0.35F;
        float a=0.5F+0.1F*(float)Math.sin(time*2), sa=a*0.7F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        Tessellator ts = Tessellator.getInstance();
        BufferBuilder bf = ts.getBuffer();

        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mnX,tY+w1,mnZ).color(r,g,b,a).endVertex();
        bf.pos(mxX,tY+w2,mnZ).color(r,g,b,a).endVertex();
        bf.pos(mxX,tY+w1,mxZ).color(r,g,b,a).endVertex();
        bf.pos(mnX,tY+w2,mxZ).color(r,g,b,a).endVertex();
        ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mnX,fY,mnZ).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(mxX,fY,mnZ).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(mxX,tY+w2,mnZ).color(r,g,b,sa).endVertex();
        bf.pos(mnX,tY+w1,mnZ).color(r,g,b,sa).endVertex();
        ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mxX,fY,mxZ).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(mnX,fY,mxZ).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(mnX,tY+w2,mxZ).color(r,g,b,sa).endVertex();
        bf.pos(mxX,tY+w1,mxZ).color(r,g,b,sa).endVertex();
        ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mnX,fY,mxZ).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(mnX,fY,mnZ).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(mnX,tY+w1,mnZ).color(r,g,b,sa).endVertex();
        bf.pos(mnX,tY+w2,mxZ).color(r,g,b,sa).endVertex();
        ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mxX,fY,mnZ).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(mxX,fY,mxZ).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(mxX,tY+w1,mxZ).color(r,g,b,sa).endVertex();
        bf.pos(mxX,tY+w2,mnZ).color(r,g,b,sa).endVertex();
        ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(mnX,fY,mnZ).color(r*.5F,g*.5F,b*.5F,sa).endVertex();
        bf.pos(mnX,fY,mxZ).color(r*.5F,g*.5F,b*.5F,sa).endVertex();
        bf.pos(mxX,fY,mxZ).color(r*.5F,g*.5F,b*.5F,sa).endVertex();
        bf.pos(mxX,fY,mnZ).color(r*.5F,g*.5F,b*.5F,sa).endVertex();
        ts.draw();

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
