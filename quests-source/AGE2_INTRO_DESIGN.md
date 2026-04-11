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
