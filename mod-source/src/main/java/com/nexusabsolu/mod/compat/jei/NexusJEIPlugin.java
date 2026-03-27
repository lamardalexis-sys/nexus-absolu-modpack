package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.tiles.CondenseurRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class NexusJEIPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
            new CondenseurCategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        // Wrap all recipes
        List<CondenseurWrapper> wrappers = new ArrayList<>();
        for (CondenseurRecipes.Recipe recipe : CondenseurRecipes.getRecipes()) {
            wrappers.add(new CondenseurWrapper(recipe));
        }

        registry.addRecipes(wrappers, CondenseurCategory.UID);

        // Condenseur block as catalyst (clickable in JEI)
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSEUR), CondenseurCategory.UID);
    }
}
