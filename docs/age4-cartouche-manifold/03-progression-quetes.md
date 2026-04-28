# 03 — Progression Quêtes Âge 4 : "L'Échappée"

> Tags: #quetes #betterquesting #age4 #progression
> Voir aussi : [[../../QUESTS_PLAN_UPDATED.md]] (état actuel) | [[../../AGE3_DESIGN.md]] (référence format)

---

## 🎯 Objectif final

**Q-FINAL : "L'Injection"** — Le joueur s'injecte la Cartouche Manifold. La perception change. Le ciel se brise. L'Âge 4 se termine. L'Âge 5 commence dans la **vraie réalité**.

---

## 🗺️ Structure narrative en 6 phases

### Phase 0 — Le Réveil (Q-INTRO)
*1 quête d'intro (donnée à la sortie de l'Âge 3)*

### Phase 1 — Les Fondations (Q1 → Q15) — 15 quêtes
*"Avant de courir, marche. Avant de marcher, respire."*
- Setup base, eau, air, énergie
- Lignes L2 (Hydro-Eau) et L3.B (Cryo-Distillation Argon)
- Premiers gaz industriels

### Phase 2 — Les Métaux (Q16 → Q30) — 15 quêtes
*"Voss avait raison sur le Théorème I. Maintenant prouve-le."*
- Lignes L4 (Pyrométallurgie complète)
- Hall-Héroult, Kroll, Eau Régale
- 30 éléments réunis

### Phase 3 — La Chimie Sèche (Q31 → Q45) — 15 quêtes
*"L'ammoniaque est le sang de l'industrie. Le pétrole en est l'os."*
- Lignes L1 (Pétrochimie), L6 (Acides + NH₃), L7 (Acétone)
- Composés α (acides), β (organométalliques) prêts

### Phase 4 — Le Feu Nucléaire (Q46 → Q55) — 10 quêtes
*"Voss a touché à des choses qu'il ne fallait pas toucher. Tu vas faire pareil."*
- Ligne L5 (Nucléaire complet)
- UF₆, Pu-Be borate, ⁶LiT — composés γ
- **DÉBLOCAGE** : Mycélium Activé via flux neutronique

### Phase 5 — Le Vivant et l'Étoile (Q56 → Q70) — 15 quêtes
*"La chimie t'a amené jusqu'ici. La magie va te porter au-delà."*
- Ligne L8 complète (Botania + Astral + Manifoldine)
- 16 champis → Essence Chromatique
- Cyclisation Manifoldine sous étoile
- Composés δ (bio-actifs) + ε (Manifoldine active)

### Phase 6 — La Convergence (Q71 → Q-FINAL) — 10 quêtes
*"Tout converge. Y compris toi."*
- Bio-Réacteur Manifold (multibloc 7x7x7)
- Stabilisation stellaire
- Encartouchage
- **Q-FINAL : L'Injection** → écran qui se brise → cinematic → Âge 5

**Total : ~80 quêtes**

---

## 📋 Détail des quêtes — Phase 0 + Phase 1

### Q-INTRO : "Tu te crois libre ?"
**Prereqs :** Q-fin-Âge-3 (Composé X-77 obtenu, fin de l'âge précédent)
**Type :** Detect (récupérer le Carnet Voss Vol IV)
**Reward :** Carnet Voss Vol IV (Patchouli book) + 4 Pain + Bouteille XP
**Description (FR avec esprit modpack) :**

```
§7§oTu sors de ta base. Tu regardes le ciel. Tu te dis "j'ai fait un truc bien".§r

§7Sauf qu'un livre vient d'apparaître sur ta table de craft.
§7Tu ne l'as pas posé là.
§7Personne d'autre n'est là.

§8§o"Sujet 47.
§8Tu as franchi trois paliers. Tu es sorti des Compact Machines
§8à la fin de l'Âge 1. Tu as exploré l'overworld pendant
§8les Âges 2 et 3. Tu te crois libre.

§8Tu te trompes.

§8Cette ligne d'horizon que tu vois — c'est un mur."§r
§8— E.V., Carnet Vol. IV — Préface

§e§lObjectif : §7Lis le carnet. Comprends que ce n'est pas fini.
§e§lRécompense : §7La liberté n'est pas pour aujourd'hui.
```

---

### Q1 : "L'eau qui ment"
**Prereqs :** [Q-INTRO]
**Type :** Retrieval (1x Distilled Water Bucket)
**Description :**

```
§7§oVoss commence par l'eau. Évidemment.§r

§7Tu pensais qu'un seau d'eau de rivière, c'était de l'eau ?
§7§lFaux.§r §7C'est de l'eau qui ment.
§7Elle contient 2.4 millions de bactéries, 14 ions différents,
§7du calcaire, du fluor, et probablement les souvenirs
§7d'au moins trois grenouilles décédées.

§7Pour la Cartouche, tu vas avoir besoin d'§lEau Tridistillée§r§7. 
§7Tu vas la faire en 3 étapes.

§8§o"L'eau pure n'existe pas dans la nature, Sujet 47. 
§8L'eau pure est une obsession humaine. Adopte-la." — E.V.

§e§lObjectif : §7Distille ton premier seau d'eau (Thermal Evaporation Plant)
§e§lRécompense : §7Plans du Filtre Ionique (étape 2/3)
```

---

### Q2 : "Le filtre qui sépare le bon grain de l'ivraie ionique"
**Prereqs :** [Q1]
**Type :** Crafting (1x Ion Exchange Resin Block - custom item)
**Description :**

```
§7§oCe que ton seau d'eau distillée a encore en lui, c'est des §lions§r§7.
§7Cations. Anions. Tu sais. Les particules chargées qui font 
§7que ton sérum exploserait au lieu de te transformer.

§7Ce qu'il te faut, c'est une §lrésine échangeuse d'ions§r§7.
§7Silice + acide sulfonique. Tu vas devoir synthétiser ça.
§7Et oui, ça veut dire que tu as déjà besoin d'acide sulfurique
§7avant même d'avoir commencé l'usine. Bienvenue dans la chimie.

§8§o"Tout dépend de tout. C'est l'essence du Théorème I." — E.V.

§e§lObjectif : §7Crafte 1 bloc de Résine Échangeuse
§e§lRécompense : §7Plans de l'Osmose Inverse (étape 3/3)
```

---

### Q3 : "L'eau au cube"
**Prereqs :** [Q2]
**Type :** Retrieval (4x Tridistilled Water - fluid custom)
**Description :**

```
§7§oTroisième passage. Membrane silicium dopé. 
§7Pression osmotique inverse. T'es en mode labo blanc maintenant.

§7Cette eau-là, elle ment plus.
§7Elle est si pure qu'elle conduit MOINS l'électricité que l'air.
§7Si tu en bois, t'as littéralement aucun sel dans le corps.
§7Donc bois pas. T'as pas signé pour mourir d'hyponatrémie aujourd'hui.

§e§lObjectif : §7Produis 4000mB d'Eau Tridistillée
§e§lRécompense : §7Schéma du Cryo-Distillateur Atmosphérique
```

---

### Q4 : "L'air aussi est sale"
**Prereqs :** [Q3]
**Type :** Crafting (1x Air Cryo-Fractionnator multibloc)
**Description :**

```
§7§oL'air que tu respires, c'est :
§7  - 78% azote (qui sert à rien pour respirer mais joli)
§7  - 21% oxygène (le truc utile)
§7  - 0.93% argon (le gaz noble dont t'auras besoin)
§7  - 0.04% CO₂ (le truc qui réchauffe la planète)
§7  - 0.03% autres conneries

§7Pour la Cartouche, il te faut isoler l'§lArgon§r§7.
§7Spoiler : tu peux pas le crafter. Tu peux que le §lcondenser à -186°C§r §7.
§7Et tu vas avoir besoin de §lGelid Cryotheum§r§7.
§7Beaucoup de Gelid Cryotheum.

§8§o"L'argon est le mensonge le plus discret de l'atmosphère. 
§8Personne ne le voit. Mais sans lui, ton sérum oxyde et meurt." — E.V.

§e§lObjectif : §7Construis le Cryo-Distillateur Atmosphérique (3x6x3)
§e§lRécompense : §7Bucket d'Argon (premier coup gratuit, le reste tu produis)
```

---

### Q5 : "Le froid intelligent"
**Prereqs :** [Q4]
**Type :** Retrieval (32x Gelid Cryotheum bucket)
**Description :**

```
§7§oFais des stocks de Cryotheum. Sérieux.
§7T'en as besoin pour le Cryo-Distillateur. T'en auras besoin
§7pour le Mélangeur Cryogénique. T'en auras besoin pour le 
§7Bio-Réacteur Manifold final.

§7Recette : Blizz Powder + Snowball + Redstone + Niter
§7Tu connais. Tu connais.

§7Si tu connais pas, c'est que t'as séché les âges 2 et 3.
§7Honte à toi. Ouvre le JEI.

§e§lObjectif : §7Stocke 32 buckets de Gelid Cryotheum
§e§lRécompense : §7Resonant Tank (Thermal) pour les stocker
```

---

### Q6-Q15 : autres quêtes Phase 1 (résumé)

- **Q6** "Le ciel s'est ouvert" : Construire Argon storage system
- **Q7** "L'azote pour le pivot" : Stocker 10 buckets de N₂ (sera utilisé pour Haber-Bosch)
- **Q8** "L'oxygène pour brûler" : Stocker 10 buckets d'O₂
- **Q9** "Et la lumière fut" : Atteindre 5M RF/t (jalon énergie #1)
- **Q10** "L'usine sait pomper" : 1ère pompe Mekanism fluide automatique
- **Q11** "Tu te crois encore en âge 1" : Setup AE2 ME System (logistique obligatoire désormais)
- **Q12** "L'OpenComputer t'aidera" : Construire 1 PC OC T3 + écran (logistique avancée)
- **Q13** "L'eau lourde rampe" : 1 bucket d'Eau Lourde D₂O (Mekanism)
- **Q14** "Le tritium et la stratégie" : 100mB Tritium (préparation phase 4)
- **Q15** "Tu peux le faire" : Lire le Carnet Voss Vol IV — Chapitre 1 (déblocage Phase 2)

---

## 🎨 Esthétique des quest pages BetterQuesting

### Code couleur cohérent (codes Minecraft)
```
§7  → Texte normal narratif (gris clair, lisible)
§7§o → Texte italique pour intro/atmosphere
§l   → Bold pour mots-clés importants
§e§l → Jaune bold pour "Objectif" / "Récompense"
§8§o → Gris foncé italique pour citations Voss
§4   → Rouge foncé pour danger (utilisé Phase 4 nucléaire)
§5   → Pourpre pour références à la Manifoldine (Phase 5+)
§6   → Or pour quêtes finales / boss / révélations
```

### Pattern par quête
1. **§7§o** Intro narrative 1-3 lignes (ce que la quête raconte)
2. **§7** Body : explication technique avec humour, références modpack
3. **§8§o** Citation de Voss (1-2 lignes max)
4. **§e§l Objectif :** **§7** [tâche concrète]
5. **§e§l Récompense :** **§7** [item utile + débloque suite]

### Ton à respecter
- **Fais comme un pote** : tu (familier), références culturelles modpack ("ouvre le JEI", "tu connais"), insults gentils ("séché les âges", "honte à toi")
- **Voss reste sérieux** : ses citations sont prophétiques, pas blagueuses
- **Parle aux modpacks experts** : assume que le joueur sait ce qu'est un Pulverizer, un Mana Pool, etc. Ne réexplique pas la base
- **Une blague par quête** minimum, pas plus de 2

---

## 🗂️ Layout BetterQuesting (positions x,y)

Référence : age2.json utilise `(x, y)` en coordonnées. Y croissant = vers le bas.

```
        Q-INTRO
           │ (0, 0)
           ▼
         Q1 ──── Q2 ──── Q3 ──── Q4 ──── Q5
       (0,80)  (80,80)  (160,80) (240,80) (320,80)
                                            │
        Q6 ──── Q7 ──── Q8 ──── Q9 ──── Q10
       (320,160) ...
                                            │
        Q11 ─── Q12 ─── Q13 ─── Q14 ─── Q15
       (Phase 1 fin)                        │
                                            ▼
                                       (déblocage Phase 2)
                                       Q16 ────► etc.
```

Layout serpentin classique BQ, comme Q139→Q144 fin Âge 1.

---

## 🎁 Récompenses par phase

| Phase | Item récompense récurrent | Item bonus fin de phase |
|-------|--------------------------|------------------------|
| Phase 1 | Bucket Cryotheum + plans schémas | Carnet Voss Vol IV ch. 1 |
| Phase 2 | Lingots métaux purs (1x chaque) | Compass Hall-Héroult |
| Phase 3 | Buckets acides | Carnet Voss Vol IV ch. 2 |
| Phase 4 | Capsules nucléaires + plans γ | Carnet Voss Vol IV ch. 5 (Brisure) |
| Phase 5 | Essences chromatiques + Mana orbs | Carnet Voss Vol IV ch. 3+4 |
| Phase 6 | Cartouche Vide + Sérum partial | **CARTOUCHE MANIFOLD** |

---

## 🔗 Voir aussi

- [[02-pipeline-overview]] — Le pourquoi industriel
- [[01-lore-integration]] — Le pourquoi narratif
- [[../../AGE3_DESIGN.md]] — Référence format design âge précédent
- [[../../QUESTS_PLAN_UPDATED.md]] — Quêtes existantes Q1-Q144
