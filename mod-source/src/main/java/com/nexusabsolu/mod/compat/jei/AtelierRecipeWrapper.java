package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.tiles.AtelierRecipes;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AtelierRecipeWrapper implements IRecipeWrapper {

    private final AtelierRecipes.Recipe recipe;

    public AtelierRecipeWrapper(AtelierRecipes.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(new ItemStack(recipe.input1, recipe.input1Count));
        inputs.add(new ItemStack(recipe.input2, recipe.input2Count));
        ingredients.setInputs(VanillaTypes.ITEM, inputs);
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }
}
