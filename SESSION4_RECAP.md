# SESSION 4 RECAP — Nexus Absolu Modpack
> À lire en début de session. Contient TOUT pour continuer sans relire la conversation.

---

## IDENTITÉ DU PROJET
- **Modpack** : Nexus Absolu — Minecraft 1.12.2 Forge
- **GitHub** : https://github.com/lamardalexis-sys/nexus-absolu-modpack
- **Token GitHub** : `TOKEN_VOIR_LOCAL`
- **Version mod** : 1.0.31
- **~200 mods** installés
- **SKILL.md** sur le repo contient la référence complète (design, bugs, méthodologie)

---

## MÉTHODOLOGIE OBLIGATOIRE (Superpowers)
Référence : https://github.com/obra/superpowers

1. **DESIGN** — Discuter le design AVANT de coder
2. **PLAN** — Lister les fichiers exacts à modifier
3. **PETITES TACHES** — 2-5 min max par changement
4. **VÉRIFIER AVANT PUSH** — Accolades, imports, pas de méthodes à risque
5. **REVIEW** — Relire le code, compter les braces
6. **BUMPER LA VERSION** dans `build.gradle` à chaque changement

### Erreurs à NE JAMAIS refaire :
- `canRenderInLayer` → ne se reobfusque pas, rend blocs invisibles
- `shouldSideBeRendered` : `pos` EST déjà le voisin (PAS `pos.offset(side)`)
- `TRANSLUCENT` pour tous les blocs → rend les solides transparents
- Oublier une accolade fermante
- `/ct reload` ne marche PAS sur cette version de Forge → relancer le jeu

---

## BUILD SYSTEM (pas Gradle !)
```bash
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull
bash mod-source/build.sh
```
Le script : git pull → copie sources → javac → package JAR → SpecialSource reobfuscation → deploy dans mods/

**Textures seules** : F3+T en jeu
**Scripts CraftTweaker** : relancer le jeu (pas de /ct reload)
**Code Java** : relancer le jeu

---

## CE QUI A ÉTÉ FAIT CETTE SESSION

### Condenseur Dimensionnel (multibloc 2x2x2)
- Structure : Condenseur + 2 Nexus Wall + 1 Redstone Block (bas) + 3 Glass + 1 Wall (haut)
- TESR : items flottants spirale baignoire + liquide violet + écran dynamique (idle=scanline, processing=texte vert)
- Rendu : `CUTOUT_MIPPED`, `isOpaqueCube` false pour pos 4,5,6
- `shouldSideBeRendered` : cache glass-glass uniquement
- `isGlobalRenderer` + `INFINITE_EXTENT_AABB`
- Recettes : 60s@50RF/t (tier 1), buffer 500k RF, 500 RF/t input
- JEI intégré avec 5 recettes
- GUI avec bouton Auto/Manuel + START

### Atelier du Dr. Voss (nouveau bloc)
- GUI : 2 inputs + 1 output + bouton craft ">>"
- Recettes : Pioche Fragmentée (2 planks + 1 stick), Pioche Renforcée (2 iron nuggets + 1 wall_dust)
- TileEntity avec NBT save/load
- Fichiers : BlockAtelier, TileAtelier, AtelierRecipes, ContainerAtelier, GuiAtelier

### Pioches Custom
- `ItemPioche` extends `ItemPickaxe` avec `dustMultiplier`
- Pioche Fragmentée (bois, multiplier=1) : wall_dust + cobble frag/flint/clay
- Pioche Renforcée (fer, multiplier=2) : 60% grits direct, 40% wall_dust

### Scavenging System (minage murs CM)
- `PlayerInteractEvent.LeftClickBlock` sur `compactmachines3:wall`
- **Mains nues** : 20% wall_dust, 25% stick, 20% plank, 10% cobble_fragment, 25% rien (cooldown 800ms)
- **Pioche Fragmentée** : wall_dust garanti + bonus
- **Pioche Renforcée** : 60% grits (fer/cuivre/étain/charbon/redstone), 40% wall_dust
- Le mur CM reste intact (ne casse pas)

### Scripts CraftTweaker (Age0_Scavenging.zs)
- 4 wall_dust → cobblestone
- 2 wall_dust → gravel
- 2 cobblestone_fragment → cobblestone
- **8 nuggets → 1 grit** (shapeless, tous les métaux)
- Grit → four → lingot (8 métaux)
- Recettes backup pioches sur crafting table
- Nexus Wall : 8 wall_dust + 1 iron nugget
- Condenseur : 4 iron + 4 wall_dust + 1 redstone
- Atelier : 3 planks + 2 wall_dust + 3 cobble

### Quêtes Âge 0 (Better Questing JSON)
13 quêtes narratives avec lore Dr. Voss :
```
Q0  Les yeux s'ouvrent (livre Patchouli)
Q1  Les murs te parlent (16 cobble)
Q2  La poussière révèle ses secrets (tamis)
Q3  Le premier souffle (table + four)
Q4  La faim te ronge (8 pain, consume=1)
Q5  L'arbre dans la boîte (3 bonsai pots)
Q6  Les entrailles de la terre (smeltery + 16 fer)
Q7  Le goût de la variété (5 plats)
Q8  L'étincelle (Stirling Dynamo)
Q9  Le sang qui murmure (Blood Altar)
Q10 La machine qui respire (Condenseur 2x2x2)
Q11 La fissure dimensionnelle (CM 5x5)
Q12 L'évasion (entre dans CM 5x5)
```
Q100 (Age 1) dépend de Q12.

### Âge 1 — existe déjà (35 quêtes)
Q100-Q181 : Thermal + EnderIO + IE + AA + Jetpacks → Fragment Mécanique

---

## PROGRESSION ÂGE 0 COMPLÈTE (CM 3x3x3)
```
Mains nues → tape mur CM → sticks + planks + wall_dust
    ↓
Planks + sticks → Atelier (crafting table)
    ↓
Atelier : 2 planks + 1 stick → Pioche Fragmentée
    ↓
Pioche → wall_dust + cobble frag + flint + clay
    ↓
4 wall_dust → cobble → gravel → sieve Ex Nihilo → pépites
    ↓
8 pépites → 1 grit (craft shapeless)
    ↓
Grit → four → 1 lingot
    ↓
Lingots → Condenseur + Nexus Walls + Redstone Block
    ↓
Condenseur 2x2x2 → craft CM 5x5 (60s @ 50 RF/t)
    ↓
CM 5x5 → ÂGE 1
```

---

## DESIGN FUTUR (pas encore codé)

### Pioches Spécialisées (comme médecin généraliste/spécialiste)
- **CM 5x5** : déblocage Pioche Généraliste (tous les grits, 10-15% chacun)
- **CM 7x7+** : Pioches Spécialistes (Pioche de Fer = 66% iron_grit, etc.)
- Chaque spécialiste se craft avec le métal correspondant

### Condenseur Tiers
- Tier 1 : 2x2x2 (actuel, CM 3x3x3)
- Tier 2 : 3x3x3 (CM 5x5x5)
- Tier 3 : 4x4x4 (CM 7x7x7)

### Quêtes Âge 2
Pas encore écrit. Botania, Astral Sorcery, Blood Magic, sortie monde ouvert.

---

## FICHIERS JAVA DU MOD (30+ fichiers)
```
src/main/java/com/nexusabsolu/mod/
├── NexusAbsoluMod.java, Reference.java
├── blocks/
│   ├── BlockNexusOre.java, BlockNexusWall.java
│   └── machines/ BlockCondenseur.java, BlockCondenseurFormed.java, BlockAtelier.java
├── compat/jei/ NexusJEIPlugin.java, CondenseurCategory.java, CondenseurWrapper.java
├── gui/ ContainerCondenseur.java, GuiCondenseur.java, ContainerAtelier.java, GuiAtelier.java, GuiHandler.java
├── init/ ModBlocks.java, ModItems.java, RegistrationHandler.java
├── items/ ItemBase.java, ItemGrit.java, ItemPioche.java
├── items/fragments/ ItemFragment.java, ItemNexusAbsolu.java
├── proxy/ ClientProxy.java, CommonProxy.java
├── render/ TESRCondenseur.java
├── scavenging/ ScavengeEventHandler.java
├── tiles/ TileCondenseur.java, TileCondenseurEnergy.java, CondenseurRecipes.java,
│          InternalEnergyStorage.java, TileAtelier.java, AtelierRecipes.java
├── util/ IHasModel.java, NexusCreativeTab.java
└── world/ NexusOreGen.java
```

---

## GIT WORKFLOW
### Push depuis Claude :
```bash
cd /home/claude/nexus
git add -A && git commit -m "vX.Y.Z: description"
git -c credential.helper='!f() { echo username=lamardalexis-sys; echo password=TOKEN_VOIR_LOCAL; }; f' push origin main
```

### Pull + Build depuis le PC :
```bash
cd "/c/Users/lamar/curseforge/minecraft/Instances/Nexus Absolu"
git pull
bash mod-source/build.sh
```

---

## CE QU'IL RESTE À FAIRE (priorité)
1. **Tester** le minage à mains nues + pioche (v1.0.31)
2. **Coffre de départ** : définir le contenu exact du coffre dans la CM 3x3x3
3. **Quêtes Âge 0** : adapter Q1/Q2 pour correspondre à la nouvelle mécanique (mains nues → pioche)
4. **Quêtes Âge 2** : écrire les quêtes Botania/Astral/Blood Magic
5. **Pioches spécialisées** : coder pour les CM 5x5+
6. **Condenseur tiers 2 et 3** : 3x3x3 et 4x4x4
7. **Textures** : améliorer le condenseur formé (bug rendu faces internes toujours visible)
8. **Bloquer la Pioche Renforcée** en CM 3x3x3 (ou la renommer Pioche Généraliste et la bloquer en 5x5)

---

## REPOS DE RÉFÉRENCE (déjà clonés dans /home/claude/)
```
/home/claude/e2e-full/      — Enigmatica 2 Expert
/home/claude/sevtech/       — SevTech Ages
/home/claude/ftb-interactions/ — FTB Interactions
/home/claude/compact-claustro/ — Compact Claustrophobia
```

*Dernière mise à jour : Session 4 — 30 mars 2026*
