package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class Diarh33Wrapper implements IRecipeWrapper {

    private final ItemStack foodExample;

    public Diarh33Wrapper(ItemStack food) {
        this.foodExample = food;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputs(VanillaTypes.ITEM,
            Collections.singletonList(foodExample));
    }

    @Override
    public void drawInfo(Minecraft mc, int w, int h, int mx, int my) {
        mc.fontRenderer.drawString("\u2192", 30, 24, 0xDD88FF);
        mc.fontRenderer.drawString("+ 100mB H2O", 42, 8, 0x4488CC);
        mc.fontRenderer.drawString("+ " + TileMachineHumaine.RF_PER_TICK
            + " Bio-E/t", 42, 18, 0xCC4444);
        mc.fontRenderer.drawString("\u2192 250mB Diarrhee", 42, 32, 0x8B6914);
        mc.fontRenderer.drawString(
            TileMachineHumaine.PROCESS_TIME / 20 + "s par cycle",
            42, 46, 0x888888);
    }
}
