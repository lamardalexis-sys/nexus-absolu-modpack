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
