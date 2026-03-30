package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileAtelier;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiAtelier extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(Reference.MOD_ID, "textures/gui/atelier_voss.png");
    private final TileAtelier tile;

    public GuiAtelier(InventoryPlayer playerInv, TileAtelier tile) {
        super(new ContainerAtelier(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        // Craft button
        addButton(new GuiButton(0, guiLeft + 93, guiTop + 30, 26, 20, ">>"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, 0);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "Atelier du Dr. Voss";
        fontRenderer.drawString(title,
            (xSize - fontRenderer.getStringWidth(title)) / 2, 6, 0x00CCCC);

        fontRenderer.drawString("Inventaire", 8, ySize - 96 + 2, 0x404040);

        // Slot labels
        fontRenderer.drawString("In", 42, 24, 0x888888);
        fontRenderer.drawString("In", 72, 24, 0x888888);
        fontRenderer.drawString("Out", 125, 24, 0x00CC00);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }
}
