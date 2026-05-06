// ============================================================================
// Nexus Absolu — Age4_L8_Multiblocs.zs
//
// Block controllers ContentTweaker pour les 5 multiblocs custom de la ligne L8
// (Botanique + Manifoldine).
//
// Reference design : docs/age4-cartouche-manifold/lines/L8-botanique-manifoldine.md
// JSON machinery : config/modularmachinery/machinery/*.json
// JSON recipes   : config/modularmachinery/recipes/soxhlet_*.json, cyclo_*.json, ...
//
// Convention : pour chaque multibloc X, le block controller est nomme
// 'X_controller' (createBlock). Le JSON machinery a registryname 'X'.
// Modular Machinery associe automatiquement le block au JSON par convention.
//
// 5 multiblocs L8 :
//   1. soxhlet_extractor (3x5x3)         - L8.C.2 extraction Soxhlet
//   2. cyclisateur_stellaire (5x6x5)     - L8.C.3 cyclisation nuit ⭐⭐⭐
//   3. evaporator (3x3x3)                - L8.C.4 stabilisation cristal
//   4. alambic_manaic (5x4x5)            - L8.A.4 alambic Botania mana
//   5. mana_enchanter (3x3x3)            - L8.A.6 charge manaique finale
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// ============================================================================
// 1. MB-SOXHLET (Extracteur Soxhlet) - L8.C.2
// ============================================================================
// Layout 3x5x3, vertical avec reflux acetone+ammoniaque+methanol
// Inputs : 8x spores_active + 200mB acetone + 100mB ammoniaque + 100mB methanol
// Output : 200mB manifoldine_extract_purified
// RF : 8000 RF/t pendant 3 min (3600 ticks) = 28.8M RF par cycle

val soxhlet_extractor_controller = VanillaFactory.createBlock("soxhlet_extractor_controller",
    <blockmaterial:iron>);
soxhlet_extractor_controller.setBlockHardness(5.0);
soxhlet_extractor_controller.setBlockResistance(15.0);
soxhlet_extractor_controller.toolClass = "pickaxe";
soxhlet_extractor_controller.toolLevel = 2;
soxhlet_extractor_controller.lightValue = 4;  // legere lumination interne
soxhlet_extractor_controller.register();


// ============================================================================
// 2. MB-CYCLO (Cyclisateur Stellaire) - L8.C.3 ⭐⭐⭐
// ============================================================================
// Layout 5x6x5, DOIT etre a ciel ouvert la NUIT pour fonctionner.
// Au-dessus : Astral Sorcery Collector Crystal (Starlight direct).
// Inputs : 200mB extract + 1x tryptamide_m_capsule + 500mB liquid_starlight
//          + 100mB heavywater + 200mB argon
// Output : 100mB manifoldine_brute
// RF : 15000 RF/t pendant 5 min (6000 ticks) = 90M RF par cycle (gros consommateur)
// Conditions speciales (dans le recipe JSON) :
//   - dimension : overworld (0)
//   - position : y_min 60 (ciel ouvert)
//   - weather : clear (pas de pluie)
//   - time : 13000-23000 (nuit minecraft)

val cyclisateur_stellaire_controller = VanillaFactory.createBlock("cyclisateur_stellaire_controller",
    <blockmaterial:iron>);
cyclisateur_stellaire_controller.setBlockHardness(8.0);
cyclisateur_stellaire_controller.setBlockResistance(25.0);
cyclisateur_stellaire_controller.toolClass = "pickaxe";
cyclisateur_stellaire_controller.toolLevel = 3;
cyclisateur_stellaire_controller.lightValue = 12;  // brille fort la nuit (effet stellaire)
cyclisateur_stellaire_controller.register();


// ============================================================================
// 3. MB-EVAPORATOR (Evaporateur cristallisation) - L8.C.4
// ============================================================================
// Layout 3x3x3, simple chambre de cristallisation chauffee
// Inputs : 100mB manifoldine_brute + 50mB h3po4
// Output : 1x cristal_manifoldine ⭐ + 100mB tridistilled_water (recyclage)
// RF : 3000 RF/t pendant 1 min (1200 ticks) = 3.6M RF par cycle
// REUTILISABLE : sera aussi utilise en L6 pour cristallisation acides.

val evaporator_controller = VanillaFactory.createBlock("evaporator_controller",
    <blockmaterial:iron>);
evaporator_controller.setBlockHardness(4.0);
evaporator_controller.setBlockResistance(10.0);
evaporator_controller.toolClass = "pickaxe";
evaporator_controller.toolLevel = 2;
evaporator_controller.lightValue = 6;  // brille (chauffe interne)
evaporator_controller.register();


// ============================================================================
// 4. MB-ALAMBIC (Alambic Manaique) - L8.A.4
// ============================================================================
// Layout 5x4x5 avec Mana Pool au centre + 8 Mana Spreaders convergents.
// Inputs : 16 extraits dosés (8 doux + 8 sombres) + 4x botania manaresource
// Output : 1x matrix_pigmentary + 4x mana_bound_capsule (recuperation mana)
// RF : 2000 RF/t + 200k Mana, pendant 1 min (1200 ticks) = 2.4M RF + Mana
// Note : integration mana via item proxy (KubeJS necessaire pour vraie consumption)

val alambic_manaic_controller = VanillaFactory.createBlock("alambic_manaic_controller",
    <blockmaterial:iron>);
alambic_manaic_controller.setBlockHardness(6.0);
alambic_manaic_controller.setBlockResistance(20.0);
alambic_manaic_controller.toolClass = "pickaxe";
alambic_manaic_controller.toolLevel = 2;
alambic_manaic_controller.lightValue = 10;  // lumiere mana bleu/turquoise
alambic_manaic_controller.register();


// ============================================================================
// 5. MB-MANA-ENCHANTER (Charge Manaique Finale) - L8.A.6
// ============================================================================
// Layout 3x3x3, plus petit mais necessite Mana Pearl x4 (= ~1M Mana).
// Inputs : 1x cristal_chromatic_raw + 4x botania:manaresource:8 (Mana Pearl)
// Output : 1000mB essence_chromatic
// RF : 10000 RF/t pendant 30s (600 ticks) = 6M RF par cycle

val mana_enchanter_controller = VanillaFactory.createBlock("mana_enchanter_controller",
    <blockmaterial:iron>);
mana_enchanter_controller.setBlockHardness(5.0);
mana_enchanter_controller.setBlockResistance(15.0);
mana_enchanter_controller.toolClass = "pickaxe";
mana_enchanter_controller.toolLevel = 2;
mana_enchanter_controller.lightValue = 14;  // brille tres fort (mana pearl)
mana_enchanter_controller.register();


print("[Nexus Absolu] Age4_L8_Multiblocs.zs loaded -- 5 controllers L8 enregistres");
