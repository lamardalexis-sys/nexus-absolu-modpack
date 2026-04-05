// ==================================================
// Age1_Signalhee.zs — Compose C via Signalhee
//
// Chaine complete:
//   Food + Eau + Bio-E -> [Diarh33] -> Diarrhee Liquide
//   Signalum + Diarrhee + Bio-E -> [KRDA125] -> Signalhee
//   Signalhee + Compose B -> [Smelter] -> Compose C
// ==================================================

// Thermal Expansion — Induction Smelter
mods.thermalexpansion.InductionSmelter.addRecipe(
    <nexusabsolu:compose_c>,
    <nexusabsolu:signalhee_ingot>,
    <nexusabsolu:compose_b>,
    8000
);

// Immersive Engineering — Alloy Kiln
mods.immersiveengineering.AlloySmelter.addRecipe(
    <nexusabsolu:compose_c>,
    <nexusabsolu:signalhee_ingot>,
    <nexusabsolu:compose_b>,
    400
);

// EnderIO — Alloy Smelter
mods.enderio.AlloySmelter.addRecipe(
    <nexusabsolu:compose_c>,
    [<nexusabsolu:signalhee_ingot>, <nexusabsolu:compose_b>],
    8000
);
