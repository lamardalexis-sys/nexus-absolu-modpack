package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CondenseurRecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static List<Recipe> getRecipes() { return RECIPES; }

    static {
        // === CREATION: Craft CM 3x3 from scratch ===
        // Slot0: poop, Slot1: poop, Slot2: machine frame, Slot3: nexus wall
        // 30s @ 25 RF/t = 15,000 RF total
        addGenericRecipe(
            "cropdusting:poop", 0,
            "cropdusting:poop", 0,
            "thermalexpansion:frame", 0,
            "nexusabsolu:nexus_wall", 0,
            "compactmachines3:machine", 0, 600, 25);

        // === FUSION: 2x same tier CM + key + catalyst -> next tier ===
        // Tier 1: 2x Tiny(3x3) -> Small(5x5) | 60s @ 50 RF/t = 60,000 RF
        addFusionRecipe("compactmachines3:machine", 0,
                  ModItems.COMPACT_KEY_5X5, ModItems.CATALYSEUR_INSTABLE,
                  "compactmachines3:machine", 1, 1200, 50);

        // Tier 2: 2x Small(5x5) -> Normal(7x7) | 90s @ 80 RF/t = 144,000 RF
        addFusionRecipe("compactmachines3:machine", 1,
                  ModItems.COMPACT_KEY_7X7, ModItems.CATALYSEUR_VOLATILE,
                  "compactmachines3:machine", 2, 1800, 80);

        // Tier 3: 2x Normal(7x7) -> Large(9x9) | 120s @ 120 RF/t = 288,000 RF
        addFusionRecipe("compactmachines3:machine", 2,
                  ModItems.COMPACT_KEY_9X9, ModItems.CATALYSEUR_CRITIQUE,
                  "compactmachines3:machine", 3, 2400, 120);

        // Tier 4: 2x Large(9x9) -> Giant(11x11) | 180s @ 200 RF/t = 720,000 RF
        addFusionRecipe("compactmachines3:machine", 3,
                  ModItems.COMPACT_KEY_11X11, ModItems.CATALYSEUR_RESONANT,
                  "compactmachines3:machine", 4, 3600, 200);

        // Tier 5: 2x Giant(11x11) -> Maximum(13x13) | 300s @ 350 RF/t = 2,100,000 RF
        addFusionRecipe("compactmachines3:machine", 4,
                  ModItems.COMPACT_KEY_13X13, ModItems.CATALYSEUR_ABSOLU,
                  "compactmachines3:machine", 5, 6000, 350);
    }

    private static void addGenericRecipe(String id0, int meta0, String id1, int meta1,
                                          String id2, int meta2, String id3, int meta3,
                                          String outputId, int outputMeta,
                                          int processTime, int rfCost) {
        RECIPES.add(new Recipe(id0, meta0, id1, meta1, id2, meta2, id3, meta3,
                               outputId, outputMeta, processTime, rfCost));
    }

    private static void addFusionRecipe(String inputId, int inputMeta,
                                         Item key, Item catalyst,
                                         String outputId, int outputMeta,
                                         int processTime, int rfCost) {
        String keyId = key.getRegistryName().toString();
        String catId = catalyst.getRegistryName().toString();
        RECIPES.add(new Recipe(inputId, inputMeta, inputId, inputMeta,
                               keyId, 0, catId, 0,
                               outputId, outputMeta, processTime, rfCost));
    }

    public static Recipe findRecipe(ItemStack s0, ItemStack s1, ItemStack s2, ItemStack s3) {
        if (s0.isEmpty() || s1.isEmpty() || s2.isEmpty() || s3.isEmpty()) return null;

        for (Recipe r : RECIPES) {
            if (r.matches(s0, s1, s2, s3)) return r;
        }
        return null;
    }

    public static class Recipe {
        public final String id0, id1, id2, id3;
        public final int meta0, meta1, meta2, meta3;
        public final String outputId;
        public final int outputMeta;
        public final int processTime;
        public final int rfPerTick;

        public Recipe(String id0, int meta0, String id1, int meta1,
                     String id2, int meta2, String id3, int meta3,
                     String outputId, int outputMeta, int processTime, int rfPerTick) {
            this.id0 = id0; this.meta0 = meta0;
            this.id1 = id1; this.meta1 = meta1;
            this.id2 = id2; this.meta2 = meta2;
            this.id3 = id3; this.meta3 = meta3;
            this.outputId = outputId;
            this.outputMeta = outputMeta;
            this.processTime = processTime;
            this.rfPerTick = rfPerTick;
        }

        private boolean slotMatches(ItemStack stack, String id, int meta) {
            String stackId = stack.getItem().getRegistryName().toString();
            return stackId.equals(id) && stack.getMetadata() == meta;
        }

        public boolean matches(ItemStack s0, ItemStack s1, ItemStack s2, ItemStack s3) {
            return slotMatches(s0, id0, meta0)
                && slotMatches(s1, id1, meta1)
                && slotMatches(s2, id2, meta2)
                && slotMatches(s3, id3, meta3);
        }

        public ItemStack getOutput() {
            Item outputItem = Item.getByNameOrId(outputId);
            if (outputItem != null) {
                return new ItemStack(outputItem, 1, outputMeta);
            }
            return ItemStack.EMPTY;
        }

        public ItemStack getSlot0() { return makeStack(id0, meta0); }
        public ItemStack getSlot1() { return makeStack(id1, meta1); }
        public ItemStack getSlot2() { return makeStack(id2, meta2); }
        public ItemStack getSlot3() { return makeStack(id3, meta3); }

        private ItemStack makeStack(String id, int meta) {
            Item item = Item.getByNameOrId(id);
            return item != null ? new ItemStack(item, 1, meta) : ItemStack.EMPTY;
        }
    }
}
