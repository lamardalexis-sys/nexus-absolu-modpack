package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.gui.GuiConvertisseur;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class ConvertisseurGuiHandler implements IAdvancedGuiHandler<GuiConvertisseur> {

    @Override
    public Class<GuiConvertisseur> getGuiContainerClass() {
        return GuiConvertisseur.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiConvertisseur gui) {
        List<Rectangle> areas = new ArrayList<>();
        if (gui.isConfigOpen()) {
            int px = gui.getGuiLeft() + gui.getXSize() + 11;
            int py = gui.getGuiTop() + 10;
            areas.add(new Rectangle(px, py, 80, 110));
        }
        // Tab button
        areas.add(new Rectangle(
            gui.getGuiLeft() + gui.getXSize() - 1,
            gui.getGuiTop() + 20, 12, 20));
        return areas;
    }
}
