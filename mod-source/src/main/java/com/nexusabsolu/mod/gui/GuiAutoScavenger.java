package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.network.PacketScavengerSpeed;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.Collections;

public class GuiAutoScavenger extends GuiContainer {

    private final ContainerAutoScavenger container;
    private final TileAutoScavenger tile;
    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    // Custom button positions (relative to guiLeft/guiTop)
    private static final int BTN_W = 18;
    private static final int BTN_H = 14;
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
    protected void drawGuiContainerBackgroundLayer(float partialTicks,
                                                    int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int gx = guiLeft;
        int gy = guiTop;

        // === BACKGROUND ===
        drawRect(gx, gy, gx + GUI_W, gy + GUI_H, 0xFF1A1A2E);
        drawRect(gx, gy, gx + GUI_W, gy + 1, 0xFF4A6FA0);
        drawRect(gx, gy + GUI_H - 1, gx + GUI_W, gy + GUI_H, 0xFF2A2A40);
        drawRect(gx, gy, gx + 1, gy + GUI_H, 0xFF4A6FA0);
        drawRect(gx + GUI_W - 1, gy, gx + GUI_W, gy + GUI_H, 0xFF2A2A40);

        // Title bar
        drawRect(gx + 2, gy + 2, gx + GUI_W - 2, gy + 14, 0xFF2D2D50);

        // === INPUT SLOT (pickaxe) ===
        drawRect(gx + 79, gy + 11, gx + 97, gy + 29, 0xFF4A6FA0);
        drawRect(gx + 80, gy + 12, gx + 96, gy + 28, 0xFF0D0D20);

        // === PROGRESS ARROW ===
        int arrowY = gy + 32;
        int interval = container.getMineInterval();
        int process = container.getProcessTime();
        drawRect(gx + 83, arrowY - 1, gx + 93, arrowY + 17, 0xFF3A3A60);
        drawRect(gx + 84, arrowY, gx + 92, arrowY + 16, 0xFF0D0D20);
        if (interval > 0 && process > 0) {
            int fillH = Math.min((process * 16) / interval, 16);
            drawRect(gx + 84, arrowY, gx + 92, arrowY + fillH, 0xFF44AA66);
        }

        // === OUTPUT SLOTS ===
        for (int i = 0; i < 5; i++) {
            int sx = gx + 43 + i * 18;
            int sy = gy + 51;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A3A60);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF0D0D20);
        }

        // === ENERGY BAR (left) ===
        int barX = gx + 10;
        int barY = gy + 16;
        int barW = 10;
        int barH = 52;
        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF4A6FA0);
        drawRect(barX, barY, barX + barW, barY + barH, 0xFF0D0D20);
        int energy = container.getEnergy();
        int maxEnergy = 10000;
        if (energy > 0) {
            int fillE = (int)((long) energy * barH / maxEnergy);
            for (int row = 0; row < fillE; row++) {
                float pct = row / (float) barH;
                int r = (int)(200 * (1.0F - pct));
                int g = (int)(80 + 175 * pct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | 60;
                drawRect(barX + 1, barY + barH - row - 1,
                         barX + barW - 1, barY + barH - row, color);
            }
        }

        // === SPEED PANEL (right) ===
        int spX = gx + 124;
        int spY = gy + 15;
        int spW = 48;
        int spH = 56;
        drawRect(spX - 1, spY - 1, spX + spW + 1, spY + spH + 1, 0xFF4A6FA0);
        drawRect(spX, spY, spX + spW, spY + spH, 0xFF0D0D20);

        // Speed level bar (7 segments)
        int speedLvl = container.getSpeedLevel();
        for (int i = 1; i <= 7; i++) {
            int sx = spX + 3 + (i - 1) * 6;
            int segColor;
            if (i <= speedLvl) {
                if (i <= 2) segColor = 0xFF44AA66;
                else if (i <= 4) segColor = 0xFFAAAA44;
                else if (i <= 5) segColor = 0xFFDD8833;
                else segColor = 0xFFCC3333;
            } else {
                segColor = 0xFF222233;
            }
            drawRect(sx, spY + 22, sx + 5, spY + 30, segColor);
        }

        // Custom - / + buttons (Nexus style)
        drawSpeedBtn(gx + BTN_MINUS_X, gy + BTN_Y, BTN_W, BTN_H,
            "-", mouseX, mouseY);
        drawSpeedBtn(gx + BTN_PLUS_X, gy + BTN_Y, BTN_W, BTN_H,
            "+", mouseX, mouseY);

        // === PLAYER INVENTORY SLOTS ===
        for (Slot slot : inventorySlots.inventorySlots) {
            if (slot.slotNumber >= 6) {
                int sx = gx + slot.xPos - 1;
                int sy = gy + slot.yPos - 1;
                drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A3A60);
                drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF0D0D20);
            }
        }
    }

    private void drawSpeedBtn(int bx, int by, int bw, int bh,
                               String label, int mx, int my) {
        boolean hov = mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;

        // 3D border (light top-left, dark bottom-right)
        int light = hov ? 0xFF6688CC : 0xFF4A6FA0;
        drawRect(bx, by, bx + bw, by + 1, light);
        drawRect(bx, by, bx + 1, by + bh, light);
        drawRect(bx, by + bh - 1, bx + bw, by + bh, 0xFF1A1A2E);
        drawRect(bx + bw - 1, by, bx + bw, by + bh, 0xFF1A1A2E);

        // Fill
        drawRect(bx + 1, by + 1, bx + bw - 1, by + bh - 1,
            hov ? 0xFF3A4A70 : 0xFF2D2D50);

        // Label centered
        int lw = fontRenderer.getStringWidth(label);
        fontRenderer.drawStringWithShadow(label,
            bx + (bw - lw) / 2.0F, by + (bh - 8) / 2.0F,
            hov ? 0xFFFFFFFF : 0xFFBB88FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        String title = "Auto-Scavenger";
        fontRenderer.drawString(title,
            (GUI_W - fontRenderer.getStringWidth(title)) / 2, 4, 0xFF88BBFF);

        // Progress label
        fontRenderer.drawString("\u2193", 86, 30, 0xFF888888);

        // Durability
        if (container.inventorySlots.size() > 0) {
            net.minecraft.inventory.Slot pickSlot = container.inventorySlots.get(0);
            if (pickSlot.getHasStack()) {
                ItemStack pick = pickSlot.getStack();
                int durLeft = pick.getMaxDamage() - pick.getItemDamage();
                int durMax = pick.getMaxDamage();
                if (durMax > 0) {
                    fontRenderer.drawString(durLeft + "/" + durMax,
                        100, 18, 0xFFAAAAFF);
                }
            }
        }

        // Energy text
        fontRenderer.drawString(container.getEnergy() + " RF", 8, 70, 0xFFAAAAAA);

        // Speed panel text - properly spaced, no overlap with buttons
        int level = container.getSpeedLevel();
        int rfTick = container.getRfPerTick();
        int interval = container.getMineInterval();
        float seconds = interval / 20.0F;

        // Title centered above bar
        String nivTxt = "Niv." + level;
        int nivW = fontRenderer.getStringWidth(nivTxt);
        fontRenderer.drawString(nivTxt, 148 - nivW / 2, 17, 0xFFBB88FF);

        // Info between bar and buttons
        fontRenderer.drawString(String.format("%.1fs", seconds), 128, 34, 0xFFAAAAFF);
        fontRenderer.drawString(rfTick + "RF/t", 128, 44, 0xFFFF8844);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        // Tooltips over JEI
        int gx = guiLeft;
        int gy = guiTop;

        // Energy bar tooltip
        if (mouseX >= gx + 9 && mouseX <= gx + 21
         && mouseY >= gy + 15 && mouseY <= gy + 69) {
            drawHoveringText(Collections.singletonList(
                container.getEnergy() + " / 10000 RF"), mouseX, mouseY);
        }

        // Button tooltips
        if (mouseX >= gx + BTN_MINUS_X && mouseX <= gx + BTN_MINUS_X + BTN_W
         && mouseY >= gy + BTN_Y && mouseY <= gy + BTN_Y + BTN_H) {
            drawHoveringText(Collections.singletonList(
                "Vitesse -1"), mouseX, mouseY);
        }
        if (mouseX >= gx + BTN_PLUS_X && mouseX <= gx + BTN_PLUS_X + BTN_W
         && mouseY >= gy + BTN_Y && mouseY <= gy + BTN_Y + BTN_H) {
            drawHoveringText(Collections.singletonList(
                "Vitesse +1"), mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            throws IOException {
        int gx = guiLeft;
        int gy = guiTop;

        // Minus button
        if (mouseX >= gx + BTN_MINUS_X && mouseX <= gx + BTN_MINUS_X + BTN_W
         && mouseY >= gy + BTN_Y && mouseY <= gy + BTN_Y + BTN_H) {
            NexusPacketHandler.INSTANCE.sendToServer(
                new PacketScavengerSpeed(tile.getPos(), false));
            return;
        }
        // Plus button
        if (mouseX >= gx + BTN_PLUS_X && mouseX <= gx + BTN_PLUS_X + BTN_W
         && mouseY >= gy + BTN_Y && mouseY <= gy + BTN_Y + BTN_H) {
            NexusPacketHandler.INSTANCE.sendToServer(
                new PacketScavengerSpeed(tile.getPos(), true));
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
