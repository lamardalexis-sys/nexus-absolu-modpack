package com.nexusabsolu.mod.archives.gui;

import java.io.IOException;
import java.util.Arrays;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.archives.tiles.TileCompresseurEau;
import com.nexusabsolu.mod.gui.util.GuiUtils;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * GUI du Compresseur d'Eau Voss.
 *
 * <p>Layout simple (pas de slots item) :
 * <ul>
 *   <li>Titre en haut</li>
 *   <li>Tank input a gauche (eau_voss_chaude ou water entrante)</li>
 *   <li>Progress bar au centre (0..100 ticks)</li>
 *   <li>Tank output a droite (eau_voss_froide sortante)</li>
 *   <li>Barre RF verticale a cote</li>
 *   <li>Inventaire joueur en bas</li>
 * </ul>
 *
 * <p>Style visuel : purple/violet coherent avec le pack Nexus (comme les fours).
 * Textures des tanks : bleu clair pour output, bleu grise pour input chaud.
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class GuiCompresseurEau extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/gui_compresseur_eau.png");

    private final TileCompresseurEau tile;

    public GuiCompresseurEau(InventoryPlayer playerInv, TileCompresseurEau tile) {
        super(new ContainerCompresseurEau(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mx, int my) {
        // Fond pourpre uniforme (fallback si texture absent)
        // La texture sera ajoutee plus tard (placeholder)
        drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xFF1A1030);
        drawRect(guiLeft + 1, guiTop + 1, guiLeft + xSize - 1, guiTop + 16, 0xFF3A1F5E);

        // Titre
        String title = "Compresseur d'Eau Voss";
        int tw = fontRenderer.getStringWidth(title);
        fontRenderer.drawStringWithShadow(title,
            guiLeft + (xSize - tw) / 2.0F, guiTop + 4, 0xFFEEAAFF);

        // Tank input (gauche, y=22-74)
        drawTank(guiLeft + 24, guiTop + 22, 16, 52, tile.getInputTank().getFluidAmount(),
            TileCompresseurEau.TANK_CAPACITY, 0xFF3C6482);  // bleu grise (eau chaude)

        // Progress bar (centre)
        int progW = 50;
        int progFilled = (int)((float) tile.getProgress() / tile.getMaxProgress() * progW);
        // Fond
        drawRect(guiLeft + 60, guiTop + 44, guiLeft + 60 + progW, guiTop + 44 + 10, 0xFF0E0818);
        // Rempli
        if (progFilled > 0) {
            drawRect(guiLeft + 60, guiTop + 44, guiLeft + 60 + progFilled, guiTop + 44 + 10, 0xFF64C8FF);
        }

        // Tank output (droite, y=22-74)
        drawTank(guiLeft + 136, guiTop + 22, 16, 52, tile.getOutputTank().getFluidAmount(),
            TileCompresseurEau.TANK_CAPACITY, 0xFF64C8FF);  // bleu clair (eau froide)

        // RF bar (a droite des 2 tanks)
        int rfX = guiLeft + 160;
        int rfY = guiTop + 22;
        int rfW = 8;
        int rfH = 52;
        drawRect(rfX, rfY, rfX + rfW, rfY + rfH, 0xFF0E0818);
        int rfFill = (int)((float) tile.getEnergyStored() / tile.getMaxEnergy() * (rfH - 2));
        if (rfFill > 0) {
            drawRect(rfX + 1, rfY + rfH - 1 - rfFill, rfX + rfW - 1, rfY + rfH - 1, 0xFFFFCC00);
        }
    }

    /**
     * Helper : dessine un tank rectangulaire avec fluid fill bottom-up.
     */
    private void drawTank(int x, int y, int w, int h, int amount, int capacity, int fluidColor) {
        // Fond sombre
        drawRect(x, y, x + w, y + h, 0xFF0E0818);
        // Fluid fill (bas -> haut)
        if (amount > 0 && capacity > 0) {
            int filled = (int)((float) amount / capacity * (h - 2));
            drawRect(x + 1, y + h - 1 - filled, x + w - 1, y + h - 1, fluidColor);
        }
        // Bordure
        drawRect(x, y, x + w, y + 1, 0xFFBB77FF);
        drawRect(x, y, x + 1, y + h, 0xFFBB77FF);
        drawRect(x + w - 1, y, x + w, y + h, 0xFFBB77FF);
        drawRect(x, y + h - 1, x + w, y + h, 0xFFBB77FF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Tooltips hover sur tanks et barre RF (coordinates relative to GUI)
        int relX = mx - guiLeft;
        int relY = my - guiTop;

        // Tank input
        if (relX >= 24 && relX <= 40 && relY >= 22 && relY <= 74) {
            drawHoveringText(Arrays.asList(
                "\u00a7bInput",
                "\u00a77" + tile.getInputTank().getFluidAmount() + " / "
                    + TileCompresseurEau.TANK_CAPACITY + " mB",
                "\u00a78Eau Chaude ou Eau vanilla"
            ), relX, relY);
        }
        // Tank output
        if (relX >= 136 && relX <= 152 && relY >= 22 && relY <= 74) {
            drawHoveringText(Arrays.asList(
                "\u00a7bOutput",
                "\u00a77" + tile.getOutputTank().getFluidAmount() + " / "
                    + TileCompresseurEau.TANK_CAPACITY + " mB",
                "\u00a78Eau Voss Froide"
            ), relX, relY);
        }
        // RF bar
        if (relX >= 160 && relX <= 168 && relY >= 22 && relY <= 74) {
            drawHoveringText(Arrays.asList(
                "\u00a7eEnergie\u00a7r: " + GuiUtils.formatRf(tile.getEnergyStored())
                    + " / " + GuiUtils.formatRf(tile.getMaxEnergy()) + " RF",
                "\u00a77Conso: 50 RF/t quand actif"
            ), relX, relY);
        }
        // Progress bar
        if (relX >= 60 && relX <= 110 && relY >= 44 && relY <= 54) {
            int pct = (int)((float) tile.getProgress() / tile.getMaxProgress() * 100);
            drawHoveringText(Arrays.asList(
                "\u00a7aProgress: " + pct + "%",
                "\u00a77" + tile.getProgress() + " / " + tile.getMaxProgress() + " ticks",
                "\u00a78200 mB in -> 150 mB out (perte 50 mB)"
            ), relX, relY);
        }
    }

    @Override
    protected void mouseClicked(int mx, int my, int btn) throws IOException {
        super.mouseClicked(mx, my, btn);
    }
}
