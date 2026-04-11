# AGE2_INTRO_V2_DESIGN.md — Refonte de la refonte (Sprint 1.5)

> **Context** : Sprint 1 a livré 9 quêtes d'intro Age 2 (v1.0.165, actuellement sur main).
> Playtest Alexis : « trop complexe dès le début, tuer le Warden en Q6 c'est pas ça ».
> Diagnostic : j'ai empilé trop de progression dans trop peu de quêtes, avec un boss fight
> comme 6ème marche, ce qui casse la courbe de difficulté. Ce doc propose un redesign
> inspiré directement des patterns d'ATM10 et Compact Claustrophobia.
>
> **Scope** : design only, aucun changement `age2.json` tant qu'Alexis n'a pas validé.

---

## 1. Erreurs du Sprint 1 (auto-critique honnête)

| # | Erreur                                         | Conséquence                                         |
|---|------------------------------------------------|-----------------------------------------------------|
| 1 | **Warden en Q6 d'intro**                       | Boss fight forcé à la 6ème quête, courbe violée     |
| 2 | **9 quêtes pour tout le début**                | Chaque quête fait trop de choses à la fois          |
| 3 | **Chaîne 100% linéaire**                       | Pas de branching, joueur en rail                    |
| 4 | **Rewards qui changent le gameplay dès Q2**    | Sobre en CC/ATM10, ici trop tôt/trop fort           |
| 5 | **Skip météorite forcé par le kill Warden**    | Récompense démesurée, casse l'écosystème AE2        |
| 6 | **Pas d'équivalent "welcome" orientation**     | Pas de pause pédagogique après la sortie de CM      |
| 7 | **Netherite en Q8-9 de l'intro**               | Tech-tier mi-jeu coincé dans l'ouverture d'Age      |

L'erreur fondamentale : **j'ai confondu "intro dramatique" avec "intro dense"**. ATM10 et CC le prouvent — une bonne intro est LENTE et MODULAIRE.

---

## 2. Les 7 règles extraites d'ATM10 + CC

### Règle 1 — Une quête = une chose
CC : chaque quête = **1 item retrieval** (1 wooden pickaxe, 1 crafting station, 1 compact machine). Jamais 2 actions combinées.
ATM10 Allthemodium root : **"obtain 1 netherite ingot"** — c'est tout.
→ **Fini les quêtes "descends sous Y=0 ET tue le Warden ET prends son œil"**. Une quête = une task atomique.

### Règle 2 — Pas de boss dans l'intro
CC : les 60 premières quêtes de la main line = **zéro combat de boss**. Que du craft et du retrieval.
ATM10 : chapter Allthemodium ne demande JAMAIS de kill le Warden. Le task est **"trouver une Ancient City"** (structure discovery), pas la combattre.
→ **Le Warden disparaît de l'intro**. Il devient un boss optionnel dans un chapter dédié plus tard.

### Règle 3 — Récompense qui enseigne, pas qui gamebreake
ATM10 : quête "find Ancient City" → reward = **64 wool blanche**. Pourquoi ? Parce que la wool étouffe les sculk sensors. **La reward t'apprend comment survivre au Warden sans le combattre**.
→ **Les rewards sont des outils**, pas des accélérateurs de progression. Fini "Q6 donne 3 presses AE2 et fait sauter tout le mid-game AE2".

### Règle 4 — Branch-and-converge
CC qid 18 dépend de 12 ET 19 simultanément. ATM10 : à partir de Allthemodium ingot, 3 branches parallèles (mining / other / beyond). 
→ **Adieu la chaîne linéaire 97→98→99→... ;** bonjour un arbre de quêtes avec parallélisation.

### Règle 5 — Task types qui auto-complètent
ATM10 utilise `dimension` (être dans une dim précise) et `structure` (avoir trouvé une structure précise). BQ2 pour Nexus n'a ni l'un ni l'autre en source, **mais** on peut retrouver l'équivalent via `retrieval` sur des items qui n'existent que dans la zone cible (ex. `minecraft:soul_sand` pour le Nether, un item sculk pour le Deep Dark).
→ **Les quêtes "arrive à X" deviennent "ramasse l'item Y qui ne se trouve qu'en X"**. Auto-complete + diégétique.

### Règle 6 — Orientation avant progression
ATM10 `welcome.snbt` = 6 quêtes, toutes à 10 XP, **toutes des `checkmark`**. Zéro gameplay. Juste "lis ce tip, configure tes paramètres, check le discord". Un onboarding doux avant la vraie progression.
→ **Il faut un "mini-welcome" Age 2** : 3-4 checkbox qui pointent vers le lore et les outils, SANS forcer la progression.

### Règle 7 — Volume = dilution
CC = 60 quêtes main line. Nexus Age 1 = 44 quêtes. Mon Sprint 1 essayait de tout caser en 9. 
→ **~15-18 quêtes pour l'intro Age 2 v2**, puis des branches dédiées derrière (AE2, Botania, Nether, Deep Dark, etc.).

---

## 3. Nouvelle structure — 17 quêtes d'intro + branches

### 3.1 Vue d'ensemble

```
ÂGE 2 INTRO v2 (17 quêtes, qid 97-113 — réutilise les slots actuels + ajoute 8)
═══════════════════════════════════════════════════════════════

WELCOME (4)         SETUP (4)            CERTUS CHAIN (5)     LORE GATES (4)
orientation         base overworld       AE2 baby steps       carnets Voss
 │                    │                    │                    │
 97 Réveil            101 Bed              105 Iron Pickaxe     112 Carnet V2 (reward Q97)
 98 Lore Q1           102 Crafting Table   106 Certus Quartz    113 Grabber Voss craft
 99 Lore Q2           103 Furnace          107 Grindstone         (leads to branches)
 100 Discord/tip      104 Chest            108 Meteorite Comp
                                           109 First Press

Après qid 113 (Grabber) → 3 branches s'ouvrent (AE2 / Botania / Deep Dark safe run)

BRANCHES (dérivent de qid 113)
 AE2 full (existant 107-126, migré qid 150+) 
 Botania intro (new, ~6 quêtes)
 Astral intro (new, ~6 quêtes)
 Deep Dark SAFE (ATM10 style, Ancient City sneak, ~5 quêtes, Warden optionnel)
 Nether + Netherite (new, ~8 quêtes, SANS kill Warden prerequisite)
 Testament Voss (existant qid 151-156)
```

### 3.2 Les 17 quêtes détaillées (pas de JSON encore, juste la spec)

Notation: `task: <type>` | `reward: <items>` | `pre: <qid>`

**WELCOME (4 quêtes — toutes checkbox, 10-20 XP chacune)**

| qid | _sym              | Titre                 | Task                     | Reward                               | Pre      |
|-----|-------------------|-----------------------|--------------------------|--------------------------------------|----------|
| 97  | `age2:wake`       | Tu es sorti           | checkbox                 | 1× pain + 10 XP                      | age1 end |
| 98  | `age2:read_v2_1`  | Carnet Vol. II (1/3)  | checkbox                 | 10 XP + hint vers qid 99             | 97       |
| 99  | `age2:read_v2_2`  | Carnet Vol. II (2/3)  | checkbox                 | 10 XP + hint vers qid 100            | 98       |
| 100 | `age2:read_v2_3`  | Carnet Vol. II (3/3)  | checkbox                 | 10 XP + hint vers setup              | 99       |

4 quêtes **orientation**, le joueur clique juste "complete" après avoir lu chaque fragment de lore. Rythme doux. Reward = quasi rien, c'est de la lecture.

**SETUP (4 quêtes — base overworld, items triviaux)**

| qid | _sym              | Titre               | Task                                    | Reward                  | Pre |
|-----|-------------------|---------------------|-----------------------------------------|-------------------------|-----|
| 101 | `age2:bed`        | Un Lit              | retrieval 1× `minecraft:bed`             | 20 XP                   | 97  |
| 102 | `age2:ct`         | Table de Craft      | retrieval 1× `minecraft:crafting_table`  | 20 XP + 4× planks       | 97  |
| 103 | `age2:furnace`    | Four                | retrieval 1× `minecraft:furnace`         | 20 XP + 1× coal         | 97  |
| 104 | `age2:chest`      | Premier Coffre      | retrieval 1× `minecraft:chest`           | 20 XP + 4× stick        | 97  |

4 quêtes **établissement**, chacune dépend du qid 97 seul (parallélisable). Le joueur peut les faire dans n'importe quel ordre. Tâches triviales. **Branch-and-converge** : toutes les 4 convergent sur qid 105.

**CERTUS CHAIN (5 quêtes — AE2 baby steps)**

| qid | _sym                | Titre                  | Task                                                 | Reward                      | Pre       |
|-----|---------------------|------------------------|------------------------------------------------------|------------------------------|-----------|
| 105 | `age2:iron_pick`    | Pioche de Fer          | retrieval 1× `minecraft:iron_pickaxe`                | 20 XP + 4× iron              | 101+102+103+104 |
| 106 | `age2:certus`       | Le Quartz Bleu         | retrieval 16× `appliedenergistics2:material` (certus)| 50 XP + 4× stone             | 105       |
| 107 | `age2:grindstone`   | Pierre Affûteuse       | retrieval 1× `ae2:quartz_grindstone`                 | 50 XP + 8× torch             | 106       |
| 108 | `age2:meteor_comp`  | Boussole à Météorite   | retrieval 1× `ae2:meteorite_compass`                 | 50 XP + 4× bread             | 107       |
| 109 | `age2:first_press`  | Première Presse        | retrieval 1× ANY AE2 press (Logic OR Calc OR Eng)    | **100 XP + 1× silicon press**| 108       |

qid 109 utilise la **matching on OreDict / multiple required items** de BQ2 pour accepter **n'importe laquelle** des 3 presses de process (Logic/Calc/Eng). Récompense = la **Silicon Press offerte** (parce qu'elle est rare dans le loot météorite et importante pour continuer). Le joueur doit donc **trouver une météorite** (RNG) mais la Silicon press n'est plus un bloqueur.

Si la météorite hunt est trop longue en pratique, on peut ajouter une quête bonus "fallback" qui permet de crafter 1 press via Mekanism precision sawmill ou un truc du genre — à décider au playtest.

**LORE GATES (4 quêtes — carnets + Grabber)**

| qid | _sym                  | Titre                 | Task                                           | Reward                                            | Pre   |
|-----|-----------------------|-----------------------|------------------------------------------------|---------------------------------------------------|-------|
| 110 | `age2:v2_in_hand`     | Le Carnet Vol. II     | retrieval 1× `patchouli:guide_book` (NBT v2)   | 30 XP + 1× diamond pickaxe                        | 109   |
| 111 | `age2:sculk_one`      | Un Signal Sculk       | retrieval 1× `stacked_dimensions_warden:sculk_tendril` | 30 XP + débloque Grabber recipe (gamestage) | 110   |
| 112 | `age2:grabber`        | Le Sac du Sujet 46    | retrieval 1× `nexusabsolu:grabber_voss`        | 100 XP + 16 pain + **débloque 3 branches (cmd)** | 111   |
| 113 | `age2:crossroads`     | La Croisée            | checkbox (player picks track)                  | 1× carnet Vol. III + pointeur vers les branches  | 112   |

qid 110 utilise NBT matching pour confirmer le carnet Vol.2 en main (le joueur l'a reçu en reward de qid 100).
qid 111 demande **1 seul sculk tendril** (pas 4). Drop rare du Deep Dark, mais **1 seul** = acceptable sans Warden.
qid 112 craft le Grabber — gameplay upgrade qui arrive au bout des 15 quêtes d'intro (pas Q4 comme en v1).
qid 113 = le hub Croisée. Choisis ta voie.

**Total : 17 quêtes (97-113).** Structure diamond + tree, pas linéaire.

### 3.3 Comparaison v1 (Sprint 1) vs v2

| Aspect                     | v1 Sprint 1 (shipped)        | v2 proposé                         |
|----------------------------|------------------------------|------------------------------------|
| Nombre de quêtes intro     | 9                            | **17** (+8)                         |
| Boss dans l'intro          | **Warden en Q6** ❌          | Aucun ✅                             |
| Linéarité                  | Linéaire stricte             | **Tree-structure parallélisable**   |
| Première quête             | Checkbox déjà OK             | Checkbox ✅ (garde ce qui marche)    |
| Rewards gameplay-changing  | Grabber dès Q4               | **Grabber en Q16** (fin d'intro)    |
| AE2 presses                | **Données gratuites par Warden** | Trouvées normalement (météorite) |
| Netherite                  | Q8-Q9 de l'intro             | **Chapter dédié hors intro**        |
| Deep Dark                  | Combat Warden Q6             | **Chapter dédié optionnel, safe sneak** |
| Carnets Patchouli          | Distribués tôt               | Distribués tôt ✅ (garde)            |

### 3.4 Ce qui reste réutilisé de Sprint 1

- ✅ **Grabber Voss** (ItemGrabberVoss.java — tel quel, juste obtenu plus tard)
- ✅ **Badge Voss, Lanterne Voss, Fragment Mémoire 1** (items Java — réutilisés dans les quêtes)
- ✅ **Carnet Voss Vol. II et Vol. III** (Patchouli books — contenu lore identique)
- ✅ **MOD_IDS.txt** (référence des mod IDs)
- ✅ **Le script `apply_sprint1_quests.py`** (architecture réutilisable pour écrire `apply_sprint1_5_quests.py`)

**Aucun travail Java n'est perdu.** On réutilise tout. Seule la **couche quête** bouge.

### 3.5 Ce qui est REMPLACÉ

- ❌ Les 9 textes descriptifs actuels de qid 97-105 → réécrits en 17 quêtes
- ❌ La task hunt Warden de qid 102 → supprimée (le Warden ne sera PAS dans l'intro)
- ❌ Le reward "3 presses gratuites" → supprimé (les presses s'obtiennent via météorite comme AE2 standard)
- ❌ La task location Nether de qid 103 → déplacée dans un chapter Nether dédié (Sprint 2+)
- ❌ Les tasks ancient_debris/netherite_ingot de qid 104-105 → déplacées dans le chapter Nether dédié

### 3.6 Ce qui devient du "Sprint 2+" (hors de cette itération)

- **Chapter Nether** (~8 quêtes) : obsidian, portail, netherrack, blaze rod, Ancient Debris ×4, Netherite Scrap, Netherite Ingot. Linéaire car chaque étape dépend de la précédente. Reward final : accès au mid-game Nexus + un trophy cosmétique.
- **Chapter Deep Dark** (~5 quêtes, ATM10-style) : trouver 1 sculk sensor, trouver 1 ancient city, ramasser 4 echo_shard (dans des vases de la cité, SANS Warden), bonus optionnel "Tuer le Warden" avec reward unique (warden_heart) — optionnel, non sur chemin critique.
- **Chapter AE2 complet** (~12 quêtes) : récupère la chaîne existante qid 107-126 et la migre derrière qid 113 "La Croisée".
- **Chapter Botania starter** (~6 quêtes) : migration de l'existant qid 117-120 + expansion.
- **Chapter Mystag starter** (~6 quêtes) : migration qid 121-126 + expansion.

Tout ça = Sprint 2 à 4. Sprint 1.5 ne touche QUE les 17 quêtes d'intro.

---

## 4. Stratégie technique d'application

### 4.1 Options pour écrire dans `age2.json`

**Option A — In-place + ajout**
- Réécris les 9 quêtes existantes (qid 97-105, keys 149:10 à 157:10)
- Ajoute 8 nouvelles quêtes (qid 106-113) — MAIS qid 106 "Première Cellule" existe déjà !
- **Conflit** : qid 106 est déjà utilisée par "Première Cellule".

**Option B — Recréer dans un range neuf**
- Crée 17 nouvelles quêtes qid 2000-2016 (range Age 2 per _meta.json)
- Supprime les 9 anciennes qid 97-105
- "Première Cellule" (qid 106) garde son id mais son prereq bascule vers le nouveau qid 2016
- Les chaînes AE2/Botania/Mystag existantes (qid 107-126) voient leur prereq racine changer
- **Avantage** : propre, pas de conflit
- **Inconvénient** : casse les saves de playtest existants

**Option C (recommandée) — In-place sur 97-105 + ajout sur 114-121**
- Réécris les 9 quêtes qid 97-105 avec les 9 premières quêtes v2 (Welcome 97-100 + Setup 101-104 + Certus 105)
- **qid 106** (Première Cellule) reste intacte dans son slot mais on l'utilise comme "Q12 Certus Chain" : rename en "Le Quartz Bleu" (ou inverse — laisse "Première Cellule" et shift) → **décision design**
- Ajoute 7-8 nouvelles quêtes en qid 114-121 (slots libres probablement, à vérifier)
- Migre les prereqs de qid 107-126 pour dépendre du nouveau qid 113 "Croisée"
- **Compromis** : réutilise les ids existants, maintient les saves

**Ma préférence** : Option C, MAIS avant tout je dois **vérifier quels qid sont libres** dans `age2.json` (on a vu qu'il y a des gaps : 7, 59, 60, 64, 69, 72-76 en Age 0-1). Peut-être il y en a en Age 2 aussi.

👉 **Question validation** : tu préfères laquelle, **A / B / C** ?

### 4.2 Processus d'écriture

Si Option C validée :
1. Inventorie les qid libres dans la range 2000-2999 (si non utilisés → idéal) ou dans les gaps existants
2. Écris un nouveau `apply_sprint1_5_quests.py` basé sur `apply_sprint1_quests.py` avec le mapping v2
3. Lance le script → réécrit age2.json
4. `merge_quests.py --check` → validation
5. `merge_quests.py` → DefaultQuests.json
6. Bump version 1.0.165 → 1.0.166
7. Commit + push

Aucun changement Java. Aucun changement Patchouli. **Seulement la couche quête bouge.**

---

## 5. Sur FTB Quests

Tu as dit « ils utilisent FTB Quests qui est mieux ». Tu as raison — FTBQ est plus riche (task types `dimension` + `structure`, arbres visuels, sections, chapter groups hiérarchiques, reward tables, etc.).

**Migration BQ2 → FTBQ** = un Sprint entier dédié :
- FTBQ doit être ajouté au modlist (ou remplacer BQ2)
- Toutes les quêtes actuelles (157 au total) doivent être réécrites en SNBT
- Les saves de playtest existants perdent leur progression (incompatible entre les 2 mods)
- Les scripts `merge_quests.py` et `apply_sprint1_quests.py` doivent être réécrits

**Ma recommandation** : **pas maintenant**. D'abord on stabilise le modpack sur BQ2 (qui marche), on finit l'Age 2 jusqu'à un état jouable, puis on ouvre un Sprint dédié à la migration BQ2 → FTBQ quand le contenu est figé. Budget estimé : 2-3 sessions de 3h chacune, + tests lourds.

👉 **Question validation** : FTBQ migration confirmée en Sprint 6+ (après stabilisation Age 2), ou tu veux la pousser plus tôt ?

---

## 6. Questions pour validation

Avant que je touche `age2.json` :

- [ ] **V2-Q1** : La nouvelle structure à 17 quêtes (Welcome + Setup + Certus + Lore Gates) te convient ? Ou tu veux plus / moins / un autre découpage ?
- [ ] **V2-Q2** : Les 4 quêtes Setup (bed/crafting/furnace/chest) — trop basiques ? Le joueur sort déjà d'un Age 1 où il maîtrise tout ça. Peut-être les rendre optionnelles ou les zapper ?
- [ ] **V2-Q3** : La Silicon Press en reward de Q109 (au lieu d'un skip complet du RNG) — OK ou tu préfères un autre fallback ?
- [ ] **V2-Q4** : Le Warden **100% hors de l'intro** et **100% optionnel** dans un chapter Deep Dark séparé — OK ?
- [ ] **V2-Q5** : Option C pour le routing des qid (in-place + ajout) — ou tu veux Option B (range neuf 2000+) ?
- [ ] **V2-Q6** : FTBQ migration en Sprint 6+ — OK ou plus tôt ?
- [ ] **V2-Q7** : Est-ce que je rollback la v1.0.165 déjà shippée (revert du commit Sprint 1), ou on itère par-dessus en v1.0.166 ?

Dès que tu valides ces 7 points, je :
1. Écris `scripts/apply_sprint1_5_quests.py` avec les 17 quêtes v2
2. Lance + valide + merge
3. Commit + push v1.0.166
4. Tu refais un playtest avec la nouvelle courbe de difficulté
