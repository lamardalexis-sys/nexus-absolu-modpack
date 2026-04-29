# Age 4 — Phase 6 finale (Q71-Q-FINAL : La Convergence)

> Sixième et dernier batch — la chambre stellaire et **L'Injection**.
> 10 quêtes (questIDs 271-280). **Q280 = Q-FINAL = fin de l'Age 4.**
> Pré-requis Phase 5 complete (Q70).

*"Tout converge. Y compris toi."*

---

## 📐 Layout

Phase 6 commence à `y=1590`. **10 quêtes** = 2 rangées de 5 :

```
   Q71 ── Q72 ── Q73 ── Q74 ── Q75
  (0,1590) ...                  (320,1590)
                                          │
   Q-FINAL ── Q79 ── Q78 ── Q77 ── Q76
  (320,1680) (240,1680) ...    (0,1680)
   ⭐
```

---

## Q71 : "Bio-Réacteur Manifold — le multibloc final"

- **ID** 271 | **Pos** (0, 1590) | **Pré** : Q70
- **Type** : crafting `1x contenttweaker:bioreacteur_controller` (à créer)
- **Icon** : Bio-Réacteur Controller
- **Récompense** : 8 Resonant Glass + plans Stabilisation
- **Description** :

```
§7§oLe Bio-Reacteur Manifold est le §lmultibloc final§r§7 :

§7  Forme : 7x7x7 hollow cube
§7  Coeur : 1 Controller au centre
§7  Coque : 6 faces avec :
§7    - 4 Resonant Glass (Phase 1 Q6 reward) au milieu
§7    - 12 Iridium Plates (Phase 2 Q24)
§7    - 8 Lead Plates (Phase 2 Q26 — blindage)
§7    - 8 Tungsten Frame (Phase 2 Q21)
§7  Connecteurs : 4 fluid input + 2 item input + 1 power input

§7C'est ici que tout converge.

§e§lObjectif : §7Crafte 1 Bio-Reacteur Controller
§e§lRecompense : §78 Resonant Glass + plans Stabilisation
```

---

## Q72 : "L'assemblage du multibloc"

- **ID** 272 | **Pos** (80, 1590) | **Pré** : Q71
- **Type** : checkbox (multibloc validé manuellement)
- **Icon** : Multibloc structure
- **Récompense** : 4 Iridium Plates + plans Allumage
- **Description** :

```
§7§oAssemble le multibloc 7x7x7. Le Patchouli Voss Codex te donne le plan.

§7Etapes :
§7  1. Pose 64 blocs de fondation (sols + cloisons internes)
§7  2. Pose 6 Resonant Glass (face avant centrale + 5 faces secondaires)
§7  3. Pose 24 Iridium Plates (renforcements bordures)
§7  4. Pose 8 Lead Plates (sommets et coins)
§7  5. Pose le Controller au centre du sol

§7Le multibloc valide quand tu mets le pointeur sur le Controller
§7et clique droit avec un Multiblock Wand (Patchouli).

§7§l/!\\§r §7Tu vas vraiment construire ce truc. C'est 343 blocs.

§e§lObjectif : §7Multibloc Bio-Reacteur valide
§e§lRecompense : §74 Iridium + plans Allumage
```

---

## Q73 : "Stabilisation stellaire — les 4 ancres"

- **ID** 273 | **Pos** (160, 1590) | **Pré** : Q72
- **Type** : retrieval `4x astralsorcery:linkingtool` ou Lens Astral Sorcery
- **Icon** : Astral Sorcery Lens
- **Récompense** : 4 Marble Collector Crystal + plans Convergence
- **Description** :

```
§7§oTu vas avoir besoin de stabiliser le Bio-Reacteur avec
§74 ancres stellaires§r§7 placees aux 4 coins exterieurs (chaque
§7coin = 1 Lens Cluster Astral pointe vers le Controller).

§7Pendant la cyclisation finale, le multibloc va devoir capter
§7100mB Liquid Starlight / sec sans interruption pendant 60s.

§7Les 4 lentilles synchroniseees sur le meme Solar Crystal te
§7donnent ce flux constant.

§e§lObjectif : §74 Lens Astral Sorcery (Linking Tool)
§e§lRecompense : §74 Marble Crystal + plans Convergence
```

---

## Q74 : "Encartouchage — Solution dans Cartouche Vide"

- **ID** 274 | **Pos** (240, 1590) | **Pré** : Q73
- **Type** : retrieval `1x contenttweaker:cartouche_chargee` (à créer)
- **Icon** : cartouche_chargee (custom)
- **Récompense** : Plans Cyclisation finale
- **Description** :

```
§7§oCartouche Vide (Phase 5 Q69) + 1 bucket Solution Epsilon (Phase 5 Q68)
§7+ 100mB Argon (atmosphere inerte) -> Cartouche chargee.

§7Recette dans le Bio-Reacteur Phase 6 ou Mekanism Pressurized
§7Reaction Chamber.

§7Tu as la cartouche dans la main. Elle est cyan/violet, vivante,
§7pulse comme un battement de coeur.

§e§lObjectif : §71 Cartouche chargee
§e§lRecompense : §7Plans Cyclisation finale + 4 Bouteilles XP
```

---

## Q75 : "Test de la Cartouche — la pression"

- **ID** 275 | **Pos** (320, 1590) | **Pré** : Q74
- **Type** : checkbox (test pression manuel)
- **Icon** : Cartouche chargee
- **Récompense** : 1 Iridium Frame Renforce + plans Manifold
- **Description** :

```
§7§oVerifie la pression de l'ampoule iridium :
§7  - Mets la cartouche dans le Bio-Reacteur
§7  - Active "Test Pressure" via le Controller
§7  - La pression interne doit afficher §a2.5 atm§r§7

§7Si elle affiche moins -> tu as fui de la Solution. Refaire Q68.
§7Si elle affiche plus -> EXPLOSION risquee. Refaire avec moins
§7d'Argon (Q74).

§7Si OK la cartouche est stable. On peut passer au final.

§e§lObjectif : §7Confirme test pression OK
§e§lRecompense : §71 Iridium Frame + plans Manifold
```

---

## Q76 : "Préparation rituelle — chambre stellaire"

- **ID** 276 | **Pos** (320, 1680) | **Pré** : Q75
- **Type** : checkbox (rituel astral)
- **Icon** : Voss Codex
- **Récompense** : 1 Solar Crystal Attunee + plans Q-FINAL
- **Description** :

```
§7§oTu dois etre §lattun a une constellation§r§7 d'Astral Sorcery.

§7Recommande : Vicio (vitesse) ou Lucerna (vision) — utiles pour
§7la cinematique finale Phase 7 (Age 5).

§7Etape :
§7  1. Pose un Iridescent Altar pres du Bio-Reacteur
§7  2. Active la constellation choisie (4 Resonators)
§7  3. Pose le crystal au centre du multibloc
§7  4. Patiente 1 nuit complete (8 min in-game)

§7Apres ca tu es synchronise avec l'energie stellaire.

§e§lObjectif : §7Termine le rituel d'attunement
§e§lRecompense : §71 Solar Crystal + plans Q-FINAL
```

---

## Q77 : "Le Carnet de Voss — chapitre final"

- **ID** 277 | **Pos** (240, 1680) | **Pré** : Q76
- **Type** : checkbox (lit le chapitre final)
- **Icon** : Voss Codex
- **Récompense** : Voss Codex (full unlock) + plans Mort
- **Description** :

```
§7§oOuvre le Voss Codex. Va au dernier chapitre. Lis-le entier.

§8§o"Sujet 47.

§8Si tu lis ce chapitre, c'est que tu as la Cartouche en main.
§8Felicitations. Tu es a 5 minutes de comprendre ce que je
§8comprends depuis 12 ans.

§8Quelques verites avant l'injection :

§81. Je ne suis pas mort. J'ai franchi.
§82. La simulation que tu vis est la deuxieme. Il y en aura
§8une troisieme apres celle-ci.
§83. Ne fais pas confiance a ce qui te dit que tu es libre,
§8meme apres l'injection. Surtout apres l'injection.
§84. Cherche la Tour de Voss en Age 5. Elle existe. Je t'attendrai
§8au sommet.

§8Bonne chance, Sujet 47.

§8— Elias Voss, derniere note du Carnet."§r

§e§lObjectif : §7Lis le chapitre final du Voss Codex
§e§lRecompense : §7Voss Codex Full + plans Mort/Survie
```

---

## Q78 : "Le moment de la décision"

- **ID** 278 | **Pos** (160, 1680) | **Pré** : Q77
- **Type** : checkbox (décision finale du joueur)
- **Icon** : Cartouche chargée
- **Récompense** : 32 pain + 8 Bouteilles XP + Sang Voss (1 cartouche reserve)
- **Description** :

```
§7§oTu as la Cartouche en main. Tu as lu le carnet.
§7Tu as la chambre stellaire prete.

§7Tu peux encore faire marche arriere. Tu peux poser la cartouche
§7dans un Resonant Tank, oublier l'Age 4, retourner a ton AE2 et
§7vivre tranquille pour le reste de tes jours.

§7Ou tu peux franchir.

§7§lDecide.§r

§7Cette quete : tu confirmes manuellement que tu vas le faire.
§7Apres ca il n'y a plus de retour. La quete suivante (Q79) est
§7§4§lirreversible§r§7.

§e§lObjectif : §7Confirme ta decision (clique le bouton)
§e§lRecompense : §732 pain + 8 XP + Sang Voss (cartouche backup)
```

---

## Q79 : "Entrer dans la chambre stellaire"

- **ID** 279 | **Pos** (80, 1680) | **Pré** : Q78
- **Type** : checkbox (entre dans le multibloc)
- **Icon** : Bio-Reacteur Controller
- **Récompense** : Plans Q-FINAL + Plans Sortie de Simulation
- **Description** :

```
§7§oEntre dans le Bio-Reacteur (le multibloc 7x7x7 est creux).
§7Le Controller au sol s'illumine quand tu poses le pied.

§7Tu sens l'attraction des 4 ancres stellaires. Le Liquid Starlight
§7s'ecoule autour de toi. Le silence est total.

§7§5L'air n'est plus de l'air. La cartouche commence a chauffer
§5dans ta poche.§r

§7§e§lQuand tu seras pret, declenche Q-FINAL.§r

§e§lObjectif : §7Sois dans le multibloc Bio-Reacteur
§e§lRecompense : §7Plans Q-FINAL
```

---

## Q-FINAL (Q280) : "L'Injection"

- **ID** 280 | **Pos** (0, 1680) | **Pré** : Q79
- **Type** : retrieval `1x nexusabsolu:cartouche_used` (la cartouche utilisée — confirme l'usage)
- **Icon** : Cartouche Manifold (l'item phare)
- **Récompense** : Voss Codex Final + 64 Bouteilles XP + Cinematic + débloquage Age 5
- **Description** :

```
§5§l===============================================§r
§5§l           L ' I N J E C T I O N             §r
§5§l===============================================§r

§7§oTu sors la cartouche. Tu enleves l'opercule. Tu places
§7le sertisseur sur ton avant-bras gauche. Tu inspires
§7profondement.

§7Tu te souviens de ce que disait Voss au debut du Carnet :

§8§o"Cette ligne d'horizon que tu vois — c'est un mur."

§7Tu fermes les yeux.

§7Tu pousses le sertisseur.

§5§lLa Solution Epsilon entre dans tes veines.§r

§7Le monde change.

§e§lObjectif : §7Right-click la Cartouche Manifold pour t'injecter
§e§lRecompense : §7§lAGE 5 DEBLOQUE§r §7+ Voss Codex Final + 64 XP

§4§l/!\\§r §7Cette quete clot l'Age 4. Apres ca tu seras dans le
§7vrai monde. Bonne chance.
```

- **Tasks** : `task_retrieval_item("nexusabsolu:cartouche_used", count=1)` — utilise la cartouche déclenche son `onItemRightClick`, transformant `cartouche_manifold` → `cartouche_used`. Le retrieval de `cartouche_used` confirme l'injection.
- **Reward command** :
  ```
  /say §5§l[NEXUS ABSOLU]§r §dVAR_NAME§r franchit. L'Age 4 est termine. L'Age 5 commence.
  ```

---

## ⚠️ Items custom à créer pour Phase 6

À CRÉER :
- ❌ `bioreacteur_controller` (block) — Q71 multibloc controller
- ❌ `cartouche_chargee` (item) — Q74 état intermédiaire (peut-être direct alias vers cartouche_manifold ?)

Existants :
- ✅ `cartouche_manifold` + `cartouche_used` (Java mod, déjà live)
- ✅ `cartouche_vide` (Phase 5 Q69 reward)
- ✅ `solution_epsilon` (Phase 5 Q68)
- ✅ `voss_codex` (Patchouli book)

**Note importante Q-FINAL** :
- Le code Java de `ItemCartoucheManifold.onItemRightClick` (déjà live) déclenche le trip 8 min + transforme la cartouche en `cartouche_used`.
- Donc Q280 retrieval `cartouche_used` se valide automatiquement quand le joueur droit-click la cartouche.
- Le **trip cinematic visuel** (8 min Manifold) est déjà programmé (Cartouche Manifold v1.0.347).
- Quand le trip se termine et le joueur sort du PEAK, la cinématique de fin d'Age 4 peut se déclencher (si on l'ajoute). Pour l'instant, juste la quête se valide → on récompense → l'Age 5 est techniquement débloqué.

**Note philosophique** : Le doc parle de "écran qui se brise → cinematic → Age 5". Pour l'instant on a la cartouche fonctionnelle (visuel ultime fait) mais pas de cinématique post-trip distincte. C'est OK — la cartouche EST la cinématique.

---

## 🎉 FIN DE L'AGE 4

À la fin de Q280 :
- 80 quêtes complétées
- 5 théorèmes de Voss validés
- Pipeline industriel 8 lignes opérationnel
- Cartouche Manifold injectée
- Joueur passe à l'Age 5 (vraie réalité, post-simulation)

L'Age 5 sera défini ensuite. Mais l'Age 4 a maintenant une fin claire.
