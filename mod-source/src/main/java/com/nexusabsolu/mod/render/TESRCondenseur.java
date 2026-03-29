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
        float cx = 0.5F + dx * 0.5F;
        float cz = 0.5F + dz * 0.5F;
        renderArms(te, x, y, z, time, cx, cz, dx, dz);
        renderItems(te, x, y, z, time, cx, cz);
        renderFluid(te, x, y, z, time, dx, dz);
    }

    private void renderArms(TileCondenseur te, double x, double y, double z,
                            float time, float cx, float cz, int dx, int dz) {
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();

        // Top layer: glass above (0,0), (dx,0), (0,dz). WALL above (dx,dz).
        // Bottom layer: master at (0,0), blocks at (dx,0), (0,dz), (dx,dz).
        //
        // Arms must go through GLASS, not wall. So only blocks with glass above:
        //   (0,0) = master (the hole) - no arm here
        //   (dx,0) = block with glass above - ARM 1 + ARM 3 (side by side)
        //   (0,dz) = block with glass above - ARM 2
        //   (dx,dz) = block with WALL above - NO ARM (blocked by wall)

        float armY = 1.05F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        // Arm 1 on block (dx, 0) - compacts (slots 0,1) - offset toward master on Z
        float a1x = dx + 0.5F;
        float a1z = 0.3F;
        boolean has1 = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        float swing1 = processing ? 0 : (float)Math.sin(time * 0.6) * 5.0F;
        float reach1 = has1 ? (processing ? 0.55F + proc * 0.2F : 0.5F) : 0.3F;
        drawArm(a1x, armY, a1z, cx, cz, swing1, reach1, time, has1 ? 1.0F : 0.5F);

        // Arm 2 on block (0, dz) - catalyst (slot 3)
        float a2x = 0.3F;
        float a2z = dz + 0.5F;
        boolean has2 = !te.getStackInSlot(3).isEmpty();
        float swing2 = processing ? 0 : (float)Math.sin(time * 0.6 + 2.1) * 5.0F;
        float reach2 = has2 ? (processing ? 0.55F + proc * 0.2F : 0.5F) : 0.3F;
        drawArm(a2x, armY, a2z, cx, cz, swing2, reach2, time, has2 ? 1.0F : 0.5F);

        // Arm 3 ALSO on block (dx, 0) - key (slot 2) - offset away from master on Z
        float a3x = dx + 0.5F;
        float a3z = 0.7F;
        boolean has3 = !te.getStackInSlot(2).isEmpty();
        float swing3 = processing ? 0 : (float)Math.sin(time * 0.6 + 4.2) * 5.0F;
        float reach3 = has3 ? (processing ? 0.55F + proc * 0.2F : 0.5F) : 0.3F;
        drawArm(a3x, armY, a3z, cx, cz, swing3, reach3, time, has3 ? 1.0F : 0.5F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawArm(float ax, float ay, float az,
                         float tx, float tz,
                         float swing, float reach, float time, float brightness) {
        float deltaX = tx - ax;
        float deltaZ = tz - az;
        float yaw = (float)Math.toDegrees(Math.atan2(deltaX, deltaZ));

        float w = 0.06F, h = 0.06F;
        float s1 = reach * 0.55F, s2 = reach * 0.5F;
        float bR=0.22F*brightness, bG=0.22F*brightness, bB=0.28F*brightness;
        float jR=0.4F*brightness, jG=0.15F*brightness, jB=0.5F*brightness;
        float cR=0.45F*brightness, cG=0.15F*brightness, cB=0.55F*brightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(ax, ay, az);

        // Base plate on top of block
        drawBox(-0.09F, -0.05F, -0.09F, 0.09F, 0.05F, 0.09F,
                0.18F*brightness, 0.18F*brightness, 0.22F*brightness, 1.0F);

        // Vertical piston going up into glass
        drawBox(-0.04F, 0.05F, -0.04F, 0.04F, 0.30F, 0.04F,
                bR*0.8F, bG*0.8F, bB*0.8F, 1.0F);

        // Shoulder at top
        GlStateManager.translate(0, 0.30F, 0);
        drawBox(-0.07F, -0.03F, -0.07F, 0.07F, 0.03F, 0.07F, jR, jG, jB, 1.0F);

        // Face center + swing
        GlStateManager.rotate(-yaw + swing, 0, 1, 0);

        // Upper arm
        drawBox(-w, -h, 0, w, h, s1, bR, bG, bB, 1.0F);

        // Elbow
        GlStateManager.translate(0, 0, s1);
        drawBox(-0.065F, -0.065F, -0.025F, 0.065F, 0.065F, 0.025F, jR, jG, jB, 1.0F);
        float bend = 10.0F + 5.0F * (float)Math.sin(time * 1.0);
        GlStateManager.rotate(bend, 1, 0, 0);

        // Forearm
        drawBox(-w*0.8F, -h*0.8F, 0, w*0.8F, h*0.8F, s2,
                bR*0.9F, bG*0.9F, bB*0.9F, 1.0F);

        // Gripper
        GlStateManager.translate(0, 0, s2);
        float co = 0.04F + 0.015F * (float)Math.sin(time * 2.0);
        drawBox(-co-0.02F, -0.025F, 0, -co+0.005F, 0.025F, 0.06F, cR, cG, cB, 1.0F);
        drawBox(co-0.005F, -0.025F, 0, co+0.02F, 0.025F, 0.06F, cR, cG, cB, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        r=Math.min(1,r); g=Math.min(1,g); b=Math.min(1,b);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(x1,y2,z1).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x2,y2,z1).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x2,y2,z2).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x1,y2,z2).color(r*1.1F,g*1.1F,b*1.1F,a).endVertex();
        buf.pos(x1,y1,z2).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x2,y1,z2).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x2,y1,z1).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x1,y1,z1).color(r*0.6F,g*0.6F,b*0.6F,a).endVertex();
        buf.pos(x1,y1,z1).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x2,y1,z1).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x2,y2,z1).color(r,g,b,a).endVertex();
        buf.pos(x1,y2,z1).color(r,g,b,a).endVertex();
        buf.pos(x2,y1,z2).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x1,y1,z2).color(r*0.8F,g*0.8F,b*0.8F,a).endVertex();
        buf.pos(x1,y2,z2).color(r,g,b,a).endVertex();
        buf.pos(x2,y2,z2).color(r,g,b,a).endVertex();
        buf.pos(x1,y1,z2).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x1,y1,z1).color(r*0.7F,g*0.7F,b*0.7F,a).endVertex();
        buf.pos(x1,y2,z1).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
        buf.pos(x1,y2,z2).color(r*0.9F,g*0.9F,b*0.9F,a).endVertex();
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
        float[][] off = {{-0.25F,-0.25F},{0.25F,-0.25F},{-0.25F,0.25F},{0.25F,0.25F}};
        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            float ox = off[i][0], oz = off[i][1];
            if (processing) { ox *= (1-proc*0.95F); oz *= (1-proc*0.95F); }
            float bob = (float)Math.sin(time*1.5+i*1.5)*0.03F;
            float sink = processing ? -proc * 0.7F : 0;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x+cx+ox, y+baseY+bob+sink, z+cz+oz);
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
        float i = 0.12F;
        float x0=Math.min(0,dx)+i, x1=Math.max(0,dx)+1-i;
        float z0=Math.min(0,dz)+i, z1=Math.max(0,dz)+1-i;
        float fY=1.02F, tY=fY+p*0.9F;
        float w1=0.02F*(float)Math.sin(time*2.5), w2=0.02F*(float)Math.sin(time*3.1+1.5);
        float r=0.4F+p*0.5F, g=0.02F+p*0.08F, b=0.6F-p*0.35F;
        float a=0.5F+0.1F*(float)Math.sin(time*2), sa=a*0.7F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableLighting(); GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D(); GlStateManager.depthMask(false);
        Tessellator ts = Tessellator.getInstance(); BufferBuilder bf = ts.getBuffer();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x0,tY+w1,z0).color(r,g,b,a).endVertex(); bf.pos(x1,tY+w2,z0).color(r,g,b,a).endVertex();
        bf.pos(x1,tY+w1,z1).color(r,g,b,a).endVertex(); bf.pos(x0,tY+w2,z1).color(r,g,b,a).endVertex(); ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x0,fY,z0).color(r*.7F,g*.7F,b*.7F,sa).endVertex(); bf.pos(x1,fY,z0).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(x1,tY+w2,z0).color(r,g,b,sa).endVertex(); bf.pos(x0,tY+w1,z0).color(r,g,b,sa).endVertex(); ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x1,fY,z1).color(r*.7F,g*.7F,b*.7F,sa).endVertex(); bf.pos(x0,fY,z1).color(r*.7F,g*.7F,b*.7F,sa).endVertex();
        bf.pos(x0,tY+w2,z1).color(r,g,b,sa).endVertex(); bf.pos(x1,tY+w1,z1).color(r,g,b,sa).endVertex(); ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x0,fY,z1).color(r*.6F,g*.6F,b*.6F,sa).endVertex(); bf.pos(x0,fY,z0).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(x0,tY+w1,z0).color(r,g,b,sa).endVertex(); bf.pos(x0,tY+w2,z1).color(r,g,b,sa).endVertex(); ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x1,fY,z0).color(r*.6F,g*.6F,b*.6F,sa).endVertex(); bf.pos(x1,fY,z1).color(r*.6F,g*.6F,b*.6F,sa).endVertex();
        bf.pos(x1,tY+w1,z1).color(r,g,b,sa).endVertex(); bf.pos(x1,tY+w2,z0).color(r,g,b,sa).endVertex(); ts.draw();
        bf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bf.pos(x0,fY,z0).color(r*.5F,g*.5F,b*.5F,sa).endVertex(); bf.pos(x0,fY,z1).color(r*.5F,g*.5F,b*.5F,sa).endVertex();
        bf.pos(x1,fY,z1).color(r*.5F,g*.5F,b*.5F,sa).endVertex(); bf.pos(x1,fY,z0).color(r*.5F,g*.5F,b*.5F,sa).endVertex(); ts.draw();
        GlStateManager.depthMask(true); GlStateManager.enableTexture2D();
        GlStateManager.disableBlend(); GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}
