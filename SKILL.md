# SKILL.md — Nexus Absolu Modpack
> Référence complète pour continuer le développement du modpack sans relire toute la conversation.

---

## 1. CONTEXTE DU PROJET

### Identité
- **Nom** : Nexus Absolu
- **Version Minecraft** : 1.12.2 Forge 14.23.5.2860
- **GitHub** : https://github.com/lamardalexis-sys/nexus-absolu-modpack
- **Instance CurseForge** : `C:\Users\lamar\curseforge\minecraft\Instances\Nexus Absolu`
- **Token GitHub** : stocker localement, ne jamais hardcoder ici

### Concept narratif
Un scientifique disparu (Dr. E. Voss) a passé 30 ans à repousser les limites de la physique. Il a réussi mais a disparu. Le joueur se réveille dans une Compact Machine et doit suivre ses traces jusqu'au **Nexus Absolu** — un item qui transcende la matière.

### Philosophie de gameplay
- Inspiré d'**Enigmatica 2 Expert** (recettes inter-mods forcées) et **Compact Claustrophobia** (débuter dans une boîte)
- Les 3 premiers âges se jouent dans des **Compact Machines** de taille croissante
- **Chaque mod doit interagir avec au moins 2 autres** via les crafts custom
- Pas de Thaumcraft (retiré — trop complexe à automatiser pour le Nexus Absolu)
- Ton : mélange sérieux/épique + humour + mystérieux

---

## 2. STRUCTURE DES 10 ÂGES

### Âges 0-1-2 : La Captivité (Compact Machines)

```
ÂGE 0 — L'Éveil (salle 3x3x3 → 5x5 → 7x7 → 9x9)
  DÉPART : Compact Machine 3x3x3 avec coffre contenant :
    - 1x Livre de Lore du Dr. Voss (Patchouli)
    - 1x Tamis Ex Nihilo en bois
    - 4x Graines variées (blé, carotte, pomme de terre, betterave)
    - 8x Pain (nourriture basique)
  Étape 1 : Ex Nihilo — cogner les murs → Compact Machine Dust → tamis → ressources
  Étape 2 : Bonsai Trees + Pam's HarvestCraft + Fonderie Tinkers
  Étape 3 : Intro Blood Magic Phase 1 (Thaumcraft retiré)
  CROP DUSTING : mobs → poop → accélère cultures Pam's + Bonsai → nourriture variée → cœurs
  CRAFT CUSTOM POSSIBLE : poop = composant engrais Âge 0
  MODS : Ex Nihilo Creatio, Tinkers Construct, Bonsai Trees, Pam's HarvestCraft,
         Spice of Life Carrot Edition, Crop Dusting, Soul Shards Respawn

ÂGE 1 — La Mécanique Brute (salle 9x9 → 11x11 → 13x13)
  Étape 4 : Thermal Expansion — première dynamo, 1M RF
  Étape 5 : Chaîne purification + EnderIO conduits
  Étape 6 : Immersive Engineering + hybridation Tech/Magie (cristaux Astral sur machines Thermal)
  MODS : Thermal Expansion/Foundation/Dynamics, IE, Actually Additions, EnderIO

ÂGE 2 — Le Paradoxe Organique (salle 13x13 → 17x17)
  Étape 7 : Botania — Mana Pool, Mystical Agriculture tier 1-2
  Étape 8 : Astral Sorcery — télescope, cristaux améliorent machines Thermal
  Étape 9 : Blood Magic Tier 1 — la Trinité des 3 Fragments
  Étape 10 : Clé du Laboratoire → SORTIE vers le monde ouvert
  MODS : Botania, Astral Sorcery, Blood Magic, Roots 2, Mystical Agriculture
```

### Âges 3-9 : Le Monde Ouvert

| Âge | Nom | Mods clés | Quête finale |
|-----|-----|-----------|--------------|
| 3 | La Chimie du Chaos | Mekanism, PneumaticCraft, EnderIO | Composé X-77 |
| 4 | L'Intelligence des Réseaux | AE2 complet, OpenComputers, XNet | Cœur de Données |
| 5 | La Fission de l'Impossible | NuclearCraft, Env. Tech, Solar Flux | Noyau Fissile Stabilisé |
| 6 | La Conquête du Vide | Galacticraft, Advanced Rocketry, RFTools Dim | Fragment Espace-Temps |
| 7 | La Synthèse des Mondes | Blood Magic avancé, Draconic, Twilight Forest | Codex Transcendant |
| 8 | L'Endgame des Dieux | Draconic max, Avaritia, Extended Crafting | Prisme de Transcendance |
| 9 | Le Nexus Absolu | Extended Crafting 9x9, tous composants | LE NEXUS ABSOLU |

### Items de progression custom (ContentTweaker)
Ces 9 items sont à créer et entrent dans la recette finale :
1. `fragment_mecanique` — Âge 1 (Thermal + IE)
2. `fragment_organique` — Âge 2 (Botania + Blood Magic)
3. `fragment_stellaire` — Âge 2 (Astral Sorcery)
4. `compose_x77` — Âge 3 (Mekanism gaz + Thermal + Astral cristaux)
5. `coeur_de_donnees` — Âge 4 (AE2 multibloc Modular Machinery)
6. `noyau_fissile` — Âge 5 (NuclearCraft isotopes)
7. `fragment_espace_temps` — Âge 6 (Galacticraft station orbitale)
8. `prisme_transcendance` — Âge 8 (Draconic + Avaritia)
9. `nexus_absolu` — Âge 9 (table 9x9, tous composants)

### Progression du vol
```
Âge 1 → Simply Jetpacks 2 (tier 1-4, RF)
Âge 2 → Angel Ring to Bauble (vol créatif Baubles)
Âge 7+ → Armure Draconic Evolution (vol + invincibilité)
CRAFT CUSTOM : Angel Ring nécessite composant de jetpack tier 4
CRAFT CUSTOM : Armure Draconic nécessite Angel Ring comme composant
```

---

## 3. LISTE DES MODS (version actuelle)

### Mods principaux installés
```
!mixinbooter-10.7.jar
AE2WTLib-1.12.2-1.0.34.jar
AEAdditions-1.12.2-1.3.8.jar
ActuallyAdditions-1.12.2-r152.jar
AdvancedRocketry-1.12.2-2.0.0-17.jar
Avaritia-1.12.2-3.3.0.37-universal.jar
Baubles-1.12-1.5.2.jar
BetterQuesting-3.5.329.jar
BiomesOPlenty-1.12.2-7.0.1.2445-universal.jar
BloodMagic-1.12.2-2.4.3-105.jar
Botania r1.10-364.4.jar
BrandonsCore-1.12.2-2.4.20.162-universal.jar
CoFHCore + CoFHWorld + ThermalExpansion + ThermalFoundation + ThermalDynamics
CodeChickenLib-1.12.2-3.2.3.358-universal.jar
ContentTweaker-1.12.2-4.10.0.jar
CraftTweaker2-1.12-4.1.20.711.jar
Draconic-Evolution-1.12.2-2.3.28.354-universal.jar
EnderCore + EnderIO-base + EnderIO-conduits + EnderIO-endergy
EnvironmentalTech + EnvironmentalMaterials + ETLunar + ValkyrieLib
ExtendedCrafting-1.12.2-1.5.6.jar
ExtraPlanets-1.12.2-0.8.0.jar
ExtremeReactors-1.12.2-0.4.5.68.jar + ZeroCore
FTBBackups + FTBLib + FTBQuests + FTBUtilities
FluxNetworks-1.12.2-4.1.1.34.jar
Galacticraft-1.12.2-4.0.7.jar
ImmersiveEngineering-0.12-98.jar
JAOPCA-1.12.2-2.3.14.38.jar
JEI + JER + JustEnoughEnergistics + JustEnoughPetroleum + JustEnoughReactors
JourneyMap-1.12.2-5.7.1p3.jar
KubeJS-forge-1.12.2-1.1.0.65.jar
LibVulpes + AdvancedRocketry
Mantle + TConstruct + TinkersComplement + TinkerToolLeveling + ConArm
Mekanism + MekanismGenerators + MekanismTools
ModularMachinery-1.12.2-1.11.1.jar
ModTweaker + MTLib
MysticalAgriculture + MysticalAgradditions + Cucumber
NuclearCraft-1.12.2-2o.9.2.jar
OpenBlocks + OpenModsLib
OpenComputers-MC1.12.2-1.8.9a.jar
Pam's HarvestCraft 1.12.2zg.jar
Patchouli-1.0-23.6.jar
PneumaticCraft-Repressurized
Quark + QuarkOddities + AutoRegLib
RFTools + RFToolsCtrl + RFToolsDim + RFToolsPower + McJtyLib
RecurrentComplex-1.4.8.6.jar
SimplyJetpacks2-1.12.2-2.2.20.0.jar
SolarFluxReborn-1.12.2-12.4.11.jar
SoulShardsRespawn-1.12.2-1.1.1-13.jar
StorageDrawers-1.12.2-5.5.3.jar
TwilightForest-1.12.2-3.11.1021-universal.jar
UniDict-1.12.2-3.0.10.jar
Woot-1.12.2-1.4.11.jar
XNet-1.12-1.8.2.jar
ae2-uel-v0.56.6.jar (AE2 Unofficial Extended Life)
ae2stuff-1.12.2-0.10.jar
angelRingToBauble-1.12-0.3.1.50.jar
astralsorcery-1.12.2-1.10.27.jar
bonsaitrees-1.1.4-b170.jar
compactmachines3-1.12.2-3.0.18-b278.jar
exnihilocreatio-1.12.2-0.4.7.2.jar
iceandfire-1.9.1-1.12.2.jar
incontrol-1.12-3.9.18.jar
industrialforegoing-1.12.2-1.12.13-237.jar
letsencryptcraft → À SUPPRIMER (inutile)
tombstone-1.12.2-4.7.5.jar
twilightforest-1.12.2-3.11.1021-universal.jar
xnet-1.12-1.8.2.jar
```

### Mods supprimés (conflits)
- GalaxySpace (conflit ExtraPlanets)
- TickCentral (conflit CoFHWorld)
- Phosphor (conflit CoFHWorld)
- VanillaFix (conflit MixinBooter)
- MekanismAddUpgradeSlots (incompatible Mekanism 9.8.3)
- MekanismEcoEnergistics (incompatible Mekanism 9.8.3)
- LagGoggles (requiert TickCentral)
- AE2CT-Legacy (jar corrompu Java 9+)
- Corpse (conflit Corail Tombstone)
- Thaumcraft + tous ses addons (retiré volontairement — trop complexe pour automatiser Nexus Absolu)

---

## 4. CONFIGS MODIFIÉES

### AE2 — `config/AppliedEnergistics2/AppliedEnergistics2.cfg`
```
I:normalChannelCapacity=32   (était 8, x4)
I:denseChannelCapacity=128   (était 32, x4)
```

### UniDict — `config/unidict/UniDict.cfg`
```
B:keepOneEntry=true          (unifie tous les minerais doublons)
ownerOfEveryThing: minecraft → thermalfoundation → mekanism → immersiveengineering → actuallyadditions → nuclearcraft → tconstruct
```
Métaux ajoutés : Enderium, Signalum, Lumium, Draconium, Yellorium, Boron, Lithium, Magnesium, Thorium, Plutonium, Cobalt, Ardite, Manasteel, Terrasteel, Elementium

### Astral Sorcery — `config/astralsorcery.cfg`
Fix clignotement du ciel :
```
S:skySupportedDimensions < >       (vider)
S:weakSkyRenders < 0 >             (ajouter 0)
```

### Forge — `config/forge.cfg`
```
B:disableVersionCheck=true
B:Global=false  (dans version_checking)
```

### Surge — `config/surge.cfg`
```
B:disableAnimatedModels=true       (gros gain démarrage)
```

### BetterFps — `config/betterfps.json`
```json
{"algorithm":"rivens-half","updateChecker":false,"preallocateMemory":true,...}
```

### Spice of Life Carrot Edition — config à faire
```
maxHearts=50
```

---

## 5. SCRIPTS CRAFTTWEAKER — SYNTAXE DE BASE

Le dossier scripts est dans `scripts/` à la racine de l'instance.
Structure apprise depuis Enigmatica 2 Expert (repo cloné dans /home/claude/e2e/).

### Imports obligatoires (Globals.zs)
```zenscript
import crafttweaker.item.IIngredient;
import crafttweaker.item.IItemStack;
import crafttweaker.oredict.IOreDict;
import crafttweaker.liquid.ILiquidStack;
import mods.jei.JEI.removeAndHide as rh;
```

### Fonctions utilitaires (à mettre dans Globals.zs)
```zenscript
global remake as function(string, IItemStack, IIngredient[][])void =
    function (name as string, item as IItemStack, input as IIngredient[][]) as void {
        recipes.remove(item);
        recipes.addShaped(name, item, input);
};

global remakeEx as function(IItemStack, IIngredient[][])void =
    function (item as IItemStack, input as IIngredient[][]) as void {
        recipes.remove(item);
        recipes.addShaped(item.definition.id.replaceAll(":", "_") ~ "_" ~ item.damage, item, input);
};
```

### Craft shapé standard
```zenscript
recipes.remove(<mod:item>);
recipes.addShaped("NomUnique", <mod:output>,
  [[<mod:item1>, <ore:ingotCopper>, <mod:item3>],
   [<minecraft:diamond>, <mod:item5>, <minecraft:diamond>],
   [<mod:item7>, <ore:ingotIron>, <mod:item9>]]);
```

### Craft shapeless
```zenscript
recipes.addShapeless("NomUnique", <mod:output>, [<mod:item1>, <ore:ingotIron>]);
```

### Machines Thermal Expansion
```zenscript
// Pulverizer
mods.thermalexpansion.Pulverizer.addRecipe(<output>, <input>, 4000);
// Induction Smelter
mods.thermalexpansion.InductionSmelter.addRecipe(<output>, <input1>, <input2>, 4000);
// Redstone Furnace
mods.thermalexpansion.RedstoneHeater.addRecipe(<output>, <input>, 4000);
```

### Machines Mekanism
```zenscript
mods.mekanism.crusher.addRecipe(<output>, <input>);
mods.mekanism.enrichment.addRecipe(<output>, <input>);
mods.mekanism.purification.addRecipe(<output>, <input>, <gas:oxygen>);
mods.mekanism.dissolution.addRecipe(<output_gas>, <input>, <gas:sulfuricacid>);
mods.mekanism.crystallization.addRecipe(<output>, <gas:input_gas>);
mods.mekanism.injection.addRecipe(<output>, <input>, <gas:hydrogen>);
```

### AE2 — Inscriber
```zenscript
mods.appliedenergistics2.Inscriber.addRecipe(<output>, <top>, <middle>, <bottom>, false);
```

### Extended Crafting — Tables
```zenscript
// Table 3x3 (tier 0)
mods.extendedcrafting.TableCrafting.addShaped(0, <output>, [[...],[...],[...]]);
// Table 5x5 (tier 1)
mods.extendedcrafting.TableCrafting.addShaped(1, <output>, [[...x5],[...x5],[...x5],[...x5],[...x5]]);
// Table 7x7 (tier 2)
mods.extendedcrafting.TableCrafting.addShaped(2, <output>, [...7x7...]);
// Table 9x9 (tier 3) — utilisée pour le Nexus Absolu
mods.extendedcrafting.TableCrafting.addShaped(3, <output>, [...9x9...]);
```

### Modular Machinery — JSON dans config/modularmachinery/
```json
{
  "registryname": "nexus:coeur_de_donnees",
  "localizedname": "Cœur de Données",
  "iodynamics": [...],
  "recipeList": [...]
}
```

### Blood Magic — Altar
```zenscript
mods.bloodmagic.AlchemyTable.addRecipe(<output>, [<input1>, <input2>], [1,1], 1000, 50);
mods.bloodmagic.Altar.addRecipe(<output>, <input>, 4, 10000, 100, 10);
// Tier 1-6, LP requis, drain/tick, soak time
```

### Botania — Mana Infusion
```zenscript
mods.botania.ManaInfusion.addRecipe(<output>, <input>, 5000);
mods.botania.ManaInfusion.addRecipe(<output>, <input>, 5000, <botania:manaresource:4>); // avec catalyseur
```

### Astral Sorcery — Starlight Infusion
Pas de ModTweaker pour AS — se fait via ContentTweaker ou JSON dans les configs AS.

### Cacher des items dans JEI
```zenscript
import mods.jei.JEI.removeAndHide as rh;
rh(<mod:item>);
```

---

## 6. CONTENTTWEAKER — ITEMS CUSTOM

### Structure des fichiers
```
scripts/
  contenttweaker/
    NexusItems.zs      ← items custom
    NexusBlocks.zs     ← blocs custom si nécessaire
```

### Créer un item custom
```zenscript
#loader contenttweaker

import mods.contenttweaker.VanillaFactory;
import mods.contenttweaker.Item;

// Créer l'onglet créatif
mods.contenttweaker.VanillaFactory.createCreativeTab("nexus", <item:minecraft:nether_star>).register();

// Créer un item
function buildItem(name as string) {
    val item = mods.contenttweaker.VanillaFactory.createItem(name);
    item.setCreativeTab(<creativetab:nexus>);
    item.register();
}

buildItem("fragment_mecanique");
buildItem("fragment_organique");
buildItem("fragment_stellaire");
buildItem("compose_x77");
buildItem("coeur_de_donnees");
buildItem("noyau_fissile");
buildItem("fragment_espace_temps");
buildItem("prisme_transcendance");
buildItem("nexus_absolu");
```

### Référencer un item custom dans CraftTweaker
```zenscript
var fragmentMecanique = <contenttweaker:fragment_mecanique>;
var nexusAbsolu = <contenttweaker:nexus_absolu>;
```

### Textures
Placer dans : `resources/assets/contenttweaker/textures/items/nom_item.png`
Format : PNG 16x16 ou 32x32

---

## 7. RECETTE DU NEXUS ABSOLU (table 9x9)

```zenscript
mods.extendedcrafting.TableCrafting.addShaped(3,
    <contenttweaker:nexus_absolu>,
    [
        [fragmentOrg, fragmentMec, fragmentStel, composeX77, coeurDonnees, noyauFissile, fragmentET, codexTrans, prismeTransc],
        [<draconicevolution:draconium_ingot>, <extendedcrafting:material:13>, <ore:ingotEnderium>, <ore:blockVoidMetal>, <minecraft:nether_star>, <contenttweaker:nexus_absolu>, <minecraft:nether_star>, <ore:blockVoidMetal>, <ore:ingotEnderium>],
        [prismeTransc, codexTrans, fragmentET, noyauFissile, coeurDonnees, composeX77, fragmentStel, fragmentMec, fragmentOrg],
        [...],
        [...],
        [...],
        [...],
        [...],
        [...]
    ]
);
```
⚠️ La recette 9x9 complète est à finaliser quand tous les items custom existent.

---

## 8. PHILOSOPHIE DES RECETTES CUSTOM

### Règle d'or
**Chaque mod principal ne doit pas se suffire à lui-même.** Les recettes importantes nécessitent des composants de 2-3 mods différents.

### Exemples de liens forcés
- **Lingot de fer purifié** = minerai broyé (Mekanism) + bain chimique (Thermal) + coulée (IE) → x4 lingots
- **Panneau Solar Flux tier 5+** = nécessite isotopes NuclearCraft → force NuclearCraft avant solaire passif
- **Angel Ring** = nécessite composant jetpack tier 4 Simply Jetpacks → force progression vol
- **Armure Draconic tier 1** = nécessite isotopes NuclearCraft + Angel Ring
- **Solar Flux tier 5-8** = nécessite isotopes NuclearCraft (empêche de court-circuiter le nucléaire)
- **Advanced Rocketry starter** = nécessite composant Galacticraft → force Galacticraft avant AR

### Liens Tech/Magie forcés
- Âge 2 : cristaux Astral Sorcery améliorent les machines Thermal (+20-30% vitesse)
- Âge 2 : Mana Botania + LP Blood Magic + Starlight AS = 3 Fragments de la Clé
- Âge 3 : Composé X-77 = gaz Mekanism + cristaux Astral + Mana Botania
- Âge 7 : Codex Transcendant = Blood Magic Tier 6 + Mana Tablet + composant Draconic

---

## 9. QUÊTES — BETTER QUESTING

### Fichiers de quêtes
Emplacement : `config/betterquesting/`
Format : JSON

### Structure d'une quête
```json
{
  "questID": 1,
  "properties": {
    "betterquesting": {
      "name": "Premier Contact",
      "desc": "Les murs te regardent. Cogne-les.",
      "icon": {"id": "exnihilocreatio:dust_compressed_cobblestone", "Count": 1, "Damage": 0},
      "tasklogic": "AND",
      "visibility": "NORMAL",
      "simultaneous": false
    }
  },
  "tasks": [...],
  "rewards": [...]
}
```

### La Bible des Quêtes
Un document Word complet existe : `NEXUS_BibleQuetes_v1.docx`
Il contient toutes les quêtes des Âges 0, 1 et 2 avec lore, objectifs, récompenses.
Les quêtes WTF (humoristiques) sont marquées 💀

---

## 10. WORKFLOW GIT

### Push depuis le serveur Claude
```bash
cd /home/claude/nexus
git config user.email "nexus@modpack.com"
git config user.name "Claude"
git add .
git commit -m "Description du changement"
git -c credential.helper='!f() { echo username=lamardalexis-sys; echo password=TOKEN; }; f' push origin main
```

### Pull depuis le PC du joueur
```bash
cd ~/curseforge/minecraft/Instances/Nexus\ Absolu
git pull
```

### Après chaque session de travail
Toujours pusher et demander au joueur de faire `git pull`.

---

## 11. ÉTAT D'AVANCEMENT

### ✅ Fait
- [x] Modpack stable — jeu se lance
- [x] ~185 mods installés et compatibles
- [x] Conflits résolus (Galacticraft, Mekanism, EnderIO, Phosphor, TickCentral, etc.)
- [x] Config AE2 channels x4
- [x] Config UniDict keepOneEntry + 16 métaux supplémentaires
- [x] Fix clignotement ciel Astral Sorcery
- [x] Optimisation démarrage (version checkers off, disableAnimatedModels, preallocateMemory)
- [x] Bible des Quêtes Âges 0-1-2 rédigée (docx)
- [x] Liste des 277 mods dans Excel avec priorités
- [x] Structure des 10 âges définie
- [x] Recette Nexus Absolu 9x9 conceptualisée

### 🔄 En cours / Prochaine étape
- [ ] Phase 2 : Scripts ContentTweaker — créer les 9 items custom
- [ ] Phase 3 : Scripts CraftTweaker par âge
- [ ] Phase 4 : Quêtes Better Questing
- [ ] Phase 5 : Test complet + équilibrage

### ❌ Pas encore fait
- [ ] Items custom (NexusItems.zs)
- [ ] Textures des items custom
- [ ] Recettes Âge 0 (verrou four vanilla, Ex Nihilo)
- [ ] Recettes Âge 1 (Thermal chaîne forcée)
- [ ] Recettes Âge 2 (3 Fragments)
- [ ] Recettes Âge 3-9
- [ ] Recette finale Nexus Absolu
- [ ] Config spawn mobs (In Control)
- [ ] Config Spice of Life (maxHearts=50)
- [ ] Quêtes Better Questing (JSON)
- [ ] Lore books Patchouli

---

## 12. NOTES IMPORTANTES

### Ne jamais faire
- Supprimer `!mixinbooter-10.7.jar` → crash garanti
- Mettre TickCentral → conflit CoFHWorld
- Mettre Phosphor → conflit CoFHWorld
- Mettre VanillaFix → conflit MixinBooter
- Mettre deux versions d'EnderIO (all-in-one + modules séparés)

### IDs ModTweaker pour les machines courantes
```
mods.thermalexpansion.Pulverizer
mods.thermalexpansion.InductionSmelter
mods.thermalexpansion.Furnace (RedstoneHeater)
mods.mekanism.crusher
mods.mekanism.enrichment
mods.mekanism.purification
mods.mekanism.dissolution
mods.mekanism.crystallization
mods.mekanism.injection
mods.appliedenergistics2.Inscriber
mods.extendedcrafting.TableCrafting
mods.bloodmagic.AlchemyTable
mods.bloodmagic.Altar
mods.botania.ManaInfusion
mods.enderio.Alloy (EnderIO Alloy Smelter)
mods.enderio.SoulBinder
```

### Référence E2E
Le repo Enigmatica 2 Expert est cloné dans `/home/claude/e2e/` pour référence.
Les scripts dans `e2e/scripts/` sont une excellente référence pour la syntaxe.

---

*Dernière mise à jour : Session 1 — Stabilisation + configs Phase 1 complétée*
