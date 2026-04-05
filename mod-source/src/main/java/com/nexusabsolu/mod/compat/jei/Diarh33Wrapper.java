package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collections;

public class Diarh33Wrapper implements IRecipeWrapper {

    private final ItemStack foodExample;

    public Diarh33Wrapper(ItemStack food) {
        this.foodExample = food;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Item input: food
        ingredients.setInputs(VanillaTypes.ITEM,
            Collections.singletonList(foodExample));

        // Fluid input: water
        ingredients.setInput(VanillaTypes.FLUID,
            new FluidStack(FluidRegistry.WATER,
                TileMachineHumaine.WATER_PER_CYCLE));

        // Fluid output: diarrhee
        net.minecraftforge.fluids.Fluid diarrhee =
            FluidRegistry.getFluid("diarrhee_liquide");
        if (diarrhee != null) {
            ingredients.setOutput(VanillaTypes.FLUID,
                new FluidStack(diarrhee,
                    TileMachineHumaine.OUTPUT_PER_CYCLE));
        }
    }
}
