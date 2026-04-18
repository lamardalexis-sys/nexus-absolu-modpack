package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.items.ItemBase;
import com.nexusabsolu.mod.items.ItemCleLiberte;
import com.nexusabsolu.mod.items.ItemCleLiberteActivee;
import com.nexusabsolu.mod.items.ItemCompose;
import com.nexusabsolu.mod.items.ItemFurnaceUpgrade;
import com.nexusabsolu.mod.items.ItemGrabberVoss;
import com.nexusabsolu.mod.items.ItemGrit;
import com.nexusabsolu.mod.items.ItemPioche;
import com.nexusabsolu.mod.items.fragments.ItemFragment;
import com.nexusabsolu.mod.items.fragments.ItemNexusAbsolu;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class ModItems {
    public static final List<Item> ITEMS = new ArrayList<>();

    // === PIOCHES CUSTOM ===
    // MAT_FRAGMENTEE: meme que WOOD vanilla sauf durabilite augmentee (97 au lieu de 59)
    // harvestLevel=0 (peut miner stone, pas iron), efficiency=2.0 (comme WOOD), enchantability=15
    private static final Item.ToolMaterial MAT_FRAGMENTEE = net.minecraftforge.common.util.EnumHelper
        .addToolMaterial("NEXUS_FRAGMENTEE", 0, 97, 2.0F, 0.0F, 15);

    public static final Item PIOCHE_FRAGMENTEE = new ItemPioche("pioche_fragmentee",
        MAT_FRAGMENTEE, 1);
    public static final Item PIOCHE_RENFORCEE = new ItemPioche("pioche_renforcee",
        Item.ToolMaterial.IRON, 2);

    // === PIOCHES SPECIALISEES (Age 1) ===
    // Custom ToolMaterials: harvestLevel, maxUses, efficiency, damage, enchantability
    private static final Item.ToolMaterial MAT_SPEC = net.minecraftforge.common.util.EnumHelper
        .addToolMaterial("NEXUS_SPEC", 2, 1500, 6.0F, 2.0F, 14);
    private static final Item.ToolMaterial MAT_VOSS = net.minecraftforge.common.util.EnumHelper
        .addToolMaterial("NEXUS_VOSS", 2, 2500, 6.0F, 2.0F, 14);

    public static final Item PIOCHE_CUIVREE = new ItemPioche("pioche_cuivree",
        MAT_SPEC, 3, "base_metals");       // copper, tin, nickel
    public static final Item PIOCHE_FERREE = new ItemPioche("pioche_ferree",
        MAT_SPEC, 3, "iron_metals");       // iron, lead, silver
    public static final Item PIOCHE_PRECIEUSE = new ItemPioche("pioche_precieuse",
        MAT_SPEC, 3, "precious");          // gold, osmium
    public static final Item PIOCHE_VOSSIUM = new ItemPioche("pioche_vossium",
        MAT_VOSS, 3, "compose");           // compose_a only (high rate)

    // === PIOCHE STEELIUM (Age 1 — post-steel) ===
    private static final Item.ToolMaterial MAT_STEEL = net.minecraftforge.common.util.EnumHelper
        .addToolMaterial("NEXUS_STEEL", 3, 3000, 7.0F, 2.5F, 14);
    public static final Item PIOCHE_STEELIUM = new ItemPioche("pioche_steelium",
        MAT_STEEL, 4, "steelium");         // compose_b, obsidian frag, diamond, emerald

    // === GRITS (scavenging) ===
    public static final Item WALL_DUST = new ItemBase("wall_dust");
    public static final Item IRON_GRIT = new ItemGrit("iron_grit", "Iron");
    public static final Item COPPER_GRIT = new ItemGrit("copper_grit", "Copper");
    public static final Item TIN_GRIT = new ItemGrit("tin_grit", "Tin");
    public static final Item SILVER_GRIT = new ItemGrit("silver_grit", "Silver");
    public static final Item NICKEL_GRIT = new ItemGrit("nickel_grit", "Nickel");
    public static final Item LEAD_GRIT = new ItemGrit("lead_grit", "Lead");
    public static final Item GOLD_GRIT = new ItemGrit("gold_grit", "Gold");
    public static final Item OSMIUM_GRIT = new ItemGrit("osmium_grit", "Osmium");

    // === RAW GRITS (4 grits -> 1 raw grit -> furnace -> ingot) ===
    public static final Item RAW_IRON_GRIT = new ItemBase("raw_iron_grit");
    public static final Item RAW_COPPER_GRIT = new ItemBase("raw_copper_grit");
    public static final Item RAW_TIN_GRIT = new ItemBase("raw_tin_grit");
    public static final Item RAW_SILVER_GRIT = new ItemBase("raw_silver_grit");
    public static final Item RAW_NICKEL_GRIT = new ItemBase("raw_nickel_grit");
    public static final Item RAW_LEAD_GRIT = new ItemBase("raw_lead_grit");
    public static final Item RAW_GOLD_GRIT = new ItemBase("raw_gold_grit");
    public static final Item RAW_OSMIUM_GRIT = new ItemBase("raw_osmium_grit");

    // === FRAGMENTS (from scavenging) ===
    public static final Item COBBLESTONE_FRAGMENT = new ItemBase("cobblestone_fragment");

    public static final Item OBSIDIAN_FRAGMENT = new ItemBase("obsidian_fragment");

    // === EXPANSION KEYS ===
    public static final Item COMPACT_KEY_5X5 = new ItemBase("compact_key_5x5");
    public static final Item COMPACT_KEY_7X7 = new ItemBase("compact_key_7x7");
    public static final Item COMPACT_KEY_9X9 = new ItemBase("compact_key_9x9");
    public static final Item COMPACT_KEY_11X11 = new ItemBase("compact_key_11x11");
    public static final Item COMPACT_KEY_13X13 = new ItemBase("compact_key_13x13");
    public static final Item LAB_KEY = new ItemBase("lab_key");

    // === CATALYSEURS DE PHASE (for Condenseur) ===
    public static final Item CATALYSEUR_INSTABLE = new ItemBase("catalyseur_instable");
    public static final Item CATALYSEUR_VOLATILE = new ItemBase("catalyseur_volatile");
    public static final Item CATALYSEUR_CRITIQUE = new ItemBase("catalyseur_critique");
    public static final Item CATALYSEUR_RESONANT = new ItemBase("catalyseur_resonant");
    public static final Item CATALYSEUR_ABSOLU = new ItemBase("catalyseur_absolu");

    // === INTER-MOD COMPONENTS ===
    public static final Item INFUSED_CIRCUIT = new ItemBase("infused_circuit");
    public static final Item RESONANT_COIL = new ItemBase("resonant_coil");
    public static final Item ORGANIC_CATALYST = new ItemBase("organic_catalyst");

    // === CUSTOM MATERIALS ===
    public static final Item VOSSIUM_INGOT = new ItemBase("vossium_ingot");
    public static final Item INVARIUM_INGOT = new ItemBase("invarium_ingot");
    public static final Item VOSSIUM_II_INGOT = new ItemBase("vossium_ii_ingot");
    public static final Item VOSSIUM_III_INGOT = new ItemBase("vossium_iii_ingot");
    public static final Item VOSSIUM_IV_INGOT = new ItemBase("vossium_iv_ingot");
    public static final Item SIGNALHEE_INGOT = new ItemBase("signalhee_ingot");
    public static final Item IRON_INSULE = new ItemBase("iron_insule");

    // === OVERWORLD MINING - TUTUOSSS (Age 2+, force le minage manuel avant Digital Miner) ===
    public static final Item TUOSSS_ROW = new ItemBase("tuosss_row");
    public static final Item TUOSSS_INGOT = new ItemBase("tuosss_ingot");

    // === COMPOSES ENERGETIQUES (A-E) ===
    public static final Item COMPOSE_A = new ItemCompose("compose_a", "A", 25);
    public static final Item COMPOSE_B = new ItemCompose("compose_b", "B", 75);
    public static final Item COMPOSE_C = new ItemCompose("compose_c", "C", 150);
    public static final Item COMPOSE_D_RAW = new ItemBase("compose_d_raw");
    public static final Item COMPOSE_D = new ItemCompose("compose_d", "D", 300);
    public static final Item COMPOSE_E = new ItemCompose("compose_e", "E", 500);

    // === COMPOSE COMPONENTS ===
    public static final Item COMPOSE_GEAR_A = new ItemBase("compose_gear_a");
    public static final Item COMPOSE_GEAR_B = new ItemBase("compose_gear_b");
    public static final Item COMPOSE_GEAR_C = new ItemBase("compose_gear_c");
    public static final Item COMPOSE_GEAR_D = new ItemBase("compose_gear_d");

    // === THE 9 FRAGMENTS ===
    public static final Item FRAGMENT_MECANIQUE = new ItemFragment(
        "fragment_mecanique",
        "L'Obeissance de la Matiere",
        "La matiere obeit. Donnez-lui les bonnes instructions.",
        TextFormatting.GOLD, EnumParticleTypes.FLAME
    );
    public static final Item FRAGMENT_ORGANIQUE = new ItemFragment(
        "fragment_organique",
        "La Persistance du Vivant",
        "La vie ne se contente pas d'exister. Elle insiste.",
        TextFormatting.GREEN, EnumParticleTypes.VILLAGER_HAPPY
    );
    public static final Item FRAGMENT_STELLAIRE = new ItemFragment(
        "fragment_stellaire",
        "La Memoire de l'Infini",
        "Les etoiles ne brillent pas. Elles se souviennent.",
        TextFormatting.BLUE, EnumParticleTypes.END_ROD
    );
    public static final Item COMPOSE_X77 = new ItemFragment(
        "compose_x77",
        "L'Etat Intermediaire",
        "77 tentatives. 76 explosions. Tu es pret pour la 77eme.",
        TextFormatting.RED, EnumParticleTypes.SMOKE_NORMAL
    );
    public static final Item COEUR_DE_DONNEES = new ItemFragment(
        "coeur_de_donnees",
        "La Conscience de la Matiere",
        "Ce programme ne resout pas un probleme. Ce programme EST le probleme.",
        TextFormatting.AQUA, EnumParticleTypes.ENCHANTMENT_TABLE
    );
    public static final Item NOYAU_FISSILE = new ItemFragment(
        "noyau_fissile",
        "La Destruction Creatrice",
        "Briser un atome, c'est tuer un monde. Parfois c'est necessaire.",
        TextFormatting.YELLOW, EnumParticleTypes.LAVA
    );
    public static final Item FRAGMENT_ESPACE_TEMPS = new ItemFragment(
        "fragment_espace_temps",
        "La Geometrie du Possible",
        "L'espace n'est pas une distance. L'espace est un choix.",
        TextFormatting.DARK_PURPLE, EnumParticleTypes.PORTAL
    );
    public static final Item CODEX_TRANSCENDANT = new ItemFragment(
        "codex_transcendant",
        "La Synthese des Contraires",
        "Un livre qui ne se lit pas. Un livre qui vous lit.",
        TextFormatting.DARK_RED, EnumParticleTypes.SPELL_WITCH
    );
    public static final Item PRISME_TRANSCENDANCE = new ItemFragment(
        "prisme_transcendance",
        "L'Instant de la Transformation",
        "La perfection n'est pas un etat. C'est un mouvement.",
        TextFormatting.WHITE, EnumParticleTypes.FIREWORKS_SPARK
    );

    // === PORTAIL VOSS - CLE DE LIBERTE (Age 1 -> Age 2) ===
    public static final Item CLE_LIBERTE = new ItemCleLiberte();
    public static final Item CLE_LIBERTE_ACTIVEE = new ItemCleLiberteActivee();

    // === AGE 2 INTRO (Sprint 1) ===
    // Narrative / progression items for the 9 rewritten intro quests.
    // Full GUI inventory for the Grabber is deferred to Sprint 1.5.
    public static final Item GRABBER_VOSS = new ItemGrabberVoss("grabber_voss");
    public static final Item BADGE_VOSS = new ItemBase("badge_voss");
    public static final Item LANTERNE_VOSS = new ItemBase("lanterne_voss");
    public static final Item FRAGMENT_MEMOIRE_1 = new ItemBase("fragment_memoire_1");

    // === THE NEXUS ABSOLU ===
    public static final Item NEXUS_ABSOLU = new ItemNexusAbsolu();

    // === FURNACES UPGRADES (Sprint B) ===
    public static final Item UPGRADE_RF_CONVERTER = new ItemFurnaceUpgrade(
        "upgrade_rf_converter",
        com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade.RF_CONVERTER);
    public static final Item UPGRADE_IO_EXPANSION = new ItemFurnaceUpgrade(
        "upgrade_io_expansion",
        com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade.IO_EXPANSION);
    public static final Item UPGRADE_SPEED_BOOSTER = new ItemFurnaceUpgrade(
        "upgrade_speed_booster",
        com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade.SPEED_BOOSTER);
    public static final Item UPGRADE_EFFICIENCY = new ItemFurnaceUpgrade(
        "upgrade_efficiency",
        com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade.EFFICIENCY);
}
