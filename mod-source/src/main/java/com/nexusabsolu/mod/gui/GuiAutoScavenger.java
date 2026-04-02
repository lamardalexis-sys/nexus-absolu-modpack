package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class GuiAutoScavenger extends GuiContainer {

    private final ContainerAutoScavenger container;
    private static final int GUI_W = 176;
    private static final int GUI_H = 166;

    public GuiAutoScavenger(InventoryPlayer playerInv, TileAutoScavenger tile) {
        super(new ContainerAutoScavenger(playerInv, tile));
        this.container = (ContainerAutoScavenger) inventorySlots;
        this.xSize = GUI_W;
        this.ySize = GUI_H;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int gx = (width - xSize) / 2;
        int gy = (height - ySize) / 2;

        // Background
        drawRect(gx, gy, gx + GUI_W, gy + GUI_H, 0xFF1A1A2E);
        // Border
        drawRect(gx, gy, gx + GUI_W, gy + 1, 0xFF4A6FA0);
        drawRect(gx, gy + GUI_H - 1, gx + GUI_W, gy + GUI_H, 0xFF4A6FA0);
        drawRect(gx, gy, gx + 1, gy + GUI_H, 0xFF4A6FA0);
        drawRect(gx + GUI_W - 1, gy, gx + GUI_W, gy + GUI_H, 0xFF4A6FA0);

        // Title bar
        drawRect(gx + 2, gy + 2, gx + GUI_W - 2, gy + 14, 0xFF2D2D50);

        // Input slot bg
        drawRect(gx + 79, gy + 11, gx + 97, gy + 29, 0xFF3A3A60);
        drawRect(gx + 80, gy + 12, gx + 96, gy + 28, 0xFF0D0D20);

        // Arrow down
        int arrowY = gy + 32;
        int process = container.getProcessTime();
        int fillH = (process > 0) ? (process * 16) / 40 : 0;
        drawRect(gx + 84, arrowY, gx + 92, arrowY + 16, 0xFF0D0D20);
        if (fillH > 0) {
            drawRect(gx + 84, arrowY, gx + 92, arrowY + fillH, 0xFF44AA66);
        }

        // Output slots bg
        for (int i = 0; i < 5; i++) {
            int sx = gx + 43 + i * 18;
            int sy = gy + 51;
            drawRect(sx, sy, sx + 18, sy + 18, 0xFF3A3A60);
            drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF0D0D20);
        }

        // Energy bar (left side)
        int barX = gx + 10;
        int barY = gy + 16;
        int barW = 8;
        int barH = 52;
        drawRect(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF4A6FA0);
        drawRect(barX, barY, barX + barW, barY + barH, 0xFF0D0D20);
        int energy = container.getEnergy();
        if (energy > 0) {
            int fillE = (energy * barH) / 5000;
            for (int row = 0; row < fillE; row++) {
                float pct = row / (float) barH;
                int r = (int)(200 * (1.0F - pct));
                int g = (int)(80 + 175 * pct);
                int color = 0xFF000000 | (r << 16) | (g << 8) | 60;
                drawRect(barX + 1, barY + barH - row - 1, barX + barW - 1, barY + barH - row, color);
            }
        }

        // Player inventory area
        drawRect(gx + 6, gy + 80, gx + GUI_W - 6, gy + GUI_H - 4, 0xFF0D0D20);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "Auto-Scavenger";
        fontRenderer.drawString(title, (GUI_W - fontRenderer.getStringWidth(title)) / 2, 4, 0xFF88BBFF);

        // Labels
        fontRenderer.drawString("[P]", 83, 30, 0xFF888888); // Pickaxe slot
        
        // Durability indicator
        if (container.inventorySlots.size() > 0) {
            net.minecraft.inventory.Slot pickSlot = container.inventorySlots.get(0);
            if (pickSlot.getHasStack()) {
                ItemStack pick = pickSlot.getStack();
                int durLeft = pick.getMaxDamage() - pick.getItemDamage();
                int durMax = pick.getMaxDamage();
                if (durMax > 0) {
                    fontRenderer.drawString(durLeft + "/" + durMax, 100, 16, 0xFFAAAAFF);
                }
            }
        }

        // Energy text
        String eStr = container.getEnergy() + " RF";
        fontRenderer.drawString(eStr, 8, 70, 0xFFAAAAAA);

        // Status
        if (container.getProcessTime() > 0) {
            fontRenderer.drawString("15 RF/t", 130, 35, 0xFFFF8844);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}
