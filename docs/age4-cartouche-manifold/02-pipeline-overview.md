# 02 — Pipeline Overview : Les 8 Lignes Parallèles

> Tags: #pipeline #usine #industriel
> Voir aussi : [[lines/L1-petrochimie]] | [[lines/L2-hydro-eau]] | [[lines/L3-electrolyse-cryo]] | [[lines/L4-pyrometallurgie]] | [[lines/L5-nucleaire]] | [[lines/L6-acides-ammoniaque]] | [[lines/L7-organique-acetone]] | [[lines/L8-botanique-manifoldine]]

---

## 🗺️ Schéma général du complexe

```
┌─────────────────────────────────────────────────────────────┐
│  COMPLEXE INDUSTRIEL MANIFOLD — 8 lignes parallèles         │
├─────────────────────────────────────────────────────────────┤
│  L1: PÉTROCHIMIE       L2: HYDRO-EAU                        │
│  L3: ÉLECTROLYSE+CRYO  L4: PYROMÉTALLURGIE                  │
│  L5: NUCLÉAIRE         L6: ACIDES + AMMONIAQUE              │
│  L7: ORGANIQUE+ACÉTONE L8: BOTANIQUE+MANIFOLDINE            │
└─────────────────────────────────────────────────────────────┘
            │ │ │ │ │ │ │ │
            ▼ ▼ ▼ ▼ ▼ ▼ ▼ ▼
     [Convergence : 10 Composés Intermédiaires]
                  │
                  ▼
       [Bio-Réacteur Manifold étendu]
                  │
                  ▼
          💉 CARTOUCHE MANIFOLD
```

---

## 🔄 Inter-dépendances entre lignes

```
PÉTROLE BRUT                    AIR AMBIANT             EAU
     │                              │                    │
[L1: Refinery PCC]            [L3.B: Cryo-Distill]   [L2: Tridistill]
     │                          │   │   │                │
Naphta/Kéro/Diesel             N₂  Ar  O₂              H₂O ultra
     │                                                   │
[L1: HDS] ──► H₂S ──[L6: Claus]──► S                     │
     │                                                   │
[L1: Hydrocrack]                                         │
     │                                                   │
Kéro Premium ──────────► [SOLVANT NEUTRE α] ◄────────────┘
                              │
                              ▼
30 ÉLÉMENTS (L4 + L5 + L3) ──► [10 COMPOSÉS α/β/γ/δ/ε]
                              │
                              ├──► [M1: Mélangeur Cryogénique]
                              │            │
                              ├──► [M2: Bio-Réacteur Manifold]
                              │            │
16 CHAMPIS ──► [L8: Botania pipeline]      │
                       │                   │
                  ESSENCE CHROMATIQUE ──────┤
                                           │
SPORES IRRADIÉES ──► [L8: Cyclisateur Stellaire] (la nuit, ciel ouvert)
                                           │
                                  Manifoldine ──┐
                                                ▼
                                       SÉRUM MANIFOLD
                                                │
                                      + Casing Titane/Iridium
                                                │
                                                ▼
                                       💉 CARTOUCHE MANIFOLD
```

---

## 📊 Matrice d'inter-dépendance des lignes

Lecture : **lignes** produisent → **colonnes** consomment

| Produit \ Consommateur | L1 | L2 | L3 | L4 | L5 | L6 | L7 | L8 | Final |
|------------------------|----|----|----|----|----|----|----|----|-------|
| **L1 Naphta**          |    |    |    |    |    |    | ✅ |    |       |
| **L1 Gaz Naturel**     |    |    |    |    |    | ✅ | ✅ |    |       |
| **L1 H₂S**             |    |    |    |    |    | ✅ |    |    |       |
| **L1 Kéro Premium**    |    |    |    |    |    |    |    |    | ✅ Solvant α |
| **L2 Eau Tridistillée**| ✅ |    | ✅ |    |    | ✅ | ✅ | ✅ |       |
| **L2 Eau Lourde**      |    |    |    |    | ✅ |    |    | ✅ |       |
| **L2 Tritium**         |    |    |    |    | ✅ |    |    |    |       |
| **L3 H₂**              | ✅ |    |    |    |    | ✅ |    |    |       |
| **L3 Cl₂**             |    |    |    | ✅ |    | ✅ |    |    |       |
| **L3 Na**              |    |    |    | ✅ |    | ✅ |    |    |       |
| **L3 Li**              |    |    |    |    | ✅ |    |    |    |       |
| **L3 P (Phosphore)**   |    |    |    | ✅ |    |    |    |    |       |
| **L3 F (Fluor)**       |    |    |    | ✅ | ✅ |    |    |    |       |
| **L3 N₂**              |    |    |    |    |    | ✅ |    |    |       |
| **L3 O₂**              | ✅ |    |    | ✅ |    | ✅ |    |    |       |
| **L3 Argon**           |    |    |    | ✅ |    |    |    | ✅ | ✅ Encartouchage |
| **L4 Métaux purs**     |    |    |    |    |    |    |    |    | ✅ Composés β/δ |
| **L4 H₃PO₄**           |    |    |    |    |    |    |    | ✅ |       |
| **L5 U/Pu/Th/Be/B**    |    |    |    |    |    |    |    |    | ✅ Composés γ |
| **L5 Flux neutronique**|    |    |    |    |    |    |    | ✅ Spores |       |
| **L6 NH₃ (pivot)**     |    |    |    |    |    |    | ✅ Indole | ✅ Manifoldine |       |
| **L6 H₂SO₄**           |    |    |    | ✅ Bayer | ✅ Uranyle |    |    | ✅ Champis sombres |       |
| **L6 HCl**             |    |    |    | ✅ Eau Régale |    |    |    | ✅ Champis doux |       |
| **L6 HNO₃**            |    |    |    | ✅ Eau Régale |    |    |    |    | ✅ δ2 Argent |
| **L6 NaOH**            |    |    |    | ✅ Bayer |    |    |    |    |       |
| **L6 Eau Régale**      |    |    |    | ✅ Métaux précieux |    |    |    |    |       |
| **L7 Acétone**         |    |    |    |    |    |    |    | ✅ Manifoldine |       |
| **L7 Tryptamide-M**    |    |    |    |    |    |    |    | ✅ Cyclisation |       |
| **L7 Méthanol**        |    |    |    |    |    |    |    | ✅ Élution |       |
| **L7 Éthanol**         |    |    |    |    |    |    |    |    | ✅ ε Solution |
| **L8 Essence Chromatique** | | |    |    |    |    |    |    | ✅ Bio-Réacteur |
| **L8 Cristaux Manifoldine**| | |    |    |    |    |    |    | ✅ Composé ε |

**Conclusion** : aucune ligne n'est isolée. **L6 est le hub central** (consomme/produit le plus). **L2 et L3 alimentent presque tout**. **L8 est l'aval final** (consomme, ne produit que pour le sérum).

---

## 🏗️ Multiblocs custom à créer (9 total)

| ID | Nom | Ligne | Mod base | Taille | Fonction |
|----|-----|-------|----------|--------|----------|
| MB1 | **Cryo-Distillateur Atmosphérique** | L3 | Modular Machinery | 3x6x3 | Air → N₂/Ar/O₂ |
| MB2 | **Cellule Castner-Kellner** | L3 | Modular Machinery | 5x3x3 | NaCl → Na/Cl₂/NaOH |
| MB3 | **Réacteur Hall-Héroult** | L4 | Modular Machinery | 5x4x5 | Bauxite → Al pur |
| MB4 | **Réacteur Kroll** | L4 | Modular Machinery | 4x5x4 | TiCl₄ → Ti pur |
| MB5 | **Tour HDS** | L1 | Modular Machinery | 3x8x3 | Kéro+H₂+CoMo → Kéro désulfuré |
| MB6 | **Alambic Manaïque** | L8 | Modular Machinery + Botania | 5x4x5 | Champis → Mana lié + Matrice |
| MB7 | **Cyclisateur Stellaire** | L8 | Modular Machinery + Astral | 5x6x5 | Extrait → Manifoldine (la nuit) |
| MB8 | **Bio-Réacteur Manifold** | Final | Modular Machinery | 7x7x7 | Composés → Sérum |
| MB9 | **Mélangeur Cryogénique** | Final | Modular Machinery | 3x3x3 | Solvant + α → Matrice acide |

---

## ⚡ Bilan énergétique

| Phase | Conso pic | Conso moyenne | Notes |
|-------|-----------|---------------|-------|
| Lignes en parallèle (full charge) | 60M RF/t | 25M RF/t | L4 électrolyse + L5 centrifugation = 70% |
| Bio-Réacteur (synthèse finale) | 5M RF/t pendant 60s | — | Pic seul |
| Stabilisation Stellaire | 0 RF (passive nuit) | — | — |

→ **Le joueur DOIT atteindre 50-70M RF/t** pour faire tourner l'usine en parallèle. Cluster de Big Reactors (Extreme Reactors) ou Mekanism Fusion Reactor obligatoire. → **Quête énergie dédiée à mi-parcours Âge 4**.

---

## 📐 Footprint au sol estimé

- **Lignes individuelles** : 10x10 à 15x15 chacune
- **Espace de stockage gaz/fluides** (tanks Mekanism Dynamic Tank) : 10x10
- **Cluster énergie** (Big Reactors / Fusion Mekanism) : 15x15
- **Logistique AE2/XNet** : intégrée dans les lignes
- **Total estimé** : **80x80 blocs** minimum pour tout caser proprement

→ Le joueur va devoir **construire une grosse base dédiée** ou **utiliser une dimension custom** (RFTools Dim si ça reste cohérent).

---

## ⏱️ Timing estimé pour finir l'Âge 4

| Étape | Durée jeu estimée |
|-------|-------------------|
| Setup base + énergie 50M RF/t | 3-5h |
| Construire L1+L2+L3 (chimie de base) | 3-4h |
| Construire L4+L5 (métaux + nucléaire) | 4-6h |
| Construire L6 (hub acides) | 2-3h |
| Construire L7+L8 (organique + magique) | 3-4h |
| Synthétiser 1ère cartouche | 2h (process complet) |
| **TOTAL** | **17-24 heures de jeu** |

→ L'Âge 4 = **gros morceau**, pleinement justifié par sa nature de "porte de sortie".

---

## 🔗 Voir aussi

- [[03-progression-quetes]] — Comment ça se découpe en 50-80 quêtes
- Lignes individuelles via les liens en haut du fichier
