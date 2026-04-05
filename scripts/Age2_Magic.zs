// ============================================
// NEXUS ABSOLU -- Age2_Magic.zs
// Botania, Astral Sorcery, Blood Magic (salle 13x13)
// Les 3 magies doivent interagir pour produire les 3 Fragments
// ============================================

// === BOTANIA ===
// Mana Pool, Endoflame, etc. -- pas modifies
// Le joueur decouvre Botania naturellement dans la 13x13
// Manasteel necessite juste Iron + Mana Pool

// === BLOOD MAGIC ===
// Altar T1-T3 fitte dans un 13x13
// Blank Slate -> Reinforced Slate -> Imbued Slate progression normale

// === ASTRAL SORCERY ===
// Necessite vue du ciel -- config JED pour la dim compact machine
// Aquamarine, Starmetal, Celestial Crystal progression normale

// === MYSTICAL AGRICULTURE (debut) ===
// Inferium seeds tier 1-2 craftables
// Les graines font pousser des ressources -- tres utile en compact

// === EXTENDED CRAFTING TABLE ===
// La table tier 0 (3x3) est necessaire pour la Lab Key
// Gate : necessite des composants des 3 magies
recipes.remove(<extendedcrafting:table_basic>);
recipes.addShaped("nexus_ec_table_basic", <extendedcrafting:table_basic>,
    [[<ore:ingotIron>, <botania:manaresource:0>, <ore:ingotIron>],
     [<bloodmagic:slate:0>, <minecraft:crafting_table>, <astralsorcery:itemcraftingcomponent:0>],
     [<ore:ingotIron>, <ore:ingotIron>, <ore:ingotIron>]]);

// Black Iron Ingot -- coal au lieu de black dye
recipes.remove(<extendedcrafting:material:0>);
recipes.addShaped("nexus_black_iron", <extendedcrafting:material:0>,
    [[<minecraft:coal>, <minecraft:coal>, <minecraft:coal>],
     [<minecraft:coal>, <minecraft:iron_ingot>, <minecraft:coal>],
     [<minecraft:coal>, <minecraft:coal>, <minecraft:coal>]]);

// === TERRASTEEL (gate pour Fragment Organique) ===
// Vanilla Botania: Terrasteel = Iron + Mana Pearl + Mana Diamond sur Terrestrial plate
// Tres cher en mana -- le joueur doit avoir une bonne setup Botania

// === NETHER STAR (requis pour Lab Key et Fragment Stellaire) ===
// Normalement obtenu en tuant le Wither
// En compact machine, pas de Wither -- alternative :
// Soul Shards tier 5 Wither Skeleton + Kill 3 -> skulls -> Wither
// OU recette custom Nether Star (tres chere)
recipes.addShaped("nexus_nether_star_compact", <minecraft:nether_star>,
    [[<astralsorcery:itemcraftingcomponent:1>, <bloodmagic:slate:2>, <astralsorcery:itemcraftingcomponent:1>],
     [<bloodmagic:slate:2>, <botania:manaresource:5>, <bloodmagic:slate:2>],
     [<astralsorcery:itemcraftingcomponent:1>, <bloodmagic:slate:2>, <astralsorcery:itemcraftingcomponent:1>]]);
// Starmetal + Imbued Slate + Mana Diamond = Nether Star
// Force les 3 magies pour un seul item -- le joueur DOIT maitriser les 3

// === ENDERIUM (requis pour Lab Key) ===
// Enderium = Tin + Silver + Lead + Ender Pearls
// En compact, les Ender Pearls viennent des fragments (pioche diamant)
// Ou d'un Enderman Soul Shard

print("[Nexus Absolu] Age2_Magic.zs loaded");
