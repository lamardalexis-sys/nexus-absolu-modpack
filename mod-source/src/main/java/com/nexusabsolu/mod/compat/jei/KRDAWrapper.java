package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

public class KRDAWrapper implements IRecipeWrapper {

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Item input: signalum ingot
        Item signalumItem = Item.getByNameOrId("thermalfoundation:material");
        ItemStack signalum = signalumItem != null
            ? new ItemStack(signalumItem, 1, 165) : ItemStack.EMPTY;
        ingredients.setInputs(VanillaTypes.ITEM,
            Collections.singletonList(signalum));

        // Item output: signalhee ingot
        ingredients.setOutput(VanillaTypes.ITEM,
            new ItemStack(ModItems.SIGNALHEE_INGOT));

        // Fluid input: diarrhee
        net.minecraftforge.fluids.Fluid diarrhee =
            FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee != null) {
            ingredients.setInput(VanillaTypes.FLUID,
                new FluidStack(diarrhee, TileMachineKRDA.FLUID_PER_CYCLE));
        }
    }
}
