// ============================================================================
// Nexus Absolu - Age4_M1_Melangeur.zs
// ============================================================================
// Phase 4 : Craft final Cartouche Manifold.
//
// Block controllers + items pour M1 Melangeur Cryogenique (machine pivot
// qui synthetise les composes alpha/beta/delta) et casing_titane_iridium
// (enveloppe de la cartouche finale).
//
// Reference design : docs/age4-cartouche-manifold/multiblocs-age4.md
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// ============================================================================
// 1. M1 Melangeur Cryogenique (5x4x5)
// ============================================================================
// Machine pivot Phase 4 : combine sortants L1+L4+L6+L8 sous atmosphere
// cryotheum (-196 deg C) pour produire les composes finaux alpha/beta/delta.
//
// Recipes :
//   compose_alpha = solvant_alpha + H2SO4 + HNO3 + cryotheum (super-acide)
//   compose_beta  = aluminum_pure + gold_pure_99 + benzene + NH3 (organometallique)
//   compose_delta = mycelium_active + tryptamide_m + cristal_manifoldine + H3PO4 (bio-actif)
//
// RF max : 120k RF/t (le + cher de Phase 4)

val melangeur_cryogenique_controller = VanillaFactory.createBlock("melangeur_cryogenique_controller",
    <blockmaterial:iron>);
melangeur_cryogenique_controller.setBlockHardness(10.0);
melangeur_cryogenique_controller.setBlockResistance(40.0);
melangeur_cryogenique_controller.toolClass = "pickaxe";
melangeur_cryogenique_controller.toolLevel = 3;
melangeur_cryogenique_controller.lightValue = 4;  // froid (basse luminosite)
melangeur_cryogenique_controller.register();


// ============================================================================
// 2. casing_titane_iridium (item)
// ============================================================================
// Enveloppe finale de la cartouche manifold. Resistance haute pression
// + transparence pour fenetre Quantum Glass.
// Recette : 4x titanium_pure + 4x iridium_pure_99 + 1x cartouche_vide.

val casing_titane_iridium = VanillaFactory.createItem("casing_titane_iridium");
casing_titane_iridium.maxStackSize = 16;
casing_titane_iridium.glowing = true;
casing_titane_iridium.register();


print("[Nexus Absolu] Age4_M1_Melangeur.zs loaded -- M1 controller + casing item");
