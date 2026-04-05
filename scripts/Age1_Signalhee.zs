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

// ==================================================
// Cle d'Extension 9x9 — "La cle c'est la reussite"
// Advanced Crafting Table (5x5, tier 1)
// Gate: Vossium III, Diarrhee, Blood Magic, Compose C gear
// ==================================================

mods.extendedcrafting.TableCrafting.addShaped(1, <nexusabsolu:compact_key_9x9>,
    [[null, null, null, null, null],
     [null, <contenttweaker:vossium_iii_ingot>, <forge:bucketfilled>.withTag({FluidName: "diarrhee_liquide", Amount: 1000}), <bloodmagic:lava_crystal>, <nexusabsolu:compose_gear_c>],
     [<contenttweaker:vossium_iii_ingot>, null, <contenttweaker:vossium_iii_ingot>, null, <nexusabsolu:compact_key_7x7>],
     [null, <contenttweaker:vossium_iii_ingot>, null, null, <compactmachines3:machine:2>],
     [null, null, null, null, null]]);

print("[Nexus Absolu] Age1_Signalhee.zs loaded");
