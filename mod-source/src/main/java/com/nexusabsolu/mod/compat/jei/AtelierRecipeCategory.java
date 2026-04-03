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

public class AtelierRecipeCategory implements IRecipeCategory<AtelierRecipeWrapper> {

    public static final String UID = Reference.MOD_ID + ".atelier";

    private final IDrawable background;
    private final IDrawable icon;
    private final String title;

    public AtelierRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 40);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.ATELIER));
        this.title = "Atelier du Dr. Voss";
    }

    @Override
    public String getUid() { return UID; }

    @Override
    public String getTitle() { return title; }

    @Override
    public String getModName() { return "Nexus Absolu"; }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, AtelierRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // Input slot 1 (left)
        stacks.init(0, true, 0, 11);
        // Input slot 2 (center-left)
        stacks.init(1, true, 24, 11);
        // Output slot (right)
        stacks.init(2, false, 94, 11);

        stacks.set(ingredients);
    }
}
