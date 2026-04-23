# NuclearCraft — Les FUELS expliqués

Guide pédagogique pour comprendre les combustibles de fission NC.

---

## Vue d'ensemble en 1 minute

NC a **9 "familles" de fuel solide**. Chaque famille = un élément radioactif
différent, qu'on obtient via chaîne d'enrichissement.

```
Thorium      →  facile à trouver, fuel débutant
Uranium      →  le classique (comme réacteur réel)
Neptunium    →  intermédiaire, obtenu en recyclant
Plutonium    →  fort, obtenu en breeding
Mixed (MOX)  →  mélange Pu+U, recyclage du spent
Americium    →  avancé
Curium       →  avancé
Berkelium    →  endgame
Californium  →  endgame ultime
```

Chaque famille se décline en **plusieurs variants** selon 2 dimensions, pour un
total de ~60 fuels distincts. On va voir ces dimensions.

---

## Anatomie du NOM d'un fuel

Un nom comme `HEU_235_OX` se décompose en 3 parties :

```
   HEU    _    235    _    OX
   ▲            ▲           ▲
   │            │           │
   │            │           └─ Forme chimique (TR / OX / NI / ZA)
   │            └─ Isotope (233 ou 235 pour Uranium par exemple)
   └─ Enrichissement (LEU ou HEU)
```

### 1) Préfixe : niveau d'enrichissement

| Préfixe | Nom complet | Meaning |
|---|---|---|
| `TBU`  | Thorium-Based Uranium | pour Thorium uniquement (fuel de base) |
| `LEU`  | Low-Enriched Uranium  | enrichissement faible (~19% fissile) |
| `HEU`  | High-Enriched Uranium | enrichissement fort (~90% fissile) |
| `LEN`  | Low-Enriched Neptunium| idem pour Np |
| `HEN`  | High-Enriched Neptunium | idem pour Np |
| `LEP`  | Low-Enriched Plutonium | idem pour Pu |
| `HEP`  | High-Enriched Plutonium | idem pour Pu |

**Règle simple** :
- LEU/LEN/LEP = cool plus froid, moins de chaleur/tick, dure plus longtemps,
  moins efficace → **safer pour débuter**
- HEU/HEN/HEP = très chaud, très efficace, mais très vite consommé
  → **endgame optimisé**

### 2) Isotope (uniquement Uranium, Neptunium, Plutonium)

- **Uranium** : `233` (fissile artificiel, ex-thorium) ou `235` (fissile naturel)
- **Plutonium** : `239` (principal fissile Pu) ou `241` (alternatif)
- **Neptunium** : pas d'isotope (juste LEN / HEN)
- **Thorium** : pas d'isotope (juste TBU)

### 3) Suffixe : forme chimique (Tri-Carbide / Oxide / Nitride / Zirconium Alloy)

C'est le vrai game-changer balance :

| Suffixe | Nom complet | Propriétés | Meaning gameplay |
|---|---|---|---|
| `TR` | Tri-Carbide (TRISO)  | base, équilibré | **fuel par défaut** |
| `OX` | Oxide                | +25% durée, −20% chaleur, +23% criticalité | **dure plus, mais plus difficile à démarrer** |
| `NI` | Nitride              | −20% durée, +25% chaleur, −33% criticalité | **plus rapide, plus chaud, plus facile à démarrer** |
| `ZA` | Zirconium Alloy      | +25% durée, −20% chaleur, +17% criticalité | **comme Oxide, démarrage +** |

**Analogie simple** :
- **TR** = fuel normal d'une voiture
- **OX** = diesel (économique, démarre plus dur)
- **NI** = essence super (puissant, consommé vite, démarre facile)
- **ZA** = version alternative du diesel

---

## Les 4 stats qui définissent un fuel

Chaque fuel a ces 4 valeurs (dans `config/nuclearcraft/nuclearcraft.cfg`) :

### Stat 1 — **fuel_time** (durée)
Combien de ticks le fuel dure. 20 ticks = 1 seconde.

Exemples :
- `TBU_TR` = 14 400 ticks = **12 minutes réelles**
- `LEU_235_TR` = 4 800 ticks = **4 minutes**
- `HEU_235_TR` = 1 600 ticks = **1m20s**

→ **Plus le fuel est puissant, plus il dure PEU.** Logique : on brûle plus vite
  ce qui est plus énergétique.

### Stat 2 — **heat_generation** (chaleur/tick)
Combien de H/t (heat per tick) produit UN cell avec CE fuel dedans.

Exemples :
- `TBU_TR` = **40 H/t** (doux)
- `LEU_235_TR` = **120 H/t** (modéré)
- `HEU_235_TR` = **360 H/t** (brûlant)

→ Plus de heat = plus de RF possible MAIS il faut que ton cooling suffise,
  sinon le réacteur **explose littéralement** quand heat_capacity est dépassée.

### Stat 3 — **efficiency** (multiplier)
Coefficient de conversion heat → RF effectif.

- `TBU_TR` = 1.25 (bonus débutant : 25% de RF en plus "gratuitement")
- `LEU_235_TR` = 1.0 (baseline)
- `HEU_235_TR` = 1.05 (légèrement mieux)

→ Un fuel à efficiency 1.25 produit plus de RF que sa heat brute suggèrerait.

### Stat 4 — **criticality** (seuil d'allumage)
Combien de **flux de neutrons** il faut pour que le fuel "s'allume" et se maintienne.

Exemples :
- `TBU_TR` = 199 flux (assez élevé, débutant peut galérer à allumer)
- `HEU_235_TR` = 87 flux (facile à allumer avec HEU)
- `HEU_235_NI` = 87/2 = **44 flux** (très facile)

→ Le flux vient des **moderators** (graphite, beryllium, heavy water) PLACÉS
  ENTRE 2 cells. Le réacteur calcule : "combien de flux de neutrons frappe
  cette cell depuis ses voisines via les moderators ?". Si < criticality,
  rien ne se passe. Si ≥ criticality, **BOOM réaction en chaîne démarre**.

### Stat 5 bonus — **self_priming** (true/false)
Si `true`, le fuel s'allume tout seul sans avoir besoin de flux externe.
Seuls Cm, Bk, Cf l'ont. **Très pratique endgame** : petit réacteur 3×3×3
peut marcher sans moderator.

---

## Exemple concret : les 4 Thorium fuels

Config par défaut v2o.9.3 :

| Fuel | fuel_time | heat_gen | efficiency | criticality | self_priming |
|---|---:|---:|---:|---:|---:|
| **TBU_TR** | 14 400 t (12min) | 40 H/t  | 1.25 | 199 | non |
| **TBU_OX** | 18 000 t (15min) | 32 H/t  | 1.25 | 234 | non |
| **TBU_NI** | 11 520 t (9m30s) | 50 H/t  | 1.25 | 199 | non |
| **TBU_ZA** | 18 000 t (15min) | 32 H/t  | 1.25 | 234 | non |

**Lecture de tableau** :
- Tu veux **doux et long** ? → **TBU_OX** (15 min, froid 32 H/t)
- Tu veux **réactif et chaud** ? → **TBU_NI** (9m30s mais 50 H/t)
- Tu es débutant ? → **TBU_TR** (équilibré, le plus standard)

Tous les TBU ont efficiency 1.25 = ils rendent tous très bien le heat en RF
quand tu as une turbine derrière.

---

## Exemple concret : les 16 Uranium fuels

C'est là que ça devient riche. 4 préfixes × 4 suffixes = 16 variants :

| Variant | fuel_time | heat_gen | efficiency | criticality |
|---|---:|---:|---:|---:|
| LEU_233_TR | 2666 | 216 | 1.10 | 66 |
| LEU_233_OX | 2666 | 216 | 1.10 | 78 |
| LEU_233_NI | 3348 | 172 | 1.10 | 98 |
| LEU_233_ZA | 2134 | 270 | 1.10 | 66 |
| HEU_233_TR | 2666 | 648 | 1.15 | 33 |
| HEU_233_OX | 2666 | 648 | 1.15 | 39 |
| HEU_233_NI | 3348 | 516 | 1.15 | 49 |
| HEU_233_ZA | 2134 | 810 | 1.15 | 33 |
| LEU_235_TR | 4800 | 120 | 1.00 | 87 |
| LEU_235_OX | 4800 | 120 | 1.00 | 102 |
| LEU_235_NI | 6000 | 96 | 1.00 | 128 |
| LEU_235_ZA | 3840 | 150 | 1.00 | 87 |
| HEU_235_TR | 4800 | 360 | 1.05 | 43 |
| HEU_235_OX | 4800 | 360 | 1.05 | 51 |
| HEU_235_NI | 6000 | 288 | 1.05 | 64 |
| HEU_235_ZA | 3840 | 450 | 1.05 | 43 |

**Observations importantes** :
- **U-235 > U-233** en durée (4800 vs 2666), mais moins de heat/tick
- **U-233** demande moins de flux pour s'allumer (critical 66) vs U-235 (87)
- **HEU versions** : ×3 de heat de la LEU équivalente, même durée, meilleure efficiency
- **NI versions** : durée +25%, heat -20% (fuel "lent mais doux")
- **ZA versions** : durée -20%, heat +25% (fuel "rapide mais chaud")

---

## Comment on OBTIENT un fuel (chaîne d'enrichissement)

C'est la partie qui effraie les débutants. Voici la chaîne simplifiée :

### Niveau 1 — Débutant : Thorium

```
Thorium Ore
    │ (Manufactory : broie)
    ▼
Thorium Dust
    │ (Furnace : fond)
    ▼
Thorium Ingot  ←── c'est ça qu'on a comme métal
    │ (Centrifuge : sépare les isotopes)
    ▼
Th-232 Dust (99%)  +  Th-230 Dust (1%)
    │
    │ (Assembler : combine + forme pellet)
    ▼
TBU Pellet (fuel prêt à charger dans cell)
    │
    │ (Pressurizer : forme finale pellet)
    ▼
TBU_TR / TBU_OX / TBU_NI / TBU_ZA (selon recipe choisie)
```

### Niveau 2 — Intermédiaire : Uranium

```
Uranium Ore
    │ (Manufactory)
    ▼
Uranium Dust
    │ (Centrifuge)
    ▼
U-238 Dust (99.3%)  +  U-235 Dust (0.7%)
         │                  │
         │                  │ (déjà fissile)
         │                  ▼
         │                  LEU-235 / HEU-235 (selon concentration)
         │
         │ (Neutron Irradiator : capture neutrons → devient Np-239 → Pu-239)
         ▼
         Pu-239 Dust
            │
            ▼
            LEP-239 / HEP-239 fuel
```

### Niveau 3 — Recyclage : MOX

```
Depleted Fuel (sortie d'un réacteur ayant consommé un fuel)
    │ (Fuel Reprocessor)
    ▼
4 sorties séparées :
  - Plutonium pur (à réutiliser)
  - Uranium résiduel
  - Neptunium bonus
  - Strontium (déchet pour RTG ou radioisotope)

Plutonium recyclé + Uranium = MOX (Mixed Oxide) fuel
```

### Niveau 4 — Endgame : Actinides lourds

```
Cf-252 source (produit via irradiation massive)
    │
    ▼
Cf-250 (self-priming, pas besoin moderator)
    │
    ▼
Cf-251 (le plus puissant fuel du mod)
```

---

## Progression "pédagogique" pour le joueur

Dans ton Age 3 Nuclear Line, voici l'ordre logique :

### Étape 1 — **TBU_TR** (Thorium-Based Uranium, Tri-Carbide)
- C'est le fuel **TUTORIEL**
- Facile à fabriquer (thorium > dust > pellet)
- Dure 12 minutes (joueur a le temps d'apprendre)
- Heat modéré (40 H/t) = cooling simple avec quelques water/iron heatsinks
- Efficiency 1.25 = bon RF/t
- Criticality 199 = **requiert moderators** → apprend le joueur à en placer

**Recette typique Nexus** :
`TBU_TR = 9 Thorium Ingot + 1 Tri-Carbide = 1 TBU_TR Pellet`

### Étape 2 — **LEU_235_TR** (Low-Enriched Uranium, Tri-Carbide)
- Après avoir centrifugé Uranium Ore
- Criticality 87 = plus facile à allumer que TBU
- Heat 120 H/t = nécessite plus de cooling
- Dure 4 minutes = plus dynamique

**Gate** : nécessite un Centrifuge fonctionnel = skill acquis

### Étape 3 — **MOX-239_TR** (Mixed Oxide de Plutonium)
- Après avoir fait tourner un réacteur et récolté le Depleted Fuel
- Recyclage obligatoire = apprend le Fuel Reprocessor
- Propriétés intermédiaires entre U et Pu purs

**Gate** : nécessite avoir complété un cycle fuel → déchet → recyclage

### Étape 4+ — **HEU_235_NI** / **HEP_239_NI** (endgame)
- Très chaud (288-400 H/t pour HEU_235_NI)
- Criticality bas (64 et moins) = s'allume facile
- Pour ceux qui ont maîtrisé le cooling optimal
- Préparation à la fusion

### Endgame — **Cf-251_OX** (self-priming)
- Pas besoin de moderator = compact 3×3×3 viable
- Ultra-chaud
- Le "nuke portable"

---

## Ce qui arrive quand un fuel est ÉPUISÉ

Quand `fuel_time` est atteint, la cell vide son contenu et produit le **Depleted Fuel**
correspondant :

```
TBU_TR (full)  →  (12 minutes plus tard)  →  Depleted TBU
LEU_235_TR     →  Depleted LEU-235
HEU_233_NI     →  Depleted HEU-233
```

Le Depleted Fuel **ne se consomme pas tout seul**. Il ne fait rien dans une cell.
MAIS tu peux :
1. Le mettre dans le **Fuel Reprocessor** → récupère Pu, U, Np, Sr
2. L'utiliser comme RTG Pellet pour générateur passif (chaleur résiduelle)
3. Le stocker dans un Radiation Scrubber pour gérer les déchets

---

## Résumé en une phrase

**Un fuel NC = 9 familles × 2 enrichissements × (2 isotopes si applicable) × 4 formes chimiques = ~60 fuels distincts, chacun avec 4 stats (durée, chaleur, efficience, seuil d'allumage), qu'on obtient via une chaîne d'enrichissement : mining → manufactory → centrifuge → assembler → pressurizer.**

---

## Pour ton Age 3 Nexus — recommandations

1. **Ne débloque pas tous les fuels d'un coup**. Force TBU_TR d'abord, puis LEU_235_TR,
   puis MOX, puis HEU, etc. via quêtes.

2. **Configure TBU_TR pour durer plus** (ex. 28 000 ticks = 23 min) pour laisser
   le joueur apprendre sans stress.

3. **Ajoute un gate Composé Nexus dans la recette TBU_TR** :
   ex. `TBU Pellet = 9 Thorium + 1 Tri-Carbide + 1 Composé A`.
   Force le joueur à avoir progressé dans Nexus avant d'entrer nuclear.

4. **Désactive via config** les fuels que tu juges "trop endgame" pour Age 3
   (Bk, Cf) → tu les rendras plus tard via quête Age 4-5.

5. **Prépare une quête "Premier Démarrage"** qui explique CLAIREMENT :
   - Placer le fuel dans la cell
   - Placer des moderators à côté (pour atteindre criticality)
   - Placer des heatsinks (pour ne pas exploser)
   - Démarrer via le Reactor Controller

Sans cette quête, le joueur échoue 3 fois minimum avant de piger.
