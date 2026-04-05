package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.gui.GuiMachineKRDA;
import mezz.jei.api.gui.IAdvancedGuiHandler;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class MachineKRDAGuiHandler implements IAdvancedGuiHandler<GuiMachineKRDA> {

    @Override
    public Class<GuiMachineKRDA> getGuiContainerClass() {
        return GuiMachineKRDA.class;
    }

    @Override
    public List<Rectangle> getGuiExtraAreas(GuiMachineKRDA gui) {
        List<Rectangle> areas = new ArrayList<>();
        areas.add(new Rectangle(gui.getGuiLeft() - 28, gui.getGuiTop() + 16, 28, 25));
        if (gui.isConfigOpen()) {
            areas.add(new Rectangle(
                gui.getGuiLeft() - 200, gui.getGuiTop() + 42, 190, 210));
        }
        return areas;
    }
}
