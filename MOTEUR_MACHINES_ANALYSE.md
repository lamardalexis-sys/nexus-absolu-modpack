# Moteur de machines Nexus — analyse technique préalable

Branche : `feature/nexus-machines`
Base : `2ad8268` (migration des 27 contrôleurs)
Cible : Modular Machinery `1.12.2-1.11.1` (HellFirePvP), le jar présent dans `mods/`

## 1. Classpath — répondu

Le jar de Modular Machinery **n'était pas** dans le `$CP` de `build.sh`. Rien
n'était cassé : il n'avait simplement jamais été ajouté, parce que jusqu'ici
aucune classe du mod ne touchait à l'API MM (`BlockMachineController` n'importe
que du Forge et du Minecraft).

Corrigé sur cette branche :

- `MODS_DIR` extrait en variable, `JEI` réécrit dessus.
- `MODULARMACHINERY` détecté par glob `modularmachinery-*.jar` (même motif que
  `NETTY`), pour survivre à un bump de version dans le pack.
- Échec explicite si le jar est absent, plutôt qu'une erreur `javac` obscure
  200 lignes plus bas.
- `$MODULARMACHINERY` ajouté en fin de `$CP`.

**À vérifier une fois sur la machine de dev** : `bash check-mm-classpath.sh`.
Le script contrôle la présence des six classes d'API dont le moteur aura besoin,
puis compile une sonde jetable qui mélange volontairement types MM et types
Minecraft dans la même signature — le cas qui casserait si le jar réobfusqué
n'exposait pas des noms exploitables. Il n'écrit rien dans le projet.

Le risque théorique est faible : SpecialSource ne réécrit que les membres
Minecraft, jamais les classes du mod. Les classes `hellfirepvp.*` sortent du jar
avec leurs noms d'origine. La sonde est là pour transformer « faible » en
« vérifié ».

## 2. Le vrai blocage : MM 1.11.1 ne supporte pas les contrôleurs custom

C'est la découverte qui change le plan. Elle confirme et précise la note du
javadoc de `BlockMachineController`.

Dans `TileMachineController.checkStructure()`, à chaque fois qu'une structure
est reconnue :

```java
this.world.setBlockState(pos,
    BlocksMM.blockController.getDefaultState()
        .withProperty(BlockController.FACING, this.patternRotation));
```

MM **écrase la position du contrôleur avec son propre bloc**, en dur, sans
condition. Et `shouldRefresh()` renvoie `oldState.getBlock() != newState.getBlock()`,
donc la TileEntity est détruite et recréée dans la foulée.

Conséquence : un bloc custom qui hériterait de `BlockController` fonctionnerait
exactement jusqu'à la première reconnaissance de structure, puis disparaîtrait
au profit du contrôleur MM. Le joueur verrait sa façade Nexus se transformer en
bloc MM au moment où la machine s'assemble. L'héritage ne suffit pas.

Le contournement par réimplémentation se heurte à trois murs :

- `checkStructure()`, `matchesRotation()`, `updateComponents()` et
  `searchAndUpdateRecipe()` sont **privées**.
- `foundMachine`, `foundPattern`, `activeRecipe`, `foundComponents` sont des
  champs **privés** sans setters.
- `DynamicMachine.createContext(...)` exige un `TileMachineController` en
  paramètre — impossible de faire tourner une recette sans instance MM réelle.

Autrement dit : soit on réécrit la boucle complète par réflexion, soit on copie
le code de MM. `DynamicMachine` de MM 1.11.1 n'a par ailleurs **aucun champ
contrôleur** — la notion de contrôleur par machine n'existe pas dans cette
version.

**Point licence** : Modular Machinery est en GPL v3. Copier sa boucle de
contrôleur dans `nexusabsolu` ferait du mod un travail dérivé, donc GPL v3 à la
redistribution. À trancher consciemment si on part sur cette voie.

## 3. Trois routes possibles

### A — Migrer vers Modular Machinery: Community Edition

MMCE (fork 1.12.2, NovaEngineering-Source) implémente nativement ce qu'on
cherche : `RegistryBlocks.registerCustomControllers()` génère **un bloc
contrôleur par machine déclarée**, avec `BlockController.MACHINE_CONTROLLERS`,
les blockstates écrits automatiquement, plus les contrôleurs d'usine
(`BlockFactoryController`), le parallélisme et la Smart Interface.

Les 27 contrôleurs deviennent fonctionnels sans écrire de moteur.

Coût et risques : remplacer le jar dans un pack de 202 mods, revalider les 25
machines et l'ensemble des scripts CraftTweaker MM existants, vérifier que le
format des JSON de machine n'a pas divergé. Et le README du dépôt annonce que
le projet **va être archivé** (successeur annoncé : PrototypeMachinery). Ce
n'est pas bloquant pour du 1.12.2 figé, mais c'est à savoir.

### B — Écrire le moteur dans `nexusabsolu`, sur MM 1.11.1

Réflexion sur les champs privés, ou copie de la boucle sous GPL v3. Beaucoup de
travail, fragile au moindre changement de MM, et cela nous rend propriétaires
d'un code qu'on n'a pas envie de maintenir. À ne choisir que si migrer le jar
est exclu.

### C — Façades-proxy, sans moteur

Les 27 blocs restent cosmétiques mais deviennent utiles : clic droit sur la
façade → ouverture de la GUI du contrôleur MM caché dans la structure, comparateur
et signalétique relayés. Le contrôleur MM est déplacé hors de vue à l'intérieur
du multibloc.

Zéro risque pour le pack, un gain d'immersion réel, mais on n'a toujours pas de
moteur maison.

## 4. Recommandation

A si la revalidation des 25 machines existantes est acceptable — c'est la seule
route qui donne des contrôleurs réellement custom sans dette de maintenance.
C en repli immédiat si on veut du visible cette session sans toucher au pack.
B seulement en dernier recours.

---

# Audit de compatibilité MM 1.11.1 → Community Edition

Fait le 31/07/2026 sur les sources des deux versions.

## Verdict : le pack passe tel quel

**Les 30 fichiers `machinery/*.json`** : aucun ajustement. Le format de CE est un
sur-ensemble strict de celui de 1.11.1 — aucune clé retirée, renommée ou rendue
obligatoire. Le pack n'utilise qu'un vocabulaire minimal (`registryname`,
`localizedname`, `requires-blueprint`, `parts`, et `modifiers` dans un seul
fichier) ; les 648 valeurs d'`elements` résolvent toutes vers `casings.var.json`.
`requires-blueprint` et `color` gardent exactement la même sémantique.

**Les 67 fichiers `recipes/*.json`** : le désérialiseur est fonctionnellement
identique. Les identifiants `modularmachinery:item`, `:fluid`, `:energy` sont
enregistrés sous les mêmes ResourceLocation dans CE, qui ne fait qu'en ajouter.

**Les scripts CraftTweaker** : rien à migrer. Aucun `.zs` du pack n'appelle
`mods.modularmachinery.*` — les seules occurrences sont des exemples non
exécutés dans les docs de conception. `<modularmachinery:blockcontroller>`
garde son nom d'enregistrement dans CE.

## Deux points à décider avant de basculer

CE charge les machines en deux passes parallèles. Si la seconde échoue, la
machine reste enregistrée avec une structure vide et l'erreur sort en `warn`
au lieu de l'écran d'erreur de 1.11.1. Les fautes de syntaxe deviennent plus
discrètes — à surveiller lors des futures éditions.

CE fait hériter les recettes de `recipeParallelizeEnabledByDefault`, **à `true`
par défaut**. Les 67 recettes deviendront parallélisables dès qu'un Parallel
Controller est construit. C'est un changement d'équilibrage à trancher
consciemment (`parallel-controller` / `recipe-parallelize-enabled-bydefault`).

## Deux bugs préexistants trouvés au passage

Aucun des deux ne vient de la migration : ils cassent déjà le pack sous 1.11.1.

**`cyclo_manifoldine_cyclization.json`** déclarait quatre requirements de types
`modularmachinery:dimension`, `:position`, `:weather` et `:time`, qui n'existent
dans aucune des deux versions. `RecipeLoader` attrape l'exception par fichier :
la recette entière ne se chargeait donc pas. Conséquence en chaîne :
`manifoldine_brute` n'était produite par rien, donc l'évaporateur ne pouvait
jamais tourner, donc `cristal_manifoldine` était inatteignable. La ligne L8
était coupée à l'étape L8.C.3, pas à l'étape que l'on surveillait.

Les quatre requirements sont supprimés pour que la recette se charge, et **le
gating est reporté sur un item**, pas perdu (fait en 1.0.355). Voir plus bas.

**`power_transformer_energy_transform.json`** ciblait `"machine":
"power_transformer"` alors que `machinery/power_transformer.json` déclare
`"registryname": "transformer"`. La recette était silencieusement écartée.
Corrigé en `"transformer"`.

Après correction, les 67 recettes passent la validation : aucun type invalide,
aucun `io-type` manquant, aucune machine inconnue.

---

# Revue avant merge — vérifications

## `power_transformer` : le fix n'est pas inversé

L'objection portait sur l'absence d'un `transformer.json` dans `machinery/`.
C'est exact, mais sans effet : Modular Machinery n'identifie jamais une machine
par son nom de fichier. `DynamicMachine.MachineDeserializer` (ligne 152 et 167)
lit la clé JSON `registryname` et construit
`new ResourceLocation(ModularMachinery.MODID, registryName)`. Le nom de fichier
ne sert qu'à la découverte (`FileType.MACHINE.accepts(f.getName())`) et à
l'affichage dans les messages d'erreur.

Or `machinery/power_transformer.json` déclare `"registryname": "transformer"`.
L'identifiant de la machine est donc `modularmachinery:transformer`, quel que
soit le nom du fichier. Remettre `"machine": "power_transformer"` réintroduirait
le `MachineRecipe loaded for unknown machine` d'origine.

## Cyclisation : l'absence d'erreur ne prouve rien

MM 1.11.1 n'enregistre que cinq types de requirement — `RegistryRequirementTypes`
lignes 29 à 34 : `item`, `fluid`, `energy`, `gas`, `duration`. Il n'existe aucun
fichier `RequirementTypeDimension`, `...Position`, `...Weather` ni `...Time` dans
l'arbre source. Un type inconnu lève
`JsonParseException("'X' is not a valid RequirementType!")` (`MachineRecipe`
ligne 325).

Cette exception est attrapée **par fichier** dans `RecipeLoader.loadRecipes`, puis
rapportée par `RecipeRegistry` ligne 96–98 avec `ModularMachinery.log.warn` —
jamais en `error`. Et `crafttweaker.log` ne contient aucun message de Modular
Machinery, c'est le journal de CraftTweaker.

Un `latest.log` sans erreur MM est donc exactement la signature attendue d'une
recette silencieusement écartée, pas une preuve que les requirements fonctionnent.

Le test reste utile et il faut le faire, mais avec la bonne chaîne. Sur un boot
avec la version `main` du fichier :

```
grep -i "Couldn't load recipe from file" logs/latest.log
grep -i "problems while loading recipes" logs/latest.log
```

**Test fait, résultat sans ambiguïté.** Le boot rapportait
`Encountered 5 problems while loading recipes!` et listait
`cyclo_manifoldine_cyclization.json` parmi les cinq. Les quatre requirements
étaient bien rejetés. Trois autres recettes tombaient pour une syntaxe
`item:meta` sur trois segments (`botania:manaresource:5` — MM attend un champ
`meta` séparé), et `ion_filtration.json` consommait `distilledwater`, un fluide
défini nulle part. Les cinq sont corrigées en 1.0.355.

## Merge : aucun conflit

Merge d'essai `origin/main` → `feature/nexus-machines` : `Automatic merge went
well`, zéro fichier en conflit. La branche est bien 4 en avance et 7 en retard,
mais les 7 commits de `main` ne touchent **aucun fichier `.zs`** — uniquement des
textures, le Carnet Voss Patchouli, les quêtes de l'Âge 3 et les scripts
`scripts/gen/*.py`.

`main` n'a supprimé aucune recette de table : les 48 seaux NBT de l'Âge 4 y sont
toujours, dans les six mêmes fichiers. La purge des raccourcis de table est le
commit `555f18d`, antérieur au point de divergence, donc déjà présente des deux
côtés. Rien à conserver manuellement.

## MMCE : le point d'équilibrage se règle par config

`recipeParallelizeEnabledByDefault` est bien à `true` par défaut
(`common/data/Config.java` ligne 39), mais c'est une option lisible :
`recipe-parallelize-enabled-bydefault`, catégorie `parallel-controller`. La
poser à `false` avant le premier boot préserve l'équilibrage énergétique de
l'Âge 3. Voir aussi `machine-parallelize-enabled-bydefault` dans la même
catégorie.

---

# Gating de la Cyclisation Stellaire — état actuel (1.0.355)

Le gating n'est plus à réimplémenter : il l'est. Les quatre conditions de design
— Overworld, y ≥ 60, ciel dégagé, temps clair, 13000–23000 ticks — sont vérifiées
dans `ItemAmpouleNuitStellaire.onItemRightClick`, et
`cyclo_manifoldine_cyclization.json` consomme une `nexusabsolu:ampoule_nuit_stellaire`
pleine. Même contrainte pour le joueur, exprimée dans un type que MM accepte.

Relu : le garde `world.isRemote` est en place, la conversion de temps utilise bien
`getWorldTime() % 24000L` (sans le modulo l'item aurait cessé de fonctionner après
le premier jour), l'ampoule pleine est droppée au sol si l'inventaire est plein,
et les deux items sont enregistrés dans `ModItems` avec textures, modèles et
traductions FR/EN.

Deux remarques, aucune bloquante.

`world.isRaining()` est global à la dimension. Un joueur sous un ciel parfaitement
dégagé dans un désert sera refusé s'il pleut ailleurs dans le monde.
`world.isRainingAt(pos)` tient compte du biome et de la visibilité du ciel, et
correspond mieux à « les nuages font écran ».

L'ampoule pleine est stockable et empilable par 16. Le gating devient « faire le
plein une nuit claire » plutôt que « faire tourner la machine la nuit » : rien
n'empêche de remplir un coffre d'ampoules puis de cycliser en plein midi. Les
anciens requirements, eux, étaient réévalués à chaque cycle. C'est peut-être le
comportement voulu — une ampoule est un consommable — mais l'intention de mise en
scène n'est pas tout à fait la même. Si la contrainte doit rester continue, la
piste est de passer le gating dans la boucle du contrôleur une fois sur MMCE.
