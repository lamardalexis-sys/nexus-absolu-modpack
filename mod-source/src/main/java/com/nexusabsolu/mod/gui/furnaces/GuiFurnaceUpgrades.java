package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

/**
 * GUI dedie aux Upgrades du Furnace Nexus.
 *
 * Pattern Mekanism (GuiUpgradeManagement) : plutot que d'avoir un side-panel
 * qui cause des conflits de tooltips, on ouvre un GUI complet separe. Le
 * bouton [<-] en haut a gauche permet de revenir au GUI Furnace principal.
 *
 * Layout : 176x166 vanilla-like, style violet Nexus (pas de texture externe,
 * tout dessine via drawRect).
 *  - Barre titre en haut
 *  - Bouton [<-] back en haut a gauche
 *  - 2x2 slots upgrades au centre (y=30)
 *  - Labels sous chaque slot (RF / IO / SP / EF)
 *  - Inventaire joueur en bas (y=84)
 */
public class GuiFurnaceUpgrades extends GuiContainer {

    private final TileFurnaceNexus tile;

    public GuiFurnaceUpgrades(InventoryPlayer playerInv, TileFurnaceNexus tile) {
        super(new ContainerFurnaceUpgrades(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();
        // Bouton back en haut a gauche (id=0)
        buttonList.add(new GuiButton(0, guiLeft + 5, guiTop + 4, 14, 14, "<"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            // Retour au GUI Furnace principal
            BlockPos pos = tile.getPos();
            mc.player.openGui(
                com.nexusabsolu.mod.NexusAbsoluMod.instance,
                com.nexusabsolu.mod.gui.GuiHandler.FURNACE_NEXUS_GUI,
                mc.world,
                pos.getX(), pos.getY(), pos.getZ()
            );
        }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        drawDefaultBackground();
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Fond violet style Nexus (pas de texture, on dessine tout en code)
        int x = guiLeft;
        int y = guiTop;

        // Cadre externe
        drawRect(x - 2, y - 2, x + xSize + 2, y + ySize + 2, 0xFF5030A0);
        drawRect(x - 1, y - 1, x + xSize + 1, y + ySize + 1, 0xFFBB77FF);
        drawRect(x, y, x + xSize, y + ySize, 0xFF1A1030);

        // Barre titre
        drawRect(x + 1, y + 1, x + xSize - 1, y + 20, 0xFF3A1F5E);
        drawRect(x + 3, y + 20, x + xSize - 3, y + 21, 0xFFBB77FF);

        // Cadres violets autour des 4 slots upgrade (positions matchent le Container)
        int slotSize = 18;
        int gap = 4;
        int totalW = slotSize * 2 + gap;
        int startX = x + (xSize - totalW) / 2;
        int startY = y + 30;

        int[][] slotPositions = { {0, 0}, {1, 0}, {0, 1}, {1, 1} };
        for (int i = 0; i < 4; i++) {
            int col = slotPositions[i][0];
            int row = slotPositions[i][1];
            int sx = startX + col * (slotSize + gap) - 1;  // -1 pour le cadre autour
            int sy = startY + row * (slotSize + gap) - 1;
            // Cadre violet autour du slot
            drawRect(sx, sy, sx + slotSize + 2, sy + slotSize + 2, 0xFF8855BB);
            drawRect(sx + 1, sy + 1, sx + slotSize + 1, sy + slotSize + 1, 0xFF0A0818);
        }

        // Labels rangee 1 (RF, IO) sous les slots de la rangee 1
        int labelY1 = startY + slotSize + 3;
        fontRenderer.drawStringWithShadow("RF",
            startX + slotSize / 2 - fontRenderer.getStringWidth("RF") / 2, labelY1, 0xFFFFAAAA);
        fontRenderer.drawStringWithShadow("IO",
            startX + slotSize + gap + slotSize / 2 - fontRenderer.getStringWidth("IO") / 2,
            labelY1, 0xFF88CCFF);

        // Labels rangee 2 (SP, EF) sous les slots de la rangee 2
        int labelY2 = startY + (slotSize + gap) + slotSize + 3;
        fontRenderer.drawStringWithShadow("SP",
            startX + slotSize / 2 - fontRenderer.getStringWidth("SP") / 2, labelY2, 0xFFFFCC88);
        fontRenderer.drawStringWithShadow("EF",
            startX + slotSize + gap + slotSize / 2 - fontRenderer.getStringWidth("EF") / 2,
            labelY2, 0xFF88DD88);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        // Titre centre
        String tierName = tile.getTier().registryName;
        String title = "Upgrades - " + Character.toUpperCase(tierName.charAt(0))
            + tierName.substring(1).replace('_', ' ');
        fontRenderer.drawStringWithShadow(title,
            (xSize - fontRenderer.getStringWidth(title)) / 2, 7, 0xFFFFDD77);

        // Label inventaire joueur (traduit si possible, sinon fallback)
        String invLabel = net.minecraft.client.resources.I18n.format("container.inventory");
        fontRenderer.drawString(invLabel, 8, ySize - 96 + 2, 0xAAAAAA);
    }
}
