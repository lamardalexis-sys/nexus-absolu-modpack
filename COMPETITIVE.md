# COMPETITIVE.md — Analyse des modpacks 1.12.2 concurrents
> Ce que chaque pack fait bien, ce qu'il fait mal, et comment Nexus Absolu fait mieux.

---

## 1. LES 6 PACKS DE RÉFÉRENCE

### A. Enigmatica 2 Expert (E2E) — 1.6M downloads
**Le standard du genre. Le pack contre lequel tout est mesuré.**

CE QUI EST BIEN :
- 650+ quêtes qui guident sans forcer
- Recettes inter-mods parfaitement équilibrées
- Chaque mod interagit avec 2-3 autres
- Progression Tech ET Magie en parallèle
- Objectif clair : crafter tous les items créatifs
- Scripts process.zs centralisés (toutes les machines partagent le même système)
- Open source (GitHub complet)

CE QUI EST MAL :
- Pas de narratif — c'est un checklist glorifié
- Début ennuyeux (monde normal, rien de spécial)
- Endgame grind (Avaritia singularities = AFK)
- BetterQuesting UI vieillotte
- Pas de thème visuel cohérent

CE QU'ON PREND :
- Pattern des recettes inter-mods (2-3 mods par machine importante)
- Fonctions Globals.zs (remake, remakeEx)
- Progression Magie/Tech parallèle obligatoire
- Scripts par mod (un fichier .zs par mod)

CE QU'ON FAIT MIEUX :
- Narratif du Dr. Voss (lore dans chaque quête)
- Début unique en Compact Machine (pas un monde vanilla)
- Objectif final clair et unique (le Nexus Absolu, pas "tous les items créatifs")
- Moins grindy (pas de singularities Avaritia pures)

---

### B. SevTech: Ages — 7M downloads
**Le roi de la progression par âges. Le plus téléchargé.**

CE QUI EST BIEN :
- Progression par âges exceptionnelle (GameStages mod)
- Les minerais sont CACHÉS jusqu'à ce que tu débloques l'âge
- Les items et recettes apparaissent dynamiquement dans JEI
- Mobs changent avec les âges (plus dangereux au fil du temps)
- Pas de quest book classique — advancements vanilla (immersif)
- Mods peu connus mis en avant (Better With Mods, Primal Tech, Totemic)
- Multijoueur : progression individuelle, pas serveur

CE QUI EST MAL :
- Début TRÈS lent et frustrant (Stone Age = punch trees pendant 1h)
- Step Assist forcé (mouvement modifié au début, irritant)
- Endgame décevant (perd en intérêt après l'Âge 4)
- Galacticraft mal intégré (planètes vides et ennuyeuses)
- Pas de Thaumcraft/Blood Magic au début (magie arrive tard)
- Burn out fréquent en mid-game

CE QU'ON PREND :
- Concept des âges (notre système est similaire)
- Idée de cacher des items/recettes par âge (JEI hiding)
- Mobs qui évoluent avec les âges (In Control)
- Progression individuelle en multi

CE QU'ON FAIT MIEUX :
- Début immédiatement intéressant (Compact Machine, pas Stone Age)
- Magie dès l'Âge 2 (pas attendre l'Âge 5)
- Endgame avec un objectif CLAIR (le Nexus, pas juste "creative age")
- Galacticraft/space mieux intégré (quêtes spécifiques, matériaux utiles)
- Pas de mods obscurs frustrants (on utilise les classiques bien configurés)

---

### C. Dungeons, Dragons & Space Shuttles (DD&SS) — 3M downloads
**Le plus massif. 302 mods, 1750 quêtes, 20+ dimensions.**

CE QUI EST BIEN :
- 1750 quêtes (le plus complet)
- Artisan Worktables (5x5, 7x7, 9x9 — concept similaire à Extended Crafting)
- Exploration récompensée (loot tables custom dans les donjons)
- Magie ET tech forcées en parallèle
- Dragons Ice&Fire très bien intégrés
- 20+ dimensions à explorer
- Loottables entièrement refaites

CE QUI EST MAL :
- Trop de grind pour les ressources en late game
- Espace boring (Galacticraft = planètes vides, donjons triviaux)
- Armes trop OP en mid-game (boss deviennent triviaux)
- 302 mods = lag et crashes fréquents
- Courbe d'apprentissage ÉNORME

CE QU'ON PREND :
- Loottables custom (LootTableTweaker — on l'a déjà)
- Exploration récompensée
- Tables de craft 5x5/7x7/9x9 (Extended Crafting — on l'a)
- Magie/Tech forcées ensemble

CE QU'ON FAIT MIEUX :
- 200 mods au lieu de 302 (plus stable)
- Compact Machines = gameplay unique que DD&SS n'a pas
- Espace mieux intégré (matériaux nécessaires pour le Nexus)
- Balance armes/armures vs mobs (pas de power creep)

---

### D. Compact Claustrophobia — 726K downloads
**Notre inspiration directe. Tout dans une boîte.**

CE QUI EST BIEN :
- Concept brillant (commencer dans un 3x3x3)
- Scavenging system (taper les murs = ressources)
- Progression Compact Machine sizes très satisfaisante
- Chaque agrandissement = percée émotionnelle
- Decay Generators (RF depuis la radioactivité)
- Expert mais accessible grâce aux quêtes

CE QUI EST MAL :
- Pas de magie (100% tech)
- Endgame décevant ("Extreme Agoraphobia" = 6 quêtes bâclées)
- Pas de narratif (juste "escape the box")
- Pas de monde ouvert (tout en compact machines)
- Visuel monotone (tout est blanc/gris)

CE QU'ON PREND :
- Commencer dans une Compact Machine (exactement notre Âge 0)
- Scavenging des murs (notre système de grits)
- Progression par taille de salle
- Config des Compact Machines

CE QU'ON FAIT MIEUX :
- SORTIE vers le monde ouvert (Âge 3+)
- Magie intégrée (Botania, Astral, Blood Magic)
- Narratif complet (Dr. Voss)
- Endgame massif (Âges 3-9 en monde ouvert)
- 10 âges au lieu de "juste escape"

---

### E. FTB Infinity Evolved Expert — Classique historique
**Le premier expert pack populaire. La fondation.**

CE QUI EST BIEN :
- Recettes inter-mods (le premier à le faire à grande échelle)
- Progressif mais jamais injuste
- Stable et bien testé
- Communauté massive et guides partout

CE QUI EST MAL :
- Vieilli (mods datés)
- Pas de Galacticraft/espace
- Pas de narratif
- UI BetterQuesting basique

CE QU'ON PREND :
- La philosophie "aucun mod ne se suffit à lui-même"
- Stabilité et tests approfondis

---

### F. GreedyCraft — 540+ mods
**Le maximaliste. Tout et n'importe quoi.**

CE QUI EST BIEN :
- 3 modes de difficulté (Casual, Adventure, Expert)
- Shaders intégrés
- Musique contextuelle (calme en village, combat en donjon)
- 3rd person combat amélioré

CE QUI EST MAL :
- 540 mods = 6GB RAM minimum, crashes
- Trop de contenu sans direction
- Pas de progression claire
- Performance catastrophique en endgame

CE QU'ON PREND :
- L'idée de musique contextuelle (pour plus tard)
- Rien d'autre (trop bloated)

---

## 2. LES ERREURS COMMUNES À NE PAS REPRODUIRE

### Erreur 1 : Le grind sans but
**Coupable : Avaritia singularities, Mystical Agriculture grind**
- JAMAIS de "craft 1000x du même item" sans raison narrative
- Si le joueur grind, c'est pour un objectif clair et excitant
- SOLUTION : chaque grind est résolu par une meilleure automatisation (récompense le joueur malin)

### Erreur 2 : L'endgame bâclé
**Coupable : Compact Claustrophobia, SevTech (après Âge 4)**
- Les derniers 20% du pack sont souvent les plus faibles
- SOLUTION : Âges 7-9 ont autant de soin que l'Âge 0-1
- Le Nexus Absolu est un vrai climax, pas un "félicitations tu as fini"

### Erreur 3 : Les planètes vides
**Coupable : DD&SS, SevTech avec Galacticraft**
- L'espace = planètes avec un donjon et c'est tout
- SOLUTION : chaque planète a un matériau UNIQUE nécessaire pour le Nexus
- Fragment Espace-Temps = obtenu UNIQUEMENT via station orbitale

### Erreur 4 : Le power creep des armes
**Coupable : DD&SS (armes OP trop tôt)**
- Si le joueur one-shot les boss en mid-game, l'exploration perd son intérêt
- SOLUTION : armures et armes gatées par âge (In Control + CraftTweaker)
- Draconic Armor = Âge 7+ seulement

### Erreur 5 : Trop de mods sans utilité
**Coupable : GreedyCraft (540 mods dont 200 inutilisés)**
- 150 mods bien intégrés > 300 mods jetés ensemble
- SOLUTION : MOD.md documente CHAQUE mod et son rôle exact
- Si un mod n'a pas au moins 2 interactions avec d'autres → le supprimer

### Erreur 6 : Le début lent et pénible
**Coupable : SevTech Stone Age, certains skyblocks**
- "Punch trees pendant 1h" n'est pas du gameplay, c'est de la torture
- SOLUTION : Âge 0 a du contenu immédiatement (coffre de départ, grits, Tinkers rapide)
- Le joueur a une Smeltery en 30 minutes, pas 3 heures

### Erreur 7 : Pas de narratif
**Coupable : TOUS les packs sauf nous**
- Aucun pack 1.12.2 majeur n'a de vrai narratif intégré
- SOLUTION : Dr. E. Voss, le Nexus Absolu, les fragments = notre USP (Unique Selling Point)
- Chaque quête a du lore, chaque fragment a une histoire
- Le joueur VEUT trouver le Nexus, pas juste "finir le pack"

---

## 3. CE QUI REND NEXUS ABSOLU UNIQUE

### Notre USP (Unique Selling Point) en une phrase :
"Le premier expert pack 1.12.2 avec un narratif intégré, qui commence dans une Compact Machine et culmine dans la création d'un item qui transcende la matière."

### Les 7 piliers de Nexus Absolu :

**1. NARRATIF** — Dr. E. Voss, 30 ans de recherche, disparition mystérieuse
- Aucun autre pack n'a ça
- Le joueur est MOTIVÉ par l'histoire, pas juste par le gameplay
- Lore dans Patchouli, hints dans les quêtes

**2. COMPACT START** — 3x3x3 → monde ouvert
- Comme CC mais avec magie + tech + narratif
- Progression claustrophobique → liberté = arc émotionnel

**3. INTER-MOD FORCÉ** — 80% des recettes modifiées
- Comme E2E mais moins sadique
- 2-3 mods par machine, pas 5-6

**4. 10 ÂGES** — Progression claire de A à Z
- Comme SevTech mais sans les ages lents
- Chaque âge a un thème et un objectif clair

**5. MAGIE + TECH DÈS LE DÉBUT** — Pas de mods cachés pendant des heures
- Magie à l'Âge 2 (Botania + Astral + Blood Magic)
- Le joueur touche à tout assez vite

**6. ENDGAME MASSIF** — Âges 6-9 = espace, Draconic, Avaritia, Nexus
- Pas bâclé comme CC ou SevTech
- Chaque fragment du Nexus nécessite un âge entier

**7. AUTOMATISATION RÉCOMPENSÉE** — Chaque grind a une solution technique
- Le joueur qui automatise avance 10x plus vite
- Pas de grind sans solution (toujours une machine qui résout le problème)

---

## 4. COMPARAISON DIRECTE

| Critère | E2E | SevTech | DD&SS | CC | Nexus Absolu |
|---------|-----|---------|-------|----|--------------|
| Narratif | ❌ Aucun | ❌ Aucun | ❌ Aucun | ❌ Aucun | ✅ Dr. Voss |
| Début unique | ❌ Monde normal | ⚠️ Stone Age lent | ❌ Normal | ✅ Compact | ✅ Compact + lore |
| Progression par âges | ❌ Libre | ✅ GameStages | ⚠️ Semi-libre | ⚠️ Sizes only | ✅ 10 âges thématiques |
| Inter-mod forcé | ✅ Excellent | ✅ Bon | ✅ Bon | ⚠️ Moyen | ✅ 80% recettes |
| Magie | ✅ Complète | ⚠️ Arrive tard | ✅ Complète | ❌ Aucune | ✅ Dès Âge 2 |
| Espace | ⚠️ Basique | ⚠️ Vide | ⚠️ Vide | ❌ Aucun | ✅ Matériaux uniques |
| Endgame | ⚠️ Grind | ❌ Bâclé | ⚠️ Grind | ❌ Bâclé | ✅ Nexus Absolu |
| Quêtes | ✅ 650 | ✅ Advancements | ✅ 1750 | ✅ Guidé | 🎯 ~500 avec lore |
| Stabilité | ✅ Excellente | ✅ Bonne | ⚠️ Crashs | ✅ Bonne | ✅ 200 mods testés |
| Difficulté | Expert+ | Expert | Expert | Expert | Expert- (accessible) |

---

## 5. FEATURES À IMPLÉMENTER (par priorité)

### Priorité 1 — OBLIGATOIRE
- [x] Items custom ContentTweaker (28 items créés)
- [ ] Scripts CraftTweaker Âge 0 (drops murs, four bloqué)
- [ ] Scripts CraftTweaker Âge 1 (machines inter-mods)
- [ ] Quêtes Âge 0-1 finalisées (après scripts)
- [ ] Config In Control (mobs par âge/dimension)

### Priorité 2 — IMPORTANT
- [ ] Scripts Âge 2-3 (magie + chimie)
- [ ] JEI hiding par âge (cacher items des âges futurs)
- [ ] Livre Patchouli du Dr. Voss
- [ ] Config Spice of Life (maxHearts=50)
- [ ] Loot tables custom (donjons, boss)

### Priorité 3 — POLISH
- [ ] Textures custom améliorées (items ContentTweaker)
- [ ] Config spawn mobs par dimension CM
- [ ] Scripts Âge 4-9
- [ ] Quêtes Âge 2-9
- [ ] Recette finale Nexus Absolu 9x9
- [ ] Test complet de A à Z
- [ ] Musique/ambiance (si possible)

---

## 6. LEÇONS DES REVIEWS JOUEURS

### Ce que les joueurs ADORENT :
- "Quand le pack te force à apprendre un mod que tu n'aurais jamais touché"
- "Le sentiment d'accomplissement après avoir fait une machine complexe"
- "Chaque breakthrough tech qui rend les étapes précédentes plus faciles"
- "La progression qui a du sens et pas juste du grind"

### Ce que les joueurs DÉTESTENT :
- "Grind le même item pendant des heures" → on évite avec l'automatisation
- "Les mods obscurs et mal documentés" → on utilise les classiques + guides Patchouli
- "L'endgame qui tombe à plat" → notre Nexus Absolu est le climax
- "Les recettes qui semblent aléatoires" → chaque recette a une logique (MOD.md)
- "Le début lent et pénible" → notre Âge 0 est immédiatement engageant
- "Les planètes vides" → chaque planète = matériau unique
- "Le power creep" → armures/armes gatées par âge

---

*Document généré le 26 Mars 2026*
*Sources : E2E (GitHub), SevTech (GitHub + CurseForge), DD&SS (CurseForge + reviews), 
 CC (Wiki + reviews), GreedyCraft (MC Forum), MMCReviews.com, FTB Forums*
