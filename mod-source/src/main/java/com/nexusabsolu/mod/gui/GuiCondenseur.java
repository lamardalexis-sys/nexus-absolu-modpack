package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiCondenseur extends GuiContainer {

    private final TileCondenseur tile;
    private GuiButton btnMode;
    private GuiButton btnStart;

    public GuiCondenseur(InventoryPlayer playerInv, TileCondenseur tile) {
        super(new ContainerCondenseur(playerInv, tile));
        this.tile = tile;
        this.xSize = 220;
        this.ySize = 220;
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        btnMode = new GuiButton(0, x + 90, y + 85, 40, 14, tile.isAutoMode() ? "AUTO" : "MANUEL");
        btnStart = new GuiButton(1, x + 133, y + 85, 40, 14, "START");
        btnStart.enabled = !tile.isAutoMode();
        this.buttonList.add(btnMode);
        this.buttonList.add(btnStart);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // Toggle auto/manual
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 0);
        } else if (button.id == 1) {
            // Manual start
            this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 1);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        btnMode.displayString = tile.isAutoMode() ? "AUTO" : "MANUEL";
        btnStart.enabled = !tile.isAutoMode() && !tile.isProcessing();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Dark background
        drawRect(x, y, x + this.xSize, y + this.ySize, 0xF00F0A14);

        // Purple border (double)
        drawRect(x, y, x + this.xSize, y + 1, 0xFF6432B4);
        drawRect(x, y + this.ySize - 1, x + this.xSize, y + this.ySize, 0xFF6432B4);
        drawRect(x, y, x + 1, y + this.ySize, 0xFF6432B4);
        drawRect(x + this.xSize - 1, y, x + this.xSize, y + this.ySize, 0xFF6432B4);
        drawRect(x + 1, y + 1, x + this.xSize - 1, y + 2, 0xFF3C1E78);
        drawRect(x + 1, y + this.ySize - 2, x + this.xSize - 1, y + this.ySize - 1, 0xFF3C1E78);
        drawRect(x + 1, y + 1, x + 2, y + this.ySize - 1, 0xFF3C1E78);
        drawRect(x + this.xSize - 2, y + 1, x + this.xSize - 1, y + this.ySize - 1, 0xFF3C1E78);

        // Circuit lines
        for (int ly : new int[]{25, 55, 85, 105}) {
            drawRect(x + 5, y + ly, x + this.xSize - 5, y + ly + 1, 0x28782DC8);
        }
        for (int lx : new int[]{30, 80, 140, 190}) {
            drawRect(x + lx, y + 5, x + lx + 1, y + 110, 0x28782DC8);
        }
        drawRect(x + 5, y + 112, x + this.xSize - 5, y + 113, 0x996432B4);

        // Slot backgrounds - cyan border
        for (int[] pos : new int[][]{{35, 38}, {59, 38}, {35, 62}, {59, 62}}) {
            drawSlot(x + pos[0] - 1, y + pos[1] - 1, 0xFF00B4DC);
        }
        // Output slot - purple border
        drawSlot(x + 159, y + 49, 0xFF9650FF);
        drawRect(x + 158, y + 48, x + 177, y + 67, 0x446432B4);

        // Progress bar
        drawRect(x + 90, y + 49, x + 145, y + 65, 0xFF141018);
        drawRect(x + 90, y + 49, x + 145, y + 65, 0xFF3C2850);
        if (tile.getMaxProcessTime() > 0 && tile.getProcessTime() > 0) {
            int pw = (tile.getProcessTime() * 53) / tile.getMaxProcessTime();
            for (int px = 0; px < pw; px++) {
                int intensity = (int)(100 + (px / 53.0) * 155);
                int color = 0xFF000000 | (intensity << 16) | (0x32 << 8) | Math.min(255, intensity + 50);
                drawRect(x + 91 + px, y + 50, x + 92 + px, y + 64, color);
            }
        }
        for (int i = 0; i < 4; i++) {
            drawRect(x + 97 + i * 12, y + 55, x + 101 + i * 12, y + 59, 0x44FFFFFF);
        }

        // Energy bar
        boolean hasEnergy = tile.getEnergyStored() > 0;
        int borderColor = hasEnergy ? 0xFFE03020 : 0xFF444444;
        drawRect(x + 198, y + 20, x + 212, y + 100, 0xFF141018);
        // Border
        drawRect(x + 198, y + 20, x + 212, y + 21, borderColor);
        drawRect(x + 198, y + 99, x + 212, y + 100, borderColor);
        drawRect(x + 198, y + 20, x + 199, y + 100, borderColor);
        drawRect(x + 211, y + 20, x + 212, y + 100, borderColor);
        if (tile.getMaxEnergyStored() > 0 && hasEnergy) {
            int eh = (tile.getEnergyStored() * 78) / tile.getMaxEnergyStored();
            for (int py = 0; py < eh; py++) {
                int intensity = (int)(80 + (py / 78.0) * 175);
                int color = 0xFF000000 | (Math.min(255, intensity + 30) << 16) | ((intensity / 6) << 8);
                drawRect(x + 199, y + 99 - py, x + 211, y + 100 - py, color);
            }
        }

        // Processing pulse
        if (tile.isProcessing()) {
            long time = tile.getWorld().getTotalWorldTime();
            if ((time / 10) % 2 == 0) {
                for (int ly : new int[]{25, 55, 85}) {
                    drawRect(x + 5, y + ly, x + this.xSize - 5, y + ly + 1, 0x40A064FF);
                }
            }
        }
    }

    private void drawSlot(int x, int y, int borderColor) {
        drawRect(x, y, x + 18, y + 18, 0xFF191420);
        drawRect(x, y, x + 18, y, borderColor);
        drawRect(x, y + 17, x + 18, y + 18, borderColor);
        drawRect(x, y, x, y + 18, borderColor);
        drawRect(x + 17, y, x + 18, y + 18, borderColor);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRenderer.drawString("CONDENSEUR DIMENSIONNEL", 20, 8, 0x00E5FF);
        this.fontRenderer.drawString("Dr. E. Voss -- Prototype VII", 20, 20, 0x7B68EE);
        this.fontRenderer.drawString("CM", 37, 30, 0x888888);
        this.fontRenderer.drawString("CM", 61, 30, 0x888888);
        this.fontRenderer.drawString("Cle", 36, 54, 0x888888);
        this.fontRenderer.drawString("Cat.", 59, 54, 0x888888);
        this.fontRenderer.drawString("OUT", 160, 42, 0x888888);

        if (!tile.isStructureValid()) {
            this.fontRenderer.drawString("Structure incomplete !", 80, 70, 0xFF4444);
        } else if (tile.isProcessing()) {
            String percent = "Phase: " + tile.getProcessPercent() + "%";
            this.fontRenderer.drawString(percent, 92, 70, 0xBB86FC);
            String quote = "\"" + tile.getCurrentQuote() + "\"";
            this.fontRenderer.drawString(quote, 10, 100, 0x666666);
        } else {
            this.fontRenderer.drawString("Pret.", 95, 70, 0x44FF44);
        }

        String energyText = tile.getEnergyStored() + " RF";
        this.fontRenderer.drawString(energyText, 195, 103, 0x00E5FF);

        // Energy tooltip
        int guiX = (this.width - this.xSize) / 2;
        int guiY = (this.height - this.ySize) / 2;
        if (mouseX >= guiX + 198 && mouseX <= guiX + 212 && mouseY >= guiY + 20 && mouseY <= guiY + 100) {
            this.drawHoveringText(tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF",
                mouseX - guiX, mouseY - guiY);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
