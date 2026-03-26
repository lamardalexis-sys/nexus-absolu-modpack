package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiCondenseur extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Reference.MOD_ID, "textures/gui/condenseur.png");

    private final TileCondenseur tile;

    public GuiCondenseur(InventoryPlayer playerInv, TileCondenseur tile) {
        super(new ContainerCondenseur(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Draw progress bar (purple fill, from left to right)
        // Progress bar position: x+76, y+34, width=40, height=16
        if (tile.getMaxProcessTime() > 0) {
            int progressWidth = (tile.getProcessTime() * 40) / tile.getMaxProcessTime();
            // Draw from the second texture row (y=166 in texture)
            this.drawTexturedModalRect(x + 76, y + 34, 176, 0, progressWidth, 16);
        }

        // Draw energy bar (cyan fill, from bottom to top)
        // Energy bar position: x+156, y+10, width=10, height=60
        if (tile.getMaxEnergyStored() > 0) {
            int energyHeight = (tile.getEnergyStored() * 60) / tile.getMaxEnergyStored();
            // Draw from bottom up
            this.drawTexturedModalRect(x + 156, y + 10 + (60 - energyHeight), 176, 16 + (60 - energyHeight), 10, energyHeight);
        }

        // Pulsing purple lines effect when processing
        if (tile.isProcessing()) {
            long time = tile.getWorld().getTotalWorldTime();
            // Alternate between two overlay states every 10 ticks
            if ((time / 10) % 2 == 0) {
                this.drawTexturedModalRect(x, y, 0, 166, this.xSize, this.ySize);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title - cyan color
        String title = "CONDENSEUR DIMENSIONNEL";
        this.fontRenderer.drawString(title, 8, -10, 0x00E5FF);

        // Subtitle
        this.fontRenderer.drawString("Dr. E. Voss -- Proto. VII", 8, 0, 0x7B68EE);

        // Slot labels (small, grey)
        this.fontRenderer.drawString("CM", 30, 14, 0x888888);
        this.fontRenderer.drawString("CM", 54, 14, 0x888888);
        this.fontRenderer.drawString("Cle", 27, 38, 0x888888);
        this.fontRenderer.drawString("Cat.", 50, 38, 0x888888);

        // Progress percentage
        if (tile.isProcessing()) {
            String percent = tile.getProcessPercent() + "%";
            this.fontRenderer.drawString(percent, 88, 52, 0xBB86FC);

            // Voss quote at the bottom
            String quote = "\"" + tile.getCurrentQuote() + "\"";
            this.fontRenderer.drawString(quote, 8, 72, 0x666666);
        } else {
            this.fontRenderer.drawString("En attente...", 76, 52, 0x444444);
        }

        // Energy text
        String energyText = tile.getEnergyStored() + " RF";
        // Draw small, right-aligned above the energy bar
        int textWidth = this.fontRenderer.getStringWidth(energyText);
        this.fontRenderer.drawString(energyText, 168 - textWidth, 72, 0x00E5FF);

        // Tooltip for energy bar
        int guiX = (this.width - this.xSize) / 2;
        int guiY = (this.height - this.ySize) / 2;
        if (mouseX >= guiX + 156 && mouseX <= guiX + 166 &&
            mouseY >= guiY + 10 && mouseY <= guiY + 70) {
            String tooltip = tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF";
            this.drawHoveringText(tooltip, mouseX - guiX, mouseY - guiY);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
