#loader contenttweaker
// =============================================================================
// AGE 4 — CARTOUCHE MANIFOLD — Items et Fluides custom
// =============================================================================
// Génère ~50 fluides custom et ~30 items custom pour le pipeline Cartouche.
// À placer dans scripts/Age4_Manifold_Content.zs
// Référence : docs/age4-cartouche-manifold/lines/L1-L8.md
// =============================================================================

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Fluid;
import mods.contenttweaker.Item;

// =============================================================================
// L1 — PÉTROCHIMIE
// =============================================================================

val crude_degazed = VanillaFactory.createFluid("crude_degazed", 0x3D2817);
crude_degazed.density = 850; crude_degazed.viscosity = 1500; crude_degazed.register();

val natural_gas = VanillaFactory.createFluid("natural_gas", 0xCFD8DC);
natural_gas.density = 1; natural_gas.viscosity = 100; natural_gas.gaseous = true; natural_gas.register();

val h2s = VanillaFactory.createFluid("h2s", 0xFFEB3B);
h2s.density = 2; h2s.viscosity = 100; h2s.gaseous = true; h2s.register();

val kerosene_desulfured = VanillaFactory.createFluid("kerosene_desulfured", 0xFFF59D);
kerosene_desulfured.density = 800; kerosene_desulfured.viscosity = 800; kerosene_desulfured.register();

val kerosene_premium = VanillaFactory.createFluid("kerosene_premium", 0xFFEE58);
kerosene_premium.density = 780; kerosene_premium.viscosity = 600; kerosene_premium.register();

val solvant_alpha = VanillaFactory.createFluid("solvant_alpha", 0x80DEEA);
solvant_alpha.density = 850; solvant_alpha.viscosity = 700; solvant_alpha.luminosity = 2; solvant_alpha.register();

// =============================================================================
// L2 — HYDRO-EAU
// =============================================================================

val bidistilled_water = VanillaFactory.createFluid("bidistilled_water", 0xB3E5FC); bidistilled_water.register();
val tridistilled_water = VanillaFactory.createFluid("tridistilled_water", 0xE1F5FE); tridistilled_water.luminosity = 3; tridistilled_water.register();
val tritium = VanillaFactory.createFluid("tritium", 0xFFFF00);
tritium.density = 1; tritium.gaseous = true; tritium.luminosity = 8; tritium.register();
// Heavy Water réutilise mekanism:heavywater (déjà existant)

// =============================================================================
// L3 — ÉLECTROLYSE + CRYOGÉNIE
// =============================================================================

val nitrogen_liquid = VanillaFactory.createFluid("nitrogen_liquid", 0xE3F2FD);
nitrogen_liquid.density = 808; nitrogen_liquid.register();

val argon = VanillaFactory.createFluid("argon", 0xCE93D8);
argon.density = 1400; argon.gaseous = true; argon.register();

val sodium_liquid = VanillaFactory.createFluid("sodium_liquid", 0xFFEB3B);
sodium_liquid.density = 970; sodium_liquid.luminosity = 5; sodium_liquid.register();

val chlorine_gas = VanillaFactory.createFluid("chlorine_gas", 0xC8E6C9);
chlorine_gas.gaseous = true; chlorine_gas.register();

val lithium_liquid = VanillaFactory.createFluid("lithium_liquid", 0xF5F5F5);
lithium_liquid.density = 530; lithium_liquid.register();

val licl_fondu = VanillaFactory.createFluid("licl_fondu", 0xFFCCBC);
licl_fondu.density = 1500; licl_fondu.register();

val fluorine_gas = VanillaFactory.createFluid("fluorine_gas", 0xFFF59D);
fluorine_gas.gaseous = true; fluorine_gas.register();

val naoh_solution = VanillaFactory.createFluid("naoh_solution", 0xFFFFFF); naoh_solution.register();

// =============================================================================
// L4 — PYROMÉTALLURGIE
// =============================================================================

val ticl4 = VanillaFactory.createFluid("ticl4", 0xFFC107);
ticl4.density = 1730; ticl4.register();

val carbon_dioxide = VanillaFactory.createFluid("carbon_dioxide", 0xBDBDBD);
carbon_dioxide.gaseous = true; carbon_dioxide.register();

val aqua_regia = VanillaFactory.createFluid("aqua_regia", 0xFF9800);
aqua_regia.luminosity = 4; aqua_regia.register();

val gold_chloride_solution = VanillaFactory.createFluid("gold_chloride_solution", 0xFFB300); gold_chloride_solution.register();

val h3po4 = VanillaFactory.createFluid("h3po4", 0xFFE0B2); h3po4.register();

// =============================================================================
// L5 — NUCLÉAIRE
// =============================================================================

val uf6_gas = VanillaFactory.createFluid("uf6_gas", 0x9C27B0);
uf6_gas.gaseous = true; uf6_gas.luminosity = 6; uf6_gas.register();

// =============================================================================
// L6 — ACIDES + AMMONIAQUE (HUB CENTRAL)
// =============================================================================

val ammoniaque = VanillaFactory.createFluid("ammoniaque", 0xC8E6C9);
ammoniaque.density = 730; ammoniaque.viscosity = 250; ammoniaque.register();

val hno3 = VanillaFactory.createFluid("hno3", 0xFFC107);
hno3.density = 1500; hno3.luminosity = 2; hno3.register();

val h2so4 = VanillaFactory.createFluid("h2so4", 0xFFEB3B);
h2so4.density = 1840; h2so4.viscosity = 2500; h2so4.register();

val hcl = VanillaFactory.createFluid("hcl", 0xFFF59D); hcl.register();

val naoh_concentrated = VanillaFactory.createFluid("naoh_concentrated", 0xFFFFFF);
naoh_concentrated.luminosity = 3; naoh_concentrated.register();

// =============================================================================
// L7 — ORGANIQUE + ACÉTONE
// =============================================================================

val naphtha = VanillaFactory.createFluid("naphtha", 0xFFE082); naphtha.register();
val benzene = VanillaFactory.createFluid("benzene", 0xFFF59D); benzene.register();
val propylene = VanillaFactory.createFluid("propylene", 0xCFD8DC); propylene.gaseous = true; propylene.register();
val cumene = VanillaFactory.createFluid("cumene", 0xFFCC80); cumene.register();
val phenol = VanillaFactory.createFluid("phenol", 0xFF8A65); phenol.register();
val acetone = VanillaFactory.createFluid("acetone", 0xE1F5FE); acetone.viscosity = 320; acetone.register();
val isopropanol = VanillaFactory.createFluid("isopropanol", 0xE0F7FA); isopropanol.register();
val methanol = VanillaFactory.createFluid("methanol", 0xF1F8E9); methanol.register();
val ethanol = VanillaFactory.createFluid("ethanol", 0xFFFDE7); ethanol.register();
val syngas = VanillaFactory.createFluid("syngas", 0xB0BEC5); syngas.gaseous = true; syngas.register();
val methane = VanillaFactory.createFluid("methane", 0xECEFF1); methane.gaseous = true; methane.register();

// =============================================================================
// L8 — BOTANIQUE + MANIFOLDINE
// =============================================================================

// 16 extraits (8 sweet + 8 dark) — couleurs basées sur les champis Botania
val extract_sweet_red = VanillaFactory.createFluid("extract_sweet_red", 0xEF5350); extract_sweet_red.luminosity = 4; extract_sweet_red.register();
val extract_sweet_orange = VanillaFactory.createFluid("extract_sweet_orange", 0xFF9800); extract_sweet_orange.luminosity = 4; extract_sweet_orange.register();
val extract_sweet_yellow = VanillaFactory.createFluid("extract_sweet_yellow", 0xFFEB3B); extract_sweet_yellow.luminosity = 4; extract_sweet_yellow.register();
val extract_sweet_pink = VanillaFactory.createFluid("extract_sweet_pink", 0xF8BBD0); extract_sweet_pink.luminosity = 4; extract_sweet_pink.register();
val extract_sweet_lime = VanillaFactory.createFluid("extract_sweet_lime", 0xCDDC39); extract_sweet_lime.luminosity = 4; extract_sweet_lime.register();
val extract_sweet_cyan = VanillaFactory.createFluid("extract_sweet_cyan", 0x00BCD4); extract_sweet_cyan.luminosity = 4; extract_sweet_cyan.register();
val extract_sweet_lightblue = VanillaFactory.createFluid("extract_sweet_lightblue", 0x81D4FA); extract_sweet_lightblue.luminosity = 4; extract_sweet_lightblue.register();
val extract_sweet_magenta = VanillaFactory.createFluid("extract_sweet_magenta", 0xE91E63); extract_sweet_magenta.luminosity = 4; extract_sweet_magenta.register();

val extract_dark_black = VanillaFactory.createFluid("extract_dark_black", 0x212121); extract_dark_black.luminosity = 1; extract_dark_black.register();
val extract_dark_purple = VanillaFactory.createFluid("extract_dark_purple", 0x7B1FA2); extract_dark_purple.luminosity = 2; extract_dark_purple.register();
val extract_dark_brown = VanillaFactory.createFluid("extract_dark_brown", 0x5D4037); extract_dark_brown.luminosity = 1; extract_dark_brown.register();
val extract_dark_gray = VanillaFactory.createFluid("extract_dark_gray", 0x424242); extract_dark_gray.luminosity = 1; extract_dark_gray.register();
val extract_dark_green = VanillaFactory.createFluid("extract_dark_green", 0x1B5E20); extract_dark_green.luminosity = 2; extract_dark_green.register();
val extract_dark_blue = VanillaFactory.createFluid("extract_dark_blue", 0x0D47A1); extract_dark_blue.luminosity = 2; extract_dark_blue.register();
val extract_dark_lightgray = VanillaFactory.createFluid("extract_dark_lightgray", 0x9E9E9E); extract_dark_lightgray.luminosity = 1; extract_dark_lightgray.register();
val extract_dark_white = VanillaFactory.createFluid("extract_dark_white", 0xEEEEEE); extract_dark_white.luminosity = 2; extract_dark_white.register();

// L8 outputs principaux
val essence_chromatic = VanillaFactory.createFluid("essence_chromatic", 0xFFFFFF);
essence_chromatic.luminosity = 12; essence_chromatic.register();
// NB: Essence Chromatic devra être tinted en mode "rainbow" via shader/animation custom

val manifoldine_extract_purified = VanillaFactory.createFluid("manifoldine_extract_purified", 0x7CB342);
manifoldine_extract_purified.luminosity = 6; manifoldine_extract_purified.register();

val manifoldine_brute = VanillaFactory.createFluid("manifoldine_brute", 0x9C27B0);
manifoldine_brute.luminosity = 14; manifoldine_brute.register();

// =============================================================================
// FINAL — SÉRUM MANIFOLD
// =============================================================================

val serum_manifold_brut = VanillaFactory.createFluid("serum_manifold_brut", 0xAA00FF);
serum_manifold_brut.luminosity = 10; serum_manifold_brut.register();

val serum_manifold_active = VanillaFactory.createFluid("serum_manifold_active", 0x00E5FF);
serum_manifold_active.luminosity = 15; serum_manifold_active.register();
// Note: glow cyan/violet animé — à custom-tint via resource pack

// =============================================================================
// ITEMS — PIGMENTS DES 16 CHAMPIS
// =============================================================================

val pigment_red = VanillaFactory.createItem("pigment_red"); pigment_red.maxStackSize = 64; pigment_red.register();
val pigment_orange = VanillaFactory.createItem("pigment_orange"); pigment_orange.register();
val pigment_yellow = VanillaFactory.createItem("pigment_yellow"); pigment_yellow.register();
val pigment_pink = VanillaFactory.createItem("pigment_pink"); pigment_pink.register();
val pigment_lime = VanillaFactory.createItem("pigment_lime"); pigment_lime.register();
val pigment_cyan = VanillaFactory.createItem("pigment_cyan"); pigment_cyan.register();
val pigment_lightblue = VanillaFactory.createItem("pigment_lightblue"); pigment_lightblue.register();
val pigment_magenta = VanillaFactory.createItem("pigment_magenta"); pigment_magenta.register();
val pigment_black = VanillaFactory.createItem("pigment_black"); pigment_black.register();
val pigment_purple = VanillaFactory.createItem("pigment_purple"); pigment_purple.register();
val pigment_brown = VanillaFactory.createItem("pigment_brown"); pigment_brown.register();
val pigment_gray = VanillaFactory.createItem("pigment_gray"); pigment_gray.register();
val pigment_green = VanillaFactory.createItem("pigment_green"); pigment_green.register();
val pigment_blue = VanillaFactory.createItem("pigment_blue"); pigment_blue.register();
val pigment_lightgray = VanillaFactory.createItem("pigment_lightgray"); pigment_lightgray.register();
val pigment_white = VanillaFactory.createItem("pigment_white"); pigment_white.register();

// =============================================================================
// ITEMS — CATALYSEURS (durables, certains sacrificiels)
// =============================================================================

val pellets_como = VanillaFactory.createItem("pellets_como"); pellets_como.maxStackSize = 16; pellets_como.register();
val zeolite_pellet = VanillaFactory.createItem("zeolite_pellet"); zeolite_pellet.register();
val tensioactif_phosphate = VanillaFactory.createItem("tensioactif_phosphate"); tensioactif_phosphate.register();
val fe_k2o_catalyst = VanillaFactory.createItem("fe_k2o_catalyst"); fe_k2o_catalyst.register(); // durable 100c
val platinum_gauze = VanillaFactory.createItem("platinum_gauze"); platinum_gauze.register(); // durable 200c
val v2o5_catalyst = VanillaFactory.createItem("v2o5_catalyst"); v2o5_catalyst.register(); // durable 150c
val pt_re_catalyst = VanillaFactory.createItem("pt_re_catalyst"); pt_re_catalyst.register();
val cu_zn_catalyst = VanillaFactory.createItem("cu_zn_catalyst"); cu_zn_catalyst.register();
val pt_al2o3_catalyst = VanillaFactory.createItem("pt_al2o3_catalyst"); pt_al2o3_catalyst.register();
val pd_c_catalyst = VanillaFactory.createItem("pd_c_catalyst"); pd_c_catalyst.register();
val cu_zno_catalyst = VanillaFactory.createItem("cu_zno_catalyst"); cu_zno_catalyst.register();
val acid_catalyst = VanillaFactory.createItem("acid_catalyst"); acid_catalyst.register();

// =============================================================================
// ITEMS — INTERMÉDIAIRES CHIMIQUES
// =============================================================================

val sulfur_pure = VanillaFactory.createItem("sulfur_pure"); sulfur_pure.register();
val alumina = VanillaFactory.createItem("alumina"); alumina.register();
val cryolithe_block = VanillaFactory.createItem("cryolithe_block"); cryolithe_block.register();
val titanium_sponge = VanillaFactory.createItem("titanium_sponge"); titanium_sponge.register();
val titanium_pure = VanillaFactory.createItem("titanium_pure"); titanium_pure.register();
val aluminum_pure = VanillaFactory.createItem("aluminum_pure"); aluminum_pure.register();
val sodium_ingot = VanillaFactory.createItem("sodium_ingot"); sodium_ingot.register();
val magnesium_ingot = VanillaFactory.createItem("magnesium_ingot"); magnesium_ingot.register();
val magnesium_chloride = VanillaFactory.createItem("magnesium_chloride"); magnesium_chloride.register();
val magnesium_fluoride = VanillaFactory.createItem("magnesium_fluoride"); magnesium_fluoride.register();
val fluorine_capsule = VanillaFactory.createItem("fluorine_capsule"); fluorine_capsule.register();
val phosphorus_white = VanillaFactory.createItem("phosphorus_white");
phosphorus_white.maxStackSize = 16; phosphorus_white.register(); // pyrophorique → stack limité
val calcium_phosphate = VanillaFactory.createItem("calcium_phosphate"); calcium_phosphate.register();
val fluorite_dust = VanillaFactory.createItem("fluorite_dust"); fluorite_dust.register();
val slag_silicate = VanillaFactory.createItem("slag_silicate"); slag_silicate.register();
val graphite_block = VanillaFactory.createItem("graphite_block"); graphite_block.register();
val mercury_pool = VanillaFactory.createItem("mercury_pool"); mercury_pool.maxStackSize = 1; mercury_pool.register();
val resin_charge = VanillaFactory.createItem("resin_charge"); resin_charge.register();
val lithium6_ingot = VanillaFactory.createItem("lithium6_ingot"); lithium6_ingot.register();
val helium4_capsule = VanillaFactory.createItem("helium4_capsule"); helium4_capsule.register();
val uranyl_dust = VanillaFactory.createItem("uranyl_dust"); uranyl_dust.register();
val bef2_dust = VanillaFactory.createItem("bef2_dust"); bef2_dust.register();
val b2o3_dust = VanillaFactory.createItem("b2o3_dust"); b2o3_dust.register();
val beryllium_pure = VanillaFactory.createItem("beryllium_pure"); beryllium_pure.register();
val boron_pure = VanillaFactory.createItem("boron_pure"); boron_pure.register();
val plutonium239_ingot = VanillaFactory.createItem("plutonium239_ingot"); plutonium239_ingot.register();
val thorium_ingot = VanillaFactory.createItem("thorium_ingot"); thorium_ingot.register();
val gold_pure_99 = VanillaFactory.createItem("gold_pure_99"); gold_pure_99.register();
val platinum_pure_99 = VanillaFactory.createItem("platinum_pure_99"); platinum_pure_99.register();
val iridium_pure_99 = VanillaFactory.createItem("iridium_pure_99"); iridium_pure_99.register();
val osmium_pure_99 = VanillaFactory.createItem("osmium_pure_99"); osmium_pure_99.register();
val iron_dust_pure = VanillaFactory.createItem("iron_dust_pure"); iron_dust_pure.register();
val iron_chloride_dust = VanillaFactory.createItem("iron_chloride_dust"); iron_chloride_dust.register();
val p2o5_capsule = VanillaFactory.createItem("p2o5_capsule"); p2o5_capsule.register();
val cryotheum_used = VanillaFactory.createItem("cryotheum_used"); cryotheum_used.register();
val air_canister = VanillaFactory.createItem("air_canister"); air_canister.register();

// =============================================================================
// ITEMS — COMPOSÉS γ (énergétiques nucléaires)
// =============================================================================

val gamma1_uf6_capsule = VanillaFactory.createItem("gamma1_uf6_capsule"); gamma1_uf6_capsule.register();
val gamma2_pube_borate_capsule = VanillaFactory.createItem("gamma2_pube_borate_capsule"); gamma2_pube_borate_capsule.register();
val gamma3_lit_capsule = VanillaFactory.createItem("gamma3_lit_capsule"); gamma3_lit_capsule.register();

// =============================================================================
// ITEMS — COMPOSÉS β (organométalliques)
// =============================================================================

val beta1_cobalt_phthalocyanine = VanillaFactory.createItem("beta1_cobalt_phthalocyanine"); beta1_cobalt_phthalocyanine.register();
val beta2_iridium_hexafluoride = VanillaFactory.createItem("beta2_iridium_hexafluoride"); beta2_iridium_hexafluoride.register();

// =============================================================================
// ITEMS — COMPOSÉS δ (bio-actifs, Théorème IV)
// =============================================================================

val delta1_glucose_phosphate = VanillaFactory.createItem("delta1_glucose_phosphate"); delta1_glucose_phosphate.register();
val delta2_neural_silver = VanillaFactory.createItem("delta2_neural_silver"); delta2_neural_silver.register();

// =============================================================================
// ITEMS — MANIFOLDINE PIPELINE
// =============================================================================

val indole_dust = VanillaFactory.createItem("indole_dust"); indole_dust.register();
val tryptamide_m_capsule = VanillaFactory.createItem("tryptamide_m_capsule"); tryptamide_m_capsule.register();
val matrix_pigmentary = VanillaFactory.createItem("matrix_pigmentary"); matrix_pigmentary.register();
val mana_bound_capsule = VanillaFactory.createItem("mana_bound_capsule"); mana_bound_capsule.register();
val cristal_chromatic_raw = VanillaFactory.createItem("cristal_chromatic_raw"); cristal_chromatic_raw.register();
val spores_active = VanillaFactory.createItem("spores_active"); spores_active.register();
val cristal_manifoldine = VanillaFactory.createItem("cristal_manifoldine");
cristal_manifoldine.maxStackSize = 16; cristal_manifoldine.register(); // ⭐⭐⭐ KEY ITEM

// =============================================================================
// ITEMS — COMPOSÉ ε (Solution Manifoldine Active)
// =============================================================================

val epsilon_manifoldine_active = VanillaFactory.createItem("epsilon_manifoldine_active"); epsilon_manifoldine_active.register();

// =============================================================================
// ITEMS — CARTOUCHE FINALE
// =============================================================================
// NB: cartouche_manifold et cartouche_used sont desormais dans le MOD SOURCE
// (com.nexusabsolu.mod.items.ItemCartoucheManifold / ItemCartoucheUsed)
// pour avoir un comportement Java natif (right-click, NBT cooldown, glint).
// IDs : nexusabsolu:cartouche_manifold et nexusabsolu:cartouche_used.

val casing_cartouche_empty = VanillaFactory.createItem("casing_cartouche_empty");
casing_cartouche_empty.maxStackSize = 16; casing_cartouche_empty.register();

val casing_cartouche_sterile = VanillaFactory.createItem("casing_cartouche_sterile");
casing_cartouche_sterile.maxStackSize = 16; casing_cartouche_sterile.register();

// =============================================================================
// PHASE 1 -- ITEMS POUR LES QUETES Q1-Q15 (Age 4)
// =============================================================================
// Items requis par les quetes BetterQuesting Phase 1.
// Voir : docs/age4-cartouche-manifold/quetes-phase1.md

// Resine Echangeuse d'ions (Q2) -- block utilise pour purifier l'eau
// Recette : silice + acide sulfonique (a definir dans Age4_Recipes.zs)
val resine_echangeuse_block = VanillaFactory.createBlock("resine_echangeuse_block",
    <blockmaterial:iron>);
resine_echangeuse_block.setBlockHardness(2.5);
resine_echangeuse_block.setBlockResistance(5.0);
resine_echangeuse_block.toolClass = "pickaxe";
resine_echangeuse_block.toolLevel = 1;
resine_echangeuse_block.register();

// Cryo Distillateur Controller (Q4) -- bloc multibloc 3x6x3
// Multibloc en game : controller + 11 cryo plates + 8 glass + 4 cryotheum tanks
val cryo_distillateur_controller = VanillaFactory.createBlock("cryo_distillateur_controller",
    <blockmaterial:iron>);
cryo_distillateur_controller.setBlockHardness(4.0);
cryo_distillateur_controller.setBlockResistance(8.0);
cryo_distillateur_controller.toolClass = "pickaxe";
cryo_distillateur_controller.toolLevel = 2;
cryo_distillateur_controller.register();

// =============================================================================
// PHASE 2 -- ITEMS POUR LES QUETES Q16-Q30 (Age 4)
// =============================================================================
// Items custom requis par les quetes BetterQuesting Phase 2.
// Voir : docs/age4-cartouche-manifold/quetes-phase2.md

// Cryolite dust (Q19) -- pour Hall-Heroult electrolyse Al
val cryolite_dust = VanillaFactory.createItem("cryolite_dust");
cryolite_dust.maxStackSize = 64;
cryolite_dust.register();

// Catalyseur CoMo (Q22) -- cobalt-molybdene sulfure pour HDS petrochimie
val catalyseur_como = VanillaFactory.createItem("catalyseur_como");
catalyseur_como.maxStackSize = 16;
catalyseur_como.register();

// Yellowcake dust (Q27) -- minerai uranium U3O8 pre-enrichissement
val yellowcake_dust = VanillaFactory.createItem("yellowcake_dust");
yellowcake_dust.maxStackSize = 16;
yellowcake_dust.glowing = true;
yellowcake_dust.register();

// =============================================================================
// PHASE 3 -- ITEMS POUR LES QUETES Q31-Q45 (Age 4)
// =============================================================================
// Items custom requis par les quetes BetterQuesting Phase 3.
// Voir : docs/age4-cartouche-manifold/quetes-phase3.md

// Compose alpha (Q35) -- super-acide stabilise (Solvant alpha + H2SO4 + HNO3)
val compose_alpha = VanillaFactory.createItem("compose_alpha");
compose_alpha.maxStackSize = 16;
compose_alpha.glowing = true;
compose_alpha.register();

// Compose beta (Q40) -- organometallique Au-PR3 (AuCl3 + benzene + tributylphosphine)
val compose_beta = VanillaFactory.createItem("compose_beta");
compose_beta.maxStackSize = 16;
compose_beta.glowing = true;
compose_beta.register();

// Ether Etoile (Q42) -- fluide stabilise par Liquid Starlight
val ether_etoile = VanillaFactory.createFluid("ether_etoile", 0xCFD8DC);
ether_etoile.luminosity = 7;
ether_etoile.viscosity = 400;
ether_etoile.register();

// Phenol Substitue (Q43) -- coeur aromatique de la Manifoldine, methoxy en para
val phenol_substitue = VanillaFactory.createItem("phenol_substitue");
phenol_substitue.maxStackSize = 64;
phenol_substitue.register();

// Carbone Actif Au (Q44) -- charbon active impregne d'AuCl3 (catalyseur cyclisation)
val carbone_actif_au = VanillaFactory.createItem("carbone_actif_au");
carbone_actif_au.maxStackSize = 32;
carbone_actif_au.register();

// =============================================================================
// PHASE 4 -- ITEMS POUR LES QUETES Q46-Q55 (Age 4 nucleaire)
// =============================================================================
// uf6_gas est deja registered comme fluide ligne 94.

// Capsule Pu-Be (Q49) -- source neutronique scellee, glow gamma
val capsule_pube = VanillaFactory.createItem("capsule_pube");
capsule_pube.maxStackSize = 4;
capsule_pube.glowing = true;
capsule_pube.register();

// Mycelium Active (Q51) -- mycelium irradie pour activer la magie Phase 5
val mycelium_active = VanillaFactory.createItem("mycelium_active");
mycelium_active.maxStackSize = 64;
mycelium_active.glowing = true;
mycelium_active.register();

// Compose gamma1 (Q52) -- borate de sodium dope au lithium
val compose_gamma1 = VanillaFactory.createItem("compose_gamma1");
compose_gamma1.maxStackSize = 16;
compose_gamma1.glowing = true;
compose_gamma1.register();

// Compose gamma2 (Q53) -- iridium-192 active (source rayons gamma)
val compose_gamma2 = VanillaFactory.createItem("compose_gamma2");
compose_gamma2.maxStackSize = 8;
compose_gamma2.glowing = true;
compose_gamma2.register();

// Compose gamma3 (Q54) -- 6LiT lithium tritide (densite energetique max)
val compose_gamma3 = VanillaFactory.createItem("compose_gamma3");
compose_gamma3.maxStackSize = 4;
compose_gamma3.glowing = true;
compose_gamma3.register();

// =============================================================================
// FIN DU FICHIER
// =============================================================================
// Nombre total de fluides : ~51
// Nombre total d'items : ~85
// Lignes : ~370
// =============================================================================
