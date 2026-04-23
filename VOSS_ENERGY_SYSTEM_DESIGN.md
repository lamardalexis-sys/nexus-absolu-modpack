# Voss Energy System — Design Dossier (BACKLOG)

**Statut** : Idée validée, implémentation reportée.
**Créé** : v1.0.282 (session fours T1-T5)
**Priorité** : Après Age 2 complet + autres idées Alexis en cours.

---

## Problème résolu

L'API Forge RF (Redstone Flux) utilise des `int` 32 bits partout :
- `int receiveEnergy(int maxReceive, boolean simulate)`
- `int getEnergyStored()`
- `int getMaxEnergyStored()`

=> **Plafond dur à Integer.MAX_VALUE = 2 147 483 647 RF** pour :
- Transport dans UN tick (débit câble)
- Stockage dans UN bloc (capacité)

Pour les quêtes endgame d'Alexis, **2.1 milliards RF ne suffit pas**.
Besoin potentiel : 10, 50, voire 100 milliards RF/tick.

## Précédent : comment Draconic Evolution contourne

Analyse du code source `brandon3055/Draconic-Evolution` :

### Leur stratégie à 2 niveaux

**Niveau 1 — Stockage interne (long 64-bit)**
```java
protected long valueStorage = 0;           // jusqu'à Long.MAX_VALUE = 9.2 quintillions
protected BigInteger overflowCount = ZERO; // rollover count pour encore plus
```

**Niveau 2 — API "OP" interne Draconic (long)**
Draconic a son propre interface `IOPStorage` :
```java
long receiveOP(long maxReceive, boolean simulate);
long extractOP(long maxExtract, boolean simulate);
long getOPStored();
long getMaxOPStored();
```
Les blocs Draconic (Energy Core, Pylon, Wireless Transceiver) se parlent
**entre eux** via cette API long-based.

**Niveau 3 — Bridge Forge RF (int, plafonné)**
Exemple TileEnergyPylon ligne 116 :
```java
public int getEnergyStored() {
    return (int) Math.min(getOPStored(), Integer.MAX_VALUE / 2);
}
public int receiveEnergy(int maxReceive, boolean simulate) {
    return (int) receiveOP(maxReceive, simulate);
}
```
**Hack** : quand Forge RF demande "combien tu stockes ?", ils mentent et
retournent au max `Integer.MAX_VALUE / 2`. Les machines RF externes
croient voir un stockage de 2.1B, la réalité interne est long/BigInteger.

### Conséquence observable

Les câbles externes (Flux Networks, Thermal Fluxduct) ne peuvent jamais
transférer plus de 2.1B RF/tick vers/depuis un Energy Core Draconic.
**Les connexions ultra-haut-débit se font UNIQUEMENT entre blocs Draconic**
via leur API OP privée (Pylon -> Core -> Stabilizer).

---

## Design proposé pour Nexus Absolu : "Voss Energy"

### Principe

Copier la stratégie Draconic : API parallèle long 64-bit, bridge Forge RF
pour compatibilité, mais **câbles Voss privés** pour les débits extrêmes.

### Nom : "Voss Energy" (VE)

Nommage in-universe cohérent avec la thématique Dr. Elias Voss. Unité :
- **VE** = 1 unité d'énergie Voss
- Conversion : 1 VE = 1 RF (échange 1:1 pour simplifier)
- Affichage GUI : formateur smart (1.5k VE, 2.3M VE, 9.2Qi VE pour quintillions)

### Composants à créer

#### 1. `IVossEnergyStorage` (capability Forge custom)
```java
package com.nexusabsolu.mod.api.energy;

public interface IVossEnergyStorage {
    long receiveVE(long maxReceive, boolean simulate);
    long extractVE(long maxExtract, boolean simulate);
    long getVEStored();
    long getMaxVEStored();
    boolean canReceiveVE();
    boolean canExtractVE();
}
```
+ CapabilityVossEnergy registration class (pattern Forge standard).

#### 2. `InternalVossEnergyStorage` (implémentation par défaut)
Équivalent long-based de notre `InternalEnergyStorage`.
- `long energy, capacity, maxReceive, maxExtract`
- Méthodes `drainInternal(long)`, `generateInternal(long)`, NBT serialize
- Si overflow prévu >Long.MAX_VALUE : ajout `BigInteger overflowCount` plus tard

#### 3. Bloc `stockage_voss` (batterie VE)
- Multiblock 1x1x1 à 3x3x3 selon taille désirée
- Tier 1 : capacité 10B VE
- Tier 2 : 100B VE
- Tier 3 : 1T VE (téra)
- Tier 4 (Pallanutro+ endgame) : 1Qi VE (quintillion)
- TileEntity avec GUI qui affiche stockage current + I/O live
- Accepte : côté "voss_in" (câble voss) ou côté "rf_in" (bridge RF)
- Exporte : côté "voss_out" ou "rf_out"

#### 4. Bloc `ligne_voss` (câble)
- Textures style IE/Mekanism mais thème industriel-vintage (rust patina)
- 3 tiers :
  - **Ligne Voss Standard** : 10M VE/tick
  - **Ligne Voss Haute Tension** : 1B VE/tick
  - **Ligne Voss Absolue** : Long.MAX_VALUE VE/tick (effectivement illimité)
- Connecte via capability `IVossEnergyStorage`
- Transport réseau (pattern Mekanism universal cable : grid network)

#### 5. Bloc `convertisseur_voss` (bridge RF <-> VE)
- 2 slots : input RF (Forge), output VE (custom) et vice-versa
- Conversion instantanée 1:1
- Throttle configurable (GUI) pour éviter de pomper tout le réseau
- Permet à un réacteur Mekanism (RF) de remplir une batterie Voss
  ET à une batterie Voss de sortir en RF pour nourrir machines tierces
- **PAS DE PLAFOND INT** sur le côté VE, plafond int côté RF (normal)

#### 6. Mise à jour fours Nexus
- TileFurnaceNexus implémente BOTH `IEnergyStorage` (Forge RF) ET `IVossEnergyStorage`
- Tiers Pallanutro / Infinite : leur `tier.baseMaxVEReceive()` = Long.MAX_VALUE
- Tiers bas : ne supportent que RF (pas besoin de VE)
- `getEnergyStored()` Forge RF continue à tricher comme Draconic si VE >2.1B

### Migration

- Tier 0-5 (Iron -> Vossium IV) : **restent en RF standard**. Pas de refactor.
- Tier 6-8 (Dark Astral, Gaia, Pallanutro) : **dual** RF + VE
- Tier 9 Infinite : **VE natif**, RF en fallback

### Quêtes futures qui deviennent possibles
- "Stocker 50 milliards VE dans un Stockage Voss Tier 3"
- "Alimenter la Machine Absolue à 10B VE/tick continu pendant 5 min"
- "Construire un réseau Ligne Voss Absolue de 100 blocs reliant 4 réacteurs fusion"
- "Convertir 1 quintillion RF en VE via 4 convertisseurs parallèles"

---

## Estimation coût implémentation

**Claude évalue : ~1500-2000 lignes Java + 300 lignes textures/models.**

Découpage Superpowers en 4 sprints indépendants :

### Sprint A — Core API (200 lignes)
- `IVossEnergyStorage`, `CapabilityVossEnergy`, `InternalVossEnergyStorage`
- Tests unitaires (au moins pour overflow Long.MAX_VALUE edges)
- Aucun bloc encore, juste l'API

### Sprint B — Stockage + GUI (600 lignes)
- `BlockStockageVoss` + 4 tiers
- `TileStockageVoss` avec NBT long-serialization
- `ContainerStockageVoss` + `GuiStockageVoss`
- Textures (side, top, front animated selon fill %)
- Formateur "k/M/B/T/Qi" pour afficher long lisiblement

### Sprint C — Câble + Réseau (500 lignes)
- `BlockLigneVoss` + 3 tiers
- `TileLigneVoss` (network pattern)
- `VossEnergyNetwork` (grid manager, similaire EnderIO/Mekanism)
- Rendu connecté (connected textures)

### Sprint D — Convertisseur + Integration fours (400 lignes)
- `BlockConvertisseurVoss` + GUI
- Update fours T6-T9 pour dual capability
- Quêtes qui utilisent le système
- Documentation in-game (Patchouli book entry)

---

## Prerequis avant de démarrer

- [ ] Age 2 complet et stable (Q97-Q126 designed + implemented)
- [ ] Sprint qid collision cleanup mergé sur main
- [ ] Alexis décide un scenario de quête concret qui justifie le système
      (pour dimensionner correctement les tiers et les débits)
- [ ] Décision : format d'affichage (Qi, long-français ?, scientifique ?)

---

## Références techniques

### Sources Draconic Evolution étudiées
- `TileEnergyCore.java` : `public OPStorageOP energy = new OPStorageOP(...)`
- `OPStorageOP.java` : long + BigInteger storage avec overflow
- `TileEnergyPylon.java` (L95-128) : bridge Forge RF avec cast plafonné

### Sources Mekanism étudiées pour pattern réseau câble
- `github.com/mekanism/Mekanism/tree/1.12` (déjà cloné à `/home/claude/mek-12`)
- Chercher : `UniversalCable`, `TileEntityUniversalCable`, `EnergyNetwork`

### Forge 1.12.2 capability system
- `CapabilityManager.INSTANCE.register()` pattern
- `hasCapability()` / `getCapability()` sur TileEntity
- `ICapabilityProvider` pour items si besoin

---

**Status actuel : BACKLOG — attendre décision Alexis pour démarrer Sprint A.**
