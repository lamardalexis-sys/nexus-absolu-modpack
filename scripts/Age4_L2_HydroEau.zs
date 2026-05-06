// ============================================================================
// Nexus Absolu - Age4_L2_HydroEau.zs
// ============================================================================
// Recettes ZS pour la ligne L2 Hydro-Eau :
//   - resin_charge (recharge filtre ionique)
//   - lithium6_ingot (separation Li-6 vs Li-7)
//
// Reference design : docs/age4-cartouche-manifold/lines/L2-hydro-eau.md
//
// Note : les recettes des multiblocs (ion_filtration, reverse_osmosis,
// tritium_breeding) sont en JSON dans config/modularmachinery/recipes/.
// ============================================================================


// ============================================================================
// L2.X.1 - Resin Charge (catalyseur filtration ionique)
// ============================================================================
// Design : 1 Silica Dust + 1 Sulfonic Acid Pellet
// Implementation simplifiee : silicon dust + sulfur dust + iron base

recipes.addShaped("nexus_resin_charge",
    <contenttweaker:resin_charge> * 4,
    [[<ore:dustSilicon>, <ore:dustSulfur>, <ore:dustSilicon>],
     [<ore:dustSulfur>, <minecraft:iron_ingot>, <ore:dustSulfur>],
     [<ore:dustSilicon>, <ore:dustSulfur>, <ore:dustSilicon>]]);


// ============================================================================
// L2.X.2 - Lithium-6 Ingot (separation isotopique)
// ============================================================================
// Design : Mekanism centrifugation Li-6 vs Li-7
// Implementation simplifiee : 6x Lithium dust + 2x Heavy Water bucket en shaped
// (proxy enrichissement Li-6)

recipes.addShaped("nexus_lithium6_ingot",
    <contenttweaker:lithium6_ingot> * 1,
    [[<ore:dustLithium>, <ore:dustLithium>, <ore:dustLithium>],
     [<ore:dustLithium>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <ore:dustLithium>],
     [<ore:dustLithium>, <ore:dustLithium>, <ore:dustLithium>]]);

// Fallback : si pas de dustLithium en oredict, on utilise nuggetLithium ou
// salt + carbon comme proxy
recipes.addShaped("nexus_lithium6_ingot_fallback",
    <contenttweaker:lithium6_ingot> * 1,
    [[<ore:dustSalt>, <ore:dustSalt>, <ore:dustSalt>],
     [<ore:dustSalt>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <ore:dustSalt>],
     [<ore:dustSalt>, <minecraft:diamond>, <ore:dustSalt>]]);


print("[Nexus Absolu] Age4_L2_HydroEau.zs loaded -- 3 recettes L2");
