// ============================================================================
// Nexus Absolu - Age4_L4_Pyrometallurgie.zs
// ============================================================================
// Recettes ZS pour la ligne L4 Pyrometallurgie :
//   - Procede Bayer (Bauxite + NaOH -> Alumina) via PCC Pressure Chamber
//   - Cryolithe synthetique
//   - Refonte Ti sponge -> Ti pur
//   - P2O5 capsule (intermediaire H3PO4)
//   - Items intermediaires (sodium_ingot, fluorine_capsule, magnesium_ingot)
//
// Reference design : docs/age4-cartouche-manifold/lines/L4-pyrometallurgie.md
// ============================================================================


// ============================================================================
// L4.B - Procede Bayer (Bauxite + NaOH -> Alumina)
// ============================================================================
// Design : PneumaticCraft Pressure Chamber 4.5 bar
// Implementation : recette shaped (PCC integration optionnelle)

recipes.addShaped("nexus_alumina",
    <contenttweaker:alumina> * 3,
    [[<ore:dustBauxite>, <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000}), <ore:dustBauxite>],
     [<minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000}), <ore:dustBauxite>, <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000})],
     [<ore:dustBauxite>, <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000}), <ore:dustBauxite>]]);

// Fallback : si pas dustBauxite, utiliser dustAluminum
recipes.addShaped("nexus_alumina_fallback",
    <contenttweaker:alumina> * 2,
    [[<ore:dustAluminum>, <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000}), <ore:dustAluminum>],
     [<ore:dustAluminum>, <ore:dustAluminum>, <ore:dustAluminum>],
     [<ore:dustAluminum>, <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000}), <ore:dustAluminum>]]);


// ============================================================================
// L4.B - Cryolithe synthetique (catalyseur Hall-Heroult)
// ============================================================================
// Na + Al + F -> Na3AlF6 (cryolithe block)

recipes.addShaped("nexus_cryolithe_block",
    <contenttweaker:cryolithe_block> * 1,
    [[<contenttweaker:sodium_ingot>, <contenttweaker:fluorine_capsule>, <contenttweaker:sodium_ingot>],
     [<contenttweaker:fluorine_capsule>, <ore:ingotAluminum>, <contenttweaker:fluorine_capsule>],
     [<contenttweaker:sodium_ingot>, <contenttweaker:fluorine_capsule>, <contenttweaker:sodium_ingot>]]);


// ============================================================================
// L4.C - Refonte Titanium Sponge -> Titanium Pure
// ============================================================================
// Etape 3 Kroll : refonte arc electrique simple
// Implementation : 1 sponge -> 1 ingot pur (shapeless)
recipes.addShapeless("nexus_titanium_pure",
    <contenttweaker:titanium_pure> * 1,
    [<contenttweaker:titanium_sponge>]);


// ============================================================================
// L4.E - P2O5 capsule (intermediaire pour H3PO4)
// ============================================================================
// Design : 4 phosphorus_white + 100mB oxygen + 100mB tridistilled_water
//          -> 1 p2o5_capsule + 50mB hydrogen (Mekanism PRC)
// Implementation : shaped recipe

recipes.addShaped("nexus_p2o5_capsule",
    <contenttweaker:p2o5_capsule> * 1,
    [[<contenttweaker:phosphorus_white>, <minecraft:bucket>.withTag({FluidName: "oxygen", Amount: 1000}), <contenttweaker:phosphorus_white>],
     [<contenttweaker:phosphorus_white>, <contenttweaker:cartouche_vide>, <contenttweaker:phosphorus_white>],
     [<contenttweaker:phosphorus_white>, <minecraft:bucket>.withTag({FluidName: "tridistilled_water", Amount: 1000}), <contenttweaker:phosphorus_white>]]);


// ============================================================================
// L4.X - Items intermediaires (Na ingot, F capsule, Mg ingot, iron_dust_pure)
// ============================================================================

// Sodium ingot : compresser sodium_liquid en bucket via 9 buckets ou shaped
recipes.addShaped("nexus_sodium_ingot",
    <contenttweaker:sodium_ingot> * 4,
    [[<minecraft:bucket>.withTag({FluidName: "sodium_liquid", Amount: 1000}), <ore:dustSalt>, <minecraft:bucket>.withTag({FluidName: "sodium_liquid", Amount: 1000})],
     [<ore:dustSalt>, <minecraft:iron_ingot>, <ore:dustSalt>],
     [<minecraft:bucket>.withTag({FluidName: "sodium_liquid", Amount: 1000}), <ore:dustSalt>, <minecraft:bucket>.withTag({FluidName: "sodium_liquid", Amount: 1000})]]);

// Fluorine capsule : compresser fluorine_gas
recipes.addShaped("nexus_fluorine_capsule",
    <contenttweaker:fluorine_capsule> * 2,
    [[<contenttweaker:cartouche_vide>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}), <contenttweaker:cartouche_vide>],
     [<minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}), <ore:ingotIron>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000})],
     [<contenttweaker:cartouche_vide>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}), <contenttweaker:cartouche_vide>]]);

// Magnesium ingot : Mekanism Mg natif (oredict ingotMagnesium) ou shaped fallback
recipes.addShaped("nexus_magnesium_ingot_fallback",
    <contenttweaker:magnesium_ingot> * 2,
    [[<ore:dustMagnesium>, <ore:dustMagnesium>, <ore:dustMagnesium>],
     [<ore:dustMagnesium>, <minecraft:iron_ingot>, <ore:dustMagnesium>],
     [<ore:dustMagnesium>, <ore:dustMagnesium>, <ore:dustMagnesium>]]);

// Iron dust pure : iron dust crusher Mekanism + purification
// Simplification : 4 iron dusts + 1 redstone -> 1 iron_dust_pure
recipes.addShapeless("nexus_iron_dust_pure",
    <contenttweaker:iron_dust_pure> * 1,
    [<ore:dustIron>, <ore:dustIron>, <ore:dustIron>, <ore:dustIron>, <minecraft:redstone>]);


print("[Nexus Absolu] Age4_L4_Pyrometallurgie.zs loaded -- 9 recettes L4");
