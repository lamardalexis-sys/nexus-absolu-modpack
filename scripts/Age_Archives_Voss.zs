// ==============================================================
// ARCHIVES VOSS - Sprint 1 recipes (v1.0.303)
// ==============================================================
// Age 1 early access. Recettes simples pour debuter le systeme.
// Les recettes utilisent des materiaux Age 0/1 accessibles
// (iron, redstone, compose A, vossium II).
// ==============================================================

// --- Bordure d'Archives Voss (6 par craft) ---
// Frame blocks du multiblock. Simple cadre iron + compose A.
recipes.addShaped("nexus_archive_frame", <nexusabsolu:archive_frame> * 6,
    [[<ore:ingotIron>, <nexusabsolu:compose_a>,  <ore:ingotIron>],
     [<ore:ingotIron>, null,                      <ore:ingotIron>],
     [<ore:ingotIron>, <nexusabsolu:compose_a>,  <ore:ingotIron>]]);

// --- Noyau Thermique Voss (4 par craft) ---
// Bloc avec tuyau central pour circulation d'eau froide.
// Materiaux : iron + vossium II (pour la resistance thermique) + redstone.
recipes.addShaped("nexus_archive_thermal_core", <nexusabsolu:archive_thermal_core> * 4,
    [[<ore:ingotIron>,               <ore:dustRedstone>,          <ore:ingotIron>],
     [<nexusabsolu:vossium_ii_ingot>, <minecraft:bucket>,         <nexusabsolu:vossium_ii_ingot>],
     [<ore:ingotIron>,               <ore:dustRedstone>,          <ore:ingotIron>]]);

// --- Controleur d'Archives Voss (1 par craft, endgame Age 1 early) ---
// Le coeur du systeme. Materiaux : 4 compose B (energie) + 4 vossium II
// (structure) + 1 redstone_block (centre, cerveau).
recipes.addShaped("nexus_archive_controller", <nexusabsolu:archive_controller>,
    [[<nexusabsolu:vossium_ii_ingot>, <nexusabsolu:compose_b>,     <nexusabsolu:vossium_ii_ingot>],
     [<nexusabsolu:compose_b>,         <minecraft:redstone_block>, <nexusabsolu:compose_b>],
     [<nexusabsolu:vossium_ii_ingot>, <nexusabsolu:compose_b>,     <nexusabsolu:vossium_ii_ingot>]]);

// --- Compresseur d'Eau Voss ---
// Machine conversion eau. Prerequis : RF converter (deja croise dans la
// progression fours) + bucket vide (pour symboliser l'eau) + piston.
recipes.addShaped("nexus_compresseur_eau", <nexusabsolu:compresseur_eau>,
    [[<ore:ingotIron>,                    <minecraft:piston>,        <ore:ingotIron>],
     [<nexusabsolu:upgrade_rf_converter>, <minecraft:bucket>,        <nexusabsolu:compose_a>],
     [<ore:ingotIron>,                    <ore:dustRedstone>,        <ore:ingotIron>]]);

// ==============================================================
// Note de design (Alexis) :
// - Frame (6x) + ThermalCore (4x) en batch evite la corvee de craft
// - Controller (1x) reste plus couteux (centre Redstone Block)
// - Le multiblock complet requiert : 2 frames + 3 thermal + 1 controller
//   + 1 item_input + 1 item_output (blocs existants deja craftables)
// - Cout total multiblock estime : ~24 iron + ~12 compose + 4 vossium II
//   + 1 redstone_block + 4 redstone + 1 bucket. Raisonnable pour Age 1.
// ==============================================================
