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

        // 3 anchors at outer corners of the 3 glass blocks
        // Glass blocks: above master(0,0), above (dx,0), above (0,dz)
        // Wall block: above (dx,dz) -- no arm here
        float sdx = dx > 0 ? 1.0F : -1.0F;
        float sdz = dz > 0 ? 1.0F : -1.0F;

        // Arm 1: above master at (0,0), anchor at outer corner
        float a1x = 0.5F - sdx * 0.35F;
        float a1z = 0.5F - sdz * 0.35F;

        // Arm 2: above (dx,0), anchor at far-X corner
        float a2x = dx + 0.5F + sdx * 0.35F;
        float a2z = 0.5F - sdz * 0.35F;

        // Arm 3: above (0,dz), anchor at far-Z corner
        float a3x = 0.5F - sdx * 0.35F;
        float a3z = dz + 0.5F + sdz * 0.35F;

        float armY = 1.35F; // In the glass layer

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Arm 1 - compacts handler (slots 0,1)
        boolean has1 = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        float angle1 = getAngle(a1x, a1z, cx, cz);
        float reach1 = has1 ? (processing ? 0.4F + proc * 0.3F : 0.4F) : 0.25F;
        float swing1 = processing ? 0 : (float)Math.sin(time * 0.7) * 8.0F;
        drawHorizontalArm(a1x, armY, a1z, angle1 + swing1, reach1, time, has1 ? 1.0F : 0.5F);

        // Arm 2 - key handler (slot 2)
        boolean has2 = !te.getStackInSlot(2).isEmpty();
        float angle2 = getAngle(a2x, a2z, cx, cz);
        float reach2 = has2 ? (processing ? 0.4F + proc * 0.3F : 0.4F) : 0.25F;
        float swing2 = processing ? 0 : (float)Math.sin(time * 0.7 + 2.0) * 8.0F;
        drawHorizontalArm(a2x, armY, a2z, angle2 + swing2, reach2, time, has2 ? 1.0F : 0.5F);

        // Arm 3 - catalyst handler (slot 3)
        boolean has3 = !te.getStackInSlot(3).isEmpty();
        float angle3 = getAngle(a3x, a3z, cx, cz);
        float reach3 = has3 ? (processing ? 0.4F + proc * 0.3F : 0.4F) : 0.25F;
        float swing3 = processing ? 0 : (float)Math.sin(time * 0.7 + 4.0) * 8.0F;
        drawHorizontalArm(a3x, armY, a3z, angle3 + swing3, reach3, time, has3 ? 1.0F : 0.5F);

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private float getAngle(float fromX, float fromZ, float toX, float toZ) {
        float deltaX = toX - fromX;
        float deltaZ = toZ - fromZ;
        return (float)Math.toDegrees(Math.atan2(deltaX, deltaZ));
    }

    private void drawHorizontalArm(float px, float py, float pz,
                                    float yawAngle, float reach, float time, float brightness) {
        float w = 0.035F;
        float h = 0.04F;
        float seg1Len = reach * 0.55F;
        float seg2Len = reach * 0.45F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(px, py, pz);

        // Base mount (small dark cube on the floor of the glass area)
        drawBox(-0.05F, -0.05F, -0.05F, 0.05F, 0.05F, 0.05F,
                0.15F * brightness, 0.15F * brightness, 0.2F * brightness, 1.0F);

        // Rotate to face center
        GlStateManager.rotate(-yawAngle, 0, 1, 0);

        // Segment 1: upper arm going horizontal toward center
        drawBox(-w, -h, 0, w, h, seg1Len,
                0.2F * brightness, 0.2F * brightness, 0.28F * brightness, 1.0F);

        // Elbow joint
        GlStateManager.translate(0, 0, seg1Len);
        drawBox(-0.04F, -0.04F, -0.02F, 0.04F, 0.04F, 0.02F,
                0.35F * brightness, 0.15F * brightness, 0.45F * brightness, 1.0F);

        // Segment 2: forearm
        float elbowBend = 10.0F + 5.0F * (float)Math.sin(time * 1.2);
        GlStateManager.rotate(elbowBend, 1, 0, 0);
        drawBox(-w * 0.7F, -h * 0.7F, 0, w * 0.7F, h * 0.7F, seg2Len,
                0.18F * brightness, 0.18F * brightness, 0.24F * brightness, 1.0F);

        // Gripper claw at end
        GlStateManager.translate(0, 0, seg2Len);
        float clawSpread = 0.025F + 0.008F * (float)Math.sin(time * 2.5);
        // Left claw
        drawBox(-clawSpread - 0.012F, -0.015F, 0, -clawSpread, 0.015F, 0.04F,
                0.4F * brightness, 0.12F * brightness, 0.5F * brightness, 1.0F);
        // Right claw
        drawBox(clawSpread, -0.015F, 0, clawSpread + 0.012F, 0.015F, 0.04F,
                0.4F * brightness, 0.12F * brightness, 0.5F * brightness, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        float lr = Math.min(1.0F, r), lg = Math.min(1.0F, g), lb = Math.min(1.0F, b);

        // Top
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1,y2,z1).color(lr*1.1F,lg*1.1F,lb*1.1F,a).endVertex();
        buf.pos(x2,y2,z1).color(lr*1.1F,lg*1.1F,lb*1.1F,a).endVertex();
        buf.pos(x2,y2,z2).color(lr*1.1F,lg*1.1F,lb*1.1F,a).endVertex();
        buf.pos(x1,y2,z2).color(lr*1.1F,lg*1.1F,lb*1.1F,a).endVertex();
        tess.draw();
        // Bottom
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1,y1,z2).color(lr*0.6F,lg*0.6F,lb*0.6F,a).endVertex();
        buf.pos(x2,y1,z2).color(lr*0.6F,lg*0.6F,lb*0.6F,a).endVertex();
        buf.pos(x2,y1,z1).color(lr*0.6F,lg*0.6F,lb*0.6F,a).endVertex();
        buf.pos(x1,y1,z1).color(lr*0.6F,lg*0.6F,lb*0.6F,a).endVertex();
        tess.draw();
        // Front (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1,y1,z1).color(lr*0.8F,lg*0.8F,lb*0.8F,a).endVertex();
        buf.pos(x2,y1,z1).color(lr*0.8F,lg*0.8F,lb*0.8F,a).endVertex();
        buf.pos(x2,y2,z1).color(lr,lg,lb,a).endVertex();
        buf.pos(x1,y2,z1).color(lr,lg,lb,a).endVertex();
        tess.draw();
        // Back (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x2,y1,z2).color(lr*0.8F,lg*0.8F,lb*0.8F,a).endVertex();
        buf.pos(x1,y1,z2).color(lr*0.8F,lg*0.8F,lb*0.8F,a).endVertex();
        buf.pos(x1,y2,z2).color(lr,lg,lb,a).endVertex();
        buf.pos(x2,y2,z2).color(lr,lg,lb,a).endVertex();
        tess.draw();
        // Left (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1,y1,z2).color(lr*0.7F,lg*0.7F,lb*0.7F,a).endVertex();
        buf.pos(x1,y1,z1).color(lr*0.7F,lg*0.7F,lb*0.7F,a).endVertex();
        buf.pos(x1,y2,z1).color(lr*0.9F,lg*0.9F,lb*0.9F,a).endVertex();
        buf.pos(x1,y2,z2).color(lr*0.9F,lg*0.9F,lb*0.9F,a).endVertex();
        tess.draw();
        // Right (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x2,y1,z1).color(lr*0.7F,lg*0.7F,lb*0.7F,a).endVertex();
        buf.pos(x2,y1,z2).color(lr*0.7F,lg*0.7F,lb*0.7F,a).endVertex();
        buf.pos(x2,y2,z2).color(lr*0.9F,lg*0.9F,lb*0.9F,a).endVertex();
        buf.pos(x2,y2,z1).color(lr*0.9F,lg*0.9F,lb*0.9F,a).endVertex();
        tess.draw();
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        float baseY = 1.4F;
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();
        float[][] offsets = {{-0.2F,-0.2F},{0.2F,-0.2F},{-0.2F,0.2F},{0.2F,0.2F}};

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
            float s = 0.28F; if (processing) s *= (1-proc*0.5F);
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
