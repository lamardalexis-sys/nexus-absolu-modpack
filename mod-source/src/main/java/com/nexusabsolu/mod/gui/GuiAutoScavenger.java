package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.network.PacketScavengerSpeed;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.Collections;

public class GuiAutoScavenger extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_auto_scavenger.png");

    private final ContainerAutoScavenger container;
    private final TileAutoScavenger tile;
    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    // Button positions (relative to guiLeft/guiTop)
    private static final int BTN_W = 18;
    private static final int BTN_H = 13;
    private static final int BTN_MINUS_X = 127;
    private static final int BTN_PLUS_X = 153;
    private static final int BTN_Y = 54;

    public GuiAutoScavenger(InventoryPlayer playerInv, TileAutoScavenger tile) {
        super(new ContainerAutoScavenger(playerInv, tile));
        this.container = (ContainerAutoScavenger) inventorySlots;
        this.tile = tile;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int gx = guiLeft, gy = guiTop;

        // === ENERGY BAR FILL ===
        int energy = container.getEnergy();
        int barH = 52;
        if (energy > 0) {
            int fillE = (int)((long) energy * barH / 10000);
            for (int row = 0; row < fillE; row++) {
                float pct = row / (float) barH;
                int r = (int)(200 * (1.0F - pct));
                int g = (int)(80 + 175 * pct);
                int c = 0xFF000000 | (r << 16) | (g << 8) | 60;
                drawRect(gx + 10, gy + 67 - row, gx + 20, gy + 68 - row, c);
            }
        }

        // === PROGRESS FILL ===
        int interval = container.getMineInterval();
        int process = container.getProcessTime();
        if (interval > 0 && process > 0) {
            int fillH = Math.min((process * 16) / interval, 16);
            drawRect(gx + 84, gy + 32, gx + 92, gy + 32 + fillH, 0xFF44AA66);
        }

        // === SPEED SEGMENTS (7) ===
        int speedLvl = container.getSpeedLevel();
        for (int i = 1; i <= 7; i++) {
            int sx = gx + 127 + (i - 1) * 6;
            int segColor;
            if (i <= speedLvl) {
                if (i <= 2) segColor = 0xFF44AA66;
                else if (i <= 4) segColor = 0xFFAAAA44;
                else if (i <= 5) segColor = 0xFFDD8833;
                else segColor = 0xFFCC3333;
            } else {
                segColor = 0xFF222233;
            }
            drawRect(sx, gy + 37, sx + 5, gy + 45, segColor);
        }

        // === BUTTON HIGHLIGHTS (hover effect) ===
        boolean hovM = mx>=gx+BTN_MINUS_X && mx<=gx+BTN_MINUS_X+BTN_W
                    && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H;
        boolean hovP = mx>=gx+BTN_PLUS_X && mx<=gx+BTN_PLUS_X+BTN_W
                    && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H;
        if (hovM) drawRect(gx+127, gy+54, gx+145, gy+67, 0xFF3A4A70);
        if (hovP) drawRect(gx+153, gy+54, gx+171, gy+67, 0xFF3A4A70);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Title
        String title = "Auto-Scavenger";
        fontRenderer.drawString(title,
            (GUI_W - fontRenderer.getStringWidth(title)) / 2, 4, 0xFF88BBFF);

        // Progress arrow
        fontRenderer.drawString("\u2193", 86, 30, 0xFF888888);

        // Durability
        if (container.inventorySlots.size() > 0) {
            Slot pickSlot = container.inventorySlots.get(0);
            if (pickSlot.getHasStack()) {
                ItemStack pick = pickSlot.getStack();
                int durLeft = pick.getMaxDamage() - pick.getItemDamage();
                int durMax = pick.getMaxDamage();
                if (durMax > 0)
                    fontRenderer.drawString(durLeft+"/"+durMax, 100, 18, 0xFFAAAAFF);
            }
        }

        // Energy text
        fontRenderer.drawString(container.getEnergy() + " RF", 8, 70, 0xFFAAAAAA);

        // Speed info
        int level = container.getSpeedLevel();
        int rfTick = container.getRfPerTick();
        float seconds = container.getMineInterval() / 20.0F;
        String nivTxt = "Niv." + level;
        int nivW = fontRenderer.getStringWidth(nivTxt);
        fontRenderer.drawString(nivTxt, 148 - nivW / 2, 17, 0xFFBB88FF);
        fontRenderer.drawString(String.format("%.1fs", seconds), 128, 48, 0xFFAAAAFF);

        // Button labels
        fontRenderer.drawStringWithShadow("-", 134, 57, 0xFFBB88FF);
        fontRenderer.drawStringWithShadow("+", 160, 57, 0xFFBB88FF);

        // RF/t
        fontRenderer.drawString(rfTick + "RF/t", 128, 72, 0xFFFF8844);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);

        int gx = guiLeft, gy = guiTop;
        // Energy tooltip
        if (mx>=gx+9 && mx<=gx+21 && my>=gy+15 && my<=gy+69)
            drawHoveringText(Collections.singletonList(
                container.getEnergy() + " / 10000 RF"), mx, my);
        // Button tooltips
        if (mx>=gx+BTN_MINUS_X && mx<=gx+BTN_MINUS_X+BTN_W
         && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H)
            drawHoveringText(Collections.singletonList("Vitesse -1"), mx, my);
        if (mx>=gx+BTN_PLUS_X && mx<=gx+BTN_PLUS_X+BTN_W
         && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H)
            drawHoveringText(Collections.singletonList("Vitesse +1"), mx, my);
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        int gx = guiLeft, gy = guiTop;
        if (mx>=gx+BTN_MINUS_X && mx<=gx+BTN_MINUS_X+BTN_W
         && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H) {
            NexusPacketHandler.INSTANCE.sendToServer(
                new PacketScavengerSpeed(tile.getPos(), false));
            return;
        }
        if (mx>=gx+BTN_PLUS_X && mx<=gx+BTN_PLUS_X+BTN_W
         && my>=gy+BTN_Y && my<=gy+BTN_Y+BTN_H) {
            NexusPacketHandler.INSTANCE.sendToServer(
                new PacketScavengerSpeed(tile.getPos(), true));
            return;
        }
        super.mouseClicked(mx, my, btn);
    }
}
