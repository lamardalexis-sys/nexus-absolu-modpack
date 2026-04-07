package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.tiles.KRDARecipes.Recipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;
import java.util.List;

/**
 * JEI wrapper for a single KRDA125 recipe.
 *
 * One wrapper instance = one recipe. NexusJEIPlugin creates one wrapper per
 * entry in KRDARecipes.getRecipes() so JEI displays all recipes.
 *
 * drawInfo() draws per-recipe stats (RF/t, time, mB) so each recipe shows
 * its own values instead of shared constants from the category.
 */
public class KRDAWrapper implements IRecipeWrapper {

    private final Recipe recipe;

    public KRDAWrapper(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() { return recipe; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Item input: oredict expansion (or fallback if oredict empty)
        List<ItemStack> inputs = recipe.getDisplayInputs();
        if (inputs.isEmpty()) inputs = Collections.singletonList(ItemStack.EMPTY);
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(inputs));

        // Item output
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());

        // Fluid input: diarrhee
        Fluid diarrhee = FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee != null) {
            ingredients.setInput(VanillaTypes.FLUID,
                new FluidStack(diarrhee, recipe.fluidAmountMb));
        }
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight,
                          int mouseX, int mouseY) {
        // Per-recipe stats (replaces the static drawExtras text from the category)
        mc.fontRenderer.drawStringWithShadow(
            recipe.rfPerTick + " Bio-E/t",
            118, 30, 0xFFCC4444);
        mc.fontRenderer.drawStringWithShadow(
            (recipe.processTime / 20) + "s",
            118, 42, 0xFF888888);
        mc.fontRenderer.drawStringWithShadow(
            recipe.fluidAmountMb + "mB",
            118, 54, 0xFF8B6914);
    }
}
