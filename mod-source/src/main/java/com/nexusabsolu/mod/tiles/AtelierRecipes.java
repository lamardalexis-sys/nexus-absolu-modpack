package com.nexusabsolu.mod.tiles;

import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AtelierRecipes {

    private static final List<Recipe> RECIPES = new ArrayList<>();

    public static List<Recipe> getRecipes() { return RECIPES; }

    static {
        // Pioche Fragmentee : 2 planks + 1 stick
        addRecipe(Item.getItemFromBlock(Blocks.PLANKS), 2, Items.STICK, 1,
                  ModItems.PIOCHE_FRAGMENTEE, 1);

        // Pioche Renforcee : 2 iron nuggets + 1 wall_dust
        addRecipe(Items.IRON_NUGGET, 2, ModItems.WALL_DUST, 1,
                  ModItems.PIOCHE_RENFORCEE, 1);
    }

    private static void addRecipe(Item input1, int count1, Item input2, int count2,
                                   Item output, int outCount) {
        RECIPES.add(new Recipe(input1, count1, input2, count2, output, outCount));
    }

    public static Recipe findRecipe(ItemStack slot1, ItemStack slot2) {
        for (Recipe r : RECIPES) {
            if (r.matches(slot1, slot2)) return r;
            if (r.matchesReversed(slot1, slot2)) return r;
        }
        return null;
    }

    public static class Recipe {
        public final Item input1;
        public final int input1Count;
        public final Item input2;
        public final int input2Count;
        public final Item output;
        public final int outputCount;

        public Recipe(Item in1, int c1, Item in2, int c2, Item out, int outC) {
            this.input1 = in1; this.input1Count = c1;
            this.input2 = in2; this.input2Count = c2;
            this.output = out; this.outputCount = outC;
        }

        public boolean matches(ItemStack s1, ItemStack s2) {
            return s1.getItem() == input1 && s1.getCount() >= input1Count
                && s2.getItem() == input2 && s2.getCount() >= input2Count;
        }

        public boolean matchesReversed(ItemStack s1, ItemStack s2) {
            return s1.getItem() == input2 && s1.getCount() >= input2Count
                && s2.getItem() == input1 && s2.getCount() >= input1Count;
        }

        public ItemStack getOutput() {
            return new ItemStack(output, outputCount);
        }
    }
}
