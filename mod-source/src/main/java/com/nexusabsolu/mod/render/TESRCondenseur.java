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

        // Glass is above: (0,0), (dx,0), (0,dz). Wall above (dx,dz).
        // Arms on blocks (dx,0) and (0,dz) - these have glass above.
        // 2 arms on (dx,0), 1 arm on (0,dz).

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        // All arms render in the GLASS layer: y+1.0 to y+2.0
        // Arm base at y+1.15 (above the block boundary)

        // Arm 1 - compacts (slots 0,1) - on block (dx,0), left side
        boolean has1 = !te.getStackInSlot(0).isEmpty() || !te.getStackInSlot(1).isEmpty();
        float swing1 = processing ? 0 : (float)Math.sin(time * 0.5) * 8.0F;
        drawArm(dx + 0.3F, 1.15F, 0.5F, cx, cz, swing1, time, has1, processing, proc);

        // Arm 2 - catalyst (slot 3) - on block (0,dz)
        boolean has2 = !te.getStackInSlot(3).isEmpty();
        float swing2 = processing ? 0 : (float)Math.sin(time * 0.5 + 2.0) * 8.0F;
        drawArm(0.5F, 1.15F, dz + 0.5F, cx, cz, swing2, time, has2, processing, proc);

        // Arm 3 - key (slot 2) - on block (dx,0), right side
        boolean has3 = !te.getStackInSlot(2).isEmpty();
        float swing3 = processing ? 0 : (float)Math.sin(time * 0.5 + 4.0) * 8.0F;
        drawArm(dx + 0.7F, 1.15F, 0.5F, cx, cz, swing3, time, has3, processing, proc);

        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawArm(float bx, float by, float bz,
                         float tx, float tz,
                         float swing, float time,
                         boolean hasItem, boolean processing, float proc) {
        float yaw = (float)Math.toDegrees(Math.atan2(tx - bx, tz - bz));
        float armLen = hasItem ? 0.35F : 0.2F;
        if (processing) armLen = 0.35F + proc * 0.15F;

        GlStateManager.pushMatrix();
        GlStateManager.translate(bx, by, bz);

        // === BASE: flat plate ===
        drawBox(-0.1F, -0.06F, -0.1F, 0.1F, 0.0F, 0.1F,
                0.15F, 0.15F, 0.2F);

        // === SHOULDER: vertical cylinder ===
        drawBox(-0.06F, 0.0F, -0.06F, 0.06F, 0.15F, 0.06F,
                0.3F, 0.12F, 0.4F);

        // === ROTATE to face center ===
        GlStateManager.translate(0, 0.15F, 0);
        GlStateManager.rotate(-yaw + swing, 0, 1, 0);

        // === UPPER ARM ===
        drawBox(-0.08F, -0.06F, -0.06F, 0.08F, 0.06F, armLen,
                0.2F, 0.2F, 0.28F);

        // === ELBOW ===
        GlStateManager.translate(0, 0, armLen);
        drawBox(-0.07F, -0.07F, -0.04F, 0.07F, 0.07F, 0.04F,
                0.45F, 0.18F, 0.55F);

        // === FOREARM (slight bend down) ===
        float bend = 12.0F + 6.0F * (float)Math.sin(time * 0.8);
        GlStateManager.rotate(bend, 1, 0, 0);
        float foreLen = armLen * 0.8F;
        drawBox(-0.065F, -0.05F, 0.0F, 0.065F, 0.05F, foreLen,
                0.18F, 0.18F, 0.25F);

        // === CLAW ===
        GlStateManager.translate(0, 0, foreLen);
        float open = 0.05F + 0.02F * (float)Math.sin(time * 2.0);
        // Left finger
        drawBox(-open - 0.03F, -0.04F, 0, -open, 0.04F, 0.08F,
                0.5F, 0.15F, 0.6F);
        // Right finger
        drawBox(open, -0.04F, 0, open + 0.03F, 0.04F, 0.08F,
                0.5F, 0.15F, 0.6F);

        GlStateManager.popMatrix();
    }

    private void drawBox(float x1, float y1, float z1, float x2, float y2, float z2,
                         float r, float g, float b) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        // Top
        buf.pos(x1,y2,z1).color(r*1.2F,g*1.2F,b*1.2F,1F).endVertex();
        buf.pos(x2,y2,z1).color(r*1.2F,g*1.2F,b*1.2F,1F).endVertex();
        buf.pos(x2,y2,z2).color(r*1.2F,g*1.2F,b*1.2F,1F).endVertex();
        buf.pos(x1,y2,z2).color(r*1.2F,g*1.2F,b*1.2F,1F).endVertex();
        // Bottom
        buf.pos(x1,y1,z2).color(r*0.5F,g*0.5F,b*0.5F,1F).endVertex();
        buf.pos(x2,y1,z2).color(r*0.5F,g*0.5F,b*0.5F,1F).endVertex();
        buf.pos(x2,y1,z1).color(r*0.5F,g*0.5F,b*0.5F,1F).endVertex();
        buf.pos(x1,y1,z1).color(r*0.5F,g*0.5F,b*0.5F,1F).endVertex();
        // South (+Z)
        buf.pos(x1,y1,z2).color(r*0.9F,g*0.9F,b*0.9F,1F).endVertex();
        buf.pos(x1,y2,z2).color(r,g,b,1F).endVertex();
        buf.pos(x2,y2,z2).color(r,g,b,1F).endVertex();
        buf.pos(x2,y1,z2).color(r*0.9F,g*0.9F,b*0.9F,1F).endVertex();
        // North (-Z)
        buf.pos(x2,y1,z1).color(r*0.9F,g*0.9F,b*0.9F,1F).endVertex();
        buf.pos(x2,y2,z1).color(r,g,b,1F).endVertex();
        buf.pos(x1,y2,z1).color(r,g,b,1F).endVertex();
        buf.pos(x1,y1,z1).color(r*0.9F,g*0.9F,b*0.9F,1F).endVertex();
        // West (-X)
        buf.pos(x1,y1,z1).color(r*0.7F,g*0.7F,b*0.7F,1F).endVertex();
        buf.pos(x1,y2,z1).color(r*0.8F,g*0.8F,b*0.8F,1F).endVertex();
        buf.pos(x1,y2,z2).color(r*0.8F,g*0.8F,b*0.8F,1F).endVertex();
        buf.pos(x1,y1,z2).color(r*0.7F,g*0.7F,b*0.7F,1F).endVertex();
        // East (+X)
        buf.pos(x2,y1,z2).color(r*0.7F,g*0.7F,b*0.7F,1F).endVertex();
        buf.pos(x2,y2,z2).color(r*0.8F,g*0.8F,b*0.8F,1F).endVertex();
        buf.pos(x2,y2,z1).color(r*0.8F,g*0.8F,b*0.8F,1F).endVertex();
        buf.pos(x2,y1,z1).color(r*0.7F,g*0.7F,b*0.7F,1F).endVertex();
        tess.draw();
    }

    private void renderItems(TileCondenseur te, double x, double y, double z,
                             float time, float cx, float cz) {
        float baseY = 1.5F;
        float proc = te.getProcessPercent() / 100.0F;
        boolean processing = te.isProcessing();
        float[][] off = {{-0.25F,-0.25F},{0.25F,-0.25F},{-0.25F,0.25F},{0.25F,0.25F}};
        for (int i = 0; i < 4; i++) {
            ItemStack stack = te.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            float ox = off[i][0], oz = off[i][1];
            if (processing) { ox *= (1-proc*0.95F); oz *= (1-proc*0.95F); }
            float bob = (float)Math.sin(time*1.5+i*1.5)*0.03F;
            float sink = processing ? -proc * 0.8F : 0;
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
        float i = 0.15F;
        float x0=Math.min(0,dx)+i, x1=Math.max(0,dx)+1-i;
        float z0=Math.min(0,dz)+i, z1=Math.max(0,dz)+1-i;
        float fY=1.02F, tY=fY+p*0.85F;
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
