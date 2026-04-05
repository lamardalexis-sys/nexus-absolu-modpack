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

public class KRDACategory implements IRecipeCategory<KRDAWrapper> {

    public static final String UID = Reference.MOD_ID + ".krda";
    private final IDrawable background;
    private final IDrawable icon;

    public KRDACategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 60);
        this.icon = guiHelper.createDrawableIngredient(
            new ItemStack(ModBlocks.MACHINE_KRDA));
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return "Machine Voss KRDA125"; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, KRDAWrapper wrapper,
                           IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(0, true, 5, 20);    // signalum input
        stacks.init(1, false, 130, 20);  // signalhee output
        stacks.set(ingredients);
    }
}
