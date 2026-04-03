package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.tiles.AtelierRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class AtelierJEIPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
            new AtelierRecipeCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        // Wrap all Atelier recipes
        List<AtelierRecipeWrapper> wrappers = new ArrayList<>();
        for (AtelierRecipes.Recipe recipe : AtelierRecipes.getRecipes()) {
            wrappers.add(new AtelierRecipeWrapper(recipe));
        }

        registry.addRecipes(wrappers, AtelierRecipeCategory.UID);

        // Atelier block as catalyst
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.ATELIER), AtelierRecipeCategory.UID);
    }
}
