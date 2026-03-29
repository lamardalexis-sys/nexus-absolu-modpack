package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.tiles.CondenseurRecipes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CondenseurWrapper implements IRecipeWrapper {

    private final CondenseurRecipes.Recipe recipe;

    public CondenseurWrapper(CondenseurRecipes.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        Item inputItem = Item.getByNameOrId(recipe.inputId);
        if (inputItem != null) {
            inputs.add(new ItemStack(inputItem, 1, recipe.inputMeta));
            inputs.add(new ItemStack(inputItem, 1, recipe.inputMeta));
        }
        inputs.add(new ItemStack(recipe.key));
        inputs.add(new ItemStack(recipe.catalyst));

        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        minecraft.fontRenderer.drawString("\u2192", 68, 24, 0xBB86FC);
        String time = recipe.processTime / 20 + "s";
        minecraft.fontRenderer.drawString(time, 65, 45, 0x888888);
        String rf = recipe.rfCost + " RF";
        minecraft.fontRenderer.drawString(rf, 58, 5, 0xCC3333);
    }
}
