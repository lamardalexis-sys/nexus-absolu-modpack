package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.item.ItemStack;

public class Diarh33Category implements IRecipeCategory<Diarh33Wrapper> {

    public static final String UID = Reference.MOD_ID + ".diarh33";
    private final IDrawable background;
    private final IDrawable icon;

    public Diarh33Category(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 60);
        this.icon = guiHelper.createDrawableIngredient(
            new ItemStack(ModBlocks.MACHINE_HUMAINE));
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return "Machine Voss Diarh33"; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, Diarh33Wrapper wrapper,
                           IIngredients ingredients) {
        layout.getItemStacks().init(0, true, 5, 20);  // food input
        layout.getItemStacks().set(ingredients);
    }
}
