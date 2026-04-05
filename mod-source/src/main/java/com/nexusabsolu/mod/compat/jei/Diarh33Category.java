package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiFluidStackGroup;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class Diarh33Category implements IRecipeCategory<Diarh33Wrapper> {

    public static final String UID = Reference.MOD_ID + ".diarh33";
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/jei_nexus.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public Diarh33Category(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 160, 70);
        this.icon = guiHelper.createDrawableIngredient(
            new ItemStack(ModBlocks.MACHINE_HUMAINE));

        IDrawableStatic arrowStatic = guiHelper.createDrawable(
            TEXTURE, 24, 152, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic,
            TileMachineHumaine.PROCESS_TIME,
            IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return "Machine Voss Diarh33"; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft mc) {
        arrow.draw(mc, 56, 26);
        // Energy indicator
        mc.fontRenderer.drawStringWithShadow(
            TileMachineHumaine.RF_PER_TICK + " Bio-E/t",
            118, 30, 0xFFCC4444);
        mc.fontRenderer.drawStringWithShadow(
            TileMachineHumaine.PROCESS_TIME / 20 + "s",
            118, 42, 0xFF888888);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, Diarh33Wrapper wrapper,
                           IIngredients ingredients) {
        IGuiItemStackGroup items = layout.getItemStacks();
        items.init(0, true, 6, 26);  // food input
        items.set(ingredients);

        IGuiFluidStackGroup fluids = layout.getFluidStacks();
        // Water input tank (slot 0 fluid, at x=31,y=9, 14x52, capacity 4000)
        fluids.init(0, true, 31, 9, 14, 52,
            TileMachineHumaine.TANK_CAPACITY, true, null);
        // Diarrhee output tank (slot 1 fluid, at x=91,y=9, 14x52, capacity 4000)
        fluids.init(1, false, 91, 9, 14, 52,
            TileMachineHumaine.TANK_CAPACITY, true, null);
        fluids.set(ingredients);
    }
}
