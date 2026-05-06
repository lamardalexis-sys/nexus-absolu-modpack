// ============================================================================
// Nexus Absolu — Age4_L1_Petrochimie.zs
//
// Recettes ZS pour la ligne L1 (Petrochimie) :
//   - Catalyseurs (pellets_como, zeolite_pellet, tensioactif_phosphate)
//   - PneumaticCraft Refinery customisation L1.3
//
// Reference design : docs/age4-cartouche-manifold/lines/L1-petrochimie.md
//
// Note : les recettes des multiblocs (MB-DESA, MB-HDS) sont en JSON dans
// config/modularmachinery/recipes/ (vacuum_desaeration, hds_kerosene,
// hydrocrack_kerosene, solvant_alpha_mix).
// ============================================================================


// ============================================================================
// L1.X.1 — Pellets CoMo (catalyseur HDS)
// ============================================================================
// Design : 1 Cobalt Dust + 2 Tungsten Dust + sintering 800°C
// Implementation : EnderIO Alloy Smelter ou Mekanism Crusher + assemblage shaped
//
// Pour simplifier : recette shaped avec dust cobalt + dust tungsten + dust phosphate
// (le tungsten est rare, on utilise le sub : ferro_uranium + cobalt)

// Recette shaped : 1x Cobalt + 2x Tungsten + 1x Iron base = 1x pellets_como
recipes.addShaped("nexus_pellets_como",
    <contenttweaker:pellets_como> * 2,
    [[<ore:dustTungsten>, <ore:dustCobalt>, <ore:dustTungsten>],
     [<ore:dustCobalt>, <ore:ingotIron>, <ore:dustCobalt>],
     [<ore:dustTungsten>, <ore:dustCobalt>, <ore:dustTungsten>]]);

// Fallback si pas de dust cobalt : utiliser dust nickel
recipes.addShaped("nexus_pellets_como_fallback",
    <contenttweaker:pellets_como> * 1,
    [[<ore:dustHardenedSteel>, <ore:dustNickel>, <ore:dustHardenedSteel>],
     [<ore:dustNickel>, <ore:nuggetTungsten>, <ore:dustNickel>],
     [<ore:dustHardenedSteel>, <ore:dustNickel>, <ore:dustHardenedSteel>]]);


// ============================================================================
// L1.X.2 — Zeolite Pellet (catalyseur Hydrocrack)
// ============================================================================
// Design : 4 Silicon Dust + 2 Aluminum Dust + 1 Sodium Dust → PRC Mekanism
// Implementation : recette shaped (PRC est gas-based, plus complexe)

recipes.addShaped("nexus_zeolite_pellet",
    <contenttweaker:zeolite_pellet> * 2,
    [[<ore:dustSilicon>, <ore:dustAluminum>, <ore:dustSilicon>],
     [<ore:dustAluminum>, <ore:dustSodium>, <ore:dustAluminum>],
     [<ore:dustSilicon>, <ore:dustAluminum>, <ore:dustSilicon>]]);

// Fallback Sodium : si pas dispo en oredict, utiliser sugar (chimie approximative)
recipes.addShaped("nexus_zeolite_pellet_fallback",
    <contenttweaker:zeolite_pellet> * 1,
    [[<ore:dustSilicon>, <ore:dustAluminum>, <ore:dustSilicon>],
     [<ore:dustAluminum>, <ore:dustSalt>, <ore:dustAluminum>],
     [<ore:dustSilicon>, <ore:dustAluminum>, <ore:dustSilicon>]]);


// ============================================================================
// L1.X.3 — Tensioactif Phosphate (additif solvant alpha)
// ============================================================================
// Design : 1 Phosphore (L3) + 1 Sodium Dust (L3) + 1 Vegetable Oil
// Implementation : recette shaped (plus simple que Crusher Mekanism)

// TEMP TODO L3 : phosphore obtenu en L3 electrolyse (compose_b en attendant)
recipes.addShaped("nexus_tensioactif_phosphate",
    <contenttweaker:tensioactif_phosphate> * 4,
    [[<minecraft:bone>, <ore:dustSalt>, <minecraft:bone>],
     [<ore:dustSalt>, <nexusabsolu:compose_b>, <ore:dustSalt>],
     [<minecraft:bone>, <ore:dustSalt>, <minecraft:bone>]]);


// ============================================================================
// L1.3 — PneumaticCraft Refinery customisation
// ============================================================================
// Design : adjust Refinery output ratio pour ajouter bitume au fond
// Note : la Refinery PCC est natif et fonctionne deja. On laisse les recettes
// natives en place. Cette section est commentee comme placeholder pour de futures
// customisations si besoin.

// Si la Refinery PCC ne contient pas deja kerosene + diesel, on peut ajouter :
// mods.pneumaticcraft.Refinery.removeRecipe(<liquid:kerosene>);
// mods.pneumaticcraft.Refinery.addRecipe(150, [<liquid:kerosene>*600, <liquid:diesel>*300]);
//
// Pour l'instant : on laisse les ratios natifs PCC.


print("[Nexus Absolu] Age4_L1_Petrochimie.zs loaded -- 5 recettes catalyseurs L1");
