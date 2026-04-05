package com.nexusabsolu.mod.compat.jei;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
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

public class KRDACategory implements IRecipeCategory<KRDAWrapper> {

    public static final String UID = Reference.MOD_ID + ".krda";
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        Reference.MOD_ID, "textures/gui/jei_nexus.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public KRDACategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(TEXTURE, 0, 76, 160, 70);
        this.icon = guiHelper.createDrawableIngredient(
            new ItemStack(ModBlocks.MACHINE_KRDA));

        IDrawableStatic arrowStatic = guiHelper.createDrawable(
            TEXTURE, 24, 152, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(arrowStatic,
            TileMachineKRDA.PROCESS_TIME,
            IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return "Machine Voss KRDA125"; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft mc) {
        arrow.draw(mc, 56, 26);
        mc.fontRenderer.drawStringWithShadow(
            TileMachineKRDA.RF_PER_TICK + " Bio-E/t",
            118, 30, 0xFFCC4444);
        mc.fontRenderer.drawStringWithShadow(
            TileMachineKRDA.PROCESS_TIME / 20 + "s",
            118, 42, 0xFF888888);
        mc.fontRenderer.drawStringWithShadow(
            TileMachineKRDA.FLUID_PER_CYCLE + "mB",
            118, 54, 0xFF8B6914);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, KRDAWrapper wrapper,
                           IIngredients ingredients) {
        IGuiItemStackGroup items = layout.getItemStacks();
        items.init(0, true, 6, 26);    // signalum input
        items.init(1, false, 90, 26);   // signalhee output
        items.set(ingredients);

        IGuiFluidStackGroup fluids = layout.getFluidStacks();
        // Diarrhee input tank
        fluids.init(0, true, 31, 9, 14, 52,
            TileMachineKRDA.TANK_CAPACITY, true, null);
        fluids.set(ingredients);
    }
}
