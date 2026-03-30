package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.items.ItemBase;
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
    public static final Item PIOCHE_FRAGMENTEE = new ItemPioche("pioche_fragmentee",
        Item.ToolMaterial.WOOD, 1);
    public static final Item PIOCHE_RENFORCEE = new ItemPioche("pioche_renforcee",
        Item.ToolMaterial.IRON, 2);

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

    // === FRAGMENTS (from scavenging) ===
    public static final Item COBBLESTONE_FRAGMENT = new ItemBase("cobblestone_fragment");
    public static final Item DIAMOND_FRAGMENT = new ItemBase("diamond_fragment");
    public static final Item EMERALD_FRAGMENT = new ItemBase("emerald_fragment");
    public static final Item ENDER_PEARL_FRAGMENT = new ItemBase("ender_pearl_fragment");
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
    public static final Item NEXIUM_INGOT = new ItemBase("nexium_ingot");
    public static final Item CLAUSTRITE_GEM = new ItemBase("claustrite_gem");
    public static final Item SUPER_FERTILIZER = new ItemBase("super_fertilizer");

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

    // === THE NEXUS ABSOLU ===
    public static final Item NEXUS_ABSOLU = new ItemNexusAbsolu();
}
