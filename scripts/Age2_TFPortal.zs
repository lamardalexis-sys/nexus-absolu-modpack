// ==================================================
// Age2_TFPortal.zs - Recipes du Portail de Sortie TF
//
// Sprint 3 - v1.0.350+
//
// Le joueur arrive en TF (Age 2 Phase 1) via la Cle de Liberte.
// Pour rejoindre la Phase 2 (DIM 145, "overworld" simulation), il
// doit construire un portail vertical 6x5 (interieur 4x3) en
// Obsidienne Voss et l'allumer avec un Catalyseur de Sortie.
//
// Cette etape n'est pas triviale : Obsidienne Voss demande du
// Vossium IV (endgame Age 1). Le Catalyseur lui-meme demande des
// trophees de boss TF (Sprint 4, recipe Extended Crafting 9x9).
// ==================================================

// === OBSIDIENNE VOSS (frame du portail) ===
// 4 obsidienne vanilla + 4 vossium IV ingot + 1 fragment mecanique
// au centre -> 4 Obsidienne Voss
recipes.addShaped("obsidienne_voss_craft",
    <nexusabsolu:obsidienne_voss> * 4,
    [[<minecraft:obsidian>,             <nexusabsolu:vossium_iv_ingot>, <minecraft:obsidian>],
     [<nexusabsolu:vossium_iv_ingot>,   <nexusabsolu:fragment_mecanique>, <nexusabsolu:vossium_iv_ingot>],
     [<minecraft:obsidian>,             <nexusabsolu:vossium_iv_ingot>, <minecraft:obsidian>]]);

// Le frame complet 4x4 = 12 blocs Obsidienne Voss.
// Avec cette recipe (4 par craft) il faut 3 crafts = 12 vossium IV
// ingot + 12 obsidienne + 3 fragment mecanique.
