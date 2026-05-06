// ============================================================================
// Nexus Absolu — Age4_L8_Manifoldine.zs
//
// PHASE 1 PILOTE : debloque la chaine cristal_manifoldine end-to-end.
//
// Reference design : docs/age4-cartouche-manifold/lines/L8-botanique-manifoldine.md
//
// Chaine implementee :
//   L8.C.1 : Mycelium Active -> Spores Activees (furnace + crusher)
//   L8.C.2 : Spores + acetone + ammoniaque + methanol -> Manifoldine Extract Purified
//   L8.C.3 : Extract + Tryptamide + Liquid Starlight + Heavy Water + Argon -> Manifoldine Brute
//   L8.C.4 : Brute + H3PO4 -> Cristal Manifoldine + Tridistilled Water
//
// MODE TEMPORAIRE :
//   - Recettes utilisent shaped/Mekanism PRC en attendant les multiblocs custom
//     (MB-SOXHLET, MB-CYCLO, MB-EVAPORATOR a creer en Phase 2)
//   - Fluides chimiques intermediaires (acetone, ammoniaque, methanol, h3po4)
//     ont des recettes TEMP qui seront remplacees par les vraies en Phase 3
//   - Chaque recette TEMP est marquee // TEMP TODO pour faciliter le remplacement
// ============================================================================


// ============================================================================
// RECETTES TEMP -- Fluides chimiques intermediaires
// A REMPLACER en Phase 3 par les vraies recettes L1/L6/L7
// ============================================================================

// TEMP TODO L7 : acetone produit en L7 via Cumene phase 4
// Pour debloquer L8 : recette shapeless basique
recipes.addShapeless("nexus_acetone_temp",
    <minecraft:bucket>.withTag({FluidName: "acetone", Amount: 1000}),
    [<minecraft:bucket>, <minecraft:redstone>, <minecraft:redstone>, <minecraft:redstone>,
     <minecraft:sugar>, <nexusabsolu:compose_a>]);

// TEMP TODO L6 : ammoniaque produit en L6 via Haber-Bosch
recipes.addShapeless("nexus_ammoniaque_temp",
    <minecraft:bucket>.withTag({FluidName: "ammoniaque", Amount: 1000}),
    [<minecraft:bucket>, <minecraft:rotten_flesh>, <minecraft:rotten_flesh>,
     <minecraft:rotten_flesh>, <minecraft:dye:15>]);  // dye:15 = bonemeal

// TEMP TODO L7 : methanol produit en L7 organique
recipes.addShapeless("nexus_methanol_temp",
    <minecraft:bucket>.withTag({FluidName: "methanol", Amount: 1000}),
    [<minecraft:bucket>, <minecraft:wheat>, <minecraft:wheat>, <minecraft:wheat>,
     <minecraft:wheat>, <minecraft:sugar>]);

// TEMP TODO L4 : h3po4 produit en L4 pyrometallurgie
recipes.addShapeless("nexus_h3po4_temp",
    <minecraft:bucket>.withTag({FluidName: "h3po4", Amount: 1000}),
    [<minecraft:bucket>, <minecraft:bone>, <minecraft:bone>, <minecraft:bone>,
     <nexusabsolu:compose_b>]);

// TEMP TODO L2 : tridistilled_water produit en L2 via osmose+distill triple
recipes.addShapeless("nexus_tridistilled_water_temp",
    <minecraft:bucket>.withTag({FluidName: "tridistilled_water", Amount: 1000}),
    [<minecraft:bucket>, <minecraft:water_bucket>, <minecraft:ice>,
     <minecraft:packed_ice>, <minecraft:snow>, <minecraft:snow>]);

// ============================================================================
// L8.C.1 -- Mycelium Active -> Spores Actives
// ============================================================================
// Note : mycelium_active doit lui-meme etre obtenu via L5 (exposition fission).
// Pour Phase 1 pilote, on ajoute aussi une recette TEMP pour mycelium_active.

// TEMP TODO L5 : mycelium_active obtenu via fission reactor exposure
recipes.addShaped("nexus_mycelium_active_temp",
    <contenttweaker:mycelium_active> * 4,
    [[<minecraft:redstone>, <minecraft:glowstone_dust>, <minecraft:redstone>],
     [<minecraft:glowstone_dust>, <minecraft:mycelium>, <minecraft:glowstone_dust>],
     [<minecraft:redstone>, <nexusabsolu:compose_d>, <minecraft:redstone>]]);

// L8.C.1 : 1x mycelium_active -> 4x spores_active (furnace)
furnace.addRecipe(<contenttweaker:spores_active> * 4, <contenttweaker:mycelium_active>, 0.5);


// ============================================================================
// L8.C.2 -- Manifoldine Extract Purified (Soxhlet Extraction)
// ============================================================================
// Design : 8x spores + 200mB acetone + 100mB ammoniaque + 100mB methanol
//          -> 200mB manifoldine_extract_purified
// 
// Phase 1 PILOTE : Mekanism PRC (Pressurized Reaction Chamber) accepte
// 1 item + 1 fluid + 1 gas -> 1 item + 1 gas, ce qui ne match pas exactement.
// On utilise donc un shaped recipe avec buckets pour le pilote.

// PILOTE shaped : approximation des inputs (ratios reduits pour pratique)
recipes.addShaped("nexus_manifoldine_extract_purified_temp",
    <minecraft:bucket>.withTag({FluidName: "manifoldine_extract_purified", Amount: 1000}),
    [[<contenttweaker:spores_active>, <minecraft:bucket>.withTag({FluidName: "acetone", Amount: 1000}), <contenttweaker:spores_active>],
     [<minecraft:bucket>.withTag({FluidName: "ammoniaque", Amount: 1000}), <contenttweaker:spores_active>, <minecraft:bucket>.withTag({FluidName: "methanol", Amount: 1000})],
     [<contenttweaker:spores_active>, <minecraft:bucket>, <contenttweaker:spores_active>]]);


// ============================================================================
// L8.C.3 -- Manifoldine Brute (Cyclisateur Stellaire)
// ============================================================================
// Design : 200mB extract + 1x tryptamide_m_capsule + 500mB liquid_starlight
//          + 100mB heavywater + 200mB argon (NUIT + ciel ouvert)
//          -> 100mB manifoldine_brute
//
// Phase 1 PILOTE : shaped recipe sans condition nuit. La condition sera
// reintroduite avec le multibloc MB-CYCLO en Phase 2.

// PILOTE : recette avec inputs conformes au design
recipes.addShaped("nexus_manifoldine_brute_temp",
    <minecraft:bucket>.withTag({FluidName: "manifoldine_brute", Amount: 1000}),
    [[<minecraft:bucket>.withTag({FluidName: "argon", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "astralsorcery.liquidstarlight", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "argon", Amount: 1000})],
     [<minecraft:bucket>.withTag({FluidName: "manifoldine_extract_purified", Amount: 1000}), <contenttweaker:tryptamide_m_capsule>, <minecraft:bucket>.withTag({FluidName: "manifoldine_extract_purified", Amount: 1000})],
     [<minecraft:bucket>.withTag({FluidName: "argon", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "argon", Amount: 1000})]]);


// ============================================================================
// L8.C.4 -- Cristal Manifoldine ⭐⭐⭐ KEY ITEM (Stabilisation finale)
// ============================================================================
// Design : 100mB manifoldine_brute + 50mB h3po4
//          -> 1x cristal_manifoldine + 100mB tridistilled_water
//
// Phase 1 PILOTE : shaped recipe. Sera remplace par MB-EVAPORATOR en Phase 2.

recipes.addShaped("nexus_cristal_manifoldine_pilote",
    <contenttweaker:cristal_manifoldine>,
    [[<nexusabsolu:vossium_iv_ingot>, <minecraft:bucket>.withTag({FluidName: "h3po4", Amount: 1000}), <nexusabsolu:vossium_iv_ingot>],
     [<minecraft:bucket>.withTag({FluidName: "manifoldine_brute", Amount: 1000}), <nexusabsolu:catalyseur_resonant>, <minecraft:bucket>.withTag({FluidName: "manifoldine_brute", Amount: 1000})],
     [<nexusabsolu:vossium_iv_ingot>, <minecraft:bucket>.withTag({FluidName: "h3po4", Amount: 1000}), <nexusabsolu:vossium_iv_ingot>]]);

// Tryptamide_m_capsule recette TEMP (utilise pour L8.C.3)
// TODO TEMP L7 : tryptamide_m -> capsule
recipes.addShaped("nexus_tryptamide_m_capsule_temp",
    <contenttweaker:tryptamide_m_capsule>,
    [[<contenttweaker:cartouche_vide>, <contenttweaker:tryptamide_m>, <contenttweaker:cartouche_vide>],
     [<contenttweaker:tryptamide_m>, <ore:plateIridium>, <contenttweaker:tryptamide_m>],
     [<contenttweaker:cartouche_vide>, <contenttweaker:tryptamide_m>, <contenttweaker:cartouche_vide>]]);


print("[Nexus Absolu] Age4_L8_Manifoldine.zs loaded -- chaine cristal_manifoldine debloquee (PHASE 1 pilote)");
