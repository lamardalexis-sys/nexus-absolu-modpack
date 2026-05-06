// ============================================================================
// Nexus Absolu - Age4_Phase4_Final.zs
// ============================================================================
// Phase 4 : recettes pour le craft FINAL Cartouche Manifold.
//
// Chaine complete (du bas vers le haut) :
//
//   Composes alpha/beta/gamma/delta (M1 multibloc + L5 multiblocs deja faits)
//                     │
//                     ▼
//   [Bio-Reacteur bio_solution_epsilon_synthesis]
//                     │
//                     ▼
//             solution_epsilon (fluide pourpre/cyan)
//                     │
//                     ▼
//   cartouche_vide + casing_titane_iridium + composes -> cartouche_chargee (shaped)
//                     │
//                     ▼
//   [Bio-Reacteur bio_cartouche_manifold_armement]
//   cartouche_chargee + solution_epsilon + starlight + argon
//                     │
//                     ▼
//          ⭐ CARTOUCHE MANIFOLD ⭐ (item Java avec trip 8min)
//
// Reference design : docs/age4-cartouche-manifold/03-progression-quetes.md
// ============================================================================


// ============================================================================
// PHASE 4.1 - casing_titane_iridium (enveloppe cartouche finale)
// ============================================================================
// 4 titanium_pure (de L4) + 4 iridium_pure_99 (de L4) + 1 cartouche_vide
// au centre + cryolithe_block en pad d'isolation = 1 casing
// (cartouche_vide reste recyclable, ici juste support pour le casing)

recipes.addShaped("nexus_casing_titane_iridium",
    <contenttweaker:casing_titane_iridium> * 1,
    [[<contenttweaker:titanium_pure>, <contenttweaker:iridium_pure_99>, <contenttweaker:titanium_pure>],
     [<contenttweaker:iridium_pure_99>, <contenttweaker:cryolithe_block>, <contenttweaker:iridium_pure_99>],
     [<contenttweaker:titanium_pure>, <contenttweaker:iridium_pure_99>, <contenttweaker:titanium_pure>]]);


// ============================================================================
// PHASE 4.2 - cartouche_chargee (intermediaire avant manifold final)
// ============================================================================
// La cartouche_chargee est l'item INTERMEDIAIRE qui rentre ensuite dans
// le Bio-Reacteur bio_cartouche_manifold_armement.json existant.
//
// Composition : casing + 4 composes (un de chaque famille α/β/γ/δ)
// + matrix_pigmentary central + tryptamide_m_capsule
//
// C'est le moment ou les 5 theoremes de Voss convergent dans un seul item :
//   α (Conservation) - β (Organique) - γ (Stellaire) - δ (Sanguine+Brisure)

recipes.addShaped("nexus_cartouche_chargee_assembly",
    <contenttweaker:cartouche_chargee> * 1,
    [[<contenttweaker:compose_alpha>, <contenttweaker:tryptamide_m_capsule>, <contenttweaker:compose_beta>],
     [<contenttweaker:compose_gamma2>, <contenttweaker:casing_titane_iridium>, <contenttweaker:compose_gamma3>],
     [<contenttweaker:compose_delta>, <contenttweaker:matrix_pigmentary>, <contenttweaker:compose_gamma1>]]);


// ============================================================================
// PHASE 4.3 - tryptamide_m brut (de la capsule)
// ============================================================================
// La recette principale produit tryptamide_m_capsule (L7.D aromatic_reactor).
// Pour decompresser la capsule en 4x dust brut (tryptamide_m), recette shapeless.

recipes.addShapeless("nexus_tryptamide_m_extract",
    <contenttweaker:tryptamide_m> * 4,
    [<contenttweaker:tryptamide_m_capsule>]);


// ============================================================================
// PHASE 4.4 - mycelium_active (TEMP fallback shaped)
// ============================================================================
// La vraie methode est : poser mycelium pres d'un breeder NC actif 24000 ticks
// (KubeJS event - L5.9). En attendant, fallback shaped craft via contraintes
// pour ne pas bloquer le test.

recipes.addShaped("nexus_mycelium_active_fallback",
    <contenttweaker:mycelium_active> * 1,
    [[<contenttweaker:cristal_manifoldine>, <minecraft:mycelium>, <contenttweaker:cristal_manifoldine>],
     [<minecraft:mycelium>, <contenttweaker:gamma3_lit_capsule>, <minecraft:mycelium>],
     [<contenttweaker:cristal_manifoldine>, <minecraft:mycelium>, <contenttweaker:cristal_manifoldine>]]);


print("[Nexus Absolu] Age4_Phase4_Final.zs loaded -- 4 recettes craft final");
