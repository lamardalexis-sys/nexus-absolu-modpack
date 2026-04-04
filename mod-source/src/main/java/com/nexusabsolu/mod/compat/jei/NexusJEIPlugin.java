package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.CondenseurRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import mezz.jei.api.ingredients.VanillaTypes;

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
        // Condenseur recipes
        List<CondenseurWrapper> wrappers = new ArrayList<>();
        for (CondenseurRecipes.Recipe recipe : CondenseurRecipes.getRecipes()) {
            wrappers.add(new CondenseurWrapper(recipe));
        }
        registry.addRecipes(wrappers, CondenseurCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSEUR), CondenseurCategory.UID);
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.CONDENSEUR_T2), CondenseurCategory.UID);

        // JEI exclusion zone for Convertisseur config panel
        registry.addAdvancedGuiHandlers(new ConvertisseurGuiHandler());

        // === PIOCHE RENFORCEE DROPS ===
        String renfHeader = "Pioche Renforcee sur mur Compact Machine:\n\n";
        String renfDrops =
            "12%  Grit de Fer\n" +
            "13%  Grit de Cuivre\n" +
            "10%  Grit d'Etain\n" +
            "10%  Charbon\n" +
            "8%   Redstone\n" +
            "5%   Grit de Nickel\n" +
            "5.5% Compose A\n" +
            "36.5% Poussiere de Mur";

        registry.addIngredientInfo(new ItemStack(ModItems.IRON_GRIT),
            VanillaTypes.ITEM, renfHeader + renfDrops);
        registry.addIngredientInfo(new ItemStack(ModItems.COPPER_GRIT),
            VanillaTypes.ITEM, renfHeader + renfDrops);
        registry.addIngredientInfo(new ItemStack(ModItems.TIN_GRIT),
            VanillaTypes.ITEM, renfHeader + renfDrops);
        registry.addIngredientInfo(new ItemStack(ModItems.NICKEL_GRIT),
            VanillaTypes.ITEM, renfHeader + renfDrops);
        registry.addIngredientInfo(new ItemStack(ModItems.COMPOSE_A),
            VanillaTypes.ITEM, renfHeader + renfDrops +
            "\n\nLe Compose A est un cristal d'energie.\n9x Compose A = 1 Bloc de Compose A");

        // === PIOCHE FRAGMENTEE DROPS ===
        String fragHeader = "Pioche Fragmentee sur mur Compact Machine:\n\n";
        String fragDrops =
            "100% Poussiere de Mur (1-2)\n" +
            "30%  Fragment de Cobble\n" +
            "20%  Silex\n" +
            "15%  Boulette d'Argile";

        registry.addIngredientInfo(new ItemStack(ModItems.WALL_DUST),
            VanillaTypes.ITEM,
            "Mains nues (mur CM):\n" +
            "20%  Poussiere de Mur\n" +
            "25%  Baton\n" +
            "20%  Planche\n" +
            "10%  Fragment de Cobble\n" +
            "25%  Rien\n\n" +
            fragHeader + fragDrops + "\n\n" +
            renfHeader + renfDrops);

        registry.addIngredientInfo(new ItemStack(ModItems.COBBLESTONE_FRAGMENT),
            VanillaTypes.ITEM, fragHeader + fragDrops);

        // === CONVERTISSEUR ===
        registry.addIngredientInfo(new ItemStack(ModBlocks.CONVERTISSEUR),
            VanillaTypes.ITEM,
            "Convertisseur du Dr. Voss\n\n" +
            "Place des Blocs de Compose a cote.\n" +
            "Genere du RF et le pousse aux machines voisines.\n\n" +
            "Plus de blocs = plus de RF.\n" +
            "Rendement decroissant au-dela de 3 blocs.");
    }
}
