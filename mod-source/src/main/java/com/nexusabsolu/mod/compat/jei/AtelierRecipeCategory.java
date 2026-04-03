package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class AtelierRecipeCategory implements IRecipeCategory<AtelierRecipeWrapper> {

    public static final String UID = Reference.MOD_ID + ".atelier";
    private static final ResourceLocation VANILLA_GUI = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic slot;
    private final IDrawableStatic arrow;
    private final String title;

    public AtelierRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(140, 36);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(ModBlocks.ATELIER));
        this.slot = guiHelper.getSlotDrawable();
        this.arrow = guiHelper.createDrawable(VANILLA_GUI, 82, 128, 24, 17);
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
    public void drawExtras(Minecraft minecraft) {
        // Draw slot backgrounds
        slot.draw(minecraft, 4, 9);     // Input 1
        slot.draw(minecraft, 30, 9);    // Input 2
        slot.draw(minecraft, 114, 9);   // Output

        // Draw arrow
        arrow.draw(minecraft, 62, 10);

        // Draw "+" between inputs
        minecraft.fontRenderer.drawString("+", 24, 14, 0x404040);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, AtelierRecipeWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();

        // Input slot 1
        stacks.init(0, true, 4, 9);
        // Input slot 2
        stacks.init(1, true, 30, 9);
        // Output slot
        stacks.init(2, false, 114, 9);

        stacks.set(ingredients);
    }
}
