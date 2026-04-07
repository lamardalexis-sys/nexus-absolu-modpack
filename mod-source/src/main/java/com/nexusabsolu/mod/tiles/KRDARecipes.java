package com.nexusabsolu.mod.tiles;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry des recettes pour la Machine Voss KRDA125.
 *
 * Pattern identique a CondenseurRecipes : noms d'items et oredicts sont
 * stockes en string, resolus a la demande pour eviter les NPE au load.
 *
 * Une recette KRDA = (input item OU oredict) + fluide diarrhee + RF -> output.
 *
 * Pour ajouter une recette : appeler addRecipe() ou addOredictRecipe() dans
 * le static block ci-dessous. JEI et le tile entity prennent en charge
 * automatiquement les nouvelles recettes via getRecipes() / findRecipe().
 */
public class KRDARecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static List<Recipe> getRecipes() { return RECIPES; }

    static {
        // === Recette 1 : Signalum -> Signalhee (Age 1, originale) ===
        // 1x ingotSignalum + 500 mB diarrhee + 50 RF/t pendant 10s -> 1x signalhee
        addOredictRecipe(
            "ingotSignalum",                       // oredict input
            "thermalfoundation:material", 165,     // fallback item (TF signalum ingot)
            500,                                    // mB diarrhee
            "nexusabsolu:signalhee_ingot", 0, 1,   // output: signalhee x1
            200,                                    // 10 secondes
            50);                                    // 50 RF/t

        // === Recette 2 : Grains of Infinity -> Ender Pearl (Age 1, post CM 9x9) ===
        // Voss a decouvert que la diarrhee fermentee au contact de la matiere
        // primordiale (poussiere de bedrock) cree une instabilite dimensionnelle.
        // 1x dustBedrock + 800 mB diarrhee + 50 RF/t pendant 10s -> 1x ender_pearl
        addOredictRecipe(
            "dustBedrock",                          // oredict input
            "enderio:item_material", 20,            // fallback item (Grains of Infinity)
            800,                                    // mB diarrhee
            "minecraft:ender_pearl", 0, 1,          // output: ender pearl x1
            200,                                    // 10 secondes (meme cadence)
            50);                                    // 50 RF/t (meme cout RF)
    }

    // ==================== HELPERS ====================

    private static void addOredictRecipe(String oredictName,
                                          String fallbackId, int fallbackMeta,
                                          int fluidAmountMb,
                                          String outputId, int outputMeta, int outputCount,
                                          int processTime, int rfPerTick) {
        RECIPES.add(new Recipe(oredictName, fallbackId, fallbackMeta,
                                fluidAmountMb,
                                outputId, outputMeta, outputCount,
                                processTime, rfPerTick));
    }

    /**
     * Cherche la premiere recette dont l'input matche le stack donne.
     * Retourne null si aucune recette ne matche.
     */
    public static Recipe findRecipe(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        for (Recipe r : RECIPES) {
            if (r.matchesInput(stack)) return r;
        }
        return null;
    }

    /**
     * Verifie si un stack peut etre insere comme input dans le KRDA125.
     * Utilise par les slot validators (Container, SidedItemHandler, isItemValidForSlot).
     */
    public static boolean isValidInput(ItemStack stack) {
        return findRecipe(stack) != null;
    }

    /**
     * Defaults pour l'affichage GUI quand aucune recette n'est active (idx = -1).
     * Match les valeurs de la 1ere recette pour coherence visuelle.
     */
    public static final int DEFAULT_PROCESS_TIME = 200;
    public static final int DEFAULT_RF_PER_TICK = 50;

    /**
     * Retourne le processTime de la recette a l'index donne, ou DEFAULT_PROCESS_TIME
     * si l'index est invalide. Utilise par le GUI client via le field sync.
     */
    public static int getProcessTimeForIdx(int idx) {
        if (idx < 0 || idx >= RECIPES.size()) return DEFAULT_PROCESS_TIME;
        return RECIPES.get(idx).processTime;
    }

    /**
     * Retourne le rfPerTick de la recette a l'index donne, ou DEFAULT_RF_PER_TICK
     * si l'index est invalide. Utilise par le GUI client via le field sync.
     */
    public static int getRfPerTickForIdx(int idx) {
        if (idx < 0 || idx >= RECIPES.size()) return DEFAULT_RF_PER_TICK;
        return RECIPES.get(idx).rfPerTick;
    }

    // ==================== RECIPE INNER CLASS ====================

    public static class Recipe {
        public final String oredictName;       // null si pas d'oredict
        public final String fallbackId;        // registry name pour le fallback / display
        public final int fallbackMeta;
        public final int fluidAmountMb;
        public final String outputId;
        public final int outputMeta;
        public final int outputCount;
        public final int processTime;
        public final int rfPerTick;

        public Recipe(String oredictName,
                      String fallbackId, int fallbackMeta,
                      int fluidAmountMb,
                      String outputId, int outputMeta, int outputCount,
                      int processTime, int rfPerTick) {
            this.oredictName = oredictName;
            this.fallbackId = fallbackId;
            this.fallbackMeta = fallbackMeta;
            this.fluidAmountMb = fluidAmountMb;
            this.outputId = outputId;
            this.outputMeta = outputMeta;
            this.outputCount = outputCount;
            this.processTime = processTime;
            this.rfPerTick = rfPerTick;
        }

        /**
         * Verifie si le stack donne matche cette recette.
         * Match par oredict en priorite, fallback sur registry name + meta.
         */
        public boolean matchesInput(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;

            // 1) Match par OreDictionary
            if (oredictName != null) {
                int[] ids = OreDictionary.getOreIDs(stack);
                int target = OreDictionary.getOreID(oredictName);
                for (int id : ids) {
                    if (id == target) return true;
                }
            }

            // 2) Fallback : match direct registry name + meta
            if (fallbackId != null) {
                String stackId = stack.getItem().getRegistryName() != null
                    ? stack.getItem().getRegistryName().toString() : "";
                if (stackId.equals(fallbackId) && stack.getMetadata() == fallbackMeta) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Construit le stack output de cette recette.
         * Resolution lazy depuis le registry pour eviter NPE au load.
         */
        public ItemStack getOutput() {
            Item item = Item.getByNameOrId(outputId);
            if (item != null) {
                return new ItemStack(item, outputCount, outputMeta);
            }
            return ItemStack.EMPTY;
        }

        /**
         * Liste de tous les ItemStacks qui matchent cet input.
         * Utilise par JEI pour afficher toutes les variantes (oredict expansion).
         * Retourne au minimum le fallback si l'oredict est vide ou null.
         */
        public List<ItemStack> getDisplayInputs() {
            if (oredictName != null) {
                List<ItemStack> ores = OreDictionary.getOres(oredictName);
                if (ores != null && !ores.isEmpty()) {
                    // Copie defensive avec count=1 pour l'affichage
                    List<ItemStack> copy = new ArrayList<ItemStack>();
                    for (ItemStack s : ores) {
                        if (!s.isEmpty()) {
                            ItemStack c = s.copy();
                            c.setCount(1);
                            copy.add(c);
                        }
                    }
                    if (!copy.isEmpty()) return copy;
                }
            }
            // Fallback si oredict vide
            ItemStack fb = getFallbackStack();
            return fb.isEmpty() ? Collections.<ItemStack>emptyList()
                                : Collections.singletonList(fb);
        }

        /**
         * Construit le stack du fallback (item utilise pour le display si l'oredict est vide).
         */
        public ItemStack getFallbackStack() {
            if (fallbackId == null) return ItemStack.EMPTY;
            Item item = Item.getByNameOrId(fallbackId);
            return item != null ? new ItemStack(item, 1, fallbackMeta) : ItemStack.EMPTY;
        }
    }
}
