# Archives Voss — Système de Stockage Nexus (DESIGN FINAL v2)

**Statut** : Design finalisé après discussion Alexis (v1.0.297+).
**Priorité** : Prochain gros chantier après stabilisation fours.
**Inspiration** : Refined Storage (étudié) + vision Alexis (thématique Voss).

---

## Vision d'ensemble

Système de stockage centralisé **natif Nexus Absolu**, intégré à l'Age 1
du pack, concurrent accessible d'AE2 avec sa propre identité :
- **Multiblock industriel** visible (pas juste des câbles cachés)
- **Refroidissement fluide** thématique (cycle eau fermé avec perte 50 mB)
- **Télécommande inter-dimensions** qui contourne la limite Compact Machines
- **Autocraft simple** mais fonctionnel
- **Visuel style Dr. Voss** (industriel-vintage, rust patina)

---

## 1. Le Multiblock Central — "Archives Voss"

### Forme (8 blocs)

```
Vue latérale (coupe) :
         ███          <- Couche 2 (3 blocs, milieu)
       █████          <- Couche 1 (5 blocs, au sol)

Vue de dessus couche 1: A A A A A
Vue de dessus couche 2:   A A A

Structure en podium / T renversé.
```

### Composition des 8 blocs

**1 × Controller (centre de couche 2)** — `voss_archive_controller`
- Cerveau du multiblock. GUI dédié (autocraft + config + monitoring).
- Connecte le réseau de câbles/drives externes.
- Requiert RF + Eau Voss Froide fournie.

**2 × Frames (extrémités de couche 2)** — `voss_archive_frame`
- Structure/cosmétique.

**3 × Blocs thermiques (milieu de couche 1)** — `voss_archive_thermal_core`
- Où circule l'eau de refroidissement.

**2 × Ports latéraux (extrémités de couche 1)** — fixes par design
- **Port Input** d'un côté (ex. gauche).
- **Port Output** forcément de l'autre côté (ex. droite).
- **Réutilise les blocs `ItemInput` / `ItemOutput` déjà codés** → ZÉRO nouveau
  travail pour ces 2 blocs (juste les placer dans la structure).
- Contrainte : Input et Output **ne peuvent pas** être du même côté.

### Validation du multiblock

Le controller fait un scan à chaque placement/destruction :
- Vérifie les 8 blocs en forme correcte
- Vérifie Input d'un côté, Output de l'autre
- Si invalide : GUI bloqué, message "Structure incomplète"
- Si valide : `structureFormed = true`, réseau opérationnel

---

## 2. Refroidissement — Le Compresseur d'Eau Voss

### Le problème Minecraft

Vanilla water = 1 seul fluide, pas de notion "chaud/froid" et même
texture. Alexis l'a noté : "dans Minecraft je ne sais pas comment on
fait de l'eau chaude et la couleur reste la même".

### Solution : 2 fluides custom Nexus

**`nexusabsolu:eau_voss_froide`**
- Couleur bleu clair vif (RGB ~100, 200, 255).
- Produite par le Compresseur d'Eau Voss.
- Consommée par les Archives Voss.

**`nexusabsolu:eau_voss_chaude`**
- Couleur bleu grisé/ternie (RGB ~60, 100, 130).
- Sortie des Archives Voss après absorption de chaleur.
- Entrée du Compresseur pour être refroidie à nouveau.

### Cycle fermé avec 50 mB de perte

```
           ┌─── Eau Froide ────►[Archives Voss]
           │                          │
    [Compresseur Voss]◄── Eau Chaude ─┘
       + RF  + 50 mB Water vanilla (appoint)
```

**La perte de 50 mB par cycle** = le joueur doit périodiquement ajouter
de l'eau vanilla pour compenser. Crée une logistique active sans être
pénible (~1 bucket tous les 20 cycles).

### Le Compresseur d'Eau Voss (single block)

- **Input** : Eau Voss Chaude (cycle) OU Water vanilla (appoint)
- **Input** : Énergie RF
- **Output** : Eau Voss Froide
- Consomme 50 mB par cycle (la perte)
- Tier unique (Age 1 accessible, pas de progression tier ici)
- GUI simple : fluide in + RF bar + progress + fluide out

### Note pédagogique in-game

La quête d'introduction doit **expliquer clairement** le cycle au joueur
(sinon il va croire que c'est cassé quand il voit 2 fluides bleus
différents). Prévoir :
- Patchouli book entry "Hydraulique Voss"
- Tooltip sur le Compresseur expliquant le cycle
- Couleurs visuellement distinctes (important)

---

## 3. Disques & Drive

### Les 4 tiers de disques (progression x5)

| Tier | Nom | Capacité | Types d'items max | Gate |
|---|---|---:|---:|---|
| T1 | **Disque Voss Fragmentaire** | 500 | 27 | Age 1 early |
| T2 | **Disque Voss Stable** | 2 500 | 63 | Composé A |
| T3 | **Disque Voss Cohérent** | 12 500 | 127 | Composé C |
| T4 | **Disque Voss Absolu** | 62 500 | 255 | Composé E |

Double limite (style AE2) : capacité totale + types distincts. Ça empêche
de stocker 500 000 items répartis en 1000 types dans un T1.

### Drive Voss (le boîtier)

- **Single block** avec GUI de 10 slots disques
- Insérer/retirer disques à la main
- Consommation RF négligeable (~10 RF/tick)
- Placeable n'importe où sur le réseau (via câble)
- **Plusieurs drives** possibles sur un même réseau

**Exemple endgame** : 5 drives × 10 disques T4 = **3 125 000 items**
stockables. Largement suffisant pour un factory.

---

## 4. Auto-Craft (simple mais fonctionnel)

### Design : "RS-simplifié, sans chaînes récursives"

Alexis : "simple simple simple enfin comme même un peu complexe enfaite
mais pas trop dans l'abu".

→ On fait : patterns + crafter fonctionnel, mais **pas de craft récursif
automatique**. Si un ingrédient manque et qu'il a lui-même un pattern,
le joueur doit le crafter manuellement avant.

### 4 composants auto-craft

**1. Encodeur de Pattern** (`voss_pattern_encoder`) — single block
- GUI à 2 grilles : input 3x3 + output 1 slot
- Le joueur dépose ingrédients + résultat voulu → produit un **Item Pattern**
- L'item Pattern encode la recette en NBT

**2. Slots Pattern dans le Controller**
- Le Controller Archives a **9 slots Pattern** insérés dans son GUI
- Chaque pattern slot = 1 recette autocraftable
- Le joueur voit "Quels items sont craftables" dans son terminal

**3. Exécution du craft (dans le multiblock)**
- Terminal de Craft → joueur clique "Crafter 64 Iron Pickaxes"
- Système vérifie pattern : "j'ai besoin de 3 iron + 2 stick par pickaxe"
- Pull des ingrédients depuis le stockage
- Progress bar + production + delivery
- Si ingrédient manquant → erreur "il faut plus de X"

**4. Accélérateur de Craft** (`voss_craft_accelerator`) — single block
- Stackable jusqu'à **8** (comme Speed Boosters fours)
- Chaque accélérateur = +25% vitesse autocraft
- Consomme RF proportionnellement à la vitesse
- Max 8 = craft ×3.0 plus rapide

### Terminal de Craft (bloc séparé)

- `voss_crafting_terminal` — single block posable partout sur le réseau
- GUI avec liste des patterns encodés
- Clic sur un item → popup quantité → launch
- Progress bar pendant le craft
- Items livrés dans l'inventaire joueur quand prêts

---

## 5. Terminal de Stockage (Grid RS-style)

### Features portées de RS (étudié dans `/home/claude/rs-src/refinedstorage`)

- **Barre de recherche** en haut du GUI
- **Grille d'items** 9×4 visible (avec scrollbar si plus)
- **Tri configurable** (nom / quantité / ID / mod)
- **Affichage quantité condensé** ("1.2k" au lieu de "1234", "5.5M" pour millions)
- **Clic gauche** = prendre 1 stack complet
- **Shift+clic** = prendre 1 stack + déposer dans inventaire direct
- **Clic droit** = prendre 1 seul item
- **Scroll wheel** au-dessus d'un item = ±1 item
- **Hover tooltip** enrichi (nom + mod + quantité totale + répartition par disque)

### Implementation technique

- Packet sync client↔serveur efficace (delta packets uniquement)
- ContainerListener pattern (refresh que quand changement réel)
- GUI renderer adapté de `GuiGrid.java` de RS
- Typage Java générique pour réutiliser avec Terminal de Craft

---

## 6. Télécommande Voss (wireless)

### Fonctionnalités

- Item tenu en main ou offhand
- **Clic droit en l'air** → ouvre le Terminal de Stockage à distance
- Fonctionne **dans n'importe quelle dimension** (Overworld, Nether, Compact Machines, Twilight, End, etc.)
- Accès à **tous les réseaux Voss liés** à cette télécommande

### Liaison réseau

- **Shift+clic droit** sur un Controller = lier ce réseau
- L'item stocke les UUID des réseaux liés dans NBT
- GUI sélecteur si plusieurs réseaux liés (style AE2 Wireless Terminal)
- Option "délier" via GUI

### Énergie (anti-OP)

- Capacité interne : 100 000 RF
- Coût par ouverture : 1 000 RF
- Coût par tick d'utilisation active : 50 RF
- Recharge via n'importe quel bloc RF externe (wireless chargeur, Energy Cell posée, etc.)

**Balance** : ~100 ouvertures sans recharge = confortable mais pas illimité.

---

## 7. Communication Inter-Compact Machines

### Solution : "Lien Dimensionnel Voss"

- Bloc single : `voss_dimension_link`
- Se pose dans un réseau Voss
- On lui donne une **Clé de Liaison** (item) encodée avec les coordonnées d'un autre Lien
- Deux Liens jumelés → les 2 réseaux se partagent les items/autocrafts
- 1 Lien peut partager avec **jusqu'à 8 autres Liens**
- Consomme **500 RF/tick par lien actif**

### Cas d'usage typique

- **CM "Base"** : stockage principal, 50 disques T4, autocraft
- **CM "Farm A"** : produit du Composé A en masse → envoie tout au stockage
- **CM "Farm B"** : produit du Composé C → idem

→ Un Lien dans chaque CM, tous jumelés au Lien de la Base.
→ La Télécommande en Overworld voit **tous** les items des 3 CM ensemble.

### Technique

- `TileEntity` sauvegardé par dimension (NBT dim ID + UUID)
- Controller maintient `linked_dimensions[]`
- Au GUI access : fusion virtuelle des listes d'items des réseaux liés
- Les transferts sont **logiques** (pas de téléportation physique d'item entity)

---

## 8. Câbles Voss

- **Single block** : `voss_cable`
- Connecte les composants du réseau (controller, drives, terminals, IO, etc.)
- Aucune consommation RF
- **Connected textures** (détection voisinage pour visuel continu)
- 1 seul tier (simplifie vs RS qui a cable/constructor/destructor/cover)

---

## 9. Import / Output avec filtres

### Réutilisation blocs existants

Les blocs `ItemInput` et `ItemOutput` sont **déjà codés** dans le pack
(confirmé par grep `mod-source/src/main/java/com/nexusabsolu/mod/blocks/`).
Ils ont déjà la gestion de faces (SideConfig standardisé).

### Extension nécessaire (~200 lignes chaque)

**ItemInput — nouveau GUI filtres** :
- 9 slots de filtres (ghost items, pas de consommation)
- Mode **whitelist** (seulement ces items passent) ou **blacklist** (tout sauf eux)
- Toggle "ignore NBT" (match par ID/meta only) ou "match NBT" (strict)
- Direction : inventaire externe → réseau Voss

**ItemOutput — nouveau GUI filtres** :
- Idem (9 slots + whitelist/blacklist + NBT toggle)
- Direction : réseau Voss → inventaire externe

---

## 10. Progression & Gating (quêtes)

### Age 1 (early)

- **Quête "Hydraulique Voss"** → débloque Compresseur d'Eau + recette
- **Quête "Les Archives"** → débloque multiblock 8 blocs
- **Quête "Premier Disque"** → Disque T1 Fragmentaire
- **Quête "Câbles et Liens"** → câbles + extension Input/Output filtres

### Age 1 (mid)

- **Quête "Stockage Stable"** → Disque T2 (gate Composé A)
- **Quête "Drive Industriel"** → Drive Voss 10 slots
- **Quête "Automatisation Voss"** → Encodeur de Pattern + 1er autocraft

### Age 2

- **Quête "Résonance Voss"** → Disque T3 Cohérent (Composé C)
- **Quête "Terminal Portable"** → Télécommande Voss
- **Quête "Accélération"** → Accélérateur de Craft
- **Quête "Craft Distribué"** → Terminal de Craft séparé

### Age 3+

- **Quête "Absolu"** → Disque T4 (Composé E)
- **Quête "Dimensions Voss"** → Lien Dimensionnel Voss
- **Quête "Réseau Total"** → connecter plusieurs CM

**Total ~15 quêtes** réparties sur 3 Ages.

---

## 11. Plan Superpowers — 7 Sprints

### Sprint 1 — Multiblock & Refroidissement (~800 lignes)

**Livrable** : multiblock Archives valide sa structure, Compresseur d'Eau
convertit les fluides, 2 fluides visibles en jeu.

- `BlockArchiveController` + TileEntity (scan 8 blocs)
- `BlockArchiveFrame` + `BlockArchiveThermalCore`
- Fluides `eau_voss_froide` + `eau_voss_chaude` (registration + textures)
- `BlockCompresseurEau` + GUI basique
- NBT serialization multiblock state

### Sprint 2 — Network + Câbles + IO filtres (~700 lignes)

**Livrable** : réseau scanné depuis Controller, IO push/pull avec filtres.

- `INexusNetworkNode` interface
- `BlockVossCable` + TileEntity (BFS scan)
- Extension `ItemInput` / `ItemOutput` avec GUI 9 slots filtres
- Capability Forge pour `IItemHandler` sur Controller

### Sprint 3 — Disques + Drive (~600 lignes)

**Livrable** : 4 tiers de disques craftables, Drive 10 slots fonctionnel.

- `ItemDisqueVoss` × 4 tiers (capacité + types + NBT items stockés)
- `BlockDriveVoss` + GUI 10 slots
- Fill policy (round-robin entre disques)
- Tests NBT : persister 500+ items puis reload

### Sprint 4 — Terminal de Stockage RS-style (~900 lignes)

**Livrable** : GUI visuel des items, search/sort/extract.

- `BlockTerminalStockage` + TileEntity
- `GuiTerminalStockage` (inspiré `GuiGrid.java` RS)
- `ContainerTerminalStockage` + delta sync packets
- ContainerListener pattern pour efficient refresh

### Sprint 5 — Auto-Craft (~900 lignes)

**Livrable** : encoder pattern + lancer craft via terminal dédié.

- `ItemPattern` NBT-encoded
- `BlockPatternEncoder` + GUI 2 grilles
- `BlockCraftAccelerator` (stackable ×8)
- `BlockTerminalCraft` + GUI liste patterns
- Engine d'exécution (pull ingredients + progress + delivery)

### Sprint 6 — Télécommande + Inter-CM (~700 lignes)

**Livrable** : télécommande accessible Overworld↔CM, Lien Dimensionnel
fonctionnel.

- `ItemTelecommandeVoss` NBT RF + linked networks + GUI selector
- Bind/unbind logic (shift+click Controller)
- `BlockLienDimensionnel` + `ItemCleDimensionnelle`
- Cross-dim network merging (virtual item list fusion)

### Sprint 7 — Polish (~400 lignes)

**Livrable** : système pleinement jouable et intégré.

- Textures PNG style Fourneaux Voss (rust patina thème)
- Modèles JSON (connected textures câbles, multiblock complet)
- Recettes CraftTweaker (`scripts/Age1_Storage.zs`)
- Quêtes BetterQuesting (~15 quêtes Ages 1-3)
- Patchouli book entries
- Balance testing

### Total estimé

- **~5000 lignes Java** + ressources + docs
- **7 sprints** = 25-30 sessions de dev focus
- **Temps calendaire** : 2-3 mois à rythme normal

---

## 12. Interdépendances & Découplages

### ❌ Pas de dépendance à Voss Energy (VE)

Alexis a explicitement choisi **RF standard**. Donc le système Archives
Voss peut être codé **sans attendre** l'implémentation du système VE.
Les deux projets sont découplés.

### ✅ Dépendance à Compact Machines (déjà installé)

Le mod `compactmachines3-1.12.2-3.0.18-b278.jar` est dans le pack.
Les Liens Dimensionnels utilisent son API (ou au pire : NBT dim ID direct).

### ✅ Dépendance aux composés Nexus existants

Composé A/C/E utilisés comme gates pour les tiers de disques. Ces composés
existent déjà dans le mod (vérifié `ModItems.java`).

### ✅ Réutilisation Blocs existants du pack

- `ItemInput` et `ItemOutput` (déjà codés, juste extension filtres)
- SideConfig standardisé (déjà utilisé par les fours, labels Ba/H/Ar/Av/Ga/Dr)
- Patterns `GuiUtils.formatRf`, `InternalEnergyStorage` (déjà existants)

---

## 13. Prochaines étapes

1. **Alexis valide** ce design doc final (relire, ajuster si besoin)
2. **Création branche** `feature/archives-voss` depuis main
3. **Sprint 1** : commencer par multiblock + fluides custom
4. **Commits réguliers**, tests in-game à chaque sprint
5. **Merge main** uniquement quand un sprint est stable et testé

**Statut actuel** : DESIGN VALIDÉ — prêt à démarrer quand Alexis dit GO.

---

## 14. Points ouverts / à décider plus tard

- Nom exact du Compresseur (Compresseur vs Détendeur Cryogénique)
- Textures finales (cohérence avec quelle palette Voss ?)
- Balance exacte RF par opération (tuning en testing)
- Sound effects custom ou vanilla
- Optimisations réseau (si réseau > 100 blocs lag, à voir)
- Phase 2 autocraft récursif (optionnel post-MVP)
- Phase 2 fluid support dans le stockage (optionnel post-MVP)
