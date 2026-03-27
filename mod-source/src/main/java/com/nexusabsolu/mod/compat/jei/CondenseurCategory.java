package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CondenseurCategory implements IRecipeCategory<CondenseurWrapper> {

    public static final String UID = Reference.MOD_ID + ".condenseur";
    private final IDrawable background;
    private final IDrawable icon;
    private final String title = "Condenseur Dimensionnel";

    public CondenseurCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 60);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.CONDENSEUR));
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return title; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, CondenseurWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // Input slots
        stacks.init(0, true, 5, 5);    // CM 1
        stacks.init(1, true, 25, 5);   // CM 2
        stacks.init(2, true, 5, 35);   // Key
        stacks.init(3, true, 25, 35);  // Catalyst

        // Arrow area is visual only

        // Output slot
        stacks.init(4, false, 110, 20); // Output

        stacks.set(ingredients);
    }
}
