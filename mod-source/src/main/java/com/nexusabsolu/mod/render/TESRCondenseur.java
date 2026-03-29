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
        // Center of the 2x2 footprint
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

        // The multiblock top layer (y+1):
        //   Glass above master(0,0), Glass above (dx,0), Wall above (dx,dz), Glass above (0,dz)
        // Wait - need to check which is wall. Wall = position 7 = the (dx,dz) diagonal
        // So glass blocks are above: (0,0), (dx,0), (0,dz)
        // Wall block is above: (dx,dz)
        //
        // For dx=1, dz=1:
        //   (0,1)=glass  (1,1)=WALL
        //   (0,0)=glass  (1,0)=glass
        //
        // Bras 1: on glass above (0,0) -> anchored at outer edge away from center
        //         For dx=1,dz=1: anchor at LEFT edge (x=0), middle of Z (z=0.5)
        // Bras 2: on glass above (dx,0) -> anchored at outer edge
        //         For dx=1,dz=1: anchor at BOTTOM edge (z=0), middle of X (x=dx+0.5)
        // Bras 3: on glass above (0,dz) -> anchored at outer edge
        //         For dx=1,dz=1: anchor at TOP edge (z=dz+1), middle of X... 
        //         Wait, looking at schema again:
        //         Top-left = wall, Top-right = glass with bras3 on RIGHT edge

        // Let me reconsider based on schema:
        // Schema shows (viewed from above):
        //   [WALL]     [Glass3 + bras3 on RIGHT edge + key]
        //   [Glass1 + bras1 on LEFT edge + compacts]  [Glass2 + bras2 on BOTTOM edge + catalyst]

        // Glass above (0,0): bras1 anchored at the edge AWAY from center on X axis
        float sdx = dx > 0 ? 1.0F : -1.0F;
        float sdz = dz > 0 ? 1.0F : -1.0F;

        // Bras 1: glass above master (0,y+1,0)
        // Anchor at outer-X edge, middle Z of that block
        float a1x = 0.5F - sdx * 0.5F;  // at x=0 edge (for dx=1)
        float a1z = 0.5F;                // middle of the block
        
        // Bras 2: glass above (dx,y+1,0)
        // Anchor at outer-Z edge, middle X of that block
        float a2x = dx + 0.5F;           // middle of the block
        float a2z = 0.5F - sdz * 0.5F;   // at z=0 edge (for dz=1)
        
        // Bras 3: glass above (0,y+1,dz)
        // Anchor at outer edge (the side NOT facing center)
        // For dx=1,dz=1: this glass is at (0,y+1,1), outer edge is at x=0 or z=2
        // Looking at schema: bras3 is on the RIGHT side of top-right glass
        // Top-right glass = above (dx, y+1, dz) but that's the wall!
        // Actually re-reading: wall is top-LEFT, glass is top-RIGHT
        // So glass3 is above (dx, y+1, dz)... no wait, wall is at (dx,dz)
        
        // Let me just think about it differently:
        // 3 glass positions: above (0,0), above (dx,0), above (0,dz)
        // Wall position: above (dx,dz)
        //
        // For the schema to match (wall = top-left for dx=1,dz=1):
        // Actually the schema shows wall at top-left, so that matches (0,dz) being wall
        // No... position 7 (wall) is at (dx,dz).
        // For dx=1,dz=1: wall at (1,1) = top-right in my coordinates
        // But in Alexis schema, wall is top-left
        // The schema might just be rotated. Let me not worry about which specific 
        // corner and just place arms at outer edges of the 3 glass blocks.

        // Glass block 1 above (0,0): outer edge is the side away from (dx,dz)
        // Arm on the edge at x = (0.5 - sdx*0.5) = 0 for dx=1
        // middle z = 0.5
        
        // Glass block 2 above (dx,0): outer edge on Z- side
        // Arm at z = (0.5 - sdz*0.5) = 0 for dz=1
        // middle x = dx + 0.5
        
        // Glass block 3 above (0,dz): outer edge on X- side
        // Arm at x = (0.5 - sdx*0.5) = 0 for dx=1
        // middle z = dz + 0.5

        float armY = 1.35F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        // Arm 1 - compacts (slots 0,1) - on glass above (0,0), outer X edge
        boolean has1 = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        float swing1 = processing ? 0 : (float)Math.sin(time * 0.6) * 5.0F;
        float reach1 = has1 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.3F;
        drawHorizontalArm(a1x, armY, a1z, cx, cz, swing1, reach1, time, has1 ? 1.0F : 0.5F);

        // Arm 2 - catalyst (slot 3) - on glass above (dx,0), outer Z edge
        boolean has2 = !te.getStackInSlot(3).isEmpty();
        float swing2 = processing ? 0 : (float)Math.sin(time * 0.6 + 2.1) * 5.0F;
        float reach2 = has2 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.3F;
        drawHorizontalArm(a2x, armY, a2z, cx, cz, swing2, reach2, time, has2 ? 1.0F : 0.5F);

        // Arm 3 - key (slot 2) - on glass above (0,dz), outer X edge
        boolean has3 = !te.getStackInSlot(2).isEmpty();
        float swing3 = processing ? 0 : (float)Math.sin(time * 0.6 + 4.2) * 5.0F;
        float reach3 = has3 ? (processing ? 0.5F + proc * 0.25F : 0.5F) : 0.3F;
        float a3x = 0.5F - sdx * 0.5F;
        float a3z = dz + 0.5F;
        drawHorizontalArm(a3x, armY, a3z, cx, cz, swing3, reach3, time, has3 ? 1.0F : 0.5F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawHorizontalArm(float anchorX, float anchorY, float anchorZ,
                                    float targetX, float targetZ,
                                    float swingOffset, float reach, float time, float brightness) {
        float deltaX = targetX - anchorX;
        float deltaZ = targetZ - anchorZ;
        float yawAngle = (float)Math.toDegrees(Math.atan2(deltaX, deltaZ));

        float armW = 0.06F;
        float armH = 0.07F;
        float seg1 = reach * 0.55F;
        float seg2 = reach * 0.5F;

        float bR = 0.22F * brightness, bG = 0.22F * brightness, bB = 0.28F * brightness;
        float jR = 0.4F * brightness, jG = 0.15F * brightness, jB = 0.5F * brightness;
        float cR = 0.45F * brightness, cG = 0.15F * brightness, cB = 0.55F * brightness;

        GlStateManager.pushMatrix();
        GlStateManager.translate(anchorX, anchorY, anchorZ);

        // Base mount
        drawBox(-0.08F, -0.08F, -0.08F, 0.08F, 0.08F, 0.08F,
                0.18F * brightness, 0.18F * brightness, 0.22F * brightness, 1.0F);

        // Face target + swing
        GlStateManager.rotate(-yawAngle + swingOffset, 0, 1, 0);

        // Upper arm
        drawBox(-armW, -armH, 0, armW, armH, seg1, bR, bG, bB, 1.0F);

        // Elbow
        GlStateManager.translate(0, 0, seg1);
        drawBox(-0.07F, -0.07F, -0.03F, 0.07F, 0.07F, 0.03F, jR, jG, jB, 1.0F);

        float elbowBend = 8.0F + 4.0F * (float)Math.sin(time * 1.0);
        GlStateManager.rotate(elbowBend, 1, 0, 0);

        // Forearm
        drawBox(-armW*0.8F, -armH*0.8F, 0, armW*0.8F, armH*0.8F, seg2,
                bR*0.9F, bG*0.9F, bB*0.9F, 1.0F);

        // Gripper
        GlStateManager.translate(0, 0, seg2);
        float clawOpen = 0.04F + 0.015F * (float)Math.sin(time * 2.0);
        drawBox(-clawOpen-0.02F, -0.03F, 0, -clawOpen+0.005F, 0.03F, 0.07F, cR, cG, cB, 1.0F);
        drawBox(clawOpen-0.005F, -0.03F, 0, clawOpen+0.02F, 0.03F, 0.07F, cR, cG, cB, 1.0F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        r = Math.min(1.0F, r); g = Math.min(1.0F, g); b = Math.min(1.0F, b);
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
