package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.gui.GuiMachineHumaine;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class MachineHumaineGuiHandler implements IAdvancedGuiHandler<GuiMachineHumaine> {

    @Override
    public Class<GuiMachineHumaine> getGuiContainerClass() {
        return GuiMachineHumaine.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiMachineHumaine gui) {
        List<Rectangle> areas = new ArrayList<>();
        // Config tab (left of GUI)
        areas.add(new Rectangle(gui.getGuiLeft() - 28, gui.getGuiTop() + 16, 28, 25));
        // Config panel (far left when open)
        if (gui.isConfigOpen()) {
            areas.add(new Rectangle(
                gui.getGuiLeft() - 200, gui.getGuiTop() + 42, 190, 210));
        }
        return areas;
    }
}
