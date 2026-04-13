// ==================================================
// Age1_Portal.zs — Crafts du Portail Voss
//
// Blocs de structure speciaux:
//   - nexus_wall_t2    : mur renforce (39x dans la structure)
//   - ecran_controle   : bloc maitre (1x, le "cerveau" du portail)
//
// Design rules (MODPACK_RECIPES_SKILL):
//   - symetrie horizontale et verticale
//   - centre = composant coeur
//   - haut = fonction, bas = base
//   - 2-3 mods par craft
// ==================================================

// === NEXUS WALL T2 — mur renforce (4x output) ===
// Base: nexus_wall + Invar plates
// Coeur: Compose B (liant d'energie)
// Symbolique: upgrade direct du mur de base avec les mats Age 1
recipes.addShaped("nexus_wall_t2_craft",
    <nexusabsolu:nexus_wall_t2> * 4,
    [[<ore:plateInvar>,       <nexusabsolu:vossium_ingot>, <ore:plateInvar>],
     [<nexusabsolu:nexus_wall>, <nexusabsolu:compose_b>,     <nexusabsolu:nexus_wall>],
     [<ore:plateInvar>,       <nexusabsolu:vossium_ingot>, <ore:plateInvar>]]);

// === ECRAN DE CONTROLE — bloc maitre du Portail Voss ===
// Haut: panneau de verre + signal redstone (interface/display)
// Centre: Vossium IV (coeur stable, resiste aux contraintes dimensionnelles)
// Cotes: Signalhee (conducteur inter-dimensionnel)
// Bas: Nexus Wall T2 + Compose Gear C (structure + liant mecanique)
recipes.addShaped("ecran_controle_craft",
    <nexusabsolu:ecran_controle>,
    [[<minecraft:glass_pane>,     <minecraft:redstone_block>,     <minecraft:glass_pane>],
     [<nexusabsolu:signalhee_ingot>, <nexusabsolu:vossium_iv_ingot>, <nexusabsolu:signalhee_ingot>],
     [<nexusabsolu:nexus_wall_t2>, <nexusabsolu:compose_gear_c>,   <nexusabsolu:nexus_wall_t2>]]);

print("[Nexus Absolu] Age1_Portal.zs loaded");
