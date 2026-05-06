// ============================================================================
// Nexus Absolu - Age4_L1_Multiblocs.zs (ContentTweaker - block controllers)
// ============================================================================
// Block controllers pour les 2 multiblocs custom de la ligne L1 (Petrochimie).
// Reference design : docs/age4-cartouche-manifold/lines/L1-petrochimie.md
//
// 2 multiblocs L1 :
//   1. vacuum_chamber (3x3x3)  - L1.2 desaeration sous vide
//   2. hds_tower (3x8x3)       - L1.4 hydrodesulfuration + L1.5 hydrocrack
//
// Note : MB-MIX (fluid mixer) substitue par le bioreacteur existant pour
// simplifier (recette solvant_alpha_mix utilise bioreacteur).
// ============================================================================

#priority 8000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;


// ============================================================================
// 1. MB-DESA (Vacuum Chamber) - L1.2
// ============================================================================
// Layout 3x3x3, chambre simple sous vide pour separer gaz legers du brut
// Inputs : 1000mB oil
// Outputs : 800mB crude_degazed + 200mB natural_gas
// RF : 5000 RF/t pendant 20s (400 ticks) = 2M RF par cycle

val vacuum_chamber_controller = VanillaFactory.createBlock("vacuum_chamber_controller",
    <blockmaterial:iron>);
vacuum_chamber_controller.setBlockHardness(5.0);
vacuum_chamber_controller.setBlockResistance(15.0);
vacuum_chamber_controller.toolClass = "pickaxe";
vacuum_chamber_controller.toolLevel = 2;
vacuum_chamber_controller.lightValue = 4;
vacuum_chamber_controller.register();


// ============================================================================
// 2. MB-HDS (Tour Hydrodesulfuration) - L1.4 + L1.5
// ============================================================================
// Layout 3x8x3, tour verticale. Reutilisable pour hydrocrack avec autre catalyseur.
// L1.4 : 1000mB kerosene + 200mB hydrogen + 1x pellets_como -> 950mB kerosene_desulfured + 50mB h2s
//        10000 RF/t pendant 30s (600 ticks) = 6M RF par cycle
// L1.5 : 1000mB kerosene_desulfured + 100mB hydrogen + 1x zeolite_pellet -> 1000mB kerosene_premium
//        8000 RF/t pendant 20s (400 ticks) = 3.2M RF par cycle

val hds_tower_controller = VanillaFactory.createBlock("hds_tower_controller",
    <blockmaterial:iron>);
hds_tower_controller.setBlockHardness(7.0);
hds_tower_controller.setBlockResistance(20.0);
hds_tower_controller.toolClass = "pickaxe";
hds_tower_controller.toolLevel = 3;
hds_tower_controller.lightValue = 6;
hds_tower_controller.register();


print("[Nexus Absolu] Age4_L1_Multiblocs.zs loaded -- 2 controllers L1 enregistres");
