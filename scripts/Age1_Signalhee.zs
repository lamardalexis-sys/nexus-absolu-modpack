// ==================================================
// Age1_Signalhee.zs — Signalhee ingot recipes
// Signalum + Compose B = Signalhee
// Available via 3 machines (alternative to KRDA125)
// ==================================================

// Thermal Expansion — Induction Smelter
// Signalum ingot (thermalfoundation:material:165) + Compose B → Signalhee
mods.thermalexpansion.InductionSmelter.addRecipe(
    <nexusabsolu:signalhee_ingot>,
    <thermalfoundation:material:165>,
    <nexusabsolu:compose_b>,
    8000
);

// Immersive Engineering — Alloy Kiln (Arc Furnace alternative)
// Slower but available earlier
mods.immersiveengineering.AlloySmelter.addRecipe(
    <nexusabsolu:signalhee_ingot>,
    <thermalfoundation:material:165>,
    <nexusabsolu:compose_b>,
    400
);

// EnderIO — Alloy Smelter
// Highest energy cost but fastest
mods.enderio.AlloySmelter.addRecipe(
    <nexusabsolu:signalhee_ingot>,
    [<thermalfoundation:material:165>, <nexusabsolu:compose_b>],
    8000
);
