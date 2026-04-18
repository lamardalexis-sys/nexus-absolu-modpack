package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.gui.furnaces.GuiFurnaceNexus;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Declare a JEI les zones hors-GUI utilisees par notre Gui Furnace
 * (onglets lateraux + panneaux Config et Upgrades quand ouverts).
 *
 * Sans ce handler, JEI dessine ses items par-dessus nos panneaux.
 * Pattern identique a ConvertisseurGuiHandler / MachineHumaineGuiHandler.
 */
public class FurnaceGuiHandler implements IAdvancedGuiHandler<GuiFurnaceNexus> {

    @Override
    public Class<GuiFurnaceNexus> getGuiContainerClass() {
        return GuiFurnaceNexus.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiFurnaceNexus gui) {
        List<Rectangle> areas = new ArrayList<>();

        int guiLeft = gui.getGuiLeft();
        int guiTop = gui.getGuiTop();
        int guiRight = guiLeft + gui.getXSize();

        // Onglet CONFIG a gauche (x=-13, y=18, 15x17)
        areas.add(new Rectangle(guiLeft - 13, guiTop + 18, 15, 17));

        // Onglet UPGRADES a droite (x=xSize-2, y=18, 15x17)
        areas.add(new Rectangle(guiRight - 2, guiTop + 18, 15, 17));

        // Panneau CONFIG (ouvert a gauche, 130x165) - seulement si configOpen
        if (gui.isConfigOpen()) {
            // CONFIG_W = 130, gap = 6, donc px = guiLeft - 136
            areas.add(new Rectangle(
                guiLeft - 136,
                guiTop + 8,          // py - 2 pour inclure bordure
                135,                  // CONFIG_W + 5 marge
                170                   // CONFIG_H + 5 marge
            ));
        }

        // Panneau UPGRADES (compact, ouvert a droite 66x80) - seulement si upgradesOpen
        if (gui.isUpgradesOpen()) {
            areas.add(new Rectangle(
                guiRight + 0,         // gap=2 dans le GUI, -2 pour marge ok
                guiTop + 8,
                72,                    // UPGRADES_W + 6 marge
                85                     // UPGRADES_H + 5 marge
            ));
        }

        return areas;
    }
}
