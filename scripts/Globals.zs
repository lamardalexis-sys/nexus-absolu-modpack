// ============================================
// NEXUS ABSOLU -- Globals.zs
// Utility functions used by all scripts
// ============================================

import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;
import crafttweaker.oredict.IOreDict;
import crafttweaker.liquid.ILiquidStack;
import mods.jei.JEI.removeAndHide as rh;

// === OREDICT REGISTRATIONS ===
// wall_dust needs an oredict tag so EnderIO Alloy Smelter accepts it in input slots
// (EnderIO filters slot inputs by their valid recipe ingredients - no oredict = rejected)
<ore:dustWall>.add(<nexusabsolu:wall_dust>);

// Remove old recipe and add new shaped recipe
global remake as function(string, IItemStack, IIngredient[][])void =
    function (name as string, item as IItemStack, input as IIngredient[][]) as void {
        recipes.remove(item);
        recipes.addShaped(name, item, input);
};

// Remove old recipe and add new shapeless recipe
global remakeSL as function(string, IItemStack, IIngredient[])void =
    function (name as string, item as IItemStack, input as IIngredient[]) as void {
        recipes.remove(item);
        recipes.addShapeless(name, item, input);
};

print("[Nexus Absolu] Globals.zs loaded");
