#priority 9000
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Item;

// ============================================
// NEXUS ABSOLU — Custom Items
// ============================================

// Creative Tab
mods.contenttweaker.VanillaFactory.createCreativeTab("nexus_absolu", <item:minecraft:nether_star>).register();

// Helper function
function buildItem(name as string) {
    val item = mods.contenttweaker.VanillaFactory.createItem(name);
    item.setCreativeTab(<creativetab:nexus_absolu>);
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
//    Chaque clé nécessite des ressources de l'âge
//    pour débloquer la salle suivante.
// ============================================

buildItem("compact_key_5x5");
buildItem("compact_key_7x7");
buildItem("compact_key_9x9");
buildItem("compact_key_11x11");
buildItem("compact_key_13x13");
buildItem("lab_key");

// ============================================
// 3. COMPOSANTS INTER-MODS
//    Forcent les interactions entre mods
// ============================================

buildItem("infused_circuit");       // Thermal + Astral Sorcery
buildItem("resonant_coil");         // IE + Thermal + EnderIO
buildItem("organic_catalyst");      // Botania + Blood Magic + Pam's

// ============================================
// 4. FRAGMENTS DE PROGRESSION
//    Les 9 composants du Nexus Absolu
// ============================================

buildItem("fragment_mecanique");     // Âge 1 — Thermal + IE
buildItem("fragment_organique");     // Âge 2 — Botania + Blood Magic
buildItem("fragment_stellaire");     // Âge 2 — Astral Sorcery
buildItem("compose_x77");           // Âge 3 — Mekanism + Thermal + Astral
buildItem("coeur_de_donnees");      // Âge 4 — AE2
buildItem("noyau_fissile");         // Âge 5 — NuclearCraft
buildItem("fragment_espace_temps"); // Âge 6 — Galacticraft
buildItem("codex_transcendant");    // Âge 7 — Blood Magic + Draconic
buildItem("prisme_transcendance");  // Âge 8 — Draconic + Avaritia

// ============================================
// 5. LE NEXUS ABSOLU
//    L'item final — table 9x9
// ============================================

buildItem("nexus_absolu");
