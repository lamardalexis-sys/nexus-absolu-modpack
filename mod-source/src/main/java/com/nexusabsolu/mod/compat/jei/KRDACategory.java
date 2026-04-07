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

    // Default animation duration for the arrow. Matches the current recipes'
    // processTime. If future recipes use different times, the wrapper can
    // override the visual via its own drawInfo overlay.
    private static final int ARROW_TICKS = 200;

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
            ARROW_TICKS,
            IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override public String getUid() { return UID; }
    @Override public String getTitle() { return "Machine Voss KRDA125"; }
    @Override public String getModName() { return Reference.MOD_NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft mc) {
        // Arrow only. Per-recipe stats (RF/t, time, mB) are drawn by KRDAWrapper.drawInfo
        // so each displayed recipe shows its own values.
        arrow.draw(mc, 56, 26);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, KRDAWrapper wrapper,
                           IIngredients ingredients) {
        IGuiItemStackGroup items = layout.getItemStacks();
        items.init(0, true, 6, 26);    // input
        items.init(1, false, 90, 26);   // output
        items.set(ingredients);

        IGuiFluidStackGroup fluids = layout.getFluidStacks();
        // Diarrhee input tank
        fluids.init(0, true, 31, 9, 14, 52,
            TileMachineKRDA.TANK_CAPACITY, true, null);
        fluids.set(ingredients);
    }
}
