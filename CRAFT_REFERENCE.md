# CRAFT_REFERENCE.md — Systeme de Craft Nexus Absolu
> Base sur l'analyse de ~20K lignes de scripts SevTech Ages + Enigmatica 2 Expert.
> Ce document est LA reference pour creer des recettes. Le lire AVANT de coder.

---

## 1. ARCHITECTURE DES SCRIPTS

### Structure des fichiers
```
scripts/
  Globals.zs              #priority 3000 - variables globales, raccourcis metaux
  NexusCraft.zs           #priority 2000 - framework de craft (inspire E2E)
  Age0_Scavenging.zs      - recettes Age 0
  Age0_Keys.zs            - cles + catalyseurs
  Age0_Blocking.zs        - items bloques
  Age0_ExNihilo.zs        - Ex Nihilo overrides
  Age0_Food.zs            - nourriture
  Age1_Thermal.zs         - Thermal inter-mod
  Age1_EnderIO.zs         - EnderIO inter-mod
  Age1_IE.zs              - Immersive Engineering
  Age1_Invarium.zs        - crafts Invarium/Vossium
  Age2_Magic.zs           - Botania/Astral/Blood Magic
  Age3_Mekanism.zs        - Mekanism gating
  Age3_PneumaticCraft.zs  - PneumaticCraft gating
  contenttweaker/
    NexusItems.zs          #loader contenttweaker - items custom
```

### Ordre de chargement
```
#priority 4000  - ContentTweaker (items/blocs custom)
#priority 3000  - Globals.zs (variables, raccourcis)
#priority 2000  - NexusCraft.zs (framework)
#priority 1000  - Scripts de recettes (par defaut)
```

---

## 2. GLOBALS.ZS — Raccourcis centraux

### Pattern SevTech : metals.X.Y
SevTech utilise un systeme MaterialSystem de ContentTweaker.
Pour Nexus Absolu, on utilise des variables simples dans Globals.zs :

```zenscript
#priority 3000

// === METAUX THERMAL ===
global ironIngot     as IItemStack = <minecraft:iron_ingot>;
global goldIngot     as IItemStack = <minecraft:gold_ingot>;
global copperIngot   as IItemStack = <thermalfoundation:material:128>;
global tinIngot      as IItemStack = <thermalfoundation:material:129>;
global silverIngot   as IItemStack = <thermalfoundation:material:130>;
global leadIngot     as IItemStack = <thermalfoundation:material:131>;
global nickelIngot   as IItemStack = <thermalfoundation:material:133>;
global steelIngot    as IItemStack = <thermalfoundation:material:160>;
global electrumIngot as IItemStack = <thermalfoundation:material:161>;
global invarIngot    as IItemStack = <thermalfoundation:material:162>;
global bronzeIngot   as IItemStack = <thermalfoundation:material:163>;
global signalumIngot as IItemStack = <thermalfoundation:material:165>;
global enderiumIngot as IItemStack = <thermalfoundation:material:167>;

// === GEARS ===
global ironGear      as IItemStack = <thermalfoundation:material:24>;
global copperGear    as IItemStack = <thermalfoundation:material:256>;
global invarGear     as IItemStack = <thermalfoundation:material:290>;
global bronzeGear    as IItemStack = <thermalfoundation:material:291>;
global signalumGear  as IItemStack = <thermalfoundation:material:293>;

// === PLATES ===
global ironPlate     as IItemStack = <thermalfoundation:material:32>;
global copperPlate   as IItemStack = <thermalfoundation:material:320>;
global steelPlate    as IItemStack = <thermalfoundation:material:352>;

// === THERMAL FRAMES ===
global machineFrame  as IItemStack = <thermalexpansion:frame>;

// === ENDERIO ===
global electricalSteel  as IItemStack = <enderio:item_alloy_ingot:0>;
global darkSteel        as IItemStack = <enderio:item_alloy_ingot:6>;
global redstoneAlloy    as IItemStack = <enderio:item_alloy_ingot:3>;
global simpleChassis     as IItemStack = <enderio:item_material:0>;

// === IE ===
global treatedWood      as IItemStack = <immersiveengineering:treated_wood>;
global steelRod         as IItemStack = <immersiveengineering:material:2>;
global wireCoilCopper   as IItemStack = <immersiveengineering:wirecoil:0>;
global ieHeater         as IItemStack = <immersiveengineering:metal_device1:1>;

// === NEXUS ABSOLU CUSTOM ===
global wallDust       as IItemStack = <nexusabsolu:wall_dust>;
global ironGrit       as IItemStack = <nexusabsolu:iron_grit>;
global copperGrit     as IItemStack = <nexusabsolu:copper_grit>;
global tinGrit        as IItemStack = <nexusabsolu:tin_grit>;
global composeA       as IItemStack = <nexusabsolu:compose_a>;

// === CONTENTTWEAKER CUSTOM ===
global invariumIngot  as IItemStack = <contenttweaker:invarium_ingot>;
global vossiumIngot   as IItemStack = <contenttweaker:vossium_ingot>;
global vossiumII      as IItemStack = <contenttweaker:vossium_ii_ingot>;
```

---

## 3. NEXUSCRAFT.ZS — Framework de craft simplifie

### Inspire de E2E (Krutoy242) mais simplifie pour notre usage

```zenscript
#priority 2000

import crafttweaker.item.IItemStack;
import crafttweaker.item.IIngredient;

// ============================================================
//  FONCTION PRINCIPALE : nexus.remake()
//  Supprime l'ancienne recette + ajoute la nouvelle
//  Pattern E2E "remove-then-replace"
// ============================================================

// Shaped remake (le plus courant)
function remake(output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShaped(output, grid);
}

// Shaped remake avec nom custom
function remakeNamed(name as string, output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShaped(name, output, grid);
}

// Shapeless remake
function remakeShapeless(output as IItemStack, inputs as IIngredient[]) {
    recipes.remove(output);
    recipes.addShapeless(output, inputs);
}

// Mirrored remake (symetrique gauche-droite automatique)
function remakeMirrored(name as string, output as IItemStack, grid as IIngredient[][]) {
    recipes.remove(output);
    recipes.addShapedMirrored(name, output, grid);
}

// ============================================================
//  FONCTIONS UTILITAIRES
// ============================================================

// Supprimer une recette + la cacher de JEI
function purge(item as IItemStack) {
    mods.jei.JEI.removeAndHide(item);
}

// Supprimer uniquement la recette (garder visible dans JEI)
function removeOnly(item as IItemStack) {
    recipes.remove(item);
}

// Ajouter un tooltip en jeu + description JEI
function describe(item as IItemStack, text as string) {
    item.addTooltip(text);
    mods.jei.JEI.addDescription(item, text);
}

// Compact : 9 items -> 1 bloc et inverse
function compactRecipe(item as IIngredient, block as IItemStack) {
    recipes.addShapeless(block, [item,item,item,item,item,item,item,item,item]);
    recipes.addShapeless(item.items[0] * 9, [block]);
}
```

### Utilisation dans les scripts de recettes

```zenscript
// Au lieu de :
recipes.remove(<thermalexpansion:machine:1>);
recipes.addShaped("Pulverizer", <thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [<ore:ingotInvar>, machineFrame, <ore:ingotInvar>],
    [copperGear, wireCoilCopper, copperGear]
]);

// On ecrit :
remake(<thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, wireCoilCopper, copperGear]
]);
```

---

## 4. PATTERNS DE RECETTES — CE QU'ON A APPRIS

### 4a. Pattern SevTech : Static Recipe Maps
Utilise quand on a BEAUCOUP de recettes pour un mod.
```zenscript
static shapedRecipes as IIngredient[][][][IItemStack] = {
    <output:item> : [[
        [A, B, A],
        [C, D, C],
        [A, B, A]
    ]]
};

static removeRecipes as IItemStack[] = [
    <output:item>
];

// Dans init() :
for item, recipeLists in shapedRecipes {
    for recipeList in recipeLists {
        recipes.addShaped(item, recipeList);
    }
}
for item in removeRecipes { recipes.remove(item); }
```

### 4b. Pattern E2E : Pretty Grid (pour reference, pas implemente)
```zenscript
craft.remake(<minecraft:piston> * 2, ["pretty",
    "# # #",
    "@ I @",
    "@ R @"], {
    "@": <ore:cobblestone>,
    "#": <ore:plankWood>,
    "R": <ore:dustRedstone>,
    "I": <ore:plateIron>,
});
```
Elegant mais trop complexe pour notre taille (~200 recettes vs 2000+ pour E2E).

### 4c. Pattern Nexus Absolu : remake() direct
Notre approche simplifiee — le meilleur compromis lisibilite/simplicite :
```zenscript
// Pulverizer : force IE + Tinkers
remake(<thermalexpansion:machine:1>, [
    [null, <actuallyadditions:block_grinder>, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, wireCoilCopper, copperGear]
]);
```

---

## 5. REGLES DE DESIGN DES RECETTES

### Regle d'or : 2-3 mods par craft
Chaque recette importante DOIT forcer des composants de 2-3 mods differents.
Le joueur ne doit JAMAIS pouvoir progresser avec un seul mod.

### Position = Role (grille 3x3)
```
HAUT    = Fonction (ce que la machine FAIT)
CENTRE  = Coeur / frame / composant cle
BAS     = Base / puissance / alimentation
COTES   = Structure / enveloppe
COINS   = Renfort / materiau secondaire
```

### Symetrie obligatoire
Toujours symetrique gauche-droite. Utiliser addShapedMirrored si un seul cote suffit.

### Progression des materiaux par age
```
Age 0 : wall_dust, grits, cobble, bois, fer basique
Age 1 : bronze, invar, cuivre, Invarium, Vossium, acier
Age 2 : electrum, signalum, manasteel, starmetal, slates
Age 3 : enderium, osmium, dark steel, compressed iron
Age 4 : certus quartz, processeurs AE2, circuits Mekanism
Age 5 : uranium, thorium, draconium, nuclearcraft
```

### Anti-exploits (inspires de E2E/SevTech)
- Bloquer la fonte de redstone/glowstone dans la Smeltery (empeche raccourci EnderIO)
- Supprimer les outils vanilla (forcer Tinkers)
- Supprimer les recettes de base de chaque mod AVANT d'ajouter les notres
- Bloquer les items d'ages futurs via ItemStages/RecipeStages

---

## 6. API REFERENCE — RECETTES PAR MOD

### CraftTweaker (vanilla)
```zenscript
// Shaped
recipes.addShaped("nom", <output>, [[A,B,C],[D,E,F],[G,H,I]]);
recipes.addShapedMirrored("nom", <output>, [[A,B,C],[D,E,F]]);

// Shapeless
recipes.addShapeless("nom", <output>, [A, B, C]);

// Remove
recipes.remove(<item>);
recipes.removeByRecipeName("mod:recipe_name");

// Furnace
furnace.addRecipe(<output>, <input>);
furnace.remove(<output>);
```

### Thermal Expansion
```zenscript
// Pulverizer (broyage)
mods.thermalexpansion.Pulverizer.addRecipe(<output>, <input>, energy);
mods.thermalexpansion.Pulverizer.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Induction Smelter (alliages)
mods.thermalexpansion.InductionSmelter.addRecipe(<output>, <input1>, <input2>, energy);

// Sawmill
mods.thermalexpansion.Sawmill.addRecipe(<output>, <input>, energy);
mods.thermalexpansion.Sawmill.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Fluid Transposer (fill)
mods.thermalexpansion.Transposer.addFillRecipe(<output>, <input>, <liquid:nom> * amount, energy);

// Dynamos (fuel)
mods.thermalexpansion.SteamDynamo.addFuel(<item>, energy);
mods.thermalexpansion.CompressionDynamo.addFuel(<liquid:nom>, energy);
mods.thermalexpansion.NumisticDynamo.addGemFuel(<item>, energy);

// Crucible (item -> fluid)
mods.thermalexpansion.Crucible.addRecipe(<liquid:nom> * amount, <input>, energy);

// Centrifuge
mods.thermalexpansion.Centrifuge.addRecipe([<output> % chance], <input>, <liquid>, energy);
```

### EnderIO
```zenscript
// Alloy Smelter
mods.enderio.AlloySmelter.addRecipe(<output>, [<in1>, <in2>, <in3>], energy);

// SAG Mill
mods.enderio.SagMill.addRecipe([<out1> % chance, <out2> % chance], <input>, "NONE", energy);

// Soul Binder
mods.enderio.SoulBinder.addRecipe(<output>, <input>, ["minecraft:zombie"], xp, energy);
```

### Immersive Engineering
```zenscript
// Crusher (broyage)
mods.immersiveengineering.Crusher.addRecipe(<output>, <input>, energy);
mods.immersiveengineering.Crusher.addRecipe(<output>, <input>, energy, <secondary>, chance);

// Arc Furnace (alliages)
mods.immersiveengineering.ArcFurnace.addRecipe(<output>, <input>, <slag>, time, energy, [<additives>]);

// Metal Press
mods.immersiveengineering.MetalPress.addRecipe(<output>, <input>, <mold>, energy);

// Alloy Kiln
mods.immersiveengineering.AlloySmelter.addRecipe(<output>, <input1>, <input2>, time);

// Blueprint
mods.immersiveengineering.Blueprint.addRecipe("category", <output>, [<inputs>]);
```

### Tinkers Construct
```zenscript
// Smeltery alliage
mods.tconstruct.Alloy.addRecipe(<liquid:output> * amount, [<liquid:in1> * a, <liquid:in2> * b]);
mods.tconstruct.Alloy.removeRecipe(<liquid:output>);

// Melting (fondre un item)
mods.tconstruct.Melting.addRecipe(<liquid:output> * amount, <input>);
mods.tconstruct.Melting.removeRecipe(<liquid:output>, <input>);

// Casting (couler dans un moule)
mods.tconstruct.Casting.addTableRecipe(<output>, <cast>, <liquid:input>, amount, consumeCast, time);
mods.tconstruct.Casting.addBasinRecipe(<output>, <cast>, <liquid:input>, amount);
```

### Mekanism
```zenscript
// Metallurgic Infuser
mods.mekanism.infuser.addRecipe("TYPE", amount, <input>, <output>);
// Types: REDSTONE, DIAMOND, OBSIDIAN, TIN, CARBON, FUNGI, BIO

// Enrichment Chamber
mods.mekanism.enrichment.addRecipe(<input>, <output>);

// Crusher
mods.mekanism.crusher.addRecipe(<input>, <output>);

// Purification Chamber
mods.mekanism.purification.addRecipe(<input>, <gas:oxygen>, <output>);

// Chemical Injection Chamber
mods.mekanism.chemical.injection.addRecipe(<input>, <gas:type>, <output>);
```

### Botania
```zenscript
// Petal Apothecary
mods.botania.Apothecary.addRecipe(<output>, [<petals>]);

// Mana Infusion
mods.botania.ManaInfusion.addInfusion(<output>, <input>, mana);
mods.botania.ManaInfusion.addAlchemy(<output>, <input>, mana);

// Elven Trade
mods.botania.ElvenTrade.addRecipe([<outputs>], [<inputs>]);

// Runic Altar
mods.botania.RuneAltar.addRecipe(<output>, [<inputs>], mana);
```

### Astral Sorcery
```zenscript
// Starlight Crafting (Luminous Table)
mods.astralsorcery.Altar.addDiscoveryAltarRecipe("nom", <output>, starlightRequired, craftTickTime, [grid]);

// Constellation Altar
mods.astralsorcery.Altar.addConstellationAltarRecipe("nom", <output>, starlight, time, [grid]);

// Lightwell
mods.astralsorcery.Lightwell.addLiquefaction(<input>, <liquid:output>, multiplier, shatter);

// Grindstone
mods.astralsorcery.Grindstone.addRecipe(<input>, <output>);
```

### Blood Magic
```zenscript
// Blood Altar
mods.bloodmagic.BloodAltar.addRecipe(<output>, <input>, tier, lpDrained, consumeRate, drainRate);

// Alchemy Table
mods.bloodmagic.AlchemyTable.addRecipe(<output>, [<inputs>], lpDrained, time, tierRequired);

// Tartaric Forge
mods.bloodmagic.TartaricForge.addRecipe(<output>, [<inputs>], minimumSouls, soulDrain);
```

### Extended Crafting
```zenscript
// Table Crafting (3x3 a 9x9)
mods.extendedcrafting.TableCrafting.addShaped(<output>, [[grid 5x5 ou 7x7 ou 9x9]]);
mods.extendedcrafting.TableCrafting.addShapeless(<output>, [ingredients]);

// Combination Crafting (pedestal + RF)
mods.extendedcrafting.CombinationCrafting.addRecipe(<output>, rfCost, rfPerTick, <center>, [<pedestals>]);

// Ender Crafting
mods.extendedcrafting.EnderCrafting.addShaped(<output>, [[grid]], seconds);
```

### Ex Nihilo Creatio
```zenscript
// Sieve
mods.exnihilocreatio.Sieve.addStringMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addFlintMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addIronMeshRecipe(<input>, <output>, chance);
mods.exnihilocreatio.Sieve.addDiamondMeshRecipe(<input>, <output>, chance);

// Hammer
mods.exnihilocreatio.Hammer.addRecipe(<input>, <output>, chance, fortuneChance, [forLevels]);

// Compost
mods.exnihilocreatio.Compost.addRecipe(<input>, amount, <output>, color);

// Crucible (heat)
mods.exnihilocreatio.Crucible.addRecipe(<input>, <liquid:output> * amount);
```

### JEI
```zenscript
import mods.jei.JEI;

// Cacher un item de JEI
JEI.removeAndHide(<item>);

// Cacher sans supprimer la recette
JEI.hide(<item>);

// Ajouter une description
JEI.addDescription(<item>, "Texte explicatif");
JEI.addDescription(<item>, ["Ligne 1", "Ligne 2"]);
```

### GameStages / ItemStages / RecipeStages
```zenscript
// Bloquer un item tant que le stage n'est pas deblocke
mods.ItemStages.addItemStage("age_1", <item>);

// Bloquer une recette
mods.recipestages.Recipes.setRecipeStage("age_1", <output>);
mods.recipestages.Recipes.setRecipeStage("age_1", "recipe_name");

// Debloquer un stage pour un joueur (via commande ou advancement)
// /gamestage add @p age_1
```

---

## 7. TEMPLATES DE RECETTES PAR AGE

### Template Age 0 (Compact Machine 3x3)
```zenscript
// Pattern : items basiques, pas de RF, crafts manuels
remake(<nexusabsolu:condenseur>, [
    [ironGrit, wallDust, ironGrit],
    [wallDust, <minecraft:redstone>, wallDust],
    [ironGrit, wallDust, ironGrit]
]);
```

### Template Age 1 (Compact Machine 5x5-7x7, premier RF)
```zenscript
// Pattern : bronze/invar + composants IE/Thermal
remake(<thermalexpansion:machine:1>, [
    [null, wireCoilCopper, null],
    [invarIngot, machineFrame, invarIngot],
    [copperGear, ieHeater, copperGear]
]);
```

### Template Age 3+ (Overworld, inter-mod agressif)
```zenscript
// Pattern : 3+ mods forces, materiaux avances
remake(<mekanism:machineblock:0>, [
    [steelPlate, <mekanism:controlcircuit:0>, steelPlate],
    [<ore:circuitBasic>, <mekanism:basicblock:8>, <ore:circuitBasic>],
    [steelPlate, ieHeater, steelPlate]
]);
```

---

## 8. CHECKLIST AVANT DE CREER UNE RECETTE

- [ ] La recette force-t-elle 2-3 mods differents ?
- [ ] Est-elle symetrique gauche-droite ?
- [ ] Le centre est-il le composant cle ?
- [ ] Le haut represente-t-il la fonction ?
- [ ] L'ancienne recette vanilla est-elle supprimee ?
- [ ] Les materiaux correspondent-ils a l'age actuel ?
- [ ] Un tooltip/description JEI guide-t-il le joueur ?
- [ ] La recette ne cree-t-elle pas de raccourci (anti-exploit) ?

---

## 9. ANTI-EXPLOITS A IMPLEMENTER

### Inspires de E2E
```zenscript
// Bloquer fonte redstone/glowstone dans Smeltery (empeche alliages EnderIO gratis)
for item in <ore:dustRedstone>.items {
    mods.tconstruct.Melting.removeRecipe(<liquid:redstone>, item);
}
for item in <ore:blockRedstone>.items {
    mods.tconstruct.Melting.removeRecipe(<liquid:redstone>, item);
}

// Bloquer duplication bronze via Tinkers
mods.tconstruct.Melting.removeRecipe(<liquid:bronze>, <ic2:pipe>);
```

### Inspires de SevTech
```zenscript
// Supprimer les outils vanilla (forcer pioche custom / Tinkers)
purge(<minecraft:wooden_pickaxe>);
purge(<minecraft:stone_pickaxe>);
purge(<minecraft:iron_pickaxe>);
purge(<minecraft:diamond_pickaxe>);
purge(<minecraft:golden_pickaxe>);

// Bloquer acces Nether/End en Age 0
purge(<minecraft:flint_and_steel>);  // pas de portail Nether
```

---

## 10. TOOLTIPS PATTERN (inspire E2E)

```zenscript
// Tooltips informatifs pour guider le joueur
describe(machineFrame, "Base de toutes les machines Thermal. Necessite Invar + Verre.");
describe(<nexusabsolu:condenseur>, "Fusionne 2 Compact Machines en une plus grande. Necessite du RF.");
describe(invariumIngot, "Alliage du Dr. Voss. Bronze + Invar dans l'Alloy Kiln.");
describe(vossiumIngot, "Invarium perfectionne avec du Compose A. Evolue avec chaque Compose.");
describe(composeA, "Energie dimensionnelle cristallisee. Drop rare du scavenging.");

// Pattern E2E : descriptions JEI pour les items non-craftables
mods.jei.JEI.addDescription(<compactmachines3:wall>,
    "Ne peut pas etre mine normalement.",
    "Utilisez vos poings ou une Pioche Custom pour extraire des ressources.",
    "Pioche Fragmentee: wall_dust + fragments",
    "Pioche Renforcee: grits + compose");
```

---

## 11. PATTERNS AVANCES (Session 10+ — ~30 scripts analyses)

### 11a. Guard clause `#modloaded`
Chaque script doit commencer par un guard. Empeche les crashs si un mod est retire.
```zenscript
#modloaded enderio
// ... tout le script EnderIO ici
```
Ne PAS mettre sur Globals.zs ni NexusCraft.zs (ils sont universels).

### 11b. Factory functions + map iteration (AE2)
Quand on a N recettes qui suivent le meme pattern (ex: storage cells, tiers de machines) :
```zenscript
// Definir la fonction une fois
function newCellRecipe(input as IIngredient, output as IItemStack) {
    recipes.remove(output);
    recipes.addShaped(output.displayName, output,
        [[<ae2:quartz_glass>, <ore:dustRedstone>, <ae2:quartz_glass>],
         [<ore:dustRedstone>, input, <ore:dustRedstone>],
         [<ore:plateIron>, <ironchest:iron_chest>, <ore:plateIron>]]);
}

// Puis iterer sur une map
val cellRecipes = {
    <ae2:material:35>: <ae2:storage_cell_1k>,
    <ae2:material:36>: <ae2:storage_cell_4k>,
    <ae2:material:37>: <ae2:storage_cell_16k>,
} as IItemStack[IIngredient];
for input, output in cellRecipes { newCellRecipe(input, output); }
```
A utiliser pour : Composes A-E, tiers de Condenseur, storage cells AE2.

### 11c. Boucle sur metadata avec `.definition.makeStack(i)`
Pour les items dont seul le metadata change :
```zenscript
for i in 0 .. 4 {
    newRecipe(
        <mod:component>.definition.makeStack(i),
        <mod:output>.definition.makeStack(i)
    );
}
```

### 11d. Ingredient combinatoire OR `|`
Accepter plusieurs items equivalents dans un meme slot :
```zenscript
var casing = <teslacorelib:machine_case>
           | <actuallyadditions:block_misc:9>
           | <mekanism:basicblock:8>;
var constructionAlloyOrIron = <ore:ingotConstructionAlloy> | <ore:ingotIron>;
```
Utiliser quand plusieurs mods produisent des items conceptuellement identiques.

### 11e. OreDict tiered (Blood Magic orbs pattern)
Creer des ore dicts ou chaque tier inclut les tiers superieurs :
```zenscript
// orbTier1 accepte TOUS les orbs (weak, apprentice, magician, master, archmage)
// orbTier3 accepte seulement magician, master, archmage
<ore:orbTier1>.add(<bloodmagic:blood_orb>.withTag({orb: "bloodmagic:weak"}));
<ore:orbTier1>.add(<bloodmagic:blood_orb>.withTag({orb: "bloodmagic:apprentice"}));
// etc.
```
Pour Nexus Absolu : `<ore:composeMinA>` accepte A,B,C,D,E ; `<ore:composeMinC>` accepte C,D,E.

### 11f. `.anyDamage()` pour prerequis outils
Quand une recette requiert un outil deja utilise :
```zenscript
// Le joueur peut utiliser sa pioche Draconic meme usee
<draconicevolution:draconic_pick>.anyDamage()
// Aussi disponible : .onlyDamageAtLeast(0) pour forcer un outil endommage
```

### 11g. Variables raccourcis pour grilles 9x9
Indispensable pour la lisibilite des crafts Extended Crafting / Avaritia :
```zenscript
var ii = <avaritia:resource:6>;   // Infinity Ingot
var ni = <avaritia:resource:4>;   // Neutronium
var cm = <ore:ingotCrystalMatrix>;
var ic = <avaritia:resource:5>;   // Infinity Catalyst
// Puis la grille est lisible :
// [null, ii, ii, ni, null, ni, ii, ii, null],
```

### 11h. Cout exponentiel en boucle (Blood Tanks)
Calculer des couts qui escaladent de facon exponentielle :
```zenscript
for i in 1 to 16 {
    val tank = itemUtils.getItem("bloodmagic:blood_tank", i);
    val prevTank = itemUtils.getItem("bloodmagic:blood_tank", i - 1);
    val cost = (((pow(1.5d, i as double) * multiplier) as int) / 500) * 500;
    mods.bloodmagic.BloodAltar.addRecipe(tank, prevTank, tier, cost, 10+10*i, 10+10*i);
}
```
A utiliser pour : tiers de Condenseur, couts progressifs des upgrades.

### 11i. Tooltips colores avec `format.X()`
```zenscript
item.addTooltip(format.red("Mod desactive. Toutes les recettes sont supprimees."));
item.addTooltip(format.gold("Craft du Dr. Voss — necessite le Condenseur."));
item.addTooltip(format.aqua("Compose B : evolution du Compose A."));
// Couleurs : red, gold, aqua, green, gray, darkRed, darkAqua, etc.
```

### 11j. Desactivation complete d'un mod
Si un mod doit etre retire en cours de route :
```zenscript
recipes.removeByMod("bigreactors");
for item in loadedMods["bigreactors"].items {
    mods.jei.JEI.addDescription(item, "Mod desactive.");
    item.addTooltip(format.red("Mod desactive."));
    mods.jei.JEI.removeAndHide(item);
}
// Recettes de conversion des materiaux vers des equivalents
recipes.addShapeless("Conversion", <mod_cible:ingot>, [<mod_retire:ingot>]);
```

### 11k. Suppression massive par array
```zenscript
val removals = [
    <iceandfire:sapphire_ore>, <iceandfire:silver_ore>,
    <iceandfire:silver_pickaxe>, <iceandfire:silver_sword>,
    // ... tous les items a retirer
] as IItemStack[];
for item in removals { mods.jei.JEI.removeAndHide(item); }
```
Utiliser pour : outils I&F redondants, items de mods qu'on ne veut pas dans certains ages.

### 11l. Conversions cross-mod shapeless
Quand deux mods ont des items identiques :
```zenscript
// Unidirectionnel (I&F dragon heart → DE dragon heart)
recipes.addShapeless("Dragon Heart", <draconicevolution:dragon_heart>, [<iceandfire:fire_dragon_heart>]);
// Bidirectionnel (witherbone I&F ↔ witherbone Tinkers)
recipes.addShapeless("Witherbone1", <tconstruct:materials:17>, [<iceandfire:witherbone>]);
recipes.addShapeless("Witherbone2", <iceandfire:witherbone>, [<tconstruct:materials:17>]);
```

### 11m. OreDict custom pour categories
Regrouper des variantes sous un meme oreDict :
```zenscript
val dragonArmor = [<iceandfire:armor_red_helmet>, ...] as IItemStack[];
for armor in dragonArmor { <ore:armorDragon>.add(armor); }
// Ensuite utilisable dans les recettes : <ore:armorDragon>
```

### 11n. Gears via machines uniquement (pas de craft table)
```zenscript
// Supprimer le craft table, forcer Metal Press IE ou Compactor Thermal
recipes.remove(<enderio:item_material:12>);
mods.immersiveengineering.MetalPress.addRecipe(<gear>, <ingot>, <ie:mold:1>, 16000, 4);
mods.thermalexpansion.Compactor.addGearRecipe(<gear>, <ingot> * 4, 16000);
```

### 11o. Upgrade path Simple → Normal
Au lieu de re-crafter une machine, upgrader l'existante :
```zenscript
recipes.addShapedMirrored("Upgrade Furnace",
<enderio:block_alloy_smelter>,
[[null, <enderio:block_simple_furnace>, null],
 [null, <enderio:item_material:1>, null],
 [null, <enderio:item_material:73>, null]]);
```

### 11p. Conditional recipes (config-aware)
Adapter les recettes selon la config du modpack :
```zenscript
var controller = itemUtils.getItem("appliedenergistics2:controller");
if (isNull(controller)) {
    // Mode sans channels — recette alternative
} else {
    // Mode avec channels — recette normale
}
```

### 11q. Avaritia → Extended Crafting migration
Pour les crafts 9x9, migrer d'Avaritia vers Extended Crafting :
```zenscript
mods.avaritia.ExtremeCrafting.remove(<avaritia:resource:6>);
mods.extendedcrafting.TableCrafting.addShaped(0, <avaritia:resource:6>, [
    // 0 = tier auto-detect, grille 9x9
    [ni, ni, ni, ni, ni, ni, ni, ni, ni],
    // ...
]);
```

### 11r. CombinationCrafting multi-pedestal (endgame)
Pour les crafts qui necessitent un core central + pedestaux + beaucoup de RF :
```zenscript
mods.extendedcrafting.CombinationCrafting.addRecipe(
    <output>,
    1000000000,  // RF total
    1000000,     // RF/tick
    <center_item>,
    [<pedestal1>, <pedestal2>, ... <pedestal20>]
);
```
Utilise pour : Fusion Crafting Core DE, Extreme Crafting Table, items endgame Age 8-9.

### 11s. Tiered recipes en Extended Crafting (5x5 → 7x7 → 9x9)
Les tiers de machines endgame utilisent des tables de taille croissante :
```
Age 3-4 : Extended Table 3x3 (basique)
Age 5-6 : Extended Table 5x5 (elite)
Age 7-8 : Extended Table 7x7 (ultimate)
Age 9   : Extended Table 9x9 (Nexus Absolu)
```
Chaque tier contient le tier precedent au centre de la grille.

---

## 12. API REFERENCE — MODS SUPPLEMENTAIRES

### Avaritia / Extreme Crafting
```zenscript
// Supprimer un craft Avaritia
mods.avaritia.ExtremeCrafting.remove(<item>);

// Ajouter un craft 9x9 (via Extended Crafting)
mods.extendedcrafting.TableCrafting.addShaped(0, <o>, [[9x9 grid]]);
mods.extendedcrafting.TableCrafting.addShapeless(0, <o>, [ingredients]);
```

### Draconic Evolution
```zenscript
// Fusion Crafting (via CombinationCrafting d'Extended Crafting)
mods.extendedcrafting.CombinationCrafting.addRecipe(<o>, rfTotal, rfPerTick, <center>, [<pedestals>]);

// Transposer Thermal pour Energy Cores
mods.thermalexpansion.Transposer.addFillRecipe(<o>, <input>, <liquid:redstone> * amount, energy);
```

### Forestry Carpenter
```zenscript
// Craft avec fluide (utilise par DE pour Wyvern/Draconic cores)
mods.forestry.Carpenter.addRecipe(<o>, [[grid]], time, <liquid:mana> * amount);
```

### Ice and Fire
```zenscript
// Dragon Forge (fire ou ice)
mods.iceandfire.recipes.addFireDragonForgeRecipe(<input1>, <input2>, <output>);
mods.iceandfire.recipes.addIceDragonForgeRecipe(<input1>, <input2>, <output>);
```

### Botania Orechid
```zenscript
// Ajouter un minerai au pool Orechid (poids relatif)
mods.botania.Orechid.addOre("oreThorium", 1285);
mods.botania.Orechid.addOre("oreBoron", 1285);

// Pure Daisy custom (forcer inter-mod magie)
mods.botania.PureDaisy.addRecipe(<thaumcraft:stone_arcane>, <botania:livingrock>);
mods.botania.PureDaisy.removeRecipe(<botania:livingrock>);
```

### NuclearCraft
```zenscript
// Infuser (item + fluide → item)
mods.nuclearcraft.infuser.addRecipe(<input>, <liquid:neutron> * 1000, <o>, time);

// Alloy Furnace
mods.nuclearcraft.alloy_furnace.addRecipe(<in1>, <in2>, <o>);

// Decay Hastener (recyclage)
mods.nuclearcraft.decay_hastener.addRecipe([<input>, <o>]);
```

---

## 13. PATTERNS DE QUETES (analyse E2E Better Questing)

### Architecture E2E vs Nexus Absolu
E2E : 21 chapitres par MOD (700 quetes), chapitre "Gates" comme hub central.
Nexus Absolu : chapitres NARRATIFS par age, pas par mod. Notre choix est meilleur pour un modpack narratif.

### Types de recompenses a utiliser
```
Item rewards     — Le plus courant, donner des items concrets
Choice rewards   — Le joueur choisit 1 parmi 2-3 options (transitions d'age)
Command rewards  — /say pour broadcasts serveur sur les milestones
XP rewards       — Points d'experience vanilla (rare, pour les quetes bonus)
Loot chests      — Recompenses aleatoires par tier (Common → Legendary)
```

### Pattern "Gates" adapte pour Nexus Absolu
Au lieu de gates par mod, des quetes milestone a chaque transition d'age :
```
Age 0 → Age 1 : milestone "Condenseur active" → reward starter Age 1
Age 1 → Age 2 : milestone "CM 7x7 construite" → reward starter Age 2
Age 2 → Age 3 : milestone "Sortie overworld" → Choice reward (path tech ou magie)
```

### Pattern "Loot Chests du Dr. Voss"
5 tiers de loot thematiques par age :
```
Commun    (poids 12) : materiaux basiques, blocs deco
Peu commun (poids 10) : composants mid-tier, outils
Rare      (poids 7)  : composants avances, machines
Epique    (poids 3)  : items uniques, upgrades
Legendaire (poids 2)  : fragments de lore, items endgame
```

### Tasks : rester simple
Seulement 2 types : Retrieval (montre l'item) et Checkbox (tutoriel). Pas de kill quests ni de quetes de localisation.

---

## 14. CONFIGS CRITIQUES PAR MOD

### Astral Sorcery — Tables de minerais
Chaque systeme de generation d'ore a sa propre config dans `config/astralsorcery/` :
```
mineralis_ritual.cfg      — Rituel qui spawn des minerais
treasure_shrine.cfg       — Loot des sanctuaires
aevitas_ore_perk.cfg      — Perk Aevitas (ore gen)
perk_void_trash_replacement.cfg — Remplacement des ores "trash"
fluid_rarities.cfg        — Fluides de la fontaine Evershifting
```
Adapter pour Nexus Absolu :
- Ajouter oreBoron, oreLithium, oreMagnesium, oreThorium (NuclearCraft)
- Retirer oreYellorite, oreRuby, oreSapphire si mods non utilises
- Reduire coal/iron dans treasure_shrine (pas de spam basique dans du loot rare)
- Ajouter liquid mana dans fluid_rarities (cross-mod Astral ↔ Botania)

### Botania — Worldgen fleurs
```
worldgen.flower.patchChance=12   (defaut 16, plus de patches)
worldgen.flower.quantity=3       (defaut 2)
worldgen.flower.tallChance=0.1   (defaut 0.05, double les grandes fleurs)
worldgen.mushroom.quantity=20    (defaut 40, moins de champignons)
harvestLevel.boreLens=3          (garder a 3 pour forcer pioches custom)
```

### Livres de spawn desactives (deja dans SKILL.md)
```
Astral Sorcery: giveJournalAtFirstJoin=false
OpenComputers: giveManualToNewPlayers=false
Tinkers: spawnWithBook=false
The One Probe: spawnNote=false
Guide-API: canSpawnWithBooks=false
```

---

*Derniere mise a jour : Session 10 — Analyse complete ~30 fichiers (scripts .zs, configs .cfg, quetes JSON)*
*Sources : SevTech Ages, Enigmatica 2 Expert, Compact Claustrophobia*
