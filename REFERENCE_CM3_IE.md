# REFERENCE_CM3_IE.md — Compact Machines 3 & Immersive Engineering Architecture
> Analysé depuis les sources: CM3 branche 1.12.x, IE branche 0.12-89
> Pour le développement du Portail Voss (sortie overworld + placement CM x9)

---

## 1. COMPACT MACHINES 3 — Système de liaison bloc ↔ salle

### 1.1 TileEntityMachine (le bloc CM dans le monde)

Chaque bloc Compact Machine dans le monde (overworld ou autre dimension) possède un `TileEntityMachine` avec deux champs critiques :

```java
public int id = -1;          // identifiant unique de la machine
private BlockPos roomPos;     // coin nord-ouest de la salle dans DIM144, y=40
```

Le `id` est l'identifiant qui relie le bloc physique à sa salle dans DIM144. Le `roomPos` est la position du coin nord-ouest de la salle intérieure dans la dimension des compact machines.

### 1.2 WorldSavedDataMachines (le registre central)

Fichier: `org.dave.compactmachines3.world.WorldSavedDataMachines`

C'est le cœur du système. Il stocke plusieurs maps persistantes :

```java
public Map<Integer, DimensionBlockPos> machinePositions = new HashMap<>();
// id → position du bloc CM dans le monde (dimension + BlockPos)

// Méthodes clés :
void addMachinePosition(int id, BlockPos pos, int dimension)
void setMachineRoomPosition(int id, BlockPos roomPos, boolean updateLastGrid)
BlockPos getMachineRoomPosition(int id)
void addSpawnPoint(int id, Vec3d destination)
```

`machinePositions` est la map qui dit "la machine #42 se trouve à la position (100, 64, 200) dans la dimension 0". C'est CE mapping qu'on doit modifier pour "déplacer" une CM de l'intérieur d'une x7 vers l'overworld.

### 1.3 Comment le PSD (Personal Shrinking Device) téléporte

Fichier: `org.dave.compactmachines3.world.tools.TeleportationTools`

Quand le joueur utilise le PSD pour ENTRER dans une CM :
1. Le jeu lit le `id` du `TileEntityMachine` sur lequel le joueur a cliqué
2. Il cherche le `roomPos` associé via `WorldSavedDataMachines.getMachineRoomPosition(id)`
3. Il téléporte le joueur au `spawnPoint` de cette salle dans DIM144

Quand le joueur utilise le PSD pour SORTIR d'une salle :
1. Le jeu détermine dans quelle salle le joueur se trouve via `StructureTools.getIdForPos(playerPos)`
2. Il cherche la position du bloc CM via `WorldSavedDataMachines.machinePositions.get(id)`
3. Il téléporte le joueur à la position du bloc + 1 dans la dimension stockée dans `DimensionBlockPos`

### 1.4 Comment le joueur se retrouve dans une salle

Quand un nouveau bloc CM est placé et qu'un joueur entre pour la première fois :
1. `TileEntityMachine.initStructure()` appelle `StructureTools.generateCubeForMachine(this)`
2. Ça génère la salle dans DIM144 (murs en `compactmachines3:wall`) et assigne un `id`
3. Le `roomPos` est sauvé dans le TE et dans `WorldSavedDataMachines`
4. Le spawn point est calculé au centre de la salle

### 1.5 Legacy ID system

```java
// readFromNBT:
id = compound.getInteger("machineId");
if (compound.hasKey("coords")) {
    id = compound.getInteger("coords"); // Legacy
    roomPos = new BlockPos(1024 * id, 40, 0);
}
```

Les anciennes machines utilisaient `coords` au lieu de `machineId`. Le `roomPos` était calculé comme `(1024 * id, 40, 0)` — chaque salle espacée de 1024 blocs sur l'axe X.

### 1.6 EnumMachineSize (metadata des blocs CM)

```java
TINY    (0, "tiny", 4),     // 3x3 intérieur,  meta 0
SMALL   (1, "small", 6),    // 5x5 intérieur,  meta 1
NORMAL  (2, "normal", 8),   // 7x7 intérieur,  meta 2
LARGE   (3, "large", 10),   // 9x9 intérieur,  meta 3
GIANT   (4, "giant", 12),   // 11x11 intérieur, meta 4
MAXIMUM (5, "maximum", 14), // 13x13 intérieur, meta 5
```

Le `dimension` ici n'est PAS la dimension Minecraft — c'est la taille intérieure de la salle.

### 1.7 Plan pour le Portail Voss (sortie vers overworld)

Pour téléporter le joueur de sa x9 (dans DIM144) vers l'overworld avec sa CM x9 visible :

```
ÉTAPE 1: Identifier la salle actuelle du joueur
  → StructureTools.getIdForPos(playerPos) retourne le machine ID
  → WorldSavedDataMachines.getMachineRoomPosition(id) retourne le roomPos

ÉTAPE 2: Placer un bloc CM x9 dans l'overworld
  → world.setBlockState(overworldPos, compactmachines3:machine[SIZE=LARGE])
  → Récupérer le TileEntityMachine du nouveau bloc

ÉTAPE 3: Lier le nouveau bloc à la salle existante
  → newTE.id = existingMachineId
  → newTE.setRoomPos(existingRoomPos)
  → WorldSavedDataMachines.addMachinePosition(id, overworldPos, 0)
  // Ça écrase l'ancienne position (qui était dans la x7 en DIM144)

ÉTAPE 4: Supprimer l'ancien bloc CM x9 de la x7
  → Optionnel, mais propre. Sinon on a un bloc orphelin dans la x7.

ÉTAPE 5: Téléporter le joueur
  → player.changeDimension(0) avec ITeleporter custom
  → Position: à côté du nouveau bloc CM x9
```

Le point critique est l'étape 3 : `addMachinePosition` remplace la position dans la map. À partir de ce moment, le PSD dirigera le joueur vers l'overworld quand il sort de la salle, pas vers la x7.

### 1.8 Accès à WorldSavedDataMachines depuis notre mod

```java
// Depuis n'importe quel code serveur:
WorldSavedDataMachines wsd = WorldSavedDataMachines.getInstance();
// Attention: peut retourner null si DIM144 n'est pas chargé

// Pour forcer le chargement:
WorldServer machineWorld = DimensionManager.getWorld(ConfigurationHandler.Settings.dimensionId);
// Par défaut dimensionId = 144
```

### 1.9 Chunk Loading

CM3 a son propre système de chunk loading (`ChunkLoadingMachines`). Quand un bloc CM existe dans le monde, sa salle correspondante dans DIM144 est force-loaded. Le nouveau bloc dans l'overworld héritera automatiquement de ce comportement via `TileEntityMachine.initialize()`.

---

## 2. IMMERSIVE ENGINEERING — Pattern Multibloc

### 2.1 Architecture générale

IE utilise un pattern où TOUS les blocs d'un multibloc sont remplacés par UN SEUL type de bloc. Chaque position a son propre TileEntity de la même classe, mais seul un (le "master") fait la logique. Les autres sont des "dummies" qui délèguent.

### 2.2 IMultiblock (l'interface)

Fichier: `blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock`

```java
public interface IMultiblock {
    String getUniqueName();
    boolean isBlockTrigger(IBlockState state);  // pré-check rapide
    boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player);
    ItemStack[][][] getStructureManual();       // pour le Manuel de l'Ingénieur
    IngredientStack[] getTotalMaterials();
    float getManualScale();
    boolean canRenderFormedStructure();
    void renderFormedStructure();
}
```

Le flow: le joueur frappe un bloc avec l'Engineer's Hammer → IE parcourt tous les `IMultiblock` enregistrés → pour chacun, `isBlockTrigger()` vérifie si le bloc frappé pourrait être le déclencheur → si oui, `createStructure()` valide la structure complète et la forme.

### 2.3 TileEntityMultiblockPart (la base de tous les TEs multibloc)

Fichier: `blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart`

```java
public abstract class TileEntityMultiblockPart<T> extends TileEntityIEBase
        implements ITickable, IDirectionalTile, IBlockBounds, IGeneralMultiblock {

    public boolean formed = false;
    public int pos = -1;            // index linéaire dans la structure
    public int[] offset = {0,0,0};  // offset par rapport au master
    public boolean mirrored = false;
    public EnumFacing facing = EnumFacing.NORTH;
    protected final int[] structureDimensions; // {hauteur, longueur, largeur}
}
```

Chaque TE sait où il est dans la structure grâce à `offset` (distance au master en X,Y,Z) et `pos` (index linéaire calculé comme `h*L*W + l*W + w`).

### 2.4 Le pattern master/dummy

```java
// Trouver le master depuis n'importe quel TE:
public T master() {
    if (offset[0]==0 && offset[1]==0 && offset[2]==0)
        return (T)this;  // je SUIS le master
    BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
    TileEntity te = world.getTileEntity(masterPos);
    return this.getClass().isInstance(te) ? (T)te : null;
}

// Vérifier si on est un dummy:
public boolean isDummy() {
    return offset[0]!=0 || offset[1]!=0 || offset[2]!=0;
}
```

Toute la logique (processing, inventaire, énergie) est dans `update()` du master. Les dummies ne font rien sauf déléguer via `master()`.

### 2.5 Formation d'un multibloc (exemple: Coke Oven)

Fichier: `MultiblockCokeOven.createStructure()`

```java
// 1. Vérifier que tous les 27 blocs sont des cokebricks
for(int h=-1; h<=1; h++)
    for(int xx=-1; xx<=1; xx++)
        for(int zz=-1; zz<=1; zz++)
            if(!Utils.isBlockAt(world, pos.add(xx,h,zz), IEContent.blockStoneDecoration, COKEBRICK))
                return false;

// 2. Remplacer TOUS les blocs par le bloc "formé"
IBlockState state = IEContent.blockStoneDevice.getStateFromMeta(COKE_OVEN);
state = state.withProperty(IEProperties.FACING_HORIZONTAL, facing);
for(int h=-1; h<=1; h++)
    for(int l=-1; l<=1; l++)
        for(int w=-1; w<=1; w++) {
            world.setBlockState(rotatedPos, state);
            TileEntityCokeOven te = (TileEntityCokeOven) world.getTileEntity(rotatedPos);
            te.offset = new int[]{xx, h, zz};
            te.pos = (h+1)*9 + (l+1)*3 + (w+1);
            te.formed = true;
        }
```

Points clés: tous les blocs deviennent le MÊME type. Le metadata du bloc formé est identique partout. Seul le TileEntity diffère (via `offset` et `pos`).

### 2.6 Destruction (disassemble)

```java
public void disassemble() {
    if (formed && !world.isRemote) {
        for (chaque position dans la structure) {
            TileEntityMultiblockPart part = getTileEntity(position);
            ItemStack original = part.getOriginalBlock();  // CHAQUE TE sait quel bloc il était
            part.formed = false;
            world.setBlockState(position, Utils.getStateFromItemStack(original));
        }
    }
}
```

`getOriginalBlock()` est une méthode abstraite que chaque multibloc implémente. Pour le Coke Oven, tous retournent un cokebrick. Pour des structures plus complexes avec des blocs mixtes, chaque TE peut retourner un bloc différent.

### 2.7 Rotation

IE gère les rotations via `EnumFacing facing` sur chaque TE. Le calcul de position utilise `facing.rotateY()` pour la largeur. Le système supporte aussi le mirroring via `boolean mirrored`.

### 2.8 Comparaison avec notre approche Nexus Absolu

Notre TilePortalVoss utilise un pattern plus simple que IE :

```
IE:    Tous les blocs remplacés par UN type → chaque position = TileEntity → master/dummy
Nexus: Les blocs restent en place → UN seul TileEntity (le master) → validation par scan
```

Notre approche est plus simple et évite les problèmes de drops (on ne remplace rien). Le trade-off : pas de possibilité de faire du routing de capabilities par face comme IE (items/fluides/énergie exposés par n'importe quelle position du multibloc). Mais pour le Portail Voss, on n'en a pas besoin — les hatches dédiés (EnergyInput, FluidInput) gèrent ça.

### 2.9 Leçons à appliquer

Pour les futurs multiblocs Nexus Absolu (Age 3+), si on a besoin de machines industrielles avec I/O par face :

1. Créer un `BlockMultiblockPart` unique (comme IE fait avec `BlockStoneDevice`)
2. Chaque position = même bloc, même TE class, différent `offset`
3. Le master délègue capabilities selon la position (`offset` → quel rôle ce bloc joue)
4. `getOriginalBlock()` sauvé dans NBT pour chaque TE → drops corrects
5. Formation via un item spécial (comme l'Engineer's Hammer) plutôt que par scan périodique

Pour le Portail Voss actuel, notre scan périodique + hatches dédiés reste la meilleure approche.

---

## 3. FICHIERS SOURCE CLÉS (à re-cloner si nécessaire)

### Compact Machines 3

```
Repo: https://github.com/CompactMods/CompactMachines/tree/1.12.x
Fichiers critiques:
  tile/TileEntityMachine.java          — TE du bloc CM (id, roomPos, NBT)
  world/WorldSavedDataMachines.java    — registre central (machinePositions, spawnPoints)
  world/tools/TeleportationTools.java  — logique de téléportation PSD
  world/tools/StructureTools.java      — génération de salles, getIdForPos()
  reference/EnumMachineSize.java       — metadata: 0=tiny...5=maximum
  misc/ConfigurationHandler.java       — dimensionId (défaut 144)
```

### Immersive Engineering

```
Repo: https://github.com/BluSunrize/ImmersiveEngineering/tree/0.12-89
Fichiers critiques:
  api/MultiblockHandler.java                              — IMultiblock interface
  common/blocks/TileEntityMultiblockPart.java             — base TE multibloc
  common/blocks/metal/TileEntityMultiblockMetal.java      — extension pour machines métal
  common/blocks/multiblocks/MultiblockCokeOven.java       — exemple simple 3x3x3
  common/blocks/stone/TileEntityCokeOven.java             — TE formé du coke oven
```

---

## 4. ÉTAT ACTUEL DU PORTAIL VOSS (v1.0.122)

Phase 1 complétée : `BlockNexusWallT2`, `BlockEcranControle`, `TilePortalVoss` avec validation de structure 9 couches et particules visuelles.

Phase 2 à faire : téléportation overworld via CM3 API (`WorldSavedDataMachines.addMachinePosition`) + placement bloc CM x9 dans l'overworld + TESR portail visuel entre les piliers.

La structure du portail (9 couches, vue du dessus pour chaque couche) est documentée dans les commentaires de `TilePortalVoss.java`.
