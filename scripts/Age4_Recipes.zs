// =============================================================================
// AGE 4 -- Recettes ZS (CraftTweaker) pour items/blocs custom
// =============================================================================
// Voir : docs/age4-cartouche-manifold/

import crafttweaker.item.IItemStack;

// =============================================================================
// PHASE 1 -- Recettes blocs Cryo + Resine
// =============================================================================

// Q2 -- Resine echangeuse d'ions
// Pattern : silice + acide sulfonique (proxy : sand + h2so4 bucket dans crafting)
// Recette shaped : pierre + sable encadres avec acide pour symboliser la synthese
recipes.addShaped("resine_echangeuse_block_recipe",
    <contenttweaker:resine_echangeuse_block>,
    [[<minecraft:sand>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <minecraft:sand>],
     [<minecraft:sand>, <minecraft:stone>, <minecraft:sand>],
     [<minecraft:sand>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <minecraft:sand>]]);

// Q4 -- Cryo-Distillateur Controller
// Materiels chers : iridium, titane, cryotheum (validation Phase 2 + 1)
recipes.addShaped("cryo_distillateur_controller_recipe",
    <contenttweaker:cryo_distillateur_controller>,
    [[<ore:plateIridium>, <minecraft:bucket>.withTag({FluidName: "cryotheum", Amount: 1000}), <ore:plateIridium>],
     [<ore:ingotTitanium>, <modularmachinery:blockcontroller>, <ore:ingotTitanium>],
     [<ore:plateIridium>, <ore:ingotTitanium>, <ore:plateIridium>]]);


// =============================================================================
// PHASE 6 -- Recette Bio-Reacteur Controller + Cartouche chargee (PRC)
// =============================================================================

// Q71 -- Bio-Reacteur Controller (le coeur du multibloc final)
// Tres cher : iridium plates + tungsten + uranium + lead (Phase 2-4)
recipes.addShaped("bioreacteur_controller_recipe",
    <contenttweaker:bioreacteur_controller>,
    [[<ore:plateIridium>, <ore:plateTungsten>, <ore:plateIridium>],
     [<ore:plateLead>, <modularmachinery:blockcontroller>, <ore:plateLead>],
     [<ore:plateIridium>, <ore:plateUranium>, <ore:plateIridium>]]);

// Q74 -- Cartouche chargee (Pressurized Reaction Chamber)
// 1 cartouche_vide + 1000mB solution_epsilon + 100mB argon -> 1 cartouche_chargee
mods.mekanism.reaction.addRecipe(
    <contenttweaker:cartouche_vide>,           // input item
    <liquid:solution_epsilon> * 1000,          // input fluide
    <gas:argon> * 100,                         // input gaz (Mekanism gas)
    <contenttweaker:cartouche_chargee>,        // output item
    <gas:hydrogen> * 0,                        // output gaz (rien)
    100,                                       // duree (ticks)
    400,                                       // energie (J/tick)
    600);                                      // extra energy

// NOTE : si <gas:argon> n'existe pas en gaz Mekanism (seulement en fluide custom),
// fallback : on utilise une autre methode comme Mekanism Chemical Infuser ou
// CraftTweaker addShapedMirrored direct sans Mekanism.


// =============================================================================
// FALLBACK -- Cartouche chargee via crafting shaped si Mekanism PRC casse
// =============================================================================
// Cette recette sert de fallback en cas de bug avec mods.mekanism.reaction.
// Peut etre commentee si la recette PRC plus haut marche.

recipes.addShaped("cartouche_chargee_fallback_recipe",
    <contenttweaker:cartouche_chargee>,
    [[null, <minecraft:bucket>.withTag({FluidName: "argon", Amount: 1000}), null],
     [<contenttweaker:compose_alpha>, <contenttweaker:cartouche_vide>, <contenttweaker:compose_beta>],
     [null, <minecraft:bucket>.withTag({FluidName: "solution_epsilon", Amount: 1000}), null]]);


// =============================================================================
// PHASE 5 -- Recettes ZS pour items intermediaires
// =============================================================================

// Q35 -- Compose alpha (Solvant alpha + H2SO4 + HNO3)
// Shaped : 2x Solvant + 1 H2SO4 bucket + 1 HNO3 bucket -> 1 compose_alpha
recipes.addShaped("compose_alpha_recipe", <contenttweaker:compose_alpha>,
    [[null, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), null],
     [<minecraft:bucket>.withTag({FluidName: "solvant_alpha", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "solvant_alpha", Amount: 1000}), <minecraft:bucket>.withTag({FluidName: "hno3", Amount: 1000})],
     [null, null, null]]);

// Q40 -- Compose beta (Eau Regale + benzene + tributylphosphine proxy)
// Eau Regale : 3 HCl + 1 HNO3 (deja dans le pipeline). On simule via aqua_regia bucket.
recipes.addShaped("compose_beta_recipe", <contenttweaker:compose_beta>,
    [[<ore:ingotGold>, <minecraft:bucket>.withTag({FluidName: "aqua_regia", Amount: 1000}), <ore:ingotGold>],
     [<minecraft:bucket>.withTag({FluidName: "benzene", Amount: 1000}), <contenttweaker:compose_alpha>, <minecraft:bucket>.withTag({FluidName: "benzene", Amount: 1000})],
     [<ore:ingotGold>, <minecraft:bucket>.withTag({FluidName: "aqua_regia", Amount: 1000}), <ore:ingotGold>]]);

// Q43 -- Phenol substitue (phenol + 2 methanol + H2SO4)
// Le phenol n'a pas de fluide custom, on utilise un placeholder via crafting
// proxy avec naphtha (fluide existant). A ameliorer en Phase 5 si besoin.
recipes.addShaped("phenol_substitue_recipe", <contenttweaker:phenol_substitue>,
    [[null, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), null],
     [<minecraft:bucket>.withTag({FluidName: "naphtha", Amount: 1000}), <ore:dustCoal>, <minecraft:bucket>.withTag({FluidName: "naphtha", Amount: 1000})],
     [null, <minecraft:bucket>.withTag({FluidName: "ammoniaque", Amount: 1000}), null]]);

// Q44 -- Carbone Actif Au (charbon + or active via Eau Regale)
recipes.addShaped("carbone_actif_au_recipe", <contenttweaker:carbone_actif_au>,
    [[<ore:dustCoal>, <ore:nuggetGold>, <ore:dustCoal>],
     [<ore:nuggetGold>, <minecraft:bucket>.withTag({FluidName: "aqua_regia", Amount: 1000}), <ore:nuggetGold>],
     [<ore:dustCoal>, <ore:nuggetGold>, <ore:dustCoal>]]);


// =============================================================================
// PHASE 4 -- Recettes capsule + composes gamma
// =============================================================================

// Q49 -- Capsule Pu-Be
// 4 Pu + 4 Be + 1 capsule blindee Lead
recipes.addShaped("capsule_pube_recipe", <contenttweaker:capsule_pube>,
    [[<ore:plateLead>, <ore:ingotPlutonium>, <ore:plateLead>],
     [<ore:ingotBeryllium>, <ore:plateLead>, <ore:ingotBeryllium>],
     [<ore:plateLead>, <ore:ingotPlutonium>, <ore:plateLead>]]);

// Q51 -- Mycelium Active (irradie via capsule Pu-Be)
recipes.addShapeless("mycelium_active_recipe", <contenttweaker:mycelium_active> * 4,
    [<minecraft:mycelium>, <minecraft:mycelium>, <minecraft:mycelium>,
     <minecraft:mycelium>, <contenttweaker:capsule_pube>]);

// Q52 -- Compose gamma1 (Na2B4O7 dope Li -> on utilise borax + lithium)
// Borax existe peut-etre sous mekanism:dust:7 ? Sinon on utilise nether_quartz comme proxy
recipes.addShaped("compose_gamma1_recipe", <contenttweaker:compose_gamma1>,
    [[<ore:ingotLithium>, <minecraft:nether_wart>, <ore:ingotLithium>],
     [<minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <minecraft:nether_wart>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000})],
     [<ore:ingotLithium>, <minecraft:nether_wart>, <ore:ingotLithium>]]);

// Q53 -- Compose gamma2 (Iridium-192 active)
// 1 Ir + capsule Pu-Be (irradiation)
recipes.addShapeless("compose_gamma2_recipe", <contenttweaker:compose_gamma2>,
    [<ore:ingotIridium>, <ore:ingotIridium>, <contenttweaker:capsule_pube>]);

// Q54 -- Compose gamma3 (6LiT)
recipes.addShaped("compose_gamma3_recipe", <contenttweaker:compose_gamma3>,
    [[<ore:ingotLithium>, <minecraft:bucket>.withTag({FluidName: "tritium", Amount: 1000}), <ore:ingotLithium>],
     [<minecraft:bucket>.withTag({FluidName: "tritium", Amount: 1000}), <contenttweaker:capsule_pube>, <minecraft:bucket>.withTag({FluidName: "tritium", Amount: 1000})],
     [<ore:ingotLithium>, <minecraft:bucket>.withTag({FluidName: "tritium", Amount: 1000}), <ore:ingotLithium>]]);


// =============================================================================
// PHASE 5 -- Tryptamide-M + Cristal Manifoldine
// =============================================================================

// Q65 -- Tryptamide-M
recipes.addShaped("tryptamide_m_recipe", <contenttweaker:tryptamide_m>,
    [[<ore:cropWheat>, <minecraft:bucket>.withTag({FluidName: "ammoniaque", Amount: 1000}), <ore:cropWheat>],
     [<contenttweaker:compose_alpha>, <minecraft:bucket>.withTag({FluidName: "acetone", Amount: 1000}), <contenttweaker:compose_alpha>],
     [<ore:cropWheat>, <ore:dustGlowstone>, <ore:cropWheat>]]);

// Q67 -- Compose delta (cristal_manifoldine + Heavy Water + Mycelium Active)
// cristal_manifoldine existe deja dans le ZS legacy, on peut l'utiliser
recipes.addShaped("compose_delta_recipe", <contenttweaker:compose_delta>,
    [[<contenttweaker:mycelium_active>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <contenttweaker:mycelium_active>],
     [<minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <contenttweaker:cristal_manifoldine>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000})],
     [<contenttweaker:mycelium_active>, <minecraft:bucket>.withTag({FluidName: "heavywater", Amount: 1000}), <contenttweaker:mycelium_active>]]);

// Q69 -- Cartouche vide (ampoule iridium vide)
recipes.addShaped("cartouche_vide_recipe", <contenttweaker:cartouche_vide>,
    [[<ore:plateIridium>, <minecraft:glass>, <ore:plateIridium>],
     [<minecraft:glass>, null, <minecraft:glass>],
     [<ore:plateIridium>, <minecraft:glass>, <ore:plateIridium>]]);


// =============================================================================
// PHASE 2 -- Items metaux divers
// =============================================================================

// Q19 -- Cryolite dust (NaF + Al(OH)3 + O2 -> Na3AlF6)
recipes.addShapeless("cryolite_dust_recipe", <contenttweaker:cryolite_dust> * 2,
    [<ore:ingotAluminium>, <minecraft:bucket>.withTag({FluidName: "fluorine_gas", Amount: 1000}),
     <minecraft:bucket>.withTag({FluidName: "naoh_solution", Amount: 1000})]);

// Q22 -- Catalyseur CoMo
recipes.addShaped("catalyseur_como_recipe", <contenttweaker:catalyseur_como>,
    [[<ore:ingotCobalt>, <ore:ingotMolybdenum>, <ore:ingotCobalt>],
     [<ore:ingotMolybdenum>, <minecraft:bucket>.withTag({FluidName: "h2so4", Amount: 1000}), <ore:ingotMolybdenum>],
     [<ore:ingotCobalt>, <ore:ingotMolybdenum>, <ore:ingotCobalt>]]);

// Q27 -- Yellowcake dust (uranium + acide nitrique = ammonium diuranate proxy)
// Si t'as deja du dust uranium, on en convertit en yellowcake
recipes.addShapeless("yellowcake_dust_recipe", <contenttweaker:yellowcake_dust>,
    [<ore:dustUranium>, <minecraft:bucket>.withTag({FluidName: "hno3", Amount: 1000})]);


// =============================================================================
// FIN
// =============================================================================
print("[Age4_Recipes] Recettes Age 4 chargees.");
