# SKILL.md — Nexus Absolu Modpack
> Référence complète pour continuer le développement du modpack sans relire toute la conversation.

---

## 1. CONTEXTE DU PROJET

### Identité
- **Nom** : Nexus Absolu
- **Version Minecraft** : 1.12.2 Forge 14.23.5.2860
- **Version Mod** : 1.0.52 (incrémentée à chaque changement)
- **GitHub** : https://github.com/lamardalexis-sys/nexus-absolu-modpack
- **Instance CurseForge** : `C:\Users\lamar\curseforge\minecraft\Instances\Nexus Absolu`
- **MDK** : `C:\Dev\NexusAbsoluMod`

### Concept narratif
Un scientifique disparu (Dr. E. Voss) a passé 30 ans à repousser les limites de la physique. Il a réussi mais a disparu. Le joueur se réveille dans une Compact Machine et doit suivre ses traces jusqu'au **Nexus Absolu** — un item qui transcende la matière.

### Philosophie de gameplay
- Inspiré d'**Enigmatica 2 Expert** (recettes inter-mods forcées) et **Compact Claustrophobia** (débuter dans une boîte)
- Les 3 premiers âges se jouent dans des **Compact Machines** de taille croissante
- **Chaque mod doit interagir avec au moins 2 autres** via les crafts custom
- Pas de Thaumcraft (retiré — trop complexe à automatiser pour le Nexus Absolu)
- Pas de chapitres par mod dans les quêtes — chapitres **narratifs** par âge
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
  CONDENSEUR 2x2x2 : craft la Compact Machine 5x5 (60s @ 50 RF/t)
  MODS : Ex Nihilo Creatio, Tinkers Construct, Bonsai Trees, Pam's HarvestCraft,
         Spice of Life Carrot Edition, Crop Dusting, Soul Shards Respawn

ÂGE 1 — La Mécanique Brute (salle 5x5 → 7x7 → 9x9)
  Étape 4 : Thermal Expansion — première dynamo, 1M RF
  Étape 5 : Chaîne purification + EnderIO conduits
  Étape 6 : Immersive Engineering + hybridation Tech/Magie
  CONDENSEUR 3x3x3 : craft la Compact Machine 7x7 (90s @ 80 RF/t)
  MODS : Thermal Expansion/Foundation/Dynamics, IE, Actually Additions, EnderIO

ÂGE 2 — Le Paradoxe Organique (salle 7x7 → 9x9 → 13x13)
  Étape 7 : Botania — Mana Pool, Mystical Agriculture tier 1-2
  Étape 8 : Astral Sorcery — télescope, cristaux améliorent machines Thermal
  Étape 9 : Blood Magic Tier 1 — la Trinité des 3 Fragments
  Étape 10 : Clé du Laboratoire → SORTIE vers le monde ouvert
  CONDENSEUR 4x4x4 : craft la Compact Machine 9x9 (120s @ 120 RF/t)
  MODS : Botania, Astral Sorcery, Blood Magic, Mystical Agriculture
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

---

## 3. CONDENSEUR DIMENSIONNEL (Multibloc Custom)

### Structure 2x2x2 (Tier 1 — Âge 0)
```
Couche bas:                    Couche haut:
[Condenseur] [Nexus Wall]     [Glass pos4] [Glass pos5]
[Redstone]   [Nexus Wall]     [Glass pos6] [Wall  pos7]
```
- **Condenseur** (pos 0) : bloc master, clic droit = GUI
- **Nexus Wall** (pos 1,2) : murs structurels
- **Redstone Block** (pos 3) : entrée énergie RF
- **Glass** (pos 4,5,6) : vitres transparentes (CUTOUT_MIPPED)
- **Wall** (pos 7) : mur plein du haut

### Recettes du Condenseur
| Tier | Input | Output | Temps | RF/t | Total RF |
|------|-------|--------|-------|------|----------|
| 1 | 2x Tiny (3x3) | Small (5x5) | 60s | 50 | 60k |
| 2 | 2x Small (5x5) | Normal (7x7) | 90s | 80 | 144k |
| 3 | 2x Normal (7x7) | Large (9x9) | 120s | 120 | 288k |
| 4 | 2x Large (9x9) | Giant (11x11) | 180s | 200 | 720k |
| 5 | 2x Giant (11x11) | Maximum (13x13) | 300s | 350 | 2,100k |

### Énergie
- Buffer : 500k RF, max input 500 RF/t
- Consommation : rfPerTick de la recette (pas hardcodé)

### Rendu TESR
- **Items flottants** dans la zone verre (y+1.45), 4 positions
- **Effet baignoire** : items spiralent vers le centre (phase 1: 0-60%), puis coulent dans le trou (phase 2: 60-100%)
- **Liquide violet** qui monte avec des vagues, vire magenta en fin de processing
- **Anneaux de vortex** à la surface à 30%+ de progression
- **Écran dynamique** sur le condenseur :
  - Idle : fond bleu-gris foncé, scanline lente, point violet pulsant, LED rouge
  - Processing : fond vert, texte vert défilant, curseur clignotant, LED verte

### Rendu blocs
- `BlockRenderLayer.CUTOUT_MIPPED` — transparence binaire (alpha 0 ou 255)
- `isOpaqueCube`/`isFullCube` : false pour positions 4,5,6 (verre)
- `shouldSideBeRendered` : cache faces verre-verre (panneau continu)
- `isGlobalRenderer(te)` : true + `INFINITE_EXTENT_AABB`

### Tiers futurs du Condenseur
- **Tier 1** : 2x2x2 (actuel) — dans CM 3x3x3
- **Tier 2** : 3x3x3 — dans CM 5x5x5
- **Tier 3** : 4x4x4 — dans CM 7x7x7

---

## 4. BUILD SYSTEM (sans Gradle)

### Workflow
```bash
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull
bash mod-source/build.sh
```

### build.sh fait :
1. `git pull`
2. Copie sources + build.gradle vers MDK
3. Lit la version depuis build.gradle
4. `javac` avec classpath direct
5. Package JAR + copie resources
6. Reobfuscation via SpecialSource (MCP→SRG)
7. Supprime anciens JARs + copie dans mods/

### Chemins critiques
```
FORGE="C:/Users/lamar/.gradle/caches/forge_gradle/minecraft_user_repo/.../forge-1.12.2-14.23.5.2860_mapped_snapshot_20171003-1.12.jar"
SPECIALSOURCE="C:/Users/lamar/.gradle/caches/forge_gradle/maven_downloader/net/md-5/SpecialSource/1.8.3/SpecialSource-1.8.3-shaded.jar"
SRG="C:/Users/lamar/.gradle/caches/minecraft/de/oceanlabs/mcp/mcp_snapshot/20171003/1.12.2/srgs/mcp-srg.srg"
JEI="mods/jei_1.12.2-4.16.1.301.jar"
```

### Raccourcis de test
- **Textures seulement** : F3+T en jeu (pas besoin de relancer)
- **Scripts CraftTweaker** : `/ct reload` en jeu
- **Code Java** : relancer obligatoire
- **Monde superflat** : charge plus vite pour tests

---

## 5. LISTE DES MODS (~200 mods)

### Core / Libs
```
!mixinbooter-10.7.jar, AutoRegLib, Baubles, Bookshelf, BrandonsCore,
CodeChickenLib, CoFHCore, CoFHWorld, CreativeCore, EnderCore, Forgelin,
FTBLib, IvToolkit, LibVulpes, MCMultiPart, MJRLegendsLib, MTLib,
RedstoneFlux, WanionLib, ZeroCore, bdlib, capabilityadapter,
_supermartijn642corelib, supermartijn642configlib, fusion, base,
p455w0rdslib, tesla-core-lib, Placebo, Cucumber, AsmodeusCore,
NCOLegacyLib, ItemFilters, Mantle, mcjtylib, LootTableTweaker,
ForgeMultipart, Guide-API, AttributeFix
```

### Tech
```
ThermalExpansion, ThermalFoundation, ThermalDynamics, Mekanism,
MekanismGenerators, MekanismTools, EnderIO-base, EnderIO-conduits,
EnderIO-endergy, ImmersiveEngineering, ImmersivePetroleum,
IndustrialWires, ActuallyAdditions, ExtraUtils2, Cyclic,
PneumaticCraft-Repressurized, NuclearCraft, BetterFusionReactor,
NC-ReactorBuilder, ExtremeReactors, FluxNetworks, SimplyJetpacks2,
SolarFluxReborn, XNet, BuildingGadgets, ElevatorMod, OpenComputers,
ComputerCraft, PackagedAuto, PackagedDraconic, industrialforegoing
```

### AE2 / Storage
```
ae2-uel (AE2 UEL), AE2WTLib, AEAdditions, ae2stuff, AE-Net-Vis-Tool,
ExtendedAE, betterp2p, FluidCraft-AE2, JustEnoughEnergistics,
WirelessInterfaceTerminal, WirelessPatternTerminal, nae2,
StorageDrawers, ironchest, EnderStorage
```

### Magie
```
Botania, BloodMagic, astralsorcery, MysticalAgriculture,
MysticalAgradditions, Draconic-Evolution, Draconic-Additions
```

### Exploration / Dimensions
```
Galacticraft, ExtraPlanets, AdvancedRocketry, RFTools, RFToolsDim,
RFToolsCtrl, RFToolsPower, TwilightForest, BiomesOPlenty,
iceandfire, RecurrentComplex, Aroma1997s-Dimensional-World,
justenoughdimensions
```

### Progression / Quêtes
```
FTBQuests, ExtraQuests, BetterQuesting, questbook, FTBUtilities,
FTBBackups, FTBGuides, DefaultOptions, ContentTweaker, CraftTweaker2,
KubeJS, modtweaker, ModularMachinery, incontrol
```

### QoL / Optimisation
```
JEI, JER, JEROreIntegration, just-enough-harvestcraft, Just-Enough-Botania,
JustEnoughReactors, JustEnoughPetroleum, AppleSkin, Controlling,
InventoryTweaks, MouseTweaks, TrashSlot, JourneyMap, TheOneProbe,
Neat, NaturesCompass, Waystones, carryon, EnchantmentDescriptions,
Morpheus, Toast-Control, NetherPortalFix, FastLeafDecay, FoamFix,
AIImprovements, Clumps, Surge, BetterFps, randompatches, WrapUp,
DarkUtils, AmbientSounds, Chameleon, NotEnoughIDs, ftb-ultimine,
ProjectIntelligence
```

### Décoration / Construction
```
Chisel, chiselsandbits, CTM, rechiseled, Quark, QuarkOddities,
OpenBlocks, Patchouli
```

### Gameplay
```
ExNihiloCreatio, TConstruct, TinkersComplement, TinkerToolLeveling,
conarm, tinkersjei, tinkersoc, Pam's-HarvestCraft, bonsaitrees,
CropDusting, SoulShardsRespawn, Avaritia, ExtendedCrafting,
angelRingToBauble, Woot, compactmachines3, tombstone,
AppleTreesRev, GardenOfGlass, WitherSkeletonTweaks,
environmentaltech, environmentalmaterials, etlunar, valkyrielib,
JAOPCA, UniDict, Spice-of-Life(?), EvenMoreTNT, SuperTNT
```

### À supprimer
```
letsencryptcraft → inutile
```

---

## 6. SCRIPTS CRAFTTWEAKER

### Structure des fichiers
```
scripts/
  Globals.zs             ← imports + fonctions utilitaires
  Age0_ExNihilo.zs       ← recettes Âge 0
  Age0_Tinkers.zs
  Age1_Thermal.zs        ← recettes Âge 1
  Age1_EnderIO.zs
  Age2_Botania.zs        ← recettes Âge 2
  contenttweaker/
    NexusItems.zs         ← items custom
```

### Référence syntaxe
Voir section 5 du SKILL original pour la syntaxe complète CraftTweaker.

### Repos de référence clonés
```
/home/claude/e2e-full/     — Enigmatica 2 Expert (342 scripts, 1 item CT, Better Questing)
/home/claude/sevtech/      — SevTech Ages (ZenStages pour bloquer items par âge, advancements)
/home/claude/ftb-interactions/ — FTB Interactions (20k+ lignes)
/home/claude/compact-claustro/ — Compact Claustrophobia (13 items CT, 228 quêtes BQ, ItemStages)
```

### Enseignements des gros modpacks
- **E2E** : 1 script par mod, Globals.zs avec fonctions utilitaires
- **SevTech** : ZenStages pour bloquer items/recettes par âge
- **CC** : ItemStages pour progression, ContentTweaker pour items custom
- **Nexus Absolu** : chapitres narratifs par âge, PAS par mod

---

## 7. QUÊTES — PHILOSOPHIE

### Règle d'or
Pas de chapitres par mod. Chapitres **narratifs** par âge. Chaque quête force 3-4 mods sans les nommer. Le joueur résout des problèmes, pas "apprend un mod".

### Exemple Âge 0
```
"Les murs te parlent" → cogne les murs (Ex Nihilo scavenging)
"La faim te ronge" → plante des graines (Pam's + Bonsai Trees)
"Le métal brut" → tamise → fonderie (Tinkers)
"L'odeur de la mort" → Soul Shards + Crop Dusting
"Le sang appelle" → Blood Magic tier 1 intro
"La clé de la cellule" → Condenseur 2x2x2 = craft CM 5x5
```

### Objectif
En finissant toutes les quêtes, le joueur a utilisé au moins **100 mods** dont les **30 principaux** à 95%.

---

## 8. FICHIERS JAVA DU MOD (27+ fichiers)

```
src/main/java/com/nexusabsolu/mod/
├── NexusAbsoluMod.java, Reference.java
├── blocks/ BlockNexusOre.java, BlockNexusWall.java
├── blocks/machines/ BlockCondenseur.java, BlockCondenseurFormed.java
├── compat/jei/ NexusJEIPlugin.java, CondenseurCategory.java, CondenseurWrapper.java
├── gui/ ContainerCondenseur.java, GuiCondenseur.java, GuiHandler.java
├── init/ ModBlocks.java, ModItems.java, RegistrationHandler.java
├── items/ ItemBase.java, ItemGrit.java
├── items/fragments/ ItemFragment.java, ItemNexusAbsolu.java
├── proxy/ ClientProxy.java, CommonProxy.java
├── render/ TESRCondenseur.java
├── scavenging/ ScavengeEventHandler.java
├── tiles/ TileCondenseur.java, TileCondenseurEnergy.java, CondenseurRecipes.java, InternalEnergyStorage.java
├── util/ IHasModel.java, NexusCreativeTab.java
└── world/ NexusOreGen.java
```

---

## 9. BUGS CONNUS / NOTES

### Ne jamais faire
- Supprimer `!mixinbooter-10.7.jar` → crash garanti
- Mettre TickCentral → conflit CoFHWorld
- Mettre Phosphor → conflit CoFHWorld
- Mettre VanillaFix → conflit MixinBooter
- Mettre Corail Tombstone → crash NullPointer dans DIM144 (WorldBorder)
- Mettre GalaxySpace → conflit ExtraPlanets
- Mettre 2 versions AE2 (ae2-uel + appliedenergistics2-rv6) → crash
- Mettre AE2WTLib 1.0.34 → crash IndexOutOfBounds creative tab
- Utiliser `canRenderInLayer` → ne se reobfusque pas, rend blocs invisibles
- `getBlockLayer()` en TRANSLUCENT pour tous → rend blocs solides semi-transparents

### Reobfuscation
Certaines méthodes Forge ne se reobfusquent pas correctement via SpecialSource :
- `canRenderInLayer` ❌
- `isGlobalRenderer` ✅ (fonctionne)
- `getRenderBoundingBox` — utiliser `INFINITE_EXTENT_AABB` à la place
- `shouldSideBeRendered` — le paramètre `pos` EST déjà le voisin (pas besoin de `pos.offset(side)`)

### Encodage
- Fichiers Java : ASCII-safe (Cp1252 sur Windows casse les unicode)
- `setUnlocalizedName` pas `setTranslationKey` (1.12.2)
- `getTabIconItem` pas `createIcon`

---

## 9.5. SYSTÈME DE SPAWN — SALLE OVERWORLD

### Architecture (comme Compact Claustrophobia)
Le joueur spawn dans une **salle overworld** (pas DIM144). La salle est auto-générée par JED via `spawn_structure`.

### Fichiers clés
- `config/justenoughdimensions/nexus_salle.schematic` — la structure de la salle
- `config/justenoughdimensions/dimensions.json` — config spawn_structure centré au spawn
- `config/worldprimer.cfg` — adventure mode dans la salle, survival dehors

### Flow joueur
```
Crée un monde → JED place nexus_salle.schematic au spawn
→ spawn dans la salle en adventure mode (World Primer, rayon 15 blocs)
→ lit les 4 écrans OpenComputers (lore, objectif, peur, tuto)
→ prend le PSD + livre Patchouli
→ clic droit sur le bloc CM 3x3 → entre dans l'Âge 0
→ World Primer passe en survival automatiquement
```

### World Primer — commandes clés
```
timedCommands:
  %10 ticks → adventure mode rayon 15 blocs du spawn (0,73,0)
  %10 ticks → survival mode hors rayon 15

playerChangedDimensionEnterCommands:
  DIM144 → gamemode survival

postWorldCreationCommands:
  setworldspawn 0 73 0
```

### Écrans OpenComputers (4 écrans T3 dans la salle)
Chaque écran = 1 PC OC T3 avec autorun.lua :
1. **Accueil/Status** — titre Voss, status système, intro lore
2. **Objectif** — explication du Nexus Absolu, 9 fragments, 9 âges
3. **Peur** — avertissement sécurité, sujets précédents disparus
4. **Tuto** — instructions de survie, clic droit sur la CM

### Livres de spawn désactivés
Configs modifiées pour ne pas polluer l'inventaire :
- Astral Sorcery: `giveJournalAtFirstJoin=false`
- OpenBlocks: `infoBook=false`
- The One Probe: `spawnNote=false`
- OpenComputers: `giveManualToNewPlayers=false`
- Guide-API: `canSpawnWithBooks=false`
- Tinkers: `spawnWithBook=false`

### Tombes à la mort
**Tombstone supprimé** (crash DIM144). **OpenBlocks Graves** gère les tombes.

---

## 10. WORKFLOW GIT

### Push depuis Claude
```bash
cd /home/claude/nexus
git add . && git commit -m "vX.Y.Z: description"
git -c credential.helper='!f() { echo username=lamardalexis-sys; echo password=TOKEN; }; f' push origin main
```

### Pull + Build depuis le PC
```bash
cd ~/curseforge/minecraft/Instances/Nexus\ Absolu
git pull
bash mod-source/build.sh
```

---

*Dernière mise à jour : Session 7 — Reinstallation instance, salle overworld, JED spawn_structure, World Primer*

---

## 11. METHODOLOGIE DE DEVELOPPEMENT (Superpowers)

Référence : https://github.com/obra/superpowers

### Avant chaque changement de code :
1. **DESIGN** — Discuter le design avec Alexis AVANT de coder
2. **PLAN** — Lister les fichiers exacts à modifier/créer, avec le contenu prévu
3. **PETITES TACHES** — Chaque changement = 2-5 min max, pas de gros blocs

### Avant chaque push :
4. **VERIFIER COMPILATION** — Compter les accolades { }, vérifier les imports
5. **REVIEW SYSTEMATIQUE** — Relire chaque fichier modifié pour :
   - Accolades équilibrées
   - Imports corrects (pas de classes manquantes)
   - Pas de méthodes qui ne se reobfusquent pas (canRenderInLayer, etc.)
   - Cohérence avec le reste du code existant
   - Version bumpée dans build.gradle
6. **PUSH** — Seulement après vérification

### Erreurs connues à ne JAMAIS refaire :
- ❌ Oublier une accolade fermante (renderScreen v1.0.23)
- ❌ Utiliser canRenderInLayer (ne se reobfusque pas)
- ❌ shouldSideBeRendered : pos EST le voisin, PAS pos.offset(side)
- ❌ Mettre TRANSLUCENT pour tous les blocs (rend les solides transparents)
- ❌ Blockstate inversé (vérifier glass=3, wall=1 pas l'inverse)
- ❌ Pusher du code sans vérifier que les braces sont équilibrées
- ❌ Hardcoder des valeurs (rfPerTick, maxProcessTime) au lieu de lire la recette
- ❌ JEI path dans build.sh doit matcher la version installée (ex: 4.16.1.1013 pas .301)
