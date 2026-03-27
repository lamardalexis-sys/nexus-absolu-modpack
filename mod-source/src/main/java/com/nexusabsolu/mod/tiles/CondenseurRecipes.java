package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CondenseurRecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static List<Recipe> getRecipes() { return RECIPES; }

    static {
        // Format: 2x input CM, key item, catalyst item, output CM, process time, RF cost
        // Tier 1: 2x Tiny(3x3) + Key5x5 + Catalyseur Instable -> Small(5x5)
        addRecipe("compactmachines3:machine", 0,
                  ModItems.COMPACT_KEY_5X5, ModItems.CATALYSEUR_INSTABLE,
                  "compactmachines3:machine", 1, 200, 1000);

        // Tier 2: 2x Small(5x5) + Key7x7 + Catalyseur Volatile -> Normal(7x7)
        addRecipe("compactmachines3:machine", 1,
                  ModItems.COMPACT_KEY_7X7, ModItems.CATALYSEUR_VOLATILE,
                  "compactmachines3:machine", 2, 400, 3000);

        // Tier 3: 2x Normal(7x7) + Key9x9 + Catalyseur Critique -> Large(9x9)
        addRecipe("compactmachines3:machine", 2,
                  ModItems.COMPACT_KEY_9X9, ModItems.CATALYSEUR_CRITIQUE,
                  "compactmachines3:machine", 3, 600, 8000);

        // Tier 4: 2x Large(9x9) + Key11x11 + Catalyseur Resonant -> Giant(11x11)
        addRecipe("compactmachines3:machine", 3,
                  ModItems.COMPACT_KEY_11X11, ModItems.CATALYSEUR_RESONANT,
                  "compactmachines3:machine", 4, 800, 20000);

        // Tier 5: 2x Giant(11x11) + Key13x13 + Catalyseur Absolu -> Maximum(13x13)
        addRecipe("compactmachines3:machine", 4,
                  ModItems.COMPACT_KEY_13X13, ModItems.CATALYSEUR_ABSOLU,
                  "compactmachines3:machine", 5, 1200, 50000);
    }

    private static void addRecipe(String inputId, int inputMeta,
                                   Item key, Item catalyst,
                                   String outputId, int outputMeta,
                                   int processTime, int rfCost) {
        RECIPES.add(new Recipe(inputId, inputMeta, key, catalyst, outputId, outputMeta, processTime, rfCost));
    }

    public static Recipe findRecipe(ItemStack cm1, ItemStack cm2, ItemStack key, ItemStack catalyst) {
        if (cm1.isEmpty() || cm2.isEmpty() || key.isEmpty() || catalyst.isEmpty()) return null;

        for (Recipe r : RECIPES) {
            if (r.matches(cm1, cm2, key, catalyst)) return r;
        }
        return null;
    }

    public static class Recipe {
        public final String inputId;
        public final int inputMeta;
        public final Item key;
        public final Item catalyst;
        public final String outputId;
        public final int outputMeta;
        public final int processTime;
        public final int rfCost;

        public Recipe(String inputId, int inputMeta, Item key, Item catalyst,
                     String outputId, int outputMeta, int processTime, int rfCost) {
            this.inputId = inputId;
            this.inputMeta = inputMeta;
            this.key = key;
            this.catalyst = catalyst;
            this.outputId = outputId;
            this.outputMeta = outputMeta;
            this.processTime = processTime;
            this.rfCost = rfCost;
        }

        public boolean matches(ItemStack cm1, ItemStack cm2, ItemStack keyStack, ItemStack catalystStack) {
            String cm1Id = cm1.getItem().getRegistryName().toString();
            String cm2Id = cm2.getItem().getRegistryName().toString();

            return cm1Id.equals(inputId) && cm1.getMetadata() == inputMeta
                && cm2Id.equals(inputId) && cm2.getMetadata() == inputMeta
                && keyStack.getItem() == key
                && catalystStack.getItem() == catalyst;
        }

        public ItemStack getOutput() {
            Item outputItem = Item.getByNameOrId(outputId);
            if (outputItem != null) {
                return new ItemStack(outputItem, 1, outputMeta);
            }
            return ItemStack.EMPTY;
        }
    }
}
