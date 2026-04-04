#priority 9000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Item;

// ============================================
// NEXUS ABSOLU — Custom Items (ContentTweaker)
// Uses the Java mod creative tab (nexusabsolu)
// ============================================

// Helper function — uses JAVA creative tab (no separate CT tab)
function buildItem(name as string) {
    val item = mods.contenttweaker.VanillaFactory.createItem(name);
    item.setCreativeTab(<creativetab:nexusabsolu>);
    item.register();
}

// ============================================
// 1. GRITS — Minage des murs Compact Machine
//    Pioche bois  → wall_dust (basique)
//    Pioche pierre → iron_grit, copper_grit, tin_grit
//    Pioche fer    → silver_grit, nickel_grit, lead_grit
//    Pioche diamant → gold_grit, osmium_grit
// ============================================

buildItem("wall_dust");
buildItem("steel_stick");
buildItem("iron_grit");
buildItem("copper_grit");
buildItem("tin_grit");
buildItem("silver_grit");
buildItem("nickel_grit");
buildItem("lead_grit");
buildItem("gold_grit");
buildItem("osmium_grit");

// ============================================
// 2. CLES D'EXPANSION — Gate la progression
//    SUPPRIMEES ICI — maintenant dans le mod Java (ModItems.java)
// ============================================

// ============================================
// 3. COMPOSANTS INTER-MODS
//    SUPPRIMES ICI — maintenant dans le mod Java (ModItems.java)
// ============================================

// ============================================
// 3b. LINGOTS VOSS — Fil rouge du modpack
//     Invarium = Bronze + Invar (Alloy Kiln)
//     Vossium  = Invarium + Compose A (Alloy Kiln)
//     Vossium evolue a chaque age avec les Composes B-E
// ============================================

buildItem("invarium_ingot");        // Age 0 — alliage Cu-Sn-Fe-Ni
buildItem("vossium_ingot");         // Age 0 — Invarium + Compose A
buildItem("vossium_ii_ingot");      // Age 1 — Vossium + Compose B
buildItem("vossium_iii_ingot");     // Age 1 — Vossium-II + Compose C
buildItem("vossium_iv_ingot");      // Age 1 — Vossium-III + Compose D

// ============================================
// 4. FRAGMENTS DE PROGRESSION
//    SUPPRIMES ICI — maintenant dans le mod Java (ModItems.java)
//    Sauf compose_x77, coeur_de_donnees, noyau_fissile,
//    fragment_espace_temps, codex_transcendant, prisme_transcendance
//    qui n'existent PAS encore dans le mod Java
// ============================================

buildItem("compose_x77");           // Âge 3 — Mekanism + Thermal + Astral
buildItem("coeur_de_donnees");      // Âge 4 — AE2
buildItem("noyau_fissile");         // Âge 5 — NuclearCraft
buildItem("fragment_espace_temps"); // Âge 6 — Galacticraft
buildItem("codex_transcendant");    // Âge 7 — Blood Magic + Draconic
buildItem("prisme_transcendance");  // Âge 8 — Draconic + Avaritia

// ============================================
// 5. LE NEXUS ABSOLU
//    SUPPRIME ICI — maintenant dans le mod Java (ModItems.java)
// ============================================
