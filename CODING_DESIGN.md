# Ligne Coding — du levier au programme

Ligne de quêtes BetterQuesting n°3, 30 quêtes (IDs 3000-3029). Générée par
`scripts/gen/gen_quests_coding.py` : ne pas l'éditer à la main dans
`DefaultQuests.json`, relancer le générateur.

## Obligatoire ou pas : oui et non

- **Non** : les 30 quêtes sont un tutoriel parallèle, à son rythme. Elles
  s'ouvrent avec l'Âge 2 (prérequis : Q97 « Tu es sorti »).
- **Oui** : une seule quête est obligatoire, **« ★ Le Rapport de Production »
  (3028)**. « VERS L'ÂGE 3 » (156) l'exige à la place de l'ancienne « Première
  Turtle » (150), qui jouait déjà ce rôle.
  - Elle ne dépend que du **Void Ore Miner T1** (Q123), construit juste avant
    dans l'Âge 2. Un joueur qui sait déjà programmer n'a pas à faire le tutoriel.
  - Elle apparaît dans les deux onglets : Coding et Âge 2 (à côté de 156).
  - Demande : mesurer combien de minerais le Void Miner produit par minute, et
    en rendre compte sur une page imprimée ou une EEPROM au bon titre.

### Ce qu'une quête peut vérifier

BetterQuesting ne voit pas le code. On exige donc un **item dont le NBT porte la
trace du travail**. La comparaison de BQ
(`ItemComparison.CompareNBTTag`) accepte un NBT qui *contient* celui demandé,
listes comprises en `partialMatch`.

| Mod | Preuve | NBT exigé |
|---|---|---|
| RFTools Control | la carte programme utilise tel opcode | `grid:[{id:"eval_countinv"}, …]` |
| ComputerCraft | page imprimée avec un titre (`printer.setPageTitle`) | `title:"Rapport de Production"` |
| OpenComputers | EEPROM gravée avec un label (`flash` ou `eeprom.setLabel`) | `oc:data:{oc:label:"Rapport de Production"}` |

La quête 3028 est en logique **OU** : page imprimée ou EEPROM.

**Limite assumée.** Pour les cartes RFTools Control, la preuve est réelle : il
faut avoir posé ces opcodes. En revanche, le titre d'une page ou le label d'une
EEPROM s'obtiennent sans rien mesurer. Par exemple, le menu *Print* de `edit`
prend le nom du fichier comme titre, et `label -a` renomme une EEPROM. La quête
le dit au joueur : le chiffre est sur l'honneur. Exiger aussi `line0` (première
ligne de la page, 25 caractères) relèverait la barre sans la rendre étanche.

`scripts/check/test_quests_coding.py` porte la comparaison de BQ en Python et la
teste sur des items reconstitués d'après le code des mods : 28 cas, dont 14
refus attendus (casse du titre, disquette vierge au lieu d'OpenOS, BIOS au lieu
d'un flash…).

## Les paliers

| Palier | Quêtes | Mod | Concept |
|---|---|---|---|
| 0 — La logique, sans code | 3000-3005 | RFTools (blocs de logique) | entrée/sortie, condition, événement, variable, boucle, ET/OU |
| 1 — Programmer avec des blocs | 3006-3011 | RFTools Control | opcodes, variables, tests, déplacer des items, débit par minute |
| 2 — Écrire du code | 3012-3021 | ComputerCraft 1.80 (Lua 5.1) | REPL, fichiers, boucles, redstone, événements, moniteur, turtle, rednet, imprimante |
| 3 — Les composants | 3022-3027 | OpenComputers 1.8.9 (OpenOS, Lua 5.3) | montage, OpenOS, composants, transposer, robot, EEPROM. La carte internet (`wget`) et le GPU T2 demandent un boîtier T2, donné en récompense de 3025 |
| Finale | 3028, 3029 | tous | Rapport de Production (obligatoire), tableau de bord (AE2, Extreme Reactors) |

Chaque concept du palier 0 est repris en code aux paliers suivants. La quête 12
(débit par minute en RFTools Control) prépare la quête verrou sans une ligne de
code.

## Programmes fournis

Dans `programmes/`, téléchargeables en jeu avec `wget` (HTTP est activé dans les
deux mods) :

| Fichier | Mod | Usage |
|---|---|---|
| `cc/phare.lua` | CC | événements : clignote, s'arrête au levier |
| `cc/tunnel.lua` | CC | turtle : tunnel 1×2, gravier, bedrock, carburant, retour |
| `cc/radio.lua` | CC | rednet : écoute / envoie |
| `cc/rapport.lua` | CC | **quête verrou** : turtle + imprimante |
| `oc/trieur.lua` | OC | transposer : trie les minerais |
| `oc/rapport.lua` | OC | **quête verrou** : transposer, puis `flash` sur EEPROM |

**Les URLs pointent sur `main`** : elles répondent 404 tant que la branche n'est
pas fusionnée.

Tests : `programmes/test/`. Les programmes CC tournent sous **LuaJ 2.0.3**, le
moteur exact de ComputerCraft 1.80. Les programmes OC tournent sous **Lua 5.3**.
Les deux utilisent des bouchons qui simulent le Void Miner, les coffres et
l'imprimante. Le test du tunnel a trouvé une boucle infinie devant la bedrock,
corrigée.

```
java -cp luaj-jse-2.0.3.jar lua programmes/test/run_rapport.lua   # depuis programmes/
python3 programmes/test/run_lua53.py oc/rapport.lua 5 up down     # depuis programmes/
```

## Faits vérifiés dans les sources (et qui piègent)

**ComputerCraft 1.80pr1 (dan200), pas CC:Tweaked**
- Ids à méta : `computer` (0 normal, 16384 avancé), `peripheral` (0 lecteur,
  1 modem sans fil, 2 moniteur, 3 imprimante, 4 moniteur avancé, 5 haut-parleur),
  `cable` (0 câble, 1 modem filaire).
- `wired_modem_full`, `monitor_advanced`, `computer_advanced` n'existent pas.
- Un ordinateur ou une turtle étiquetés changent de damage : les quêtes exigent
  le joker 32767.
- Une turtle peut être rangée sous trois items (`turtle`, `turtle_expanded`,
  `turtle_advanced`) selon l'outil, le côté et la couleur.
- Un coffre n'est pas un périphérique : pour compter, il faut une turtle
  (`suck`, puis `getItemDetail`, puis `drop`).
- Bugs du jar : `io.lines(fichier)` et `paintutils.loadImage` sont cassés.

**OpenComputers 1.8.9a**
- Items regroupés à méta :
  - `component` : CPU 0-2, RAM T1 6 ;
  - `card` : GPU 1-3, redstone T1 4, internet 8 ;
  - `storage` : EEPROM 0, disquette 1, HDD 2-4 ;
  - `upgrade` : Inventory 17.
- La disquette OpenOS et l'EEPROM Lua BIOS ne se distinguent que par le NBT.
- Recettes par défaut. Le pack donne 8 pépites par lingot, un GPU consomme une
  RAM, et le clavier demande 37 boutons.
- `transferItem` renvoie le nombre d'items déplacés, et les cases commencent
  à 1.

**RFTools Control 2.0.2** (modid `rftoolscontrol`)
- 0 variable sans RAM Chip.
- Il n'y a pas d'opcode `test_count` : on enchaîne `eval_countinv` puis
  `test_gt_number`.
- Pas de transfert direct entre deux coffres : `do_fetchitems` puis
  `do_pushitems`.
- Les connecteurs changent au double-clic, réglage du pack
  (`doubleClickToChangeConnector`).

**Environmental Tech 2.0.20**
- Aucune API ordinateur trouvée.
- Void Miner, sans modificateur : 3 / 3,75 / 7,5 / 15 / 30 / 60 minerais par
  minute de T1 à T6. Le temps de cycle vient du wiki, l'énergie de `main.cfg`.
- Les listes de minerais (`config/environmentaltech/multiblocks/void_miner/ore/`)
  n'ont jamais été adaptées au pack. Elles citent Thaumcraft, DeepResonance…,
  qui sont absents. **À mesurer en jeu une fois.**

## À faire en jeu

1. La ligne s'affiche, les 30 quêtes sont dans l'ordre, et 3028 apparaît aussi
   dans l'onglet Âge 2.
2. Un `flash` avec label, puis retrait de l'EEPROM : l'infobulle doit afficher le
   label. C'est ce que lit la quête ; la persistance du label au retrait n'est pas
   vérifiée dans le code.
3. Mesurer le vrai débit d'un Void Miner T1 avec `rapport.lua` et comparer aux
   3/min théoriques.
4. Vérifier que le driver AE2 d'OpenComputers fonctionne avec AE2-UEL
   (`component.me_controller`). C'est cité dans la quête 30, mais non testé.

## Proposé, pas fait

- **Cœur de Données (Âge 4) — recette avec une EEPROM `NEXUS`.** Le lore dit
  que le Cœur tourne sous NEXUS.lua. Une recette CraftTweaker peut exiger
  `<opencomputers:storage:0>.withTag({"oc:data": {"oc:label": "NEXUS"}})`, car la
  comparaison est partielle et un renommage à l'enclume ne passe pas. Ce serait
  le « oui » de l'Âge 4, comme le Rapport l'est de l'Âge 3.
- **Passer à CC:Tweaked 1.89.2 (1.12.2).** Il lit les coffres sans turtle
  (périphériques génériques) et corrige les bugs de 1.80. En revanche, il faut
  remplacer un jar, et ses ids sont NON VÉRIFIÉS contre les quêtes.
- `quests-source/` est désynchronisé de `DefaultQuests.json`. Il contient
  encore les quêtes 143-149 retirées, et `age4.json` n'est pas fusionné.
  **Ne pas lancer `merge_quests.py`** : il écraserait la ligne Coding et
  l'Âge 3. Les générateurs écrivent directement dans `DefaultQuests.json`.
