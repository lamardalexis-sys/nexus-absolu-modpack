package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.CondenseurRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import mezz.jei.api.ingredients.VanillaTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JEIPlugin
public class NexusJEIPlugin implements IModPlugin {

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(
            new CondenseurCategory(registry.getJeiHelpers().getGuiHelper()),
            new Diarh33Category(registry.getJeiHelpers().getGuiHelper()),
            new KRDACategory(registry.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void register(IModRegistry registry) {
        // === CONDENSEUR ===
        List<CondenseurWrapper> condenseurWrappers = new ArrayList<>();
        for (CondenseurRecipes.Recipe recipe : CondenseurRecipes.getRecipes()) {
            condenseurWrappers.add(new CondenseurWrapper(recipe));
        }
        registry.addRecipes(condenseurWrappers, CondenseurCategory.UID);
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.CONDENSEUR), CondenseurCategory.UID);
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.CONDENSEUR_T2), CondenseurCategory.UID);

        // === DIARH33 ===
        List<Diarh33Wrapper> diarh33Wrappers = new ArrayList<>();
        // Show common food examples
        diarh33Wrappers.add(new Diarh33Wrapper(new ItemStack(Items.APPLE)));
        diarh33Wrappers.add(new Diarh33Wrapper(new ItemStack(Items.BREAD)));
        diarh33Wrappers.add(new Diarh33Wrapper(new ItemStack(Items.COOKED_BEEF)));
        diarh33Wrappers.add(new Diarh33Wrapper(new ItemStack(Items.BAKED_POTATO)));
        registry.addRecipes(diarh33Wrappers, Diarh33Category.UID);
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.MACHINE_HUMAINE), Diarh33Category.UID);

        // === KRDA125 ===
        registry.addRecipes(
            Collections.singletonList(new KRDAWrapper()), KRDACategory.UID);
        registry.addRecipeCatalyst(
            new ItemStack(ModBlocks.MACHINE_KRDA), KRDACategory.UID);

        // === JEI EXCLUSION ZONES ===
        registry.addAdvancedGuiHandlers(new ConvertisseurGuiHandler());
        registry.addAdvancedGuiHandlers(new MachineHumaineGuiHandler());
        registry.addAdvancedGuiHandlers(new MachineKRDAGuiHandler());

        // === ITEM INFO ===
        // Signalhee ingot
        registry.addIngredientInfo(
            new ItemStack(ModItems.SIGNALHEE_INGOT), VanillaTypes.ITEM,
            "Lingot de Signalhee\n\n" +
            "Obtenu via:\n" +
            "- Machine Voss KRDA125 (Signalum + Diarrhee)\n\n" +
            "Utilise pour:\n" +
            "- Signalhee + Compose B = Compose C\n" +
            "  (Induction Smelter / Alloy Kiln / Alloy Smelter)");

        // Machine descriptions
        registry.addIngredientInfo(
            new ItemStack(ModBlocks.MACHINE_HUMAINE), VanillaTypes.ITEM,
            "Machine Voss Diarh33\n\n" +
            "Transforme la nourriture en Diarrhee Liquide.\n" +
            "Necessite: Food + Eau + Bio-Energie (10 RF/t)\n" +
            "1 Food + 100mB H2O = 250mB+ Diarrhee");

        registry.addIngredientInfo(
            new ItemStack(ModBlocks.MACHINE_KRDA), VanillaTypes.ITEM,
            "Machine Voss KRDA125\n\n" +
            "Transmute le Signalum via la Diarrhee Liquide.\n" +
            "Necessite: Signalum + Diarrhee + Bio-Energie (50 RF/t)\n" +
            "1 Signalum + 500mB Diarrhee = 1 Signalhee");

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
