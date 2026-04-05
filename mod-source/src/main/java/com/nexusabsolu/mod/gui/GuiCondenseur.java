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
import java.util.Collections;

@SideOnly(Side.CLIENT)
public class GuiCondenseur extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_condenseur.png");

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
        int x = guiLeft, y = guiTop;
        btnMode = new GuiButton(0, x + 90, y + 85, 40, 14,
            tile.isAutoMode() ? "AUTO" : "MANUEL");
        btnStart = new GuiButton(1, x + 133, y + 85, 40, 14, "START");
        btnStart.enabled = !tile.isAutoMode();
        buttonList.add(btnMode);
        buttonList.add(btnStart);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0)
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, 0);
        else if (button.id == 1)
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, 1);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        btnMode.displayString = tile.isAutoMode() ? "AUTO" : "MANUEL";
        btnStart.enabled = !tile.isAutoMode() && !tile.isProcessing();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int x = guiLeft, y = guiTop;

        // === PROGRESS BAR FILL ===
        if (tile.getMaxProcessTime() > 0 && tile.getProcessTime() > 0) {
            int pw = (tile.getProcessTime() * 53) / tile.getMaxProcessTime();
            for (int px = 0; px < pw; px++) {
                int intensity = (int)(100 + (px / 53.0) * 155);
                int color = 0xFF000000 | (intensity << 16) | (0x32 << 8)
                    | Math.min(255, intensity + 50);
                drawRect(x + 91 + px, y + 50, x + 92 + px, y + 64, color);
            }
        }
        // Progress tick marks
        for (int i = 0; i < 4; i++) {
            drawRect(x + 97 + i * 12, y + 55, x + 101 + i * 12, y + 59,
                0x44FFFFFF);
        }

        // === ENERGY BAR FILL ===
        if (tile.getMaxEnergyStored() > 0 && tile.getEnergyStored() > 0) {
            int eh = (tile.getEnergyStored() * 78) / tile.getMaxEnergyStored();
            for (int py = 0; py < eh; py++) {
                int intensity = (int)(80 + (py / 78.0) * 175);
                int color = 0xFF000000
                    | (Math.min(255, intensity + 30) << 16)
                    | ((intensity / 6) << 8);
                drawRect(x + 199, y + 99 - py, x + 211, y + 100 - py, color);
            }
        }

        // === PROCESSING PULSE (animated circuit lines) ===
        if (tile.isProcessing()) {
            long time = tile.getWorld().getTotalWorldTime();
            if ((time / 10) % 2 == 0) {
                for (int ly : new int[]{25, 55, 85}) {
                    drawRect(x + 5, y + ly, x + xSize - 5, y + ly + 1,
                        0x40A064FF);
                }
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        fontRenderer.drawString("CONDENSEUR DIMENSIONNEL", 20, 8, 0x00E5FF);
        fontRenderer.drawString("Dr. E. Voss -- Prototype VII", 20, 20, 0x7B68EE);

        // Slot labels
        fontRenderer.drawString("CM", 37, 30, 0x888888);
        fontRenderer.drawString("CM", 61, 30, 0x888888);
        fontRenderer.drawString("Cle", 36, 54, 0x888888);
        fontRenderer.drawString("Cat.", 59, 54, 0x888888);
        fontRenderer.drawString("OUT", 160, 42, 0x888888);

        // Status
        if (!tile.isStructureValid()) {
            fontRenderer.drawString("Structure incomplete !", 80, 70, 0xFF4444);
        } else if (tile.isProcessing()) {
            fontRenderer.drawString(
                "Phase: " + tile.getProcessPercent() + "%", 92, 70, 0xBB86FC);
            String quote = "\"" + tile.getCurrentQuote() + "\"";
            fontRenderer.drawString(quote, 10, 100, 0x666666);
        } else {
            fontRenderer.drawString("Pret.", 95, 70, 0x44FF44);
        }

        // Energy text
        fontRenderer.drawString(tile.getEnergyStored() + " RF", 195, 103, 0x00E5FF);

        // Inventory label
        fontRenderer.drawString("Inventaire", 30, 120, 0x888888);
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);

        // Energy tooltip (over JEI)
        int x = guiLeft, y = guiTop;
        if (mx >= x + 198 && mx <= x + 212 && my >= y + 20 && my <= y + 100) {
            drawHoveringText(Collections.singletonList(
                tile.getEnergyStored() + " / " + tile.getMaxEnergyStored() + " RF"),
                mx, my);
        }
    }
}
