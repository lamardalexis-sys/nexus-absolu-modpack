package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class KRDAWrapper implements IRecipeWrapper {

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Signalum ingot = thermalfoundation:material:165
        Item signalumItem = Item.getByNameOrId("thermalfoundation:material");
        ItemStack signalum = signalumItem != null
            ? new ItemStack(signalumItem, 1, 165) : ItemStack.EMPTY;
        ingredients.setInputs(VanillaTypes.ITEM,
            Collections.singletonList(signalum));
        ingredients.setOutput(VanillaTypes.ITEM,
            new ItemStack(ModItems.SIGNALHEE_INGOT));
    }

    @Override
    public void drawInfo(Minecraft mc, int w, int h, int mx, int my) {
        mc.fontRenderer.drawString("\u2192", 28, 24, 0xFFFF8800);
        mc.fontRenderer.drawString("+ " + TileMachineKRDA.FLUID_PER_CYCLE
            + "mB Diarrhee", 40, 8, 0x8B6914);
        mc.fontRenderer.drawString("+ " + TileMachineKRDA.RF_PER_TICK
            + " Bio-E/t", 40, 18, 0xCC4444);
        mc.fontRenderer.drawString("\u2192 Signalhee", 40, 32, 0xFF8800);
        mc.fontRenderer.drawString(
            TileMachineKRDA.PROCESS_TIME / 20 + "s par cycle",
            40, 46, 0x888888);
    }
}
