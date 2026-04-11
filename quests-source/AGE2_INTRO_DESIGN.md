# AGE2_INTRO_DESIGN.md — Refonte intro Age 2 (Nexus Absolu)

> Document de design. Ne touche PAS `age2.json` tant qu'Alexis n'a pas choisi une direction.
> Objectif : remplacer les 10 premières quêtes de l'Age 2 (qid 97→106) par une séquence
> dramaturgique qui exploite la sortie de CM 9×9, le Nether overhaul et le boss Cave (Warden-like).

---

## 1. Intro Age 2 actuelle (ce qui existe)

Chaîne linéaire (par prereq) :

| # | qid  | Titre                       | Task réelle                                | Prereq    | Problème                                      |
|---|------|-----------------------------|--------------------------------------------|-----------|-----------------------------------------------|
| 1 | 97   | Premier Souffle             | retrieve `minecraft:bed`                   | 149 (A1)  | hook plat : "pose un lit"                     |
| 2 | 98   | Le Retour                   | checkbox (entrer dans la CM via PSD)       | 97        | rien à faire, pas de dramaturgie              |
| 3 | 99   | Campement de Fortune        | retrieve `minecraft:chest` x1              | 97        | **desc dit table+four+coffre, task = 1 chest** |
| 4 | 100  | Transfert des Richesses     | retrieve `minecraft:iron_ingot` x64        | 98, 99    | fetch, pas de décision                        |
| 5 | 101  | Le Quartz Bleu              | retrieve `ae2:material` (certus) x16       | 100       | fetch, OK mais sans contexte                  |
| 6 | 102  | La Pierre Affuteuse         | retrieve `ae2:quartz_grindstone` x1        | 101       | pédagogique, fade narrativement               |
| 7 | 103  | Les Presses de Voss         | retrieve `ae2:inscriber` x1                | 102       | RNG météorite, **peut bloquer 1h+**           |
| 8 | 104  | Premier Processeur Logique  | retrieve `ae2:material` (logic proc) x4    | 103       | craft direct, rien de nouveau                 |
| 9 | 105  | Le Contrôleur               | retrieve `ae2:controller` x1               | 104       | OK mais enchainé à plat                       |
| 10| 106  | Première Cellule            | retrieve `ae2:drive` x1                    | 105       | OK mais rien ne change dans le gameplay       |

**Diagnostic global :**

- **Zéro hook narratif** : on sort de la CM, on pose un lit, on mine du quartz. L'Age 0 et 1 avaient le mystère du confinement ; Age 2 devient un manuel AE2.
- **Aucun shift environnemental** : pas d'exploitation de l'overworld ouvert, ni du Nether, ni des caves. Le monde "hors CM" est traité comme un décor neutre.
- **Track unique forcé** : pas de choix entre explorer / construire / lire le lore. Linéaire comme un tutoriel.
- **Q99 cassée** : la tâche ne matche pas la description.
- **Q103 bloquante** : dépendance RNG (météorite AE2) sans fallback.
- **Récompenses invisibles** : aucune des 10 quêtes ne change le gameplay du joueur (ni buff, ni item qui débloque une mécanique, ni révélation de lore).
- **Rien de Voss** : la plus grosse transition du modpack (sortir enfin de la captivité 3 Ages) se fait sans lore-drop, sans journal, sans question.

---

## 2. Patterns extraits des packs de référence

### SevTech: Ages — Age 3
Hook : Nether. On découvre qu'un **portail pré-généré** existe déjà (on "tombe sur lui" avant de pouvoir en construire un).
Détour obligatoire : **les Betweenlands avant la Smeltery** (tech tree parallèle, matériaux uniques). On ne peut pas rusher la techline principale.
Enseignement : *dramaturgie par la découverte imposée*, non par le texte. La quête la plus mémorable est celle qui détourne le joueur d'où il pensait aller.
Sources :
- [SevTech Wiki — Age 3](https://sevtechages.fandom.com/wiki/Age_3)
- [Team Three Quarters — A Scoop of Discovery](https://www.teamthreequarters.com/blog/sevtech-ages-age-3-a-scoop-of-exploration)
- [Griffin SevTech — The Journey](https://griffin-sevtech.tumblr.com/post/179518861555/the-journey)

### Enigmatica 2 Expert — début
Hook : quête "Welcome" qui tient en deux lignes et donne **leather armor + sac à dos**. Récompense immédiate qui change le gameplay (inventaire étendu dès la minute 5).
Progression parallèle : *tech (Mekanism, AE2) et magie (Thaumonomicon, Botania) démarrent ENSEMBLE*. Le joueur arbitre en permanence sur quoi investir. Pas de "magic age" ou "tech age" séparé.
Enseignement : *récompense initiale qui change la boucle gameplay du jour 1, pas après 10 quêtes*.
Sources :
- [E2E Wiki — Getting Started](https://e2e-expert.fandom.com/wiki/Getting_Started)
- [E2E Wiki — Walkthrough](https://e2e.fandom.com/wiki/Walkthrough)
- [MineYourMind — E2E guide](https://mineyourmind.net/forum/threads/enigmatica-2-expert-guide-tips-tricks-and-warnings.31137/)

### FTB Inferno
Hook : **tu spawnes dans le Nether**. Le pitch de l'entier du modpack est une punition narrative — un rituel tourné mal. Cinq boss fights majeurs jalonnent la progression, avec des "Seven Deadly Sin dungeons".
Enseignement : *le lore n'est pas un texte à lire, c'est une contrainte environnementale permanente*. Chaque quête rappelle où tu es et pourquoi.
Sources :
- [FTB Inferno — Feed The Beast](https://www.feed-the-beast.com/modpacks/99-ftb-inferno)
- [FTB Inferno — Official Wiki](https://ftb.fandom.com/wiki/FTB_Inferno)

### Nomifactory (GTCEu)
Hook : Lost Cities au début — **exploration de ruines pour looter les premiers composants**. Le joueur est lâché dans un monde étrange avant d'avoir construit son atelier.
Progression : questbook refondu avec "Processing Lines Tab" visuel. Chaque chaîne de craft a sa propre page dédiée.
Enseignement : *lâcher le joueur dans un environnement non-généré-procédural au moment de la transition crée une ambiance immédiate*. Pas besoin de cinematics.
Sources :
- [Nomifactory CEu — CurseForge](https://www.curseforge.com/minecraft/modpacks/nomi-ceu)
- [Nomi-CEu — GitHub](https://github.com/Nomi-CEu/Nomi-CEu)

### Méta-patterns qui marchent partout

| Pattern | Appliqué dans | Impact sur le joueur |
|---------|---------------|---------------------|
| **Shift environnemental brutal** | FTB Inferno (Nether spawn), SevTech (Betweenlands) | "je ne suis plus dans le même jeu" |
| **Récompense qui change le gameplay ≤ quête 3** | E2E (sac à dos), Nomifactory (ruines lootables) | le joueur sent la progression tout de suite |
| **Détour forcé** | SevTech (Betweenlands avant smeltery) | mystère, non-linéarité |
| **Multi-track parallèle** | E2E (tech + magie) | choix = engagement |
| **Lore en tant que contrainte** | FTB Inferno (Nether permanent) | pas de texte à lire pour immerger |
| **Ruines / sites pré-générés** | Nomifactory, SevTech | découverte sans dialogue |

---

## 3. Contraintes spécifiques Nexus Absolu

### Lore déjà posé (à respecter)
- Dr. E. Voss, scientifique disparu, a passé 30 ans à repousser la physique.
- 3 ans enfermé dans des CM par "Protocole Voss-7".
- Nexus Absolu = l'item final, transcendance de la matière.
- Les quêtes fin Age 2 existantes (qid 151-156) parlent déjà de "La Chute", "Composés Oubliés", "Coordonnées", "Fragment de Bedrock", "Le Testament". L'intro doit PLANTER ces révélations, pas les gâcher.

### Ton à tenir
Sérieux/épique + humour + mystérieux. Horror SCP-flavor OK, torture-porn non.

### Mods récemment ajoutés (à exploiter, IDs à confirmer par Alexis)
- **Nether overhaul visuel** : probablement BetterNether et/ou NetherEx pour 1.12.2. Nouveaux biomes, plantes, blocs ambiants. *IDs à confirmer post-décision.*
- **Cave Update avec boss Warden-like** : pas encore identifié dans `modlist.txt`. *Nom du mod + id du boss et de ses drops à confirmer par Alexis.*

### Choix techniques imposés par l'infra
- Prereqs symboliques possibles via `_symbolic` + `{"$ref": "age2:<nom>"}` (voir `merge_quests.py`).
- IDs nouvelles quêtes : idéalement dans la range `[2000..2999]` documentée dans `_meta.json`. Mais on peut aussi remplir les trous de l'existant si l'objectif est de garder les saves — **à décider post-sélection**.
- Les 10 quêtes actuelles (qid 97-106) peuvent être RÉÉCRITES in-place (titre/desc/task/reward changent, qid/prereq conservés) ou REMPLACÉES (nouvelles qid, les anciennes partent aux oubliettes + migration des dependants). Remplacer = plus propre narrativement, plus cher en refactoring.

### Ce que l'intro doit accomplir (cahier des charges)
1. **Quête 1** = scène d'ouverture forte, pas un "pose un lit".
2. **Quête 2-3** = récompense qui change la boucle gameplay (comme le sac à dos E2E).
3. **Quête 4-5** = détour forcé vers Nether ou Caves — pas de rush AE2 direct.
4. **Quête 6-7** = première confrontation avec une entité / un site mystérieux (Voss ou Warden).
5. **Quête 8-9** = retour avec un artefact / une info qui débloque la vraie progression AE2 (plus rapide qu'actuellement).
6. **Quête 10** = embranchement clair : tu sais où tu vas ensuite (choix entre 2-3 trajectoires parallèles AE2 / Botania / Blood Magic).

---

## 4. Trois directions narratives

Chacune propose **9 quêtes d'intro** (qid de travail 97→105, prereqs symboliques). La 10ème quête est un *hub de choix* qui ouvre les trajectoires parallèles de la suite de l'Age 2.

> Notation : `[BLK:<id>]` = bloc/item à fournir par Alexis post-décision (mod conc., item custom, etc).
> `?ref:<name>` = prereq symbolique résolu au merge.

---

### DIRECTION A — « Le Protocole d'Extraction »

**Pitch (3 phrases)**
Sortir de la CM 9×9 n'est pas une libération : c'est une **brèche** dans le confinement Voss-7. La "Safe Zone" autour du point de sortie est un anneau artificiel construit par Voss pour contenir ce qui a tué tous les autres sujets — une entité sentinelle dans les profondeurs (le boss Cave). Chaque pas hors de la zone déclenche un signal, et le Nether corrompu est la seule voie détournée pour atteindre les coordonnées du "Testament".

**Ton**
SCP-horror. Paranoïa discrète. Le joueur se sent observé. Aucune action n'est sans conséquence. Voss a peur de quelque chose, et tu vas comprendre quoi.

**Arc dramatique**
Libération → surveillance → refus de la boucle → brèche du périmètre → confrontation → fuite → préparation du vrai passage.

**Quêtes (9)**

| # | qid | _symbolic              | Titre                         | Task                                                                                     | Reward                                                                                             | Prereq                      |
|---|-----|------------------------|-------------------------------|------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|-----------------------------|
| 1 | 97  | `age2:breach`          | La Brèche                     | **detect** : se trouver à >1 bloc du point de spawn CM en overworld (location trigger)   | 1× livre Patchouli "Journal 47 — Entrée 1" (tag custom) + `/say` lore                              | `?ref:age1:cm9x9_unlocked`  |
| 2 | 98  | `age2:periphery`       | Le Périmètre                  | **explore** : découvrir 4 balises Voss pré-générées (structure custom, 64 blocs radius)  | 1× Scanner de Fréquence (custom OC item) — overlay HUD des balises                                 | `?ref:age2:breach`          |
| 3 | 99  | `age2:first_signal`    | Premier Signal                | **retrieve** : 4× [BLK:balise_fragment] (drop en cassant les balises)                    | 3× experience_bottle + **débloque crafting du Grabber Voss** (sac à dos custom 36 slots)           | `?ref:age2:periphery`       |
| 4 | 100 | `age2:grabber`         | Le Sac du Sujet 12            | **craft** : 1× nexusabsolu:grabber_voss                                                  | 16× pain + 4× fer — *change la boucle gameplay : inventaire étendu dès le début*                    | `?ref:age2:first_signal`    |
| 5 | 101 | `age2:corrupted_nether`| Nether Corrompu               | **enter** : atteindre un biome [BetterNether:<biome>] (ou équivalent)                    | 1× Masque de Respirateur (immunité effets Nether custom) + 1× livre "Voss / Nether"                | `?ref:age2:grabber`          |
| 6 | 102 | `age2:voss_lab`        | Laboratoire de Voss           | **locate** : trouver la structure pré-générée "voss_outpost" (nether, 400 blocs portal)  | **3× presse d'inscription AE2 (Logic, Calc, Eng) — contourne le RNG météorite**                     | `?ref:age2:corrupted_nether` |
| 7 | 103 | `age2:warden_first`    | Ce Qui Dort                   | **approach** : entrer dans la structure "deep_cavern" et tenir 30s à <10 blocs du boss   | 1× Fragment de Mémoire 1/3 + Absorption IV (5min) — *non-combat, pure ambiance*                     | `?ref:age2:voss_lab`         |
| 8 | 104 | `age2:retreat`         | Retrait                       | **retrieve** : 1× [BLK:warden_eye] (drop boss Cave tier 1, sans le tuer — sonar miss)    | **2× inscriber_press (Silicon) + 1× debloque Quartz Fiber** (skip total de l'étape AE2 manuelle)   | `?ref:age2:warden_first`     |
| 9 | 105 | `age2:crossroads`      | La Croisée                    | **checkbox** : choisis ta voie (3 boutons OC → `/gamestage add` distinct)                | 1× Carnet Voss Vol. 2 + **3 trajectoires ouvertes** : AE2 fast / Botania / Blood Magic             | `?ref:age2:retreat`          |

**Nouveaux mods exploités**
- Nether overhaul → biome corrompu obligatoire (Q5) + structure lab Voss (Q6).
- Cave boss → Q7 approach + Q8 drop *non-létal* (le boss est invincible au premier contact, tu voles un œil pendant qu'il dort).

**Ce que ça casse**
- Remplace complètement la chaîne qid 97-106. Q100 "Transfert des Richesses" (fetch 64 iron) et Q103 "Inscriber via météorite" **supprimées** — remplacées par la drop-run Nether / Cave.
- La quête 102 donne les 3 presses d'un coup → la suite AE2 (qid 104-106 "Logic Processor / Controller / Cell") peut être conservée mais déplacée APRÈS qid 105 (Croisée).

**Pros**
- Ambiance SCP forte, aligne parfaitement avec le lore Voss-7.
- Q7 non-létal est mémorable (joueur rencontre le boss mais fuit) — rappelle les rencontres Wither ambiant de GregTech New Horizons.
- Contourne le blocage RNG météorite par narration.
- Exploite les 2 nouveaux mods sans les nommer frontalement.

**Cons**
- Demande **2 structures pré-générées custom** (balises + voss_outpost + deep_cavern) → coûteux à produire (schematic / RecurrentComplex).
- Le Grabber Voss est un **item custom** à coder (ou réutiliser Iron Backpacks / Traveler's Backpack déjà dans le pack ?).
- Boss non-létal = il faut un mécanisme de script pour empêcher le kill précoce (gamestage + command_block ou event handler).

---

### DIRECTION B — « L'Héritage de Voss »

**Pitch**
Voss n'a pas seulement testé des sujets — il a construit un **laboratoire extérieur** à côté du point de sortie, partiellement effondré, que tu découvres dans les 2 premières minutes. Le lab est **un tutoriel narratif incarné** : les machines AE2 de base sont déjà là, mais brisées. Les réparer est la vraie progression technique. Chaque pièce du lab raconte une étape du Protocole Voss-7 par ses notes et ses schémas.

**Ton**
Indiana Jones + X-Files. Archéologie industrielle. Humour des notes de Voss ("Test #17 — Le sujet a survécu à la perte du frame-coil. Note pour moi-même : augmenter le voltage."). Pas de horror, de la curiosité.

**Arc dramatique**
Sortie → découverte du lab → "oh il a tout laissé derrière" → réparation pièce par pièce → découverte qu'une pièce est scellée → clé = descente dans les caves + gardien automatique → ouverture de la pièce = révélation Nether.

**Quêtes (9)**

| # | qid | _symbolic                | Titre                         | Task                                                                                 | Reward                                                                                             | Prereq                     |
|---|-----|--------------------------|-------------------------------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|----------------------------|
| 1 | 97  | `age2:daylight`          | La Porte Ouverte              | **advancement** : sortir de la CM 9×9 (advancement trigger)                          | 1× pain + 1× livre Patchouli "Journal 47 — Entrée 1" + pointeur vers le lab                        | `?ref:age1:cm9x9_unlocked` |
| 2 | 98  | `age2:lab_entrance`      | L'Entrée du Laboratoire       | **location** : atteindre la structure "voss_lab_surface" (<200 blocs du spawn)       | 1× Badge Voss (tag NBT) — ouvre portes scriptées + 6× torches                                      | `?ref:age2:daylight`       |
| 3 | 99  | `age2:journal_1`         | Notes de Travail              | **retrieve** : 3× pages de journal (items custom dropped par des lecterns scriptés)  | **débloque recette Grindstone AE2 cheap** (2× quartz_block au lieu de 8) + 5 XP                    | `?ref:age2:lab_entrance`   |
| 4 | 100 | `age2:broken_inscriber`  | L'Établi Brisé                | **retrieve** : 3× [BLK:inscriber_shattered] (drop des blocs cassés du lab)           | 1× Presse Logic déjà forgée + schéma pour assembler les 2 autres                                   | `?ref:age2:journal_1`      |
| 5 | 101 | `age2:broken_press`      | Trois Presses                 | **craft** : 3× ae2:inscriber (avec les presses déjà en main → skip RNG météorite)    | **1× ME Controller pré-assemblé** + 4× logic processors                                            | `?ref:age2:broken_inscriber` |
| 6 | 102 | `age2:sealed_door`       | La Porte Scellée              | **interact** : trouver une porte du lab qui refuse de s'ouvrir — clé est ailleurs    | 1× Radar OC (item custom, indique direction de la clé)                                             | `?ref:age2:broken_press`   |
| 7 | 103 | `age2:deep_descent`      | Descente                      | **location** : atteindre profondeur Y<20 dans un biome [CaveUpdate:<biome>]          | 1× Lanterne de Voss (lumière permanente + immune au Sculk) + 8× Glowstone                          | `?ref:age2:sealed_door`    |
| 8 | 104 | `age2:guardian_key`      | Le Gardien Automatique        | **boss** : battre [MOB:cave_guardian_tier1] (boss Cave à faible HP, tier d'échauffement) | **1× Clé du Lab Scellé** + 1× Fragment Organique (nécessaire fin Age 2)                         | `?ref:age2:deep_descent`   |
| 9 | 105 | `age2:unsealed`          | Ce Qu'il Cachait              | **interact** : utiliser la clé sur la porte scellée (right-click)                    | Cutscene texte + **portail Nether pré-construit** + 1× Carnet Voss Vol.2 + choix de voie           | `?ref:age2:guardian_key`   |

**Nouveaux mods exploités**
- Cave boss en **combat réel** (tier 1 d'échauffement, pas le full boss).
- Nether overhaul → découverte en quête 9 (portail déjà construit au fond du lab).

**Ce que ça casse**
- Remplace les 10 quêtes existantes mais **réutilise la progression AE2** (presses / logic / controller / cell) — plus de 50% de compat avec qid 104-106 existantes.
- Q103 (inscriber via météorite) = supprimée, remplacée par drop lab.
- Q100 (64 iron) = supprimée, remplacée par loot lab.

**Pros**
- Plus facile techniquement : **1 seule structure pré-générée** (le lab), pas besoin de boss non-létal.
- Combat boss tier 1 = feedback clair et gratifiant (vs Direction A non-combat).
- Récompense Q5 (ME Controller pré-assemblé) = gameplay accéléré visible immédiatement.
- Parfaitement dans le lore Voss sans forcer le SCP-horror.
- Narration incarnée par la structure elle-même — pas besoin de beaucoup de texte.

**Cons**
- Moins "frisson" que Direction A.
- La structure voss_lab_surface doit être **assez riche** pour tenir 5-6 quêtes → grosse schematic à produire.
- Boss tier 1 = nécessite de balancer la progression du boss en plusieurs tiers (pas juste "un boss").

---

### DIRECTION C — « Le Jour d'Après »

**Pitch**
Quand tu sors de la CM 9×9, tu réalises que **47 ans se sont écoulés dehors** alors que 3 ans dans la CM. Le monde est un *post-apocalyptique doux* : camps abandonnés des autres sujets disparus, restes de leur travail, notes laissées par des gens qui ont fini par mourir ou par s'enfuir. Un seul autre survivant (NPC script) vit dans une cabane et te donne les premières infos. Le Nether est devenu un **site de culte** pour les derniers sujets qui ont vénéré le boss Cave ("Le Dormeur") comme un dieu endormi.

**Ton**
Horizon Zero Dawn / Death Stranding. Mélancolique, pas horrifique. Beaucoup de "ceux qui étaient là avant toi". Le joueur est le DERNIER à sortir, pas le premier.

**Arc dramatique**
Sortie → réalisation du temps écoulé → camp abandonné → rencontre NPC → apprentissage par héritage → descente au site du Dormeur → choix : réveiller ou préserver.

**Quêtes (9)**

| # | qid | _symbolic                | Titre                         | Task                                                                                       | Reward                                                                                        | Prereq                      |
|---|-----|--------------------------|-------------------------------|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|-----------------------------|
| 1 | 97  | `age2:wake_outside`      | Quarante-Sept Ans             | **advancement** : sortir de la CM 9×9                                                      | 1× livre "Calendrier Voss" (révèle le gap temporel) + 2× pain                                  | `?ref:age1:cm9x9_unlocked`  |
| 2 | 98  | `age2:abandoned_camp`    | Le Camp Abandonné             | **location** : trouver structure "camp_sujet_12" (200 blocs du spawn)                       | 1× coffre loot (5 random early-AE2 items) + 1× journal Sujet 12                                | `?ref:age2:wake_outside`    |
| 3 | 99  | `age2:inherited`         | Ce Qu'ils Ont Laissé          | **retrieve** : 4× journal_page (drops dans le camp + loot)                                 | **débloque la recette Grabber Sujet (sac à dos 27 slots)** + 1× pelle de fer                   | `?ref:age2:abandoned_camp`  |
| 4 | 100 | `age2:npc_contact`       | Le Dernier                    | **interact** : parler au NPC "Survivant" (via command block / right-click)                 | 1× Carte du Camp + lore dump + 1× inscriber press Logic (cadeau de l'NPC)                      | `?ref:age2:inherited`       |
| 5 | 101 | `age2:trade_iron`        | Le Troc                       | **retrieve** : 16× iron_ingot (à déposer au NPC via shop_block script)                     | **2× inscriber press (Calc + Eng) — skip total RNG météorite**                                 | `?ref:age2:npc_contact`     |
| 6 | 102 | `age2:nether_cult`       | Le Culte du Dormeur           | **enter** : entrer dans biome [NetherOverhaul:<biome>] + trouver structure cult_site        | 1× Offrande au Dormeur (item quest) + 4× blaze_powder                                          | `?ref:age2:trade_iron`      |
| 7 | 103 | `age2:deep_pilgrimage`   | Pèlerinage                    | **retrieve** : 1× [BLK:dormeur_relic] (loot dans un autel cult_site)                       | 1× Fragment Stellaire (nécessaire fin Age 2) + 1× nouvelle recette Controller "étendu"         | `?ref:age2:nether_cult`     |
| 8 | 104 | `age2:awaken_or_keep`    | Réveiller ou Préserver        | **checkbox** : choix scripté (2 boutons OC → `/gamestage add` distinct)                    | *Branch A : boss fight futur* OU *Branch B : lore pacifique futur*                             | `?ref:age2:deep_pilgrimage` |
| 9 | 105 | `age2:testament_opens`   | Le Vrai Testament             | **retrieve** : 1× Carnet Voss Vol. 2                                                       | 1× ME Drive + 1× 4k Storage Cell + ouverture explicite des 3 voies Age 2                       | `?ref:age2:awaken_or_keep`  |

**Nouveaux mods exploités**
- Nether overhaul → biome-cible et site de culte (Q6).
- Cave boss → *pas combattu directement dans l'intro*. Le choix Q8 détermine si on le réveillera plus tard (payoff fin d'Age 2 ou début d'Age 3).

**Ce que ça casse**
- Supprime qid 100 (64 iron) → remplacé par Q5 troc avec NPC.
- Supprime qid 103 (météorite Inscriber) → le NPC les donne.
- Le NPC demande un système de dialogue : soit command_block + right-click trigger, soit OC script. Pas trivial mais réalisable.
- La structure camp_sujet_12 + cult_site = 2 nouvelles schematics.

**Pros**
- Ton unique, aucun autre modpack ne tente ça en intro Age 2.
- Le choix Q8 ouvre un **branching** qui se paye plus tard — très rare dans BQ 1.12, très mémorable.
- L'NPC "Survivant" est le premier humain vu dans tout le modpack, énorme impact émotionnel après 3 Ages de solitude en CM.

**Cons**
- **Complexité la plus élevée des 3** : 2 structures + 1 NPC scripté + système de dialogue + branching.
- Risque de casser le ton SCP/mystère posé aux Ages 0-1 ("le monde devient trop vivant").
- Le branching Q8 oblige à dédoubler une partie de la suite de l'Age 2.

---

## 5. Matrice de comparaison

| Critère                               | A — Protocole   | B — Héritage    | C — Jour d'Après |
|---------------------------------------|-----------------|-----------------|-------------------|
| Alignement lore Voss existant         | ⭐⭐⭐⭐⭐         | ⭐⭐⭐⭐⭐         | ⭐⭐⭐              |
| Force du hook narratif                | ⭐⭐⭐⭐⭐         | ⭐⭐⭐⭐           | ⭐⭐⭐⭐⭐           |
| Exploitation Nether overhaul          | ⭐⭐⭐⭐           | ⭐⭐              | ⭐⭐⭐⭐             |
| Exploitation Cave boss                | ⭐⭐⭐⭐⭐ (non-létal) | ⭐⭐⭐ (tier 1) | ⭐⭐ (différé)      |
| Coût implémentation (1=cheap, 5=cher) | 4               | 2               | 5                 |
| Change le gameplay ≤ quête 4          | ⭐⭐⭐⭐ (Grabber) | ⭐⭐⭐⭐⭐ (presses)| ⭐⭐⭐ (sac 27)     |
| Mémorabilité joueur                   | ⭐⭐⭐⭐⭐         | ⭐⭐⭐⭐           | ⭐⭐⭐⭐⭐           |
| Compat progression AE2 existante      | 30% réutilisé   | 60% réutilisé   | 40% réutilisé     |
| Risque de bugs                        | ⭐⭐⭐ (non-létal) | ⭐⭐             | ⭐⭐⭐⭐ (NPC+branch) |

**Shortlist éditoriale** (sans préjuger du choix d'Alexis) :
- **Direction B** si on veut un truc **implémentable en 2 sessions** et qui réutilise 60% du travail AE2 existant.
- **Direction A** si on veut la plus forte ambiance SCP et qu'on accepte le coût du non-létal scripté.
- **Direction C** si on veut une narration inédite dans tout le modded Minecraft, mais avec 3× le coût d'impl.

---

## 6. Notes d'implémentation (une fois la direction choisie)

### Choix structurel à trancher avec Alexis
- **Réécrire in-place vs remplacer** les qid 97-106 ?
  - *In-place* (réécriture des 10 quêtes existantes, même qid) : zéro migration de prereqs des quêtes suivantes, saves existantes toujours compatibles.
  - *Remplacement* (nouvelles qid 2000-2009, anciennes supprimées) : plus propre narrativement, casse les saves en cours, oblige à re-pointer les prereqs de qid 107+ (qui dépendent actuellement de 106).

  Recommandation par défaut : **in-place** — on conserve la shape de la chaîne et on réécrit le contenu. Ça respecte le principe "IDs existants préservés" documenté dans `_meta.json`.

### Prereqs symboliques à introduire
Chaque quête de la nouvelle intro reçoit un `_symbolic` stable (`age2:breach`, `age2:periphery`, …). Les quêtes aval qui dépendent aujourd'hui numériquement de 106 peuvent être migrées pour utiliser `{"$ref": "age2:<last>"}` — mais ce n'est pas obligatoire, le numéro marche aussi.

### Nouveaux items / blocs / structures à créer
À budgéter APRÈS le choix de direction. Liste minimale par direction :

- **A** : Scanner de Fréquence, Grabber Voss, Masque Respirateur, Fragment Mémoire 1/3, 3 structures pré-générées (balises × 4, voss_outpost, deep_cavern), mécanisme non-létal Warden.
- **B** : Badge Voss, Radar OC, Lanterne de Voss, Clé Lab Scellé, 1 structure (voss_lab_surface), 3 drops custom (press shattered, guardian drops).
- **C** : NPC Survivant (entité ou command_block), Carte du Camp, Offrande au Dormeur, Dormeur Relic, 2 structures (camp_sujet_12, cult_site), système de dialogue, branching gamestage.

### Intégration au pipeline `merge_quests.py`
- Nouvelles quêtes ajoutées à `quests-source/age2.json` (in-place ou replacement).
- `py scripts/merge_quests.py --check` doit passer.
- `py scripts/merge_quests.py` → écrit `DefaultQuests.json` + backup auto.
- Test in-game avec un monde neuf : vérifier l'enchaînement des 9 quêtes de bout en bout avant d'enchaîner les trajectoires suivantes.

### Validation à effectuer avant de pusher une direction
- [ ] Tous les items référencés existent (IDs confirmés en pack).
- [ ] Toutes les structures pré-générées ont un schematic ou un plan de génération documenté.
- [ ] Le prereq du premier quest (`?ref:age1:cm9x9_unlocked`) pointe vers la bonne quête Age 1 (qid 149 actuel).
- [ ] Aucune quête casse le prereq de qid 107+ (sauf si migration explicite).
- [ ] Le round-trip `--split` / merge reste byte-identique sur les age files non touchés.

---

## 7. Décision attendue

Alexis : lis, choisis **A / B / C** (ou dis "mix" et on itère), et on passe au plan technique détaillé → écriture dans `age2.json` → merge → test.

Je ne touche pas `age2.json` avant ta validation.

---

## 8. Direction retenue — **A+B Fusion : « Le Poste Voss-7 »**

> Alexis a validé un mix A+B. Cette section détaille la fusion narrative et les 9 quêtes.
> **Rien n'est encore écrit dans `age2.json`** — ce plan attend une dernière validation avant implémentation.

### 8.1 Pitch fusionné

Tu sors de la CM 9×9. À 100-200 blocs du spawn, une structure pré-générée partiellement effondrée : **un Poste Voss-7**. Ce n'est pas un labo neutre abandonné (B pur). Ce n'est pas non plus une brèche dans un périmètre invisible (A pur). C'est les deux à la fois : le poste a été **construit par Voss pour te surveiller**, et son effondrement a déclenché la dégradation d'un anneau de "Safe Zone" artificielle autour de la zone de sortie. Les journaux dans le poste révèlent que Voss te regardait depuis 3 ans — et que tu n'étais **pas le premier**. Sujet 46 l'a été. Il est toujours ici, dans une cave scellée sous le poste, transformé par une expérience ratée en entité sentinelle qui dort.

Voss, avant de disparaître, a laissé des outils DANS le poste **pour briser son propre confinement**, parce qu'il savait qu'il allait échouer et qu'il voulait que son successeur sorte. Les outils sont là, mais le générateur du poste est mort — il faut descendre dans la cave du Dormeur, voler un **fragment de ce qu'il est devenu** (un œil, le seul composant transdimensionnel que Voss sait fabriquer), remonter, et activer le poste. L'activation révèle un **portail Nether pré-construit** qui mène aux vraies coordonnées laissées par Voss.

### 8.2 Ton

Archéologie industrielle + paranoïa SCP mesurée. Humour noir dans les notes de Voss. Pas de jumpscares. Le joueur est supposé **comprendre progressivement** qu'il est le successeur d'une lignée d'expériences, pas la cible d'un complot actif. Le Dormeur est tragique, pas hostile (jusqu'à l'Age 2 tardif / Age 3).

### 8.3 Arc dramatique

1. **Sortie** → découverte visuelle du poste au loin.
2. **Entrée** → comprendre que c'est un outil de surveillance dirigé sur ta CM.
3. **Lecture** → réaliser que tu n'es pas le premier sujet.
4. **Bris du confinement** → le Grabber Voss devient ton premier upgrade gameplay (36 slots).
5. **Descente** → générateur mort, il faut aller sous terre pour le réparer.
6. **Rencontre non-létale** → Sujet 46 dort. Tu voles un œil. Tu ne le combats pas.
7. **Activation** → les 3 presses d'inscription s'assemblent dans le lab (skip RNG météorite).
8. **Révélation** → portail Nether apparaît, coordonnées Voss déverrouillées.
9. **Croisée** → 3 trajectoires parallèles AE2 / Botania / Blood Magic, chacune annotée par une note manuscrite de Voss.

### 8.4 Les 9 quêtes

Symbolic IDs sous `age2:*`. qid = in-place sur 97-105 (la quête actuelle qid 106 "Première Cellule" est poussée en Q10 hub de choix, section 8.6).

| # | qid | _symbolic            | Titre                        | Task (BQ type)                                    | Détail task                                                                                  | Reward                                                                                                      | Prereq                       |
|---|-----|----------------------|------------------------------|---------------------------------------------------|----------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|------------------------------|
| 1 | 97  | `age2:surface`       | À la Surface                 | `bq_standard:location`                            | Se trouver en overworld à >20 blocs du point de sortie CM (trigger auto).                    | 1× `nexusabsolu:carnet_voss_v2` (livre Patchouli, entrée "Le Poste") + 1× pain + `/say` lore                 | `?ref:age1:exit_cm9x9`       |
| 2 | 98  | `age2:voss_post`     | Le Poste                     | `bq_standard:location`                            | Entrer dans la structure `voss_post_surface` (détectée par location trigger, 20×15×10).      | 1× `nexusabsolu:badge_voss` (NBT-tag, ouvre portes scriptées) + 4× torch                                    | `?ref:age2:surface`          |
| 3 | 99  | `age2:logs_01`       | Les Logs de Confinement      | `bq_standard:retrieval`                           | Collecter 3× `nexusabsolu:journal_page` (drops placés manuellement dans le poste via loot).  | 1× `nexusabsolu:scanner_frequence` (HUD overlay) + 5 XP + lore dump via reward command                     | `?ref:age2:voss_post`        |
| 4 | 100 | `age2:beacons`       | Les Balises Fantômes         | `bq_standard:retrieval`                           | Casser 4× blocs `nexusabsolu:balise_fragment` placés autour du poste (100 blocs radius).      | **Débloque recette `nexusabsolu:grabber_voss` (sac à dos 36 slots)** + 2× iron_ingot                        | `?ref:age2:logs_01`          |
| 5 | 101 | `age2:grabber`       | Le Sac du Sujet 46           | `bq_standard:crafting` OU `bq_standard:retrieval` | Craft 1× `nexusabsolu:grabber_voss` (recette débloquée par Q4).                              | **Gameplay-shift** : inventaire effectif 36 slots, dispo dès maintenant + 16× torch + 8× pain               | `?ref:age2:beacons`          |
| 6 | 102 | `age2:generator`     | Le Générateur Effondré       | `bq_standard:retrieval`                           | Descendre sous le poste (cave pré-générée Y<20) et récupérer 1× `nexusabsolu:generator_core`.| 1× `nexusabsolu:lanterne_voss` (lumière permanente + immune effets cave boss) + 4× glowstone                | `?ref:age2:grabber`          |
| 7 | 103 | `age2:sleeping_one`  | Celui Qui Dort               | `bq_standard:retrieval`                           | Récupérer 1× `nexusabsolu:warden_eye` — drop UNIQUEMENT si boss `cave_boss` en mode dormant.  | 1× `nexusabsolu:fragment_memoire_1` (lore item, 1/3) + Absorption IV 5min + `/say` lore                    | `?ref:age2:generator`        |
| 8 | 104 | `age2:three_presses` | Les Trois Presses            | `bq_standard:retrieval`                           | Placer l'œil sur un autel scripté → reçoit 3× `ae2:inscriber_press` (Logic + Calc + Eng).    | **3× ae2:inscriber_press** + 4× `ae2:material:22` (logic processor) + skip total RNG météorite              | `?ref:age2:sleeping_one`     |
| 9 | 105 | `age2:nether_gate`   | La Porte de Voss             | `bq_standard:advancement` OU `bq_standard:location` | Activer le portail Nether dans le poste (right-click scripté) + entrer dans le portail.     | 1× `nexusabsolu:carnet_voss_v3` (coordonnées Nether) + 1× `ae2:controller` + ouverture 3 voies (cmd `/gamestage add age2_crossroads`) | `?ref:age2:three_presses`    |

### 8.5 Nouveaux assets requis

**Items custom Nexus (11 nouveaux)**
- `nexusabsolu:carnet_voss_v2` — livre Patchouli (entrées "Le Poste", "Sujet 46", "Le Protocole Voss-7").
- `nexusabsolu:carnet_voss_v3` — livre Patchouli (entrées "Nether Voss", "Coordonnées", "Croisée").
- `nexusabsolu:badge_voss` — item NBT, ouvre portes scriptées du poste.
- `nexusabsolu:journal_page` — drop item (3× nécessaires).
- `nexusabsolu:scanner_frequence` — HUD overlay item (affiche direction balises).
- `nexusabsolu:balise_fragment` — block cassable (drop self).
- `nexusabsolu:grabber_voss` — sac à dos 36 slots. **Alternative** : réutiliser Iron Backpacks (`ironbackpacks:backpack` taille moyenne) avec une recette custom si la dépendance mod est OK.
- `nexusabsolu:generator_core` — drop item loot-placé.
- `nexusabsolu:lanterne_voss` — block éclairage permanent + immunity Sculk/Darkness (si le cave mod a un effet).
- `nexusabsolu:warden_eye` — drop item obtenu par event handler quand proximité boss dormant.
- `nexusabsolu:fragment_memoire_1` — lore item (1/3, les 2/3 et 3/3 viendront dans les quêtes actuelles qid 151-155 du Testament).

**Structures pré-générées (2)**
- `voss_post_surface` — ~20×15×10, generation via RecurrentComplex ou schematic dans `config/justenoughdimensions/` : 4 pièces internes (entrée, salle logs, salle presses, portail Nether désactivé), 4 balises externes pré-placées à 80-100 blocs radius.
- `voss_underground_cave` — ~15×15×8, spawn sous le poste à Y≈15, scellée par `age2:generator` quest (mur de blocs indestructibles cassables après Q6), contient le boss endormi + l'œil sur un autel.

**Boss mécanique**
- `cave_boss` (nom de mod à confirmer) doit exister en 2 modes :
  - **Dormant** : passif, ne bouge pas, ne prend aucun dégât, drop `warden_eye` via event CT quand le joueur reste <8 blocs pendant 30s sans attaquer.
  - **Éveillé** : combat réel, à déclencher uniquement après activation du poste (gamestage `age2_post_activated`) — hors scope de l'intro, géré plus tard dans l'Age 2.
- Script CraftTweaker `nexus_cave_boss.zs` : `events.onEntityLivingHurt` annulé si l'attaqué est `cave_boss` ET gamestage `age2_post_activated` absent.

**Patchouli entries (3)**
- Vol.2 : "Le Poste", "Sujet 46", "Protocole Voss-7".
- Vol.3 : "Nether Voss", "Coordonnées".
- Mise à jour du carnet existant : entrée "L'Éveil" → ajouter pointeur vers Vol.2.

**Scripts CraftTweaker (3 nouveaux fichiers)**
- `scripts/Age2_Intro_CaveBoss.zs` — immunity mode dormant + drop `warden_eye` sur proximity.
- `scripts/Age2_Intro_PostActivation.zs` — right-click œil sur autel → donne 3 presses.
- `scripts/Age2_Intro_Balises.zs` — drop `balise_fragment` au break + sound effect.

### 8.6 Ce qui arrive aux quêtes existantes

| qid | Nom actuel             | Nouveau sort                                                                              |
|-----|------------------------|-------------------------------------------------------------------------------------------|
| 97  | Premier Souffle        | **Réécrite** en `À la Surface` (lit retiré, location trigger).                             |
| 98  | Le Retour              | **Réécrite** en `Le Poste`.                                                                |
| 99  | Campement de Fortune   | **Réécrite** en `Les Logs de Confinement` — le bug desc≠task disparaît.                   |
| 100 | Transfert des Richesses| **Réécrite** en `Les Balises Fantômes` — plus de fetch 64 iron.                            |
| 101 | Le Quartz Bleu         | **Réécrite** en `Le Sac du Sujet 46`.                                                      |
| 102 | La Pierre Affuteuse    | **Réécrite** en `Le Générateur Effondré`.                                                  |
| 103 | Les Presses de Voss    | **Réécrite** en `Celui Qui Dort` — le RNG météorite disparaît.                             |
| 104 | Premier Processeur Logique | **Réécrite** en `Les Trois Presses`.                                                   |
| 105 | Le Controleur          | **Réécrite** en `La Porte de Voss`.                                                        |
| 106 | Première Cellule       | **Déplacée** comme première quête du hub Croisée (trajectoire AE2), prereq migré à `?ref:age2:nether_gate`. |

Les qid 107-126 (suite AE2 : canaux, terminal, crafting terminal, fibre, interface, import bus, export bus, auto-craft, P2P, stockage profond, fleurs, livewood, mana, manasteel, inferium, litherite, void ore miner, osmium, enrichment, purification) **ne sont pas touchées** — elles s'enchaînent derrière le hub Q10 par simple migration du prereq racine.

La chaîne Testament (qid 151-156) **n'est pas touchée** — elle reste comme payoff fin d'Age 2.

### 8.7 Coût estimé

| Catégorie           | Détail                                  | Effort |
|---------------------|-----------------------------------------|--------|
| Items Java custom   | 11 items (3 avec logique spéciale)      | 3/5    |
| Structures          | 2 schematics + intégration JED / RC     | 3/5    |
| Boss script         | 1 CT file, mode dormant + drop handler  | 2/5    |
| Patchouli books     | 2 nouveaux + 1 update                   | 2/5    |
| Quêtes BQ           | 9 réécrites in-place + 1 migration prereq (qid 106) | 2/5    |
| Build / test cycle  | ~3-4 itérations monde neuf              | 2/5    |
| **Total**           |                                         | **~3/5** |

Comparé aux directions pures : A = 4/5, B = 2/5, **Fusion = 3/5**. On paie une structure de plus que B (la cave scellée) mais on garde le Grabber qui est le reward gameplay-changing le plus marquant des 3 directions.

### 8.8 Validation avant écriture — à confirmer par Alexis

- [ ] **Pitch + arc dramatique** : OK ou tu veux ajuster le ton ?
- [ ] **Non-létal Q7** : accepté, ou tu préfères un vrai combat tier-1 comme en Direction B ?
- [ ] **Grabber Voss** = item custom Java **OU** réutilisation d'un sac à dos existant dans le pack (Iron Backpacks / Traveler's Backpack) avec recette custom ? (deuxième option 2× moins chère à coder)
- [ ] **Nom du mod Cave Boss** + ID exact du boss pour que je puisse le référencer dans le script CT.
- [ ] **Nom du mod Nether overhaul** + ID d'au moins 1 bloc signature pour le détecteur de biome/structure.
- [ ] **Structures** : OK pour 2 nouvelles schematics ? Tu les produis ou tu veux que je génère un plan RecurrentComplex / un Litematica ?
- [ ] **Réécriture in-place des qid 97-105** confirmée ? (alternative = nouvelles qid 2000-2008 + suppression des anciennes, plus coûteux)
- [ ] **Q106 "Première Cellule"** : confirmée comme première marche du hub AE2 post-Croisée, prereq migré ?

Dès que ces 8 points sont tranchés, je passe au plan technique fichier-par-fichier (quels items Java créer, quelles structures, quelles lignes de `age2.json`), puis j'écris et je merge.

---

## 9. Itération — Plan final validé (partiellement) par Alexis

Alexis a répondu :

1. **Pitch + arc** → OK ✅
2. **Non-létal Q7** → **NON**, il veut un **vrai combat de boss** façon ATM10 (Ender Dragon, boss du Nether, Warden = les 3 grandes étapes). Le Warden devient un combat réel dans l'intro Age 2. ✅
3. **Grabber Voss** → **item Java custom** (pas de réutilisation Iron Backpacks). ✅
4. **Cave boss mod** → **Deeper in the Caves - RESTART (+ 1.12.2 Version)** par Linfox. Accès Deep Dark par trous pré-générés dans la bedrock, le Warden drop du Sculk. ✅
5. **Nether** → **BetterNether** + **NetherUpdate Netherite [Forge]** par FrostBreker (Ancient Debris → Netherite Scrap → Netherite Ingot = 4 scrap + 4 gold, Ancient Debris mine avec pioche diamant). Alexis veut aussi des **quêtes Netherite** dans l'intro Age 2. ✅
6. **Structures** → **PAS DE STRUCTURES**, juste du lore. Le "Poste Voss-7" devient un concept raconté dans les carnets, pas un bâtiment physique. Ça simplifie énormément (budget -1/5). ✅
7. **In-place vs remplacement** → question pas comprise, **réexpliquée en 9.4 ci-dessous**.
8. **Sort de qid 106 Première Cellule** → question pas comprise, **réexpliquée en 9.5 ci-dessous**.

### 9.1 Impact des réponses sur le design

- **Fini le "non-létal Dormeur"** (plus de script CraftTweaker d'immunité). Le Warden est un vrai boss qu'on tue. Les rewards de Q7 migrent sur le kill.
- **Fini les 2 structures pré-générées**. Le poste Voss n'est qu'une fiction racontée dans 2 carnets Patchouli ; les "balises fantômes" deviennent des fragments sculk pré-scattered dans le Deep Dark (ou drops du Warden mineur via sculk sensor au lieu de blocs custom).
- **Le Nether overhaul + Netherite** prennent une vraie place dans l'intro — la quête chaîne s'élargit à 9 quêtes avec un finale Netherite et un portail Nether ouvert à la fin.
- **Coût impl.** : 3/5 → **2/5** grâce à la suppression des structures. Reste les 11 items custom (Grabber + carnets + drops lore-items) et le combat Warden.

### 9.2 Nouvelle chaîne — 9 quêtes (in-place sur qid 97-105)

> Notation : `[MOD:deeper]` = mod Deeper in the Caves (Linfox), mod_id exact à confirmer par Alexis quand il inspecte le jar.
> `[MOD:netherupdate]` = mod NetherUpdate Netherite [Forge] (FrostBreker).
> `[MOD:betternether]` = BetterNether.

| # | qid | _symbolic              | Titre                         | Task (BQ type)                  | Détail task                                                                                                            | Reward                                                                                                             | Prereq                       |
|---|-----|------------------------|-------------------------------|---------------------------------|------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|------------------------------|
| 1 | 97  | `age2:surface`         | À la Surface                  | `bq_standard:location`          | Se trouver en overworld à >20 blocs du point de sortie CM.                                                             | 1× `nexusabsolu:carnet_voss_v2` + 4× pain + `/say` lore                                                            | `?ref:age1:exit_cm9x9`       |
| 2 | 98  | `age2:carnet_read`     | Les Pages de Voss             | `bq_standard:retrieval`         | Avoir `nexusabsolu:carnet_voss_v2` dans l'inventaire (le joueur l'a reçu en Q1, il doit juste l'ouvrir).               | 1× `nexusabsolu:badge_voss` (item lore) + 1× diamond_pickaxe (pour Ancient Debris plus tard) + `/say` lore Sujet 46 | `?ref:age2:surface`          |
| 3 | 99  | `age2:sculk_signal`    | Signal Sculk                  | `bq_standard:retrieval`         | Collecter 4× `[MOD:deeper]:sculk_fragment` — drop random par sculk sensors pré-générés ou mobs mineurs de Deep Dark.   | **Débloque recette `nexusabsolu:grabber_voss`** + 1× iron_sword + 8× torches                                        | `?ref:age2:carnet_read`      |
| 4 | 100 | `age2:grabber_craft`   | Le Sac du Sujet 46            | `bq_standard:crafting` OU `retrieval` | Craft 1× `nexusabsolu:grabber_voss`.                                                                            | **Gameplay-shift** : inventaire 36 slots permanent + 16× torch + 8× pain + 2× golden_apple                          | `?ref:age2:sculk_signal`     |
| 5 | 101 | `age2:deep_descent`    | La Descente                   | `bq_standard:location`          | Entrer dans le biome `[MOD:deeper]:deep_dark` (Y<0, accès par trou bedrock pré-généré).                                | 1× `nexusabsolu:lanterne_voss` + 4× glowstone + 1× potion_night_vision                                              | `?ref:age2:grabber_craft`    |
| 6 | 102 | `age2:warden_kill`     | Celui Qui Dort                | `bq_standard:hunt`              | **Tuer 1× `[MOD:deeper]:warden`** (boss tier fort, équivalent ATM10).                                                  | **3× ae2:inscriber_press (Logic + Calc + Eng)** + 1× `nexusabsolu:fragment_memoire_1` + 16× XP + `/say` lore        | `?ref:age2:deep_descent`     |
| 7 | 103 | `age2:nether_open`     | La Porte de Voss              | `bq_standard:location`          | Entrer dans la dim `-1` (Nether) via portail vanilla (crafté avec obsidian).                                           | 1× `nexusabsolu:carnet_voss_v3` (entrées "Nether Voss" + coordonnées) + 4× fire_charge                              | `?ref:age2:warden_kill`      |
| 8 | 104 | `age2:ancient_debris`  | Le Fragment de Voss           | `bq_standard:retrieval`         | Miner 4× `[MOD:netherupdate]:ancient_debris` (pioche diamant requise, Alexis a donné la diamond_pickaxe en Q2).        | 4× `[MOD:netherupdate]:netherite_scrap` (head start) + 4× gold_ingot + `/say` lore Netherite/Voss                   | `?ref:age2:nether_open`      |
| 9 | 105 | `age2:netherite_forged`| Héritage Forgé                | `bq_standard:retrieval`         | Obtenir 1× `[MOD:netherupdate]:netherite_ingot` (forgé via 4 scrap + 4 gold).                                          | **1× ae2:controller** + 4× `ae2:material` (logic processor) + ouverture 3 voies (cmd `/gamestage add age2_cross`)   | `?ref:age2:ancient_debris`   |

### 9.3 Mapping ancien → nouveau contenu

| qid | Ancien nom              | Nouveau nom           | Type de changement                                                     |
|-----|-------------------------|-----------------------|------------------------------------------------------------------------|
| 97  | Premier Souffle         | À la Surface          | réécriture complète (plus de "pose un lit")                             |
| 98  | Le Retour               | Les Pages de Voss     | réécriture — lecture du carnet, plus retour CM                          |
| 99  | Campement de Fortune    | Signal Sculk          | réécriture — bug desc≠task corrigé                                      |
| 100 | Transfert des Richesses | Le Sac du Sujet 46    | réécriture — plus de fetch 64 iron, Grabber gameplay-changing à la place|
| 101 | Le Quartz Bleu          | La Descente           | réécriture — skip du certus quartz manuel, direct Deep Dark             |
| 102 | La Pierre Affuteuse     | Celui Qui Dort        | réécriture — vrai combat Warden                                         |
| 103 | Les Presses de Voss     | La Porte de Voss      | réécriture — plus de météorite RNG, les presses viennent du kill Warden |
| 104 | Premier Processeur Log. | Le Fragment de Voss   | réécriture — chaîne Netherite                                           |
| 105 | Le Contrôleur           | Héritage Forgé        | réécriture — reward = ME Controller, plus craft direct                  |
| 106 | Première Cellule        | **CONSERVÉE**         | juste son prereq migre de 105 vers `?ref:age2:netherite_forged`         |

### 9.4 Point 7 réexpliqué — « In-place vs remplacement »

**Le problème concret** : dans `age2.json`, il y a déjà 10 quêtes avec les numéros 97 à 106. On veut changer ces quêtes. Deux façons :

**Option A — In-place (je recommande)**
On garde les **mêmes numéros** (97 à 106) et on change juste le titre, le texte, la tâche et la récompense **à l'intérieur** de chacune.
Comme si tu renommais 10 dossiers dans une armoire sans déplacer les dossiers.
- ✅ Plus simple à coder (10 éditions ciblées dans le fichier).
- ✅ Dans un save existant, les quêtes déjà "terminées" resteront cochées — pas de régression de progression.
- ✅ Les quêtes qid 107+ qui dépendent de ces IDs ne cassent pas.
- ⚠️ Si un joueur a déjà terminé l'ancienne qid 97 "Premier Souffle", sa quête apparaîtra comme terminée MAIS avec le nouveau titre "À la Surface" — il n'aura pas vu le nouveau contenu. Anecdotique sur un pack en cours de dev.

**Option B — Remplacement**
On crée 10 nouvelles quêtes avec de nouveaux numéros (2000 à 2009) et on **supprime** les anciennes.
Comme si tu enlevais 10 dossiers et tu en remettais 10 neufs à la place.
- ⚠️ Tous les prereqs des qid 107+ qui pointaient vers 97-106 doivent être réécrits pour pointer vers 2000-2009. Travail supplémentaire.
- ⚠️ Dans un save existant, les nouvelles quêtes partent "non-commencées" → le joueur doit les refaire.
- ✅ Fichier plus propre "philosophiquement" — pas de débris de l'ancienne intro.

**Ma recommandation** : **Option A (in-place)**. C'est plus rapide, moins de risques, et on respecte la convention du `_meta.json` "existing numeric questIDs are preserved verbatim".

👉 **Question pour toi** : OK pour Option A ? (si oui, je continue ; si non, dis-moi pourquoi et on va sur B)

### 9.5 Point 8 réexpliqué — « qid 106 Première Cellule »

**Le contexte** : aujourd'hui qid 106 s'appelle "Première Cellule" et demande au joueur de craft 1× ME Drive + 1× 1k Storage Cell. Elle vient juste après qid 105 ("Le Contrôleur" dans l'ancienne version).

**Dans le nouveau plan**, mes 9 quêtes refondues prennent les slots 97 à 105. La place 106 est encore là — il faut décider ce qu'on en fait.

**Option (a) — La garder quasi intacte, migrer juste son prereq (je recommande)**
- Son titre, son texte, sa tâche (craft ME Drive + 1k Cell) et sa récompense restent identiques.
- On change juste **qui débloque cette quête** : au lieu de dépendre de "Le Contrôleur" (qui n'existe plus), elle dépend de "Héritage Forgé" (ma nouvelle qid 105).
- Résultat : la chaîne AE2 existante (qid 107+ canaux, terminal, crafting terminal, etc.) continue normalement derrière qid 106.

**Option (b) — La réécrire elle aussi**
- On en fait une 10ème quête du nouveau Poste Voss (ex : "Le Réseau Voss", qui demande d'activer un mini-réseau ME).
- Plus de travail, mais ça uniformise le ton narratif sur 10 quêtes.

**Ma recommandation** : **Option (a)**. qid 106 "Première Cellule" est une bonne transition naturelle vers la suite AE2. Pas besoin de la toucher, juste migrer son prereq.

👉 **Question pour toi** : OK pour Option (a) ? (qid 106 conservée telle quelle, juste son prereq migre vers `age2:netherite_forged`)

### 9.6 Question ATM10 — portée des 3 boss

Tu as dit « je veux comme ATM10 où il faut tuer l'Ender Dragon, le boss du Nether, et le Warden ». Ma compréhension :

- **Warden** → boss de fin d'**intro Age 2** (= Q6 de ma chaîne ci-dessus). ✅ intégré.
- **Ender Dragon** → grande étape plus tard dans l'Age 2 ou en entrée d'Age 3.
- **Boss du Nether** (Wither ? ou un boss custom de BetterNether ?) → grande étape mi-Age 2 probablement, après avoir ouvert le Nether via Q7-9 de l'intro.

👉 **Question pour toi** : les 3 boss sont-ils tous dans l'Age 2 (Warden = intro, Wither/Nether = milieu, Ender Dragon = fin) ? Ou tu les veux répartis différemment (ex. Warden = Age 2 intro, Wither = Age 2 fin, Ender Dragon = Age 3 opening) ?

**Cette session** : je ne traite que l'intro (qid 97-106). Les 2 autres boss seront planifiés dans une section dédiée plus tard.

### 9.7 Checklist finale avant écriture de `age2.json`

- [ ] Tu valides **9.4** → Option A (in-place) pour les qid 97-105.
- [ ] Tu valides **9.5** → Option (a) pour qid 106 (migration prereq seulement).
- [ ] Tu confirmes **9.6** → portée des 3 boss (répondre juste par "tous Age 2" ou "Warden A2 + Wither A2 + Dragon A3" ou autre).
- [ ] Tu me donnes le **mod_id exact** de Deeper in the Caves (`modid:warden`, `modid:sculk_fragment`, `modid:deep_dark`) — tu peux ouvrir le jar et regarder `mods.toml` ou `mcmod.info`, ou je peux le faire si tu me dis où est le fichier jar.
- [ ] Tu me donnes le **mod_id exact** de NetherUpdate Netherite (`modid:ancient_debris`, `modid:netherite_scrap`, `modid:netherite_ingot`).
- [ ] Tu confirmes qu'on peut utiliser `bq_standard:hunt` (task Better Questing standard) pour le kill Warden — si le boss a un entity_id exposé.
- [ ] Coding Java : tu es OK pour que je planifie 11 nouveaux items Java (`grabber_voss` + carnets + lore items + lanterne) dans `mod-source/src/main/java/` ? Ou tu préfères que ce soit CraftTweaker / ContentTweaker (moins de code Java mais plus limité) ?

Dès que ces points sont tranchés, je passe au plan technique final (quelles lignes exactes dans `age2.json`, quels fichiers Java à créer, quels scripts CT) et j'exécute.

Sources de recherche (Section 9) :
- [Deeper in the Caves — CurseForge (1.12.2 version)](https://www.curseforge.com/minecraft/mc-mods/linfox-stacked-dimensions-warden-edition)
- [NetherUpdate Netherite — CurseForge](https://www.curseforge.com/minecraft/mc-mods/netherite-1-7-10-1-12-1-14)
- [NetherUpdate Netherite — Modrinth](https://modrinth.com/mod/netherupdate-netherite)
- [BetterNether — CurseForge](https://www.curseforge.com/minecraft/mc-mods/betternether)

---

## 10. Étude du modèle ATM10 — repenser la place des 3 boss dans Nexus

Alexis a demandé : « regarde comment ATM10 a fait, c'est super bien fait leur système de quête ». Voici ce qui ressort de l'étude de leur quest book, appliqué à Nexus.

### 10.1 Les 5 patterns ATM10 qui marchent

**1. Structure hiérarchique Quest Groups → Chapters → Quests**
ATM10 organise tout en 3 niveaux : des **groupes** (Main, Technology, Magic, Resources, Tools & Gear), chaque groupe contient des **chapitres** (1 par gros mod typiquement : Mekanism, AE2, Ars Nouveau, etc.), et chaque chapitre contient des **quêtes** individuelles. Le joueur peut naviguer horizontalement entre les chapitres d'un même groupe.
→ *Différence avec Nexus* : on a aujourd'hui 4 questLines à plat (Age 0, Age 1, Age 2, Coding). Pas de groupes. Pas de chapitres intra-age. Ça donne une grande page verticale dans BQ où tout est mélangé.

**2. Branch-and-converge**
Le schéma de dépendances ATM10 fait diverger les chemins puis les reconverger sur des milestones. Exemple : après le hub d'ouverture, 3 branches parallèles (tech / magie / ressources) convergent sur le Warden, puis divergent à nouveau vers 3 branches tier-2.
→ *Différence avec Nexus* : on a actuellement une chaîne linéaire dans l'Age 2 (97→98→...→106), pas de branches. L'intro actuellement proposée en Section 9 est aussi linéaire.

**3. Les 3 boss vanilla = GATES, pas ÉTAPES NARRATIVES**
ATM10 ne traite pas Warden/Wither/Dragon comme « 3 climax du jeu ». Ce sont des **portes qui débloquent des tiers de matériaux**, exactement comme une Alloy Kiln débloque le Bronze.

| Boss | Gate vers | Équivalent Nexus probable |
|------|-----------|---------------------------|
| Warden (Deep Dark) | **Allthemodium** (tier 1 ATM) | **Vossium-III** ou équivalent tier-1 magique |
| Wither | Nether Star → summon **The Harbinger** (boss Cataclysm) + crafts avancés | **Composé Stellaire** pour les fragments Nexus fin Age 2 |
| Ender Dragon | End access → **Unobtainium / Vibranium** (tier 2-3 ATM) | **Fragment Stellaire** + matériaux Draconic pour Age 3 |

Point crucial : ATM10 les a **tous dans la partie "early-to-mid game"**, parce que ce sont des portes. L'endgame ATM10, ce n'est pas tuer l'Ender Dragon — c'est tuer les 8 boss Cataclysm (Netherite Monstrosity → Ender Guardian → Harbinger → Ancient Remnant → Leviathan → Scylla → Maledictus → Ignis) et les 8 boss Twilight Forest (Naga → Lich → Minoshroom → Hydra → Knight Phantoms → Ur-Ghast → Alpha Yeti → Snow Queen), sur des mods ajoutés.

**4. Sequential boss mods en PARALLÈLE, pas imbriqués dans la main line**
Les 8 boss Twilight Forest sont obligatoires DANS LE TWILIGHT FOREST (chaque boss débloque le suivant), mais le Twilight Forest entier est une **branche optionnelle** du groupe Main. Le joueur peut faire Twilight Forest ou pas — ça donne des rewards mais ce n'est pas sur le chemin critique.
→ *Transposable à Nexus* : on peut introduire Twilight Forest / Cataclysm / autres boss packs comme branches **optionnelles** de l'Age 2, qui donnent du bonus mais ne bloquent pas l'ATM Star nexus-équivalent.

**5. Reward loot-table (drops randomisés) en complément des rewards fixes**
ATM10 utilise 3 types de rewards : items fixes, XP, **et random draws depuis une loot table**. Ça donne de la variété (2 joueurs qui refont la même quête ne reçoivent pas exactement les mêmes rewards) et économise du travail de design (une seule loot table → N quêtes).
→ *Nexus actuel* : 100 % items fixes. Opportunité d'ajouter un type `bq_standard:loot` à la fin d'Age 2 pour les quêtes « explorer le Deep Dark » / « nettoyer le Nether ».

### 10.2 Gaps dans la documentation ATM10

Honnêteté : la doc publique ATM10 ne dit **pas explicitement** où sont positionnés les 3 boss vanilla dans leur questbook (leurs guides parlent de matériaux et de tiers, pas de chronologie quête). L'analyse ci-dessus est une inférence à partir des mécaniques de dépendance et du matériau que chaque boss débloque. Le vrai questbook ATM10 (stocké en SNBT dans `ftbquests/quests/chapters/*.snbt`) donnerait les placements exacts si on voulait valider — c'est téléchargeable depuis le repo `AllTheMods/ATM-10` sur GitHub mais je n'ai pas fetché le SNBT direct dans cette session.

### 10.3 Proposition de **boss map Nexus Age 2** inspirée du modèle ATM10

À la place de « les 3 boss sont 3 quêtes de l'intro Age 2 », je propose un placement étalé sur l'**Age 2 complet**, chacun gâtant un tier de matériau Nexus :

```
ÂGE 2 (vue d'ensemble — proposition)
════════════════════════════════════════════════════════════════════
              INTRO                        MID                 FIN
  ┌────────────────────────┐  ┌────────────────────┐  ┌────────────────┐
  │ qid 97-106             │  │ qid 107-140        │  │ qid 141-156    │
  │                        │  │                    │  │                │
  │ 9 quêtes refondues     │  │ 3 branches         │  │ Convergence    │
  │ + Première Cellule     │  │ parallèles :       │  │                │
  │                        │  │                    │  │                │
  │ ▼ Warden en Q6 ◄───────┼──┤ AE2 (107-116)      │  │ Wither ◄───────┤ gate Composé Stellaire
  │   gate Sculk + 3       │  │ Botania (117-120)  │  │   → summon     │
  │   AE2 Presses          │  │ Myst. Agri.(121-26)│  │   "Sujet 00"   │ boss custom intermédiaire
  │                        │  │                    │  │                │
  │ ▼ Netherite Q7-9 ◄─────┼──┤ + side: Nether,    │  │ Ender Dragon ◄─┤ gate End +
  │   (BetterNether +      │  │   Deep Dark,       │  │   → Fragment   │ Fragment Organique
  │   NetherUpdate)        │  │   Twilight Forest  │  │   Stellaire    │
  └────────────────────────┘  └────────────────────┘  └────────────────┘
         ▲                          ▲                        ▲
   gate Grabber +              gate tier AE2             gate sortie
   Inscriber Presses           autocraft + mana          vers Age 3
```

### 10.4 Placement concret des 3 boss — proposition

| Boss | Moment | qid approximatif | Gate vers | Récompense principale |
|------|--------|------------------|-----------|-----------------------|
| **Warden** (Deeper in the Caves) | **Intro Age 2, Q6 / qid 102** | `age2:warden_kill` | Sculk drops + 3 AE2 Inscriber Presses + accès Deep Dark comme ressource tier-1 | 3× ae2:inscriber_press + sculk_fragments + fragment_memoire_1 |
| **Wither** (vanilla, buffable via mod) | **Mi-Age 2, qid ≈135** | `age2:wither_kill` | Nether Star → summon boss custom "Sujet 00" qui drop le Composé Stellaire (pour fragments fin Age 2) | 1× nether_star + 2× composé_stellaire_shard |
| **Ender Dragon** (vanilla) | **Fin Age 2, qid ≈155** (ou début Age 3, à décider) | `age2:dragon_kill` | Fragment Stellaire complet + accès End → matériaux Draconic pour Age 3 | 1× fragment_stellaire + 1× dragon_egg + 8× dragon_breath |

**Logique** :
- Le Warden est **tôt** parce qu'il débloque les AE2 Presses (c'est un gate **obligatoire** pour toute l'industrie AE2). Position = intro.
- Le Wither est **mi-Age 2** parce qu'il débloque le Composé Stellaire qui est déjà requis par les quêtes existantes qid 151-155 (Journal 47, Composés Oubliés, Testament). Le Wither devient le **matériau-gate** de la chaîne Testament, pas juste un « crafts Nether Star ».
- L'Ender Dragon est **fin Age 2** (ou début Age 3) parce qu'il débloque l'accès End qui a besoin pour l'Age 3 de toute façon. Ça évite de dédoubler « kill Dragon » entre deux ages.

### 10.5 Changements en cascade sur la chaîne Age 2 existante

Si on accepte ce placement, ça implique :
- **Nouvelle quête qid ≈135** « Sujet 00 » (boss custom summon via Nether Star) → ajoutée au milieu de l'Age 2, insère un point de rupture dans la chaîne AE2/Botania/Mystag.
- **Migration de prereq** pour les quêtes Testament existantes (qid 151-155) : elles dépendent actuellement de qid 142 (fin chaîne linéaire). On les fait dépendre de `age2:dragon_kill` → plus cohérent narrativement, et ça force le joueur à tuer le Dragon avant le Testament.
- La quête actuelle qid 156 « VERS L'AGE 3 » garde son prereq triple (142, 150, 155) mais on ajoute `age2:dragon_kill` → 4 prereqs.

### 10.6 Ce qui reste côté intro (inchangé vs Section 9)

La chaîne des 9 quêtes de l'intro Age 2 reste telle qu'en Section 9.2 — c'est juste le **contexte global** qui change : l'intro est maintenant clairement positionnée comme **un chapitre d'ouverture** qui débloque 3 branches parallèles (AE2 / Botania / Mystag), avec le Warden comme porte du chapitre.

Les 6 blockers de la Section 9.7 restent les mêmes — rien de tout ça n'est écrit dans `age2.json`.

### 10.7 Nouvelles questions ouvertes (Section 10)

- **Est-ce qu'on structure l'Age 2 en groupes/chapitres façon ATM10 ?** BQ supporte techniquement 1 questLine par chapitre. Dans Nexus aujourd'hui, on a 1 questLine = 1 Age. Si on veut adopter la structure ATM10, il faudrait passer à **N questLines par Age** (ex: Age 2 Intro, Age 2 AE2, Age 2 Botania, Age 2 Mystag, Age 2 Testament). Ça rendrait la navigation dans BQ beaucoup plus claire. **Lourd à migrer** (il faut bouger les quêtes entre lignes dans `lines.json`). Je recommande de le garder pour une passe future, **pas dans cette session**.
- **Wither = boss vanilla suffisant, ou on fait summon un boss custom dessus ?** ATM10 utilise le Wither comme prérequis pour summon "The Harbinger". On peut faire pareil : Wither vanilla → drop Nether Star → crafter un item d'invocation → summon "Sujet 00" qui est le vrai boss drop-source du Composé Stellaire. Plus mémorable narrativement mais ça demande un boss custom.
- **Boss Sujet 00** : s'il existe, c'est un item Java custom (entity + AI + loot) — **gros boulot**. Alternative : réutiliser un boss de Twilight Forest (Lich ? Ur-Ghast ?) comme proxy et l'habiller via un CT reward handler.
- **Placement Ender Dragon : fin Age 2 ou début Age 3 ?** J'ai mis fin Age 2 mais c'est discutable. Fin Age 2 = tu dois tuer le Dragon pour quitter l'Age. Début Age 3 = le Dragon est la première mission d'Age 3. Les deux marchent ; fin Age 2 est plus « punchline », début Age 3 est plus « ouverture ».

### 10.8 TL;DR

**Ce que j'apporte comme changement concret** : replacer Wither et Ender Dragon **hors de l'intro** (parce qu'ATM10 n'a pas 3 boss en intro non plus — ils sont étalés), les lier chacun à un matériau-gate (Composé Stellaire / Fragment Stellaire) qui existe déjà dans ta chaîne Testament fin Age 2, et garder le Warden comme boss de fin d'intro.

**Ce que je ne change pas dans cette session** : la structure BQ (1 questLine par Age), les 9 quêtes d'intro de la Section 9.2, les chaînes AE2/Botania/Mystag actuelles.

**Nouvelle question pour toi** (en plus des 6 blockers Section 9.7) :
- [ ] **10-Q1** : OK pour le placement étalé Warden=intro / Wither=mi-Age 2 / Dragon=fin Age 2 ?
- [ ] **10-Q2** : Wither vanilla ou invocation "Sujet 00" custom ?
- [ ] **10-Q3** : Ender Dragon fin Age 2 OU début Age 3 ?
- [ ] **10-Q4** : Regroupement en sous-chapitres (plusieurs questLines) pour l'Age 2 — tu veux qu'on le garde en session future ou c'est mort d'office ?

Sources (Section 10) :
- [ATM10 Bosses Guide (all-themods.com)](https://all-themods.com/bosses/)
- [ATM10 Beginner Guide (all-themods.com)](https://all-themods.com/beginner-guide/)
- [ATM10 Quest System (DeepWiki)](https://deepwiki.com/AllTheMods/ATM-10/2-quest-system)
- [ATM10 ATM Star & Custom Items (DeepWiki)](https://deepwiki.com/AllTheMods/ATM-10/3.1-atm-star-and-custom-items)
- [ATM10 CurseForge](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10)
