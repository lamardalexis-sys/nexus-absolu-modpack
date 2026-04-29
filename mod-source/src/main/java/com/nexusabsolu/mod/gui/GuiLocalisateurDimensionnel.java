package com.nexusabsolu.mod.gui;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.network.PacketTeleportToMachine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * GUI Localisateur Dimensionnel.
 *
 * Affiche la liste des Compact Machines visitees par le joueur (par
 * roomId, dans l'ordre de visite). Click sur une ligne -> envoie un
 * PacketTeleportToMachine au serveur et ferme la GUI.
 *
 * Layout: panneau central 200x200px, titre + sous-titre + liste de
 * boutons "Salle #ID" + bouton "Annuler". Scroll par boutons fleche
 * si la liste depasse VISIBLE_ROWS lignes.
 *
 * Pas de Container ni de TileEntity associe : c'est une GUI pure
 * client, ouverte par PacketOpenLocalisateur en reponse au clic-droit
 * sur l'item.
 */
@SideOnly(Side.CLIENT)
public class GuiLocalisateurDimensionnel extends GuiScreen {

    // Layout
    private static final int PANEL_W = 200;
    private static final int PANEL_H = 200;
    private static final int BTN_W = 160;
    private static final int BTN_H = 20;
    private static final int BTN_SPACING = 22;
    private static final int VISIBLE_ROWS = 6;

    // Button ids (utilises pour distinguer dans actionPerformed)
    private static final int ID_SCROLL_UP   = 9000;
    private static final int ID_SCROLL_DOWN = 9001;
    private static final int ID_CANCEL      = 9002;
    // Les boutons "Salle #ID" ont les ids 0..machineIds.length-1

    private final int[] machineIds;
    private int scrollOffset = 0;

    public GuiLocalisateurDimensionnel(int[] machineIds) {
        this.machineIds = machineIds != null ? machineIds : new int[0];
    }

    @Override
    public void initGui() {
        super.initGui();
        rebuildButtons();
    }

    /** Reconstruit la liste de boutons en fonction du scrollOffset courant. */
    private void rebuildButtons() {
        this.buttonList.clear();

        int panelLeft = (this.width - PANEL_W) / 2;
        int panelTop  = (this.height - PANEL_H) / 2;
        int btnLeft   = panelLeft + (PANEL_W - BTN_W) / 2;
        int listTop   = panelTop + 50; // sous le titre + sous-titre

        // Boutons "Salle #ID" pour les machines visibles
        int total = machineIds.length;
        int firstVisible = scrollOffset;
        int lastVisible = Math.min(total, firstVisible + VISIBLE_ROWS);

        for (int i = firstVisible; i < lastVisible; i++) {
            int rowIndex = i - firstVisible;
            int y = listTop + rowIndex * BTN_SPACING;
            // L'id du bouton = i (index reel dans machineIds), pas rowIndex
            GuiButton btn = new GuiButton(i, btnLeft, y, BTN_W, BTN_H,
                "Salle #" + machineIds[i]);
            this.buttonList.add(btn);
        }

        // Boutons scroll si necessaire
        if (total > VISIBLE_ROWS) {
            int scrollY = panelTop + PANEL_H - 56;
            // Up
            GuiButton up = new GuiButton(ID_SCROLL_UP,
                panelLeft + 30, scrollY, 60, BTN_H, "Haut");
            up.enabled = (scrollOffset > 0);
            this.buttonList.add(up);
            // Down
            GuiButton down = new GuiButton(ID_SCROLL_DOWN,
                panelLeft + PANEL_W - 90, scrollY, 60, BTN_H, "Bas");
            down.enabled = (scrollOffset + VISIBLE_ROWS < total);
            this.buttonList.add(down);
        }

        // Bouton Annuler en bas
        GuiButton cancel = new GuiButton(ID_CANCEL,
            btnLeft, panelTop + PANEL_H - 30, BTN_W, BTN_H, "Annuler");
        this.buttonList.add(cancel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fond assombri
        this.drawDefaultBackground();

        // Panneau central
        int panelLeft = (this.width - PANEL_W) / 2;
        int panelTop  = (this.height - PANEL_H) / 2;
        // Bordure exterieure (cadre dore)
        Gui.drawRect(panelLeft - 2, panelTop - 2,
            panelLeft + PANEL_W + 2, panelTop + PANEL_H + 2, 0xFFB48438);
        // Fond interieur (violet sombre, opacite ~95%)
        Gui.drawRect(panelLeft, panelTop,
            panelLeft + PANEL_W, panelTop + PANEL_H, 0xF0140820);

        // Titre
        String title = "Localisateur Dimensionnel";
        this.drawCenteredString(this.fontRenderer, title,
            this.width / 2, panelTop + 12, 0xFFFFD86C);

        // Sous-titre
        String subtitle;
        if (machineIds.length == 0) {
            subtitle = "Aucune salle visitee";
        } else if (machineIds.length == 1) {
            subtitle = "1 salle connue";
        } else {
            subtitle = machineIds.length + " salles connues";
        }
        this.drawCenteredString(this.fontRenderer, subtitle,
            this.width / 2, panelTop + 28, 0xFFAAAAAA);

        // Si vide, message d'aide
        if (machineIds.length == 0) {
            this.drawCenteredString(this.fontRenderer,
                "Visite une Compact Machine pour la memoriser.",
                this.width / 2, panelTop + 90, 0xFF888888);
        }

        // Boutons (appel super pour rendu standard)
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == ID_CANCEL) {
            this.mc.displayGuiScreen(null);
            return;
        }
        if (button.id == ID_SCROLL_UP) {
            scrollOffset = Math.max(0, scrollOffset - VISIBLE_ROWS);
            rebuildButtons();
            return;
        }
        if (button.id == ID_SCROLL_DOWN) {
            int max = Math.max(0, machineIds.length - VISIBLE_ROWS);
            scrollOffset = Math.min(max, scrollOffset + VISIBLE_ROWS);
            rebuildButtons();
            return;
        }
        // Sinon : id = index dans machineIds
        if (button.id >= 0 && button.id < machineIds.length) {
            int targetId = machineIds[button.id];
            NexusAbsoluMod.LOGGER.info(
                "[Localisateur] Click TP vers room #" + targetId);
            NexusPacketHandler.INSTANCE.sendToServer(
                new PacketTeleportToMachine(targetId));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        // Support scroll molette
        int wheel = org.lwjgl.input.Mouse.getEventDWheel();
        if (wheel != 0 && machineIds.length > VISIBLE_ROWS) {
            int max = Math.max(0, machineIds.length - VISIBLE_ROWS);
            if (wheel > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else {
                scrollOffset = Math.min(max, scrollOffset + 1);
            }
            rebuildButtons();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
