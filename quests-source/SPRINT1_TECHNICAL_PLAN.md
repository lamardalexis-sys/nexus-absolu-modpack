# SPRINT1_TECHNICAL_PLAN.md — Refonte intro Age 2 (Sprint 1)

> Playbook d'exécution pour la session suivante. Tout ce qui est nécessaire pour
> livrer les 9 nouvelles quêtes d'intro Age 2 (qid 97-105) + Grabber Voss (item Java) +
> 2 carnets Patchouli, sans revenir en arrière pour (re)designer quoi que ce soit.
>
> À utiliser comme checklist : tout ce qui est marqué `[ ]` est une action à cocher.
> À la fin du sprint, `age2.json` contient les 9 quêtes refondues, le mod JAR contient
> le `grabber_voss`, et Patchouli affiche les Vol.2 + Vol.3.

---

## 0. Décisions Alexis (validées)

| # | Décision | Valeur |
|---|----------|--------|
| 1 | Stratégie qid 97-105 | **In-place** (garder les numéros, réécrire le contenu) |
| 2 | qid 106 "Première Cellule" | **Conservée**, prereq migré vers `age2:netherite_forged` |
| 3 | Grabber Voss | **Item Java custom** (classe `ItemGrabberVoss`) |
| 4 | Mod des caves | **Deeper in the Caves - RESTART (1.12.2)** par Linfox |
| 5 | Mods Nether | **BetterNether** + **NetherUpdate Netherite** (FrostBreker) |
| 6 | Ores | **Option C hybride** : Claustrite→Deep Dark, Nexium→Nether, Endrium nouveau→End, Vossium inchangée (*hors Sprint 1 — pour Sprint 2+*) |
| 7 | Non-létal Warden | **Rejeté**, vrai combat ATM10-style |
| 8 | Structures pré-générées | **Aucune**, tout par lore |
| 9 | Ordre des sprints | **1 → 2 → 3 → 4 → 5** (intro avant tout le reste) |
| 10 | 3 boss vanilla | **Étalés** : Warden=intro / Wither=mi-A2 (*Sprint 4*) / Dragon=fin-A2 (*Sprint 4*) |

---

## 1. Prérequis — ce qu'Alexis apporte au début de la session suivante

### 1.1 Bloquant — sans ces 2 infos, rien ne peut être écrit dans `age2.json`

- [ ] **Mod ID exact de Deeper in the Caves - RESTART (1.12.2)**
  - Ouvrir `mods/<le jar Deeper in the Caves>` avec 7-Zip
  - Regarder `mcmod.info` → champ `modid`
  - Aussi noter les IDs des items/entités critiques :
    - Entité du Warden : `<modid>:<entity_name>` (probablement `warden` ou similaire)
    - Drop sculk principal : `<modid>:<sculk_item>` (au moins 1 item utilisable pour Q99)
    - Biome Deep Dark (si query-able côté BQ) : `<modid>:<biome_name>` ou constante
  - **Valeur attendue** : chaîne de caractères `X`. Je la substituerai à `<MOD_DEEPER>` dans tout le doc + les JSON pré-écrits.

- [ ] **Mod ID exact de NetherUpdate Netherite [Forge]** (FrostBreker)
  - Ouvrir `mods/<le jar NetherUpdate Netherite>` avec 7-Zip → `mcmod.info` → `modid`
  - Noter les IDs de 3 items :
    - `<modid>:ancient_debris` (ou nom alternatif)
    - `<modid>:netherite_scrap`
    - `<modid>:netherite_ingot`
  - **Valeur attendue** : chaîne `Y`. Substituée à `<MOD_NETHERUPDATE>`.

### 1.2 Non-bloquant — informations utiles mais on peut avancer sans

- [ ] **Confirmer l'entity name exact du Warden** (peut-être pas juste "warden" — certains mods 1.12.2 utilisent des noms comme `deeperandcaves:ancient_warden` ou similaire)
- [ ] **Confirmer si BQ peut détecter les biomes Deep Dark via `bq_standard:location`** (si non, on fallback sur une détection Y<0 + dim=0 en combinaison)
- [ ] **Screenshot ou copie du contenu de `mods/`** (modlist.txt actuel ne liste pas ces 3 nouveaux mods — c'est probablement juste que `modlist.txt` n'a pas été mis à jour côté Alexis)

### 1.3 Ce que moi (Claude) apporte
- [x] Les 9 quest JSON pré-écrits (Section 3 ci-dessous) prêts à merge
- [x] Spec complète du `ItemGrabberVoss` Java (Section 4)
- [x] Contenu intégral des carnets Patchouli Vol.2 + Vol.3 en français (Section 5)
- [x] Checklist d'exécution pas à pas (Section 6)
- [x] Plan de validation + rollback (Section 7)

---

## 2. Vue d'ensemble — les 9 quêtes Sprint 1

Rappel de la chaîne validée (Section 9.2 d'`AGE2_INTRO_DESIGN.md`) :

| # | qid | _symbolic | Titre | Task BQ | Reward principal | Prereq |
|---|-----|-----------|-------|---------|------------------|--------|
| 1 | 97 | `age2:surface` | À la Surface | `location` (dim 0) | 1× `carnet_voss_v2` + 4× pain | Age 1 exit (qid 149) |
| 2 | 98 | `age2:carnet_read` | Les Pages de Voss | `retrieval` (carnet) | 1× `badge_voss` + 1× diamond_pickaxe | Q97 |
| 3 | 99 | `age2:sculk_signal` | Signal Sculk | `retrieval` (4× sculk_fragment) | Unlock recette Grabber + iron_sword | Q98 |
| 4 | 100 | `age2:grabber_craft` | Le Sac du Sujet 46 | `retrieval` (grabber_voss) | +36 slots permanents + food | Q99 |
| 5 | 101 | `age2:deep_descent` | La Descente | `location` (dim 0 + Y<0) | 1× `lanterne_voss` + night vision potion | Q100 |
| 6 | 102 | `age2:warden_kill` | Celui Qui Dort | `hunt` (1× Warden) | **3× ae2:inscriber_press** + fragment_memoire_1 | Q101 |
| 7 | 103 | `age2:nether_open` | La Porte de Voss | `location` (dim -1) | 1× `carnet_voss_v3` + 4× fire_charge | Q102 |
| 8 | 104 | `age2:ancient_debris` | Le Fragment de Voss | `retrieval` (4× ancient_debris) | 4× netherite_scrap + 4× gold_ingot | Q103 |
| 9 | 105 | `age2:netherite_forged` | Héritage Forgé | `retrieval` (1× netherite_ingot) | 1× ae2:controller + 4× logic_processor + cmd gamestage | Q104 |

+ qid 106 "Première Cellule" **inchangée dans son contenu**, juste son `preRequisites:11` passe de `[105]` à `[105]` (même valeur numérique, mais sémantiquement c'est le nouveau qid 105 "Héritage Forgé" au lieu de l'ancien "Le Contrôleur").

> **Note importante** : la valeur numérique `105` dans `preRequisites:11` ne change PAS (on est en in-place). C'est le CONTENU du qid 105 qui change. qid 106 restera donc intact dans `age2.json`.

---

## 3. Les 9 quêtes — JSON pré-écrit (format BQ source)

> Toutes les valeurs `<MOD_DEEPER>` et `<MOD_NETHERUPDATE>` sont des **placeholders**
> à remplacer par les mod_ids confirmés par Alexis en début de session.
> Une fois les 2 valeurs substituées, ces blobs remplacent directement les quêtes
> correspondantes dans `quests-source/age2.json`.

### 3.1 Convention de substitution

```
<MOD_DEEPER>       → exemple : "deeperandcaves" ou "linfox_deepercaves" (à confirmer)
<MOD_NETHERUPDATE> → exemple : "netherupdate" ou "frostnetherite" (à confirmer)
<NEXUS>            → "nexusabsolu" (déjà connu, pas besoin de substitution)
```

### 3.2 qid 97 — "À la Surface" (clé source `149:10`)

```json
{
  "questID:3": 97,
  "preRequisites:11": [149],
  "_symbolic": "age2:surface",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0,
      "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1,
      "partySingleReward:8": "false",
      "tasklogic:8": "AND",
      "repeattime:3": -1,
      "visibility:8": "ALWAYS",
      "simultaneous:1": 0,
      "globalshare:1": 0,
      "questlogic:8": "AND",
      "partysinglereward:1": 0,
      "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0,
      "ismain:1": 1,
      "repeat_relative:1": 1,
      "icon:10": {
        "id:8": "patchouli:guide_book",
        "Count:3": 1,
        "Damage:2": 0,
        "OreDict:8": "",
        "tag:10": { "patchouli:book:8": "nexusabsolu:carnet_voss_v2" }
      },
      "name:8": "§l§6À la Surface",
      "desc:8": "§7§oLa lumière t'aveugle. Tes jambes tremblent.\nTu sens la pluie, le vent, la terre sous tes pieds.§r\n\n§7Trois Ages dans des boîtes.\n§7Tu ne savais même plus si l'extérieur existait encore.\n\n§8§o\"Jour 0 — Sujet S-47 sorti du confinement Voss-7.\n§8Aucune surveillance active depuis l'effondrement du Poste.\n§8Bienvenue dehors, enfant.\"\n— Journal automatisé§r\n\n§e§lObjectif : §7Quitte la Compact Machine 9x9\n§e§lRécompense : §7Carnet de Voss Vol. II"
    }
  },
  "tasks:9": {
    "0:10": {
      "index:3": 0,
      "taskID:8": "bq_standard:location",
      "name:8": "Overworld",
      "dim:3": 0,
      "range:3": -1,
      "posX:3": 0,
      "posY:3": 0,
      "posZ:3": 0,
      "visible:1": 0,
      "hideInfo:1": 0,
      "taxiCab:1": 0,
      "invert:1": 0
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item",
      "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "nexusabsolu:carnet_voss_v2", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:bread", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:experience_bottle", "Count:3": 2, "Damage:2": 0, "OreDict:8": "" }
      }]
    },
    "1:10": {
      "rewardID:8": "bq_standard:command",
      "index:3": 1,
      "hideBlockIcon:1": 1,
      "asScript:1": 1,
      "description:8": "",
      "viaPlayer:1": 0,
      "title:8": "bq_standard.reward.command",
      "command:8": "/say §5[Nexus Absolu]§r §7VAR_NAME§r sort enfin de sa boîte. L'Âge 2 commence."
    }
  }
}
```

### 3.3 qid 98 — "Les Pages de Voss" (clé source `150:10`)

```json
{
  "questID:3": 98,
  "preRequisites:11": [97],
  "_symbolic": "age2:carnet_read",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": {
        "id:8": "patchouli:guide_book", "Count:3": 1, "Damage:2": 0, "OreDict:8": "",
        "tag:10": { "patchouli:book:8": "nexusabsolu:carnet_voss_v2" }
      },
      "name:8": "§l§6Les Pages de Voss",
      "desc:8": "§7Le Carnet Vol. II est dans ton inventaire.\n§7Ouvre-le. Lis-le. C'est la première fois\n§7que tu vois l'écriture de Voss directement.\n\n§8§oEntrées :\n§8- \"Le Poste\"\n§8- \"Sujet 46\"\n§8- \"Protocole Voss-7\"§r\n\n§7Tu comprendras qu'il t'a surveillé\n§7pendant trois ans. Et que tu n'étais\n§7pas le premier.\n\n§e§lObjectif : §7Garde le Carnet en main\n§e§lRécompense : §7Badge Voss, Pioche Diamant"
    }
  },
  "tasks:9": {
    "0:10": {
      "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "ignoreNBT:1": 0,
      "index:3": 0, "consume:1": 0, "ignoreDamage:1": 0,
      "taskID:8": "bq_standard:retrieval",
      "requiredItems:9": {
        "0:10": { "id:8": "nexusabsolu:carnet_voss_v2", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" }
      }
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "nexusabsolu:badge_voss", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:diamond_pickaxe", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:experience_bottle", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" }
      }]
    }
  }
}
```

### 3.4 qid 99 — "Signal Sculk" (clé source `151:10`)

```json
{
  "questID:3": 99,
  "preRequisites:11": [98],
  "_symbolic": "age2:sculk_signal",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": {
        "id:8": "<MOD_DEEPER>:sculk_fragment", "Count:3": 1, "Damage:2": 0, "OreDict:8": ""
      },
      "name:8": "§l§6Signal Sculk",
      "desc:8": "§7Il y a un bruit. Pas un son, un §obruit§r§7.\n§7Une vibration dans le sol qui te suit\n§7quand tu marches. Voss a écrit là-dessus.\n\n§8§o\"Sujet 46 émet des fragments sculk en permanence.\n§8C'est sa façon de parler. Ou de nous\n§8compter. Je ne sais plus.\"\n— Carnet Voss Vol. II, p.4§r\n\n§7Mine 4 blocs §bSculk§r§7 ou ramasse 4\n§7fragments sculk pour commencer à comprendre.\n\n§e§lObjectif : §74× Fragment Sculk\n§e§lRécompense : §7Recette du Sac du Sujet 46 (débloquée)"
    }
  },
  "tasks:9": {
    "0:10": {
      "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "ignoreNBT:1": 1,
      "index:3": 0, "consume:1": 0, "ignoreDamage:1": 0,
      "taskID:8": "bq_standard:retrieval",
      "requiredItems:9": {
        "0:10": { "id:8": "<MOD_DEEPER>:sculk_fragment", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" }
      }
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "minecraft:iron_sword", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:torch", "Count:3": 16, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:experience_bottle", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" }
      }]
    },
    "1:10": {
      "rewardID:8": "bq_standard:command", "index:3": 1, "hideBlockIcon:1": 1, "asScript:1": 1,
      "description:8": "", "viaPlayer:1": 0, "title:8": "bq_standard.reward.command",
      "command:8": "/gamestage add @p age2_grabber_recipe"
    }
  }
}
```

### 3.5 qid 100 — "Le Sac du Sujet 46" (clé source `152:10`)

```json
{
  "questID:3": 100,
  "preRequisites:11": [99],
  "_symbolic": "age2:grabber_craft",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "nexusabsolu:grabber_voss", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§6Le Sac du Sujet 46",
      "desc:8": "§7Le Sujet 46 portait un sac.\n§7Voss l'appelait le §eGrabber§r§7.\n§7Il stocke dans un pli spatial au lieu\n§7de ton inventaire physique.\n\n§8§o\"Le sac de 46 contient ses affaires,\n§8mais aussi ses souvenirs. Je n'ai jamais\n§8compris comment il a fait ça.\"\n§8— Carnet Voss Vol. II, p.7§r\n\n§7Les fragments sculk suffisent pour\n§7assembler une réplique. Crafte-la.\n\n§e§lObjectif : §71× Grabber Voss\n§e§lRécompense : §7+36 slots permanents + nourriture"
    }
  },
  "tasks:9": {
    "0:10": {
      "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "ignoreNBT:1": 0,
      "index:3": 0, "consume:1": 0, "ignoreDamage:1": 0,
      "taskID:8": "bq_standard:retrieval",
      "requiredItems:9": {
        "0:10": { "id:8": "nexusabsolu:grabber_voss", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" }
      }
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "minecraft:bread", "Count:3": 16, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:torch", "Count:3": 32, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:golden_apple", "Count:3": 2, "Damage:2": 0, "OreDict:8": "" },
        "3:10": { "id:8": "minecraft:experience_bottle", "Count:3": 6, "Damage:2": 0, "OreDict:8": "" }
      }]
    }
  }
}
```

### 3.6 qid 101 — "La Descente" (clé source `153:10`)

```json
{
  "questID:3": 101,
  "preRequisites:11": [100],
  "_symbolic": "age2:deep_descent",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "minecraft:sculk_shrieker_fallback_placeholder_for_<MOD_DEEPER>_sculk_shrieker", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§6La Descente",
      "desc:8": "§7Sous ton point de sortie, les grottes\n§7sont plus profondes que tu ne le pensais.\n§7Trop profondes. Trop §osilencieuses§r§7.\n\n§7Creuse vers le bas. Cherche les trous\n§7pré-percés dans la bedrock — ils mènent\n§7à quelque chose que Voss n'a pas documenté.\n\n§8§o\"Le Deep Dark n'est pas une grotte. C'est\n§8un fossile d'expérience. Voss a ouvert une\n§8porte qu'il n'a jamais refermée.\"\n— Carnet Voss Vol. II, p.11§r\n\n§e§lObjectif : §7Descendre sous Y=0 dans l'Overworld\n§e§lRécompense : §7Lanterne de Voss + potion de vision nocturne"
    }
  },
  "tasks:9": {
    "0:10": {
      "index:3": 0, "taskID:8": "bq_standard:location",
      "name:8": "Deep Dark Area",
      "dim:3": 0, "range:3": -1,
      "posX:3": 0, "posY:3": 0, "posZ:3": 0,
      "visible:1": 0, "hideInfo:1": 0, "taxiCab:1": 0, "invert:1": 0
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "nexusabsolu:lanterne_voss", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:glowstone", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:potion", "Count:3": 2, "Damage:2": 0, "OreDict:8": "",
                  "tag:10": { "Potion:8": "minecraft:long_night_vision" } }
      }]
    }
  }
}
```

> ⚠️ Le task `location` ci-dessus ne peut pas détecter directement "Y<0" car BQ2 n'a pas
> de contrainte Y. Il faut soit : (a) accepter que la quête se valide dès qu'on est dans dim 0
> (elle se complétera dès Q1 en réalité — mauvais), soit (b) utiliser un trigger personnalisé.
> **Fallback** : changer `bq_standard:location` → `bq_standard:advancement` et pointer vers
> `minecraft:adventure/sleep_in_bed` ou un advancement custom `nexusabsolu:enter_deep_dark`
> à créer dans `mod-source/src/main/resources/data/nexusabsolu/advancements/`.
> **Action next session** : décider du trigger exact avec Alexis selon ce que `<MOD_DEEPER>` expose.

### 3.7 qid 102 — "Celui Qui Dort" (clé source `154:10`)

```json
{
  "questID:3": 102,
  "preRequisites:11": [101],
  "_symbolic": "age2:warden_kill",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.wither.spawn",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "minecraft:sculk_catalyst_placeholder_for_<MOD_DEEPER>_item", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§4Celui Qui Dort",
      "desc:8": "§7Il est là. Il te voit par vibration.\n§7Il ne te laissera pas repartir sans combattre.\n\n§7C'était le Sujet 46.\n§7Maintenant c'est autre chose.\n\n§8§o\"Le Sujet 46 a survécu à l'expérience\n§8du 17 mars. Mais il n'est plus vraiment\n§8le Sujet 46. Je n'ai pas le courage de\n§8le terminer moi-même. Je suis désolé.\"\n— Carnet Voss Vol. II, p.14 (dernière entrée)§r\n\n§c§lObjectif : §7Tuer le Gardien\n§c§lDanger : §4EXTRÊME — prépare-toi\n§e§lRécompense : §7Les 3 Presses d'Inscription AE2"
    }
  },
  "tasks:9": {
    "0:10": {
      "index:3": 0, "taskID:8": "bq_standard:hunt",
      "required:3": 1,
      "ignoreNBT:1": 1, "subtypes:1": 0,
      "target:8": "<MOD_DEEPER>:warden",
      "targetNBT:10": {}
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "appliedenergistics2:material", "Count:3": 1, "Damage:2": 13, "OreDict:8": "" },
        "1:10": { "id:8": "appliedenergistics2:material", "Count:3": 1, "Damage:2": 14, "OreDict:8": "" },
        "2:10": { "id:8": "appliedenergistics2:material", "Count:3": 1, "Damage:2": 15, "OreDict:8": "" },
        "3:10": { "id:8": "nexusabsolu:fragment_memoire_1", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "4:10": { "id:8": "minecraft:experience_bottle", "Count:3": 16, "Damage:2": 0, "OreDict:8": "" }
      }]
    }
  }
}
```

> ⚠️ Les 3 presses AE2 ont des damage values `13`, `14`, `15` dans `appliedenergistics2:material`
> pour Logic / Calculation / Engineering. **À vérifier avec JEI in-game** en début de session :
> c'est plus sûr que de faire confiance à ma mémoire des damage values.

### 3.8 qid 103 — "La Porte de Voss" (clé source `155:10`)

```json
{
  "questID:3": 103,
  "preRequisites:11": [102],
  "_symbolic": "age2:nether_open",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "minecraft:obsidian", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§6La Porte de Voss",
      "desc:8": "§7Le Gardien est mort. L'œil dans sa main\n§7gauche contenait des coordonnées.\n§7Pas sur l'Overworld. §cDans le Nether§r§7.\n\n§7Voss y cachait quelque chose — peut-être\n§7lui-même. Construis un portail. Entre.\n\n§8§o\"Le Nether pour moi n'est pas une dimension.\n§8C'est un cimetière que j'ai creusé moi-même.\"\n— Carnet Voss Vol. III, p.1§r\n\n§e§lObjectif : §7Entrer dans le Nether\n§e§lRécompense : §7Carnet de Voss Vol. III + 4 boules de feu"
    }
  },
  "tasks:9": {
    "0:10": {
      "index:3": 0, "taskID:8": "bq_standard:location",
      "name:8": "Nether",
      "dim:3": -1, "range:3": -1,
      "posX:3": 0, "posY:3": 0, "posZ:3": 0,
      "visible:1": 0, "hideInfo:1": 0, "taxiCab:1": 0, "invert:1": 0
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "nexusabsolu:carnet_voss_v3", "Count:3": 1, "Damage:2": 0, "OreDict:8": "",
                  "tag:10": { "patchouli:book:8": "nexusabsolu:carnet_voss_v3" } },
        "1:10": { "id:8": "minecraft:fire_charge", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" }
      }]
    }
  }
}
```

### 3.9 qid 104 — "Le Fragment de Voss" (clé source `156:10`)

```json
{
  "questID:3": 104,
  "preRequisites:11": [103],
  "_symbolic": "age2:ancient_debris",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "<MOD_NETHERUPDATE>:ancient_debris", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§6Le Fragment de Voss",
      "desc:8": "§7Au fond du Nether, des blocs noirs\n§7résistent à ta pioche de fer comme si\n§7c'était du papier. Il te faut du diamant.\n\n§7C'est l'§4Ancient Debris§r§7 — une matière\n§7que Voss a fait tomber dans le Nether\n§7en 2019 pendant un test de densité.\n§7Personne n'était censé la revoir.\n\n§e§lObjectif : §74× Ancient Debris\n§e§lRécompense : §74× Netherite Scrap + 4× Gold"
    }
  },
  "tasks:9": {
    "0:10": {
      "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "ignoreNBT:1": 1,
      "index:3": 0, "consume:1": 0, "ignoreDamage:1": 0,
      "taskID:8": "bq_standard:retrieval",
      "requiredItems:9": {
        "0:10": { "id:8": "<MOD_NETHERUPDATE>:ancient_debris", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" }
      }
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "<MOD_NETHERUPDATE>:netherite_scrap", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "minecraft:gold_ingot", "Count:3": 4, "Damage:2": 0, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:experience_bottle", "Count:3": 8, "Damage:2": 0, "OreDict:8": "" }
      }]
    }
  }
}
```

### 3.10 qid 105 — "Héritage Forgé" (clé source `157:10`)

```json
{
  "questID:3": 105,
  "preRequisites:11": [104],
  "_symbolic": "age2:netherite_forged",
  "properties:10": {
    "betterquesting:10": {
      "issilent:1": 0, "snd_complete:8": "minecraft:entity.player.levelup",
      "lockedprogress:1": 1, "partySingleReward:8": "false", "tasklogic:8": "AND",
      "repeattime:3": -1, "visibility:8": "ALWAYS", "simultaneous:1": 0, "globalshare:1": 0,
      "questlogic:8": "AND", "partysinglereward:1": 0, "snd_update:8": "minecraft:entity.player.levelup",
      "autoclaim:1": 0, "ismain:1": 1, "repeat_relative:1": 1,
      "icon:10": { "id:8": "<MOD_NETHERUPDATE>:netherite_ingot", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
      "name:8": "§l§6Héritage Forgé",
      "desc:8": "§7Quatre fragments + quatre lingots d'or\n§7+ un feu assez chaud. C'est la recette\n§7de Voss pour le §4Netherite§r§7.\n\n§7Tiens. Regarde-le briller dans ta main.\n§7Tu portes maintenant quelque chose\n§7que Voss a fabriqué, touché, oublié.\n\n§8§o\"Le Netherite n'est pas un alliage. C'est\n§8une mémoire. Chaque lingot garde la trace\n§8de celui qui l'a forgé.\"\n— Carnet Voss Vol. III, p.7§r\n\n§e§lObjectif : §71× Netherite Ingot\n§e§lRécompense : §7ME Controller + 4 Logic Processors + Ouverture des 3 voies Age 2"
    }
  },
  "tasks:9": {
    "0:10": {
      "partialMatch:1": 1, "autoConsume:1": 0, "groupDetect:1": 0, "ignoreNBT:1": 1,
      "index:3": 0, "consume:1": 0, "ignoreDamage:1": 0,
      "taskID:8": "bq_standard:retrieval",
      "requiredItems:9": {
        "0:10": { "id:8": "<MOD_NETHERUPDATE>:netherite_ingot", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" }
      }
    }
  },
  "rewards:9": {
    "0:10": {
      "rewardID:8": "bq_standard:item", "index:3": 0,
      "rewards:9": [{
        "0:10": { "id:8": "appliedenergistics2:controller", "Count:3": 1, "Damage:2": 0, "OreDict:8": "" },
        "1:10": { "id:8": "appliedenergistics2:material", "Count:3": 4, "Damage:2": 22, "OreDict:8": "" },
        "2:10": { "id:8": "minecraft:experience_bottle", "Count:3": 16, "Damage:2": 0, "OreDict:8": "" }
      }]
    },
    "1:10": {
      "rewardID:8": "bq_standard:command", "index:3": 1, "hideBlockIcon:1": 1, "asScript:1": 1,
      "description:8": "", "viaPlayer:1": 0, "title:8": "bq_standard.reward.command",
      "command:8": "/gamestage add @p age2_crossroads"
    },
    "2:10": {
      "rewardID:8": "bq_standard:command", "index:3": 2, "hideBlockIcon:1": 1, "asScript:1": 1,
      "description:8": "", "viaPlayer:1": 0, "title:8": "bq_standard.reward.command",
      "command:8": "/say §5[Nexus Absolu]§r §7VAR_NAME§r a forgé un lingot de Netherite. L'héritage de Voss passe à la génération suivante."
    }
  }
}
```

### 3.11 qid 106 — "Première Cellule" (AUCUN CHANGEMENT)

**Ne touche PAS** le contenu. Juste s'assurer après merge que `preRequisites:11` reste bien `[105]` (même valeur numérique, nouveau sens puisque qid 105 = "Héritage Forgé" maintenant au lieu de l'ancien "Le Contrôleur").

---

## 4. Grabber Voss — spec Java

### 4.1 Fichiers à créer

```
mod-source/src/main/java/com/nexusabsolu/mod/
├── items/ItemGrabberVoss.java              (classe principale, 150-200 lignes)
├── items/grabber/GrabberInventory.java     (IItemHandler 36 slots NBT-backed)
├── items/grabber/GrabberCapabilityProvider.java  (ICapabilityProvider wrapper)
├── gui/ContainerGrabberVoss.java           (Container BQ-like 36 slots)
├── gui/GuiGrabberVoss.java                 (GUI 9x4 grid, texture custom)

mod-source/src/main/resources/assets/nexusabsolu/
├── models/item/grabber_voss.json           (item model pointing to texture)
├── textures/items/grabber_voss.png         (64x64 or 32x32, à générer)
├── textures/gui/grabber_voss_gui.png       (176x168, grid de slots)
└── lang/en_us.lang + fr_fr.lang            (add item.nexusabsolu.grabber_voss.name)
```

Plus mise à jour :
- `ModItems.java` : ajouter `public static final Item GRABBER_VOSS = new ItemGrabberVoss("grabber_voss");`
- `GuiHandler.java` : ajouter un case pour le GUI ID du Grabber
- Recipe JSON : `assets/nexusabsolu/recipes/grabber_voss.json` (recipe shaped, uses sculk_fragment + iron + leather)

### 4.2 Classe `ItemGrabberVoss`

```java
package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.grabber.GrabberCapabilityProvider;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGrabberVoss extends Item implements IHasModel {

    public static final int INVENTORY_SIZE = 36;  // 9x4 grid
    public static final int GUI_ID = 42;  // arbitrary, must be unique in GuiHandler

    public ItemGrabberVoss(String name) {
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxStackSize(1);
        ModItems.ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            player.openGui(NexusAbsoluMod.instance, GUI_ID, world,
                (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new GrabberCapabilityProvider(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Sac du Sujet 46");
        tooltip.add(TextFormatting.DARK_GRAY + TextFormatting.ITALIC.toString() + "« Il y a plus dedans qu'il n'y paraît. »");
        tooltip.add(TextFormatting.DARK_PURPLE + "36 emplacements dimensionnels");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            this, 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
```

### 4.3 Classe `GrabberInventory`

```java
package com.nexusabsolu.mod.items.grabber;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.items.ItemStackHandler;

public class GrabberInventory extends ItemStackHandler {

    private final ItemStack parent;
    private static final String NBT_KEY = "GrabberInventory";

    public GrabberInventory(ItemStack parent) {
        super(ItemGrabberVoss.INVENTORY_SIZE);
        this.parent = parent;
        loadFromStack();
    }

    @Override
    protected void onContentsChanged(int slot) {
        saveToStack();
    }

    private void loadFromStack() {
        if (parent.hasTagCompound()) {
            NBTTagCompound tag = parent.getTagCompound();
            if (tag.hasKey(NBT_KEY)) {
                this.deserializeNBT(tag.getCompoundTag(NBT_KEY));
            }
        }
    }

    private void saveToStack() {
        NBTTagCompound tag = parent.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            parent.setTagCompound(tag);
        }
        tag.setTag(NBT_KEY, this.serializeNBT());
    }
}
```

### 4.4 Recette craft (JSON)

```json
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SLS",
    "LIL",
    "SLS"
  ],
  "key": {
    "S": { "item": "<MOD_DEEPER>:sculk_fragment" },
    "L": { "item": "minecraft:leather" },
    "I": { "item": "minecraft:iron_ingot" }
  },
  "result": {
    "item": "nexusabsolu:grabber_voss",
    "count": 1
  }
}
```

> **Gated par gamestage** `age2_grabber_recipe` (reward Q99). Sans ce stage, la recette ne s'affiche pas
> dans JEI et le craft échoue. Géré via CraftTweaker :
> ```zs
> import mods.recipestages.Recipes;
> Recipes.setRecipeStage("age2_grabber_recipe", <nexusabsolu:grabber_voss>);
> ```
> À ajouter dans `scripts/Age2_Gates.zs` (nouveau fichier).

### 4.5 GuiHandler / Container / Gui — résumé

- `ContainerGrabberVoss` : 36 slots (9x4 grid), plus l'inventaire du joueur (27+9). Interdit de shift-click le Grabber lui-même dedans (loop crash).
- `GuiGrabberVoss` : texture 176x168, slot grid en haut, inventaire joueur en bas. Texture à créer.
- `GuiHandler.java` : ajouter le case pour GUI_ID=42.

---

## 5. Carnets Patchouli — contenu complet

### 5.1 Setup Patchouli

Le mod `nexusabsolu:carnet_voss` existe déjà (référencé dans la quête existante qid 0). Les Vol.2 et Vol.3 sont des **nouveaux books Patchouli** séparés, chacun avec son propre item NBT.

Structure fichiers :
```
mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/
├── carnet_voss_v2/
│   ├── book.json
│   └── en_us/
│       ├── categories/
│       │   ├── age2_intro.json
│       │   └── voss_notes.json
│       └── entries/
│           ├── age2_intro/
│           │   ├── le_poste.json
│           │   ├── sujet_46.json
│           │   └── protocole_voss_7.json
│           └── voss_notes/
│               └── first_entry.json
└── carnet_voss_v3/
    ├── book.json
    └── en_us/
        ├── categories/
        │   └── nether_voss.json
        └── entries/
            └── nether_voss/
                ├── coordonnees.json
                └── netherite_origine.json
```

### 5.2 Carnet Vol. II — entrées

**`book.json`**
```json
{
  "name": "Carnet de Voss — Volume II",
  "landing_text": "§lSeconde partie. Sortie de confinement.§r\n\nCe volume contient les notes du Dr. Voss concernant le §lPoste Voss-7§r, le §lSujet 46§r, et les protocoles de surveillance qui t'ont accompagné pendant les trois Ages précédents.\n\n§oSi tu lis ceci, tu es sorti. Félicitations. Maintenant, reste attentif.§r",
  "book_texture": "nexusabsolu:textures/gui/carnet_voss_v2.png",
  "model": "nexusabsolu:carnet_voss_v2",
  "creative_tab": "nexusabsolu"
}
```

**Entry 1 : `le_poste.json`** — 5 pages
```json
{
  "name": "§lLe Poste Voss-7",
  "icon": "patchouli:guide_book",
  "category": "nexusabsolu:age2_intro",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "§lLe Poste Voss-7§r\n\nCe que tu appelles « la sortie » n'est pas un point géographique. C'est une brèche dans un système de confinement que j'ai conçu en 2019.\n\nLe Poste Voss-7 était la station de surveillance centrale. Il s'est effondré quelques semaines avant ton arrivée ici."
    },
    {
      "type": "patchouli:text",
      "text": "Tu ne le verras jamais — il n'est pas un bâtiment, il est un §lchamp§r. Un anneau de données stockées dans le sol et dans l'air, que je mettais à jour chaque nuit.\n\nSans entretien, le champ s'est désagrégé. Les balises se sont éteintes. Et quelque chose s'est réveillé."
    },
    {
      "type": "patchouli:text",
      "text": "§oJe ne sais pas ce qui se passera quand tu sortiras. Peut-être rien. Peut-être §lquelque chose. §r§oLe Sujet 46 est toujours là-dessous. Fais attention aux vibrations du sol.§r"
    },
    {
      "type": "patchouli:text",
      "text": "Note : Le Poste Voss-7 était aussi un §lentrepôt§r. J'y avais stocké trois presses d'inscription AE2, un lot de pioches diamant, et une quantité indécente de boules de feu.\n\nSi tu arrives à battre le Gardien, tu récupéreras tout ça."
    },
    {
      "type": "patchouli:text",
      "text": "§l— Dr. E. Voss§r\n§o§8Entrée écrite 14 jours avant la disparition.§r"
    }
  ]
}
```

**Entry 2 : `sujet_46.json`** — 4 pages
```json
{
  "name": "§lSujet 46",
  "icon": "minecraft:sculk_catalyst_placeholder",
  "category": "nexusabsolu:age2_intro",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "§lSujet 46 — première mémoire§r\n\nIl était gentil. Je l'appelais Marcus.\n\nC'était le 46ème à accepter de participer au protocole Voss-7. Il pensait qu'on allait étudier des composés. Je ne lui ai pas menti — je ne lui ai juste pas tout dit."
    },
    {
      "type": "patchouli:text",
      "text": "Le 17 mars, nous avons testé l'exposition prolongée à un flux de sculk compressé. Les 45 sujets précédents avaient supporté des durées variables. Marcus a tenu douze minutes avant que le flux ne commence à §lle réorganiser§r."
    },
    {
      "type": "patchouli:text",
      "text": "§oJe ne l'ai pas tué. Je n'ai pas pu. Il est descendu dans les tunnels sous le Poste quand il a senti ce qui lui arrivait. Il m'a dit au revoir en vibrations. Puis plus rien.§r\n\nPendant des mois, j'ai cru qu'il était mort."
    },
    {
      "type": "patchouli:text",
      "text": "Puis les balises ont commencé à détecter une présence. Une signature qui n'était ni humaine ni minérale.\n\nMarcus est toujours là. Mais il ne parle plus.\n\n§4§lSi tu le rencontres, ne le regarde pas. Frappe vite.§r"
    }
  ]
}
```

**Entry 3 : `protocole_voss_7.json`** — 3 pages
```json
{
  "name": "§lProtocole Voss-7",
  "icon": "minecraft:written_book",
  "category": "nexusabsolu:age2_intro",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "§lProtocole Voss-7 — résumé§r\n\nObjectif : stabiliser un sujet humain dans un environnement dimensionnellement compressé (« Compact Machine ») pendant des durées prolongées, afin d'étudier les effets de l'isolement absolu sur la perception."
    },
    {
      "type": "patchouli:text",
      "text": "§lSujets 1-45§r : survie moyenne 47 jours. Causes de décès variées.\n\n§lSujet 46 (Marcus)§r : survie 212 jours, puis §otransformation§r.\n\n§lSujet 47 (toi)§r : survie en cours. Tu es le premier à avoir franchi la barrière de l'Age 1."
    },
    {
      "type": "patchouli:text",
      "text": "§oSi tu lis ceci, c'est que tout a marché. Trois ans de confinement, trois Ages, et tu es sorti avec tous tes souvenirs.§r\n\n§lTu n'es pas un sujet. Tu es la preuve que le protocole a un sens. Continue.§r"
    }
  ]
}
```

**Entry 4 : `first_entry.json`** (dans catégorie voss_notes) — 2 pages
```json
{
  "name": "§lPremière note personnelle",
  "icon": "minecraft:feather",
  "category": "nexusabsolu:voss_notes",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "Je ne sais pas si tu liras ceci un jour. Peut-être que tu seras mort. Peut-être que tu seras devenu autre chose — comme Marcus.\n\nMais si tu lis, je veux te dire merci. D'avoir tenu. D'avoir survécu à mes expériences sans le savoir."
    },
    {
      "type": "patchouli:text",
      "text": "§oLa prochaine étape t'attend dans le Nether. Les coordonnées sont dans l'œil du Gardien. Prends-les. Ne reviens pas.§r\n\n§l— E. V.§r"
    }
  ]
}
```

### 5.3 Carnet Vol. III — entrées

**`book.json`** — similaire au Vol.2, renommé en III.

**Entry 1 : `coordonnees.json`** — 3 pages
```json
{
  "name": "§lCoordonnées Nether",
  "icon": "minecraft:compass",
  "category": "nexusabsolu:nether_voss",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "§lCoordonnées extraites de l'œil§r\n\nX : +2048\nZ : -768\nY : ~18 (à trouver)\n\nC'est la position de mon ancien laboratoire Nether. Tu n'y trouveras pas de murs — il n'y en a jamais eu. Juste des coffres enterrés et des blocs d'Ancient Debris que j'ai fait tomber en 2019."
    },
    {
      "type": "patchouli:text",
      "text": "§oSi tu arrives jusque là, creuse. Les débris sont organisés en spirale autour du point zéro. Il y en a assez pour forger quatre lingots de Netherite.§r"
    },
    {
      "type": "patchouli:text",
      "text": "Attention : le Nether que tu vas voir n'est plus celui de tes souvenirs d'avant-confinement. J'ai installé là-bas des choses qui ne sont plus à moi.\n\n§4§lReste vigilant.§r"
    }
  ]
}
```

**Entry 2 : `netherite_origine.json`** — 3 pages
```json
{
  "name": "§lOrigine du Netherite",
  "icon": "<MOD_NETHERUPDATE>:netherite_ingot",
  "category": "nexusabsolu:nether_voss",
  "pages": [
    {
      "type": "patchouli:text",
      "text": "§lOrigine du Netherite§r\n\nCe qu'on appelle Netherite n'est pas un minerai du Nether. C'est une §lmémoire§r.\n\nQuand j'ai jeté les premiers blocs d'Ancient Debris dans le Nether en 2019, je leur ai inséré des patterns de données brutes issus de mes sujets."
    },
    {
      "type": "patchouli:text",
      "text": "Chaque lingot de Netherite que tu forges contient une §lsignature personnelle§r : un fragment de l'identité de l'un des 47 sujets du protocole Voss-7.\n\nTu portes leurs mémoires sans le savoir."
    },
    {
      "type": "patchouli:text",
      "text": "§oCe n'est pas de la sentimentalité. C'est de la physique appliquée.§r\n\n§lLe Netherite résiste aux dégâts parce que les mémoires qu'il contient refusent de disparaître. C'est tout.§r\n\n§o§8— E. V., 14 août 2019§r"
    }
  ]
}
```

---

## 6. Ordre d'exécution (session suivante)

### Phase 1 — Pré-requis (15 min)
1. [ ] Alexis fournit les 2 mod_ids (`<MOD_DEEPER>` et `<MOD_NETHERUPDATE>`)
2. [ ] Claude fait un find-and-replace global dans ce doc pour substituer les placeholders
3. [ ] Claude vérifie dans JEI in-game les damage values AE2 presses (Logic/Calc/Eng → 13/14/15 ou autres)

### Phase 2 — Quêtes BQ (20 min)
4. [ ] Claude ouvre `quests-source/age2.json`
5. [ ] Pour chaque qid 97→105 : remplace le bloc JSON existant (clé `149:10`→`157:10`) par le bloc pré-écrit en Section 3
6. [ ] Vérifie que qid 106 (clé `158:10`) est intact et que son `preRequisites:11` est bien `[105]`
7. [ ] `py scripts/merge_quests.py --check` → doit passer sans erreur
8. [ ] `py scripts/merge_quests.py` → écrit `DefaultQuests.json` + backup auto
9. [ ] Commit : `v1.0.165: Age 2 intro rewrite (qid 97-105)`

### Phase 3 — Items Java (60-90 min)
10. [ ] Créer `ItemGrabberVoss.java` + `GrabberInventory.java` + `GrabberCapabilityProvider.java`
11. [ ] Créer `ContainerGrabberVoss.java` + `GuiGrabberVoss.java`
12. [ ] Mettre à jour `ModItems.java` : `GRABBER_VOSS`, `CARNET_VOSS_V2`, `CARNET_VOSS_V3`, `BADGE_VOSS`, `LANTERNE_VOSS`, `FRAGMENT_MEMOIRE_1`
13. [ ] Mettre à jour `GuiHandler.java` : case `GUI_ID=42`
14. [ ] Générer les textures (placeholders noir/violet OK pour cette session, retouches plus tard)
15. [ ] Créer les JSON models + recipes
16. [ ] Bump version `build.gradle` → `1.0.166`
17. [ ] `bash mod-source/build.sh` → build doit passer sans warnings bloquants

### Phase 4 — Patchouli books (30-45 min)
18. [ ] Créer `assets/nexusabsolu/patchouli_books/carnet_voss_v2/book.json` + categories + entries (3 entrées Section 5.2)
19. [ ] Créer `assets/nexusabsolu/patchouli_books/carnet_voss_v3/book.json` + categories + entries (2 entrées Section 5.3)
20. [ ] Ajouter les textures de couverture (placeholders OK)
21. [ ] Bump version → `1.0.167`
22. [ ] `bash mod-source/build.sh`

### Phase 5 — Validation in-game (30 min)
23. [ ] Lancer un monde neuf en mode créatif
24. [ ] `/gamestage add @p age_1` puis `/gamestage add @p age_1_done`
25. [ ] Sortir vers dim 0 → Q97 "À la Surface" doit se compléter automatiquement
26. [ ] Vérifier que le reward donne bien `carnet_voss_v2` + 4 pain
27. [ ] Ouvrir le carnet → Patchouli affiche les 3 entrées Vol.2
28. [ ] Craft 1× Grabber Voss (après avoir triché 4× sculk_fragment via `/give`)
29. [ ] Right-click Grabber → GUI 9x4 s'ouvre, 36 slots fonctionnent
30. [ ] Tuer un Warden via `/summon` → Q102 se complète, reward donne 3 presses AE2
31. [ ] Ouvrir un portail Nether → Q103 location trigger
32. [ ] `/give` 4× ancient_debris + forger 1× netherite_ingot → Q104 + Q105 se complètent
33. [ ] Q105 reward donne ME Controller + active gamestage `age2_crossroads`
34. [ ] qid 106 "Première Cellule" doit maintenant être débloquée (son prereq pointe vers qid 105)

### Phase 6 — Commit final (10 min)
35. [ ] `v1.0.168: Age 2 intro Sprint 1 complete — 9 quests + Grabber Voss + 2 Patchouli books`
36. [ ] Push sur main (attention token — utiliser credential.helper wincred ou gh auth, pas PAT en clair)

---

## 7. Plan de validation + rollback

### 7.1 Si le merge casse `age2.json`
- `scripts/merge_quests.py` fait un backup auto `DefaultQuests.json.bak` avant l'écriture
- `quests-source/age2.json` est sous git — rollback trivial via `git checkout HEAD -- quests-source/age2.json`
- Retester avec `--check` avant de relancer le merge

### 7.2 Si le build Java échoue
- Le `build.sh` liste les erreurs javac — les traiter une par une
- Accolades, imports, signatures d'interface — causes fréquentes, voir SKILL.md Section 11 "Erreurs à ne JAMAIS refaire"
- Backup des .java via git avant modification — on peut `git checkout` au besoin

### 7.3 Si Patchouli crashe au chargement
- Cause typique : JSON mal formé dans book.json ou entries
- Lire `crash-reports/*.txt` : Patchouli loggue précisément quelle entrée a échoué
- Fallback : commenter temporairement l'entrée cassée, commit, itérer

### 7.4 Si une quête BQ ne se complète jamais in-game
- Vérifier le mod_id de l'item attendu — c'est la cause la plus fréquente
- Vérifier le `Damage:2` — pour les items AE2 c'est critique
- Utiliser `/bq_admin edit` pour inspecter l'état de la quête et ses tasks
- Ouvrir `logs/latest.log` → les erreurs BQ sont préfixées `[betterquesting]`

---

## 8. Ce qui reste HORS Sprint 1

Pour rappel — ces items sont explicitement reportés à Sprint 2+ :

- ❌ Chaînes Nexium + Claustrite (ingots + alloy tiers + recettes) — Sprint 2
- ❌ Multibloc Résonateur Stellaire — Sprint 3
- ❌ Ore Endrium + Dragon gate + Wither placement mid-A2 — Sprint 4
- ❌ Mega Furnace (9 slots, ×10000 speed) — Sprint 5 (Age 8)
- ❌ Refactor des questLines BQ en sous-chapitres façon ATM10 — session future
- ❌ Quêtes pour BetterNether new biomes / structures — Sprint 2 ou 4
- ❌ Ancient Debris gen modifié pour spawner aussi en Deep Dark — non
- ❌ Gamestage branching complexe post-`age2_crossroads` (AE2 / Botania / Mystag) — session future

---

## 9. Contacts et références

- Doc parent : `quests-source/AGE2_INTRO_DESIGN.md` (sections 1-10)
- Sister doc : `quests-source/PROGRESSION_GATES_DESIGN.md` (ore remapping + Mega Furnace)
- Format source BQ : `quests-source/age2.json` (exemples sur clés `149:10` à `158:10`)
- Script de build : `mod-source/build.sh`
- Script de merge quêtes : `scripts/merge_quests.py`
- Code Java Grabber reference : `ModItems.java`, `GuiHandler.java`, `ItemBase.java`
- Mod Deeper in the Caves : https://www.curseforge.com/minecraft/mc-mods/linfox-stacked-dimensions-warden-edition
- Mod NetherUpdate Netherite : https://www.curseforge.com/minecraft/mc-mods/netherite-1-7-10-1-12-1-14
