// ============================================================================
// Nexus Absolu -- Age4_L8_Manifoldine.zs
//
// Ligne L8 : Botanique / Manifoldine.
// Reference design : docs/age4-cartouche-manifold/lines/L8-botanique-manifoldine.md
//
// TOUTE la chaine chimique L8 vit dans Modular Machinery, pas ici :
//   L8.C.1  mycelium vanilla + tritium -> mycelium_active
//           config/modularmachinery/recipes/gamma_forge_mycelium_activation.json
//   L8.C.2  spores + acetone + ammoniaque + methanol -> extract_purified
//           config/modularmachinery/recipes/soxhlet_manifoldine_extraction.json
//   L8.C.3  extract + tryptamide + starlight + heavywater + argon -> brute
//           config/modularmachinery/recipes/cyclo_manifoldine_cyclization.json
//   L8.C.4  brute + h3po4 -> cristal_manifoldine + tridistilled_water
//           config/modularmachinery/recipes/evaporator_cristal_manifoldine.json
//
// Ce fichier ne contient plus qu'une seule etape : le broyage des spores.
// Toutes les recettes "TEMP" / "PILOTE" qui vivaient ici ont ete supprimees :
// elles doublonnaient une machine existante et permettaient au joueur de
// fabriquer le cristal_manifoldine sur une table de craft 3x3, ce qui rendait
// les 13 multiblocs de l'Age 4 purement decoratifs.
// ============================================================================


// ----------------------------------------------------------------------------
// L8.C.1b -- Mycelium Active -> Spores Actives
// ----------------------------------------------------------------------------
// Passe par le Crusher Mekanism et non par un four vanilla : une machine,
// pas une table, et une dependance mod assumee pour le gating.
// Le mycelium_active vient du Gamma Forge (ligne L5), ce qui force le joueur
// a monter la chaine nucleaire avant de pouvoir toucher a la Manifoldine.

mods.mekanism.crusher.addRecipe(
    <contenttweaker:mycelium_active>,
    <contenttweaker:spores_active> * 8);


print("[Nexus Absolu] Age4_L8_Manifoldine.zs loaded -- broyage spores (chaine L8 en Modular Machinery)");


// ----------------------------------------------------------------------------
// L8.C.3 -- Gating de la Cyclisation Stellaire
// ----------------------------------------------------------------------------
// Le design imposait quatre conditions d'environnement a la cyclisation :
// Overworld, y >= 60, ciel degage, temps clair, nuit (13000-23000 ticks).
// Elles etaient ecrites comme requirements "dimension"/"position"/"weather"/
// "time" dans cyclo_manifoldine_cyclization.json -- des types que Modular
// Machinery 1.11.1 ne connait pas. La recette entiere etait rejetee au
// chargement, donc la manifoldine_brute n'etait produite par rien.
//
// Le gating vit desormais dans l'Ampoule de Nuit Stellaire : elle ne se
// remplit que si les cinq conditions sont reunies (voir
// ItemAmpouleNuitStellaire.java), et le Cyclisateur en consomme une.

recipes.addShaped("nexus_ampoule_nuit_stellaire_vide",
    <nexusabsolu:ampoule_nuit_stellaire_vide> * 2,
    [[null, <minecraft:glass_pane>, null],
     [<minecraft:glass_pane>, <ore:dustGlowstone>, <minecraft:glass_pane>],
     [null, <ore:ingotIron>, null]]);
