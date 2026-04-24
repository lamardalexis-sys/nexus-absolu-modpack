# Nexus Storage System — Design Document (BACKLOG)

**Statut** : Design en cours — inspiration Refined Storage étudié.
**Créé** : v1.0.297 (session fours)
**Priorité** : À démarrer quand Alexis le décide (post Age 2 complet probable).

---

## Objectif

Créer un **système de stockage centralisé natif Nexus Absolu**, alternatif à
AE2 et Refined Storage (que Alexis refuse d'ajouter), intégré thématiquement
à l'univers du Dr. Voss et au système Voss Energy (VE) prévu.

Principe de design : **apprendre de RS, simplifier drastiquement**.

---

## Analyse Refined Storage (source github.com/refinedmods/refinedstorage)

**Taille totale RS** : 641 fichiers Java, 55 164 lignes (8+ ans de dev)

### Architecture RS (les concepts fondamentaux)

RS se découpe en 4 concepts :

**1. Network** (`INetwork`) — le cerveau
- Un bloc "Controller" qui représente le réseau
- Scan des blocs connectés (BFS depuis le controller)
- Maintient une liste de tous les nœuds connectés
- Gère l'énergie consommée par tous les nœuds

**2. Network Node** (`INetworkNode`) — tous les blocs du réseau
- Chaque bloc du réseau implémente cette interface
- Un câble, un disk drive, un importer, un exporter = tous NetworkNode
- Chacun consomme X énergie par tick
- Chacun peut "visit" ses voisins pour étendre le réseau

**3. Storage** (`IStorage`) — stockage physique
- Représente 1 unité de stockage (cell, external inventory, etc.)
- Methods `insert(stack)`, `extract(stack, size)`, `getStored()`
- Plusieurs storages = plusieurs disks dans un drive

**4. Grid** (`IGrid`) — interface utilisateur
- Le GUI que le joueur ouvre
- Accès au réseau depuis l'interface
- Search, filter, tri, craft

### Ce qu'on GARDE de RS (bonnes idées)

- **Séparation Network / Node / Storage** : clean et scalable
- **BFS pour découvrir le réseau** depuis un Controller
- **NBT serialization du graphe** (persistance entre sessions)
- **Capability système Forge** (meilleure intégration que custom interface)

### Ce qu'on NE GARDE PAS (sur-ingénierie pour notre cas)

- **Autocrafting Manager** (ICraftingManager, ~2000 lignes RS) → trop complexe,
  on laisse AE2 être le "meilleur système autocraft"
- **Security Manager** (ISecurityManager) → pas de multi-joueur security layer
- **Wireless Grid** (IWirelessTransmitter) → nice-to-have, pas MVP
- **External Storage** (adapter vers inventaires externes) → phase 2
- **Fluid support** → phase 2 (items only pour MVP)

---

## Design Nexus Storage System

### Thème & narrative

**"Archives Quantiques du Dr. Voss"** (ou "Matrix Voss") :
- Dr. Voss a découvert une technologie de stockage dans son exploration dimensionnelle
- Les items sont "déphasés" dans une dimension de poche stable
- Besoin de Voss Energy pour maintenir la cohésion quantique
- Aesthétique industrielle-vintage cohérente avec les Fourneaux Voss

### Progression — Débloqué Age 3

- **Age 0-2** : joueur utilise des coffres vanilla + Storage Drawers (existant)
- **Age 3 early** : Alexis introduit le système Nexus Storage (alternative accessible)
- **Age 3 mid** : les Archives Quantiques deviennent le backbone du pack
- **Age 4+** : AE2 reste disponible pour ceux qui veulent autocraft avancé

Le Nexus Storage est un **choix** entre AE2 (power gamer, complexe) et Nexus
(plus intégré narrativement, plus simple, pas d'autocraft).

### MVP (5 blocs + 1 item)

#### 1. **Cœur des Archives** (`nexus_archive_core`)
- Bloc controller central du réseau
- Stocke l'état global (liste des items), consomme VE (Voss Energy) passive
- GUI basique (voir items, config)
- Un seul par réseau (détection de conflit si 2 cœurs connectés)

#### 2. **Cellule Quantique** (`nexus_quantum_cell`)
- 4 tiers de capacité :
  - T1 Cellule Basique : 1k items (64 stacks)
  - T2 Cellule Stable : 8k items
  - T3 Cellule Améliorée : 64k items
  - T4 Cellule Absolue : 256k items
- Peut contenir jusqu'à **N types d'items différents** (pas infini comme AE2)
- Tier 4 absolu : 1024 types d'items

#### 3. **Conduit Quantique** (`nexus_quantum_conduit`)
- Câble qui connecte les blocs du réseau
- Pas de coût en VE (passif)
- Style visuel cohérent avec Fourneaux Voss (rust patina)

#### 4. **Terminal d'Archives** (`nexus_archive_terminal`)
- Interface d'accès au réseau (search + extract)
- Un GUI simple avec barre de recherche
- Peut être placé n'importe où sur le réseau
- Pas de wireless (= doit être posé physiquement connecté)

#### 5. **Interface E/S** (`nexus_archive_io`)
- Un bloc avec 6 faces configurables (input / output / none)
- Utilise déjà la convention SideConfig du pack (cohérence UX)
- Un côté input tire d'un inventaire externe, un côté output pousse
- Même système de face labels que les Fourneaux Voss

#### 6. **Item : Clé d'Archives** (`nexus_archive_key`)
- Item qui permet de "lier" un réseau à un autre bloc (ex: coffre externe)
- Shift+click sur Cœur → clé mémorise le réseau
- Click sur inventaire externe → ajouté au réseau comme "external storage"

### Balance par rapport à AE2

| Aspect | AE2 | Nexus Storage |
|---|---|---|
| Courbe d'apprentissage | ⭐⭐⭐⭐⭐ très haute | ⭐⭐ basse |
| Autocraft | ✅ Complet, puissant | ❌ Absent (MVP) |
| Capacité | Quasi infinie (types) | 4 tiers + plafond types |
| Power | RF | **VE (Voss Energy)** |
| Wireless | ✅ Nombreux adapters | ❌ MVP local only |
| Conduits | Multi-type, complexe | 1 type simple |
| Storage externe | ✅ | ✅ via clé |

Clairement le Nexus Storage est **"AE2 simplifié"**, pas "AE2 en mieux".
Positionnement : 90% des joueurs Nexus l'adopteront, les power gamers
continueront AE2 pour l'autocraft.

### Intégration Voss Energy

Le Nexus Storage serait **la première utilisation pratique** du système VE
(currently in `VOSS_ENERGY_SYSTEM_DESIGN.md` backlog).

Consommation VE par réseau :
- Cœur : 50 VE/tick passive
- Par cellule : +10 VE/tick
- Par terminal actif : +20 VE/tick pendant recherche
- Par IO actif : +30 VE/tick pendant transfert

Donc un réseau moyen (1 cœur + 5 cellules + 1 terminal + 2 IO) consomme :
50 + 50 + 20 + 60 = **180 VE/tick** (~360 RF/tick converti, très gérable).

**Interdependance forte** : pas de Nexus Storage sans VE opérationnel.
Alexis voudra peut-être **coder VE avant Storage** pour cette raison.

---

## Plan d'implémentation Superpowers (MVP)

### Sprint S1 — API & Network Core (~500 lignes, 3-4 sessions)
**Livrable** : le controller scanne le réseau, garde une liste de blocs connectés.

- `INetworkNode` interface
- `BlockArchiveCore` + `TileArchiveCore`
- `BlockArchiveConduit` + `TileArchiveConduit`
- Algorithme BFS de scan + invalidation sur break
- Tests manuels : poser cœur + câbles + vérifier que le cœur "voit" les câbles

### Sprint S2 — Cellules & Storage (~600 lignes, 3-4 sessions)
**Livrable** : on peut stocker des items dans des cellules connectées au cœur.

- `IStorage` interface (nexus)
- `BlockQuantumCell` + `TileQuantumCell` x 4 tiers
- Capability Forge `ItemHandler` pour chaque cellule
- Ajout / retrait d'items (via commande debug au début)
- NBT serialization

### Sprint S3 — Terminal GUI (~700 lignes, 4-5 sessions)
**Livrable** : joueur ouvre un terminal, voit les items, peut les extract.

- `BlockArchiveTerminal` + `TileArchiveTerminal`
- `GuiArchiveTerminal` avec barre recherche + scroll
- `ContainerArchiveTerminal` avec network sync
- Icons custom + texture tooltips
- Packet sync client-serveur pour refresh list

### Sprint S4 — IO + Clé (~400 lignes, 2-3 sessions)
**Livrable** : auto-import/export via blocs IO + linking via clé.

- `BlockArchiveIO` + `TileArchiveIO` avec SideConfig réutilisé
- `ItemArchiveKey` avec NBT storing coordinates
- Handler pour external storage

### Sprint S5 — Polish + recettes + quêtes (~300 lignes, 2-3 sessions)
- Textures PNG finalisées (style Fourneaux Voss)
- Recettes CraftTweaker
- Quêtes BetterQuesting (5-10 quêtes "Archives Quantiques")
- Patchouli book entry

**Total estimé MVP** : ~2500 lignes Java + ~400 lignes ressources + docs.
**Temps estimé** : 15-20 sessions de dev focus (~3-4 semaines à rythme normal).

---

## Prérequis avant de démarrer

- [ ] Voss Energy system implémenté (ou au moins Sprint A de son design doc fait)
- [ ] Age 2 complet et stable (pour que le storage soit vraiment utile)
- [ ] Décision Alexis sur : garder ou pas l'autocraft dans une v2 plus tard

---

## Prochaines étapes (quand on lance)

1. Commencer Sprint S1 : créer le package `com.nexusabsolu.mod.storage`
2. Implémenter `INetworkNode` + `BlockArchiveCore` minimal
3. Algo BFS de scan réseau
4. Tests manuels in-game avant de passer au S2

**Status actuel : BACKLOG** — attendre feu vert Alexis pour démarrer.
