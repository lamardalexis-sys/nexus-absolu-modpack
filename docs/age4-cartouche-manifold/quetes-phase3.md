# Age 4 — Phase 3 détaillée (Q31-Q45 : La Chimie Sèche)

> Troisième batch — pétrochimie, ammoniaque, acides, organique. 15 quêtes (questIDs 231-245).
> Pré-requis Phase 2 complete (Q30).
> Voir : `docs/age4-cartouche-manifold/lines/L1-petrochimie.md`,
> `L6-acides-ammoniaque.md`, `L7-organique-acetone.md`

*"L'ammoniaque est le sang de l'industrie. Le pétrole en est l'os."*

---

## 📐 Layout

Phase 3 commence à `y=720` :

```
   Q31 ── Q32 ── Q33 ── Q34 ── Q35
  (0,720) (80,720) ... (320,720)
                                  │
   Q40 ── Q39 ── Q38 ── Q37 ── Q36
  (320,810)
   │
   Q41 ── Q42 ── Q43 ── Q44 ── Q45
  (0,900) ...                      (320,900) ──► Phase 4 unlock
```

---

## Q31 : "Le pétrole brut, ce mélange dégueulasse"

- **ID** 231 | **Pos** (0, 720) | **Pré** : Q30
- **Type** : retrieval `8x bucket immersivepetroleum:crudeoil` (ou IE oil)
- **Icon** : Crude oil bucket
- **Récompense** : 1 Pumpjack item (IP) + plans Distillation
- **Description** :

```
§7§oTu commences la chimie organique avec ce que la Terre te donne de pire :
§7du §lpetrole brut§r§7. C'est un melange chaotique de 200 hydrocarbures
§7+ soufre + metaux lourds + bouts d'animaux du Crétacé.

§7Avant de pouvoir l'utiliser tu vas devoir :
§7  1. Le pomper (Pumpjack IP/PneumaticCraft)
§7  2. Le degazer (separer le gaz naturel)
§7  3. Le distiller (kerosene + naphta + diesel)
§7  4. Le desulfurer (catalyseur CoMo de Phase 2 Q22)
§7  5. Le re-cracker (Cracker IP) -> Solvant Neutre alpha

§7Cette quete : juste avoir 8 buckets de crude.

§e§lObjectif : §78 buckets Crude Oil
§e§lRecompense : §71 Pumpjack item + plans Distillation"""
```

---

## Q32 : "La distillation atmosphérique"

- **ID** 232 | **Pos** (80, 720) | **Pré** : Q31
- **Type** : retrieval `4x bucket nexusabsolu:kerosene_desulfured`
- **Icon** : kerosene bucket (custom)
- **Récompense** : 4 Diesel + plans HDS
- **Description** :

```
§7§oDistillation atmospherique = chauffer le crude a 350 C et separer
§7par point d'ebullition :
§7  - 30-200 C  -> naphta (essence brute)
§7  - 200-300 C -> kerosene (jet fuel)
§7  - 300-350 C -> diesel
§7  - residu lourd -> bitume

§7Tu vas vouloir le kerosene. C'est notre base pour le Solvant Neutre.

§7L'IP Distillation Tower fait ca naturellement. Sinon le Crude Oil
§7Distillation Multiblock de PneumaticCraft.

§e§lObjectif : §74 buckets Kerosene Desulfure
§e§lRecompense : §74 Diesel + plans HDS
```

---

## Q33 : "L'hydrodésulfuration et le CoMo"

- **ID** 233 | **Pos** (160, 720) | **Pré** : Q32
- **Type** : retrieval `4x bucket nexusabsolu:kerosene_premium`
- **Icon** : kerosene_premium bucket
- **Récompense** : 8 Catalyseur CoMo + plans Cracker
- **Description** :

```
§7§oTu as ton kerosene. Mais il contient 0.3% de soufre.
§7Pour le Solvant Neutre il faut moins de 10 ppm.

§7§lHDS§r§7 (HydroDeSulfuration) :
§7  Kerosene + H2 + CoMo a 350 C 50 atm -> Kerosene_premium + H2S

§7Le H2S sera bridge vers la Phase 6 (Procede Claus -> Soufre pur).
§7Tu connais maintenant les §lcatalyseurs§r§7.

§8§o"Sans catalyseur la chimie est lente. Avec catalyseur la chimie
§8est tactique. Voila pourquoi tu es la Sujet 47." — E.V.

§e§lObjectif : §74 buckets Kerosene Premium (desulfure < 10 ppm)
§e§lRecompense : §78 Catalyseur CoMo + plans Cracker
```

---

## Q34 : "Le craquage et le Solvant alpha"

- **ID** 234 | **Pos** (240, 720) | **Pré** : Q33
- **Type** : retrieval `2x bucket nexusabsolu:solvant_alpha`
- **Icon** : solvant_alpha bucket
- **Récompense** : 1 Solvant alpha + plans Composes alpha
- **Description** :

```
§7§oCracker le Kerosene Premium :
§7  Kerosene + chaleur 800 C + catalyseur Pt -> alcanes leger + alcenes

§7On retient les alcenes ramifies. Avec un peu d'aromatique benzene
§7on obtient le §lSolvant Neutre alpha§r§7.

§7C'est la base de l'§lampoule§r§7 chimique de la cartouche.
§7Ca dissout n'importe quoi sauf l'eau et les metaux nobles.

§e§lObjectif : §72 buckets Solvant alpha
§e§lRecompense : §71 Solvant alpha + plans Composes alpha
```

---

## Q35 : "Composé alpha — l'acide stabilisé"

- **ID** 235 | **Pos** (320, 720) | **Pré** : Q34
- **Type** : retrieval `1x contenttweaker:compose_alpha`
- **Icon** : compose_alpha (custom)
- **Récompense** : 4 Compose alpha + plans Procede Haber-Bosch
- **Description** :

```
§7§oOK ton premier compose intermediaire.

§7§lCompose alpha§r§7 = 1 Solvant alpha + 1 H2SO4 + 1 HNO3 a basse temp.
§7C'est un acide super-acide stabilise (proton tres reactif).

§7Tu vas l'utiliser pour activer le centre cyclique du compose epsilon
§7final (Phase 5).

§7Pour l'instant garde-en 4 dans un Resonant Tank dedie. Ne le mets
§7pas en contact avec l'eau ordinaire ou ca explose.

§e§lObjectif : §71 Compose alpha synthese
§e§lRecompense : §74 Compose alpha + plans Haber-Bosch
```

---

## Q36 : "Haber-Bosch et l'ammoniaque"

- **ID** 236 | **Pos** (320, 810) | **Pré** : Q35
- **Type** : retrieval `8x bucket nexusabsolu:ammoniaque`
- **Icon** : ammoniaque bucket
- **Récompense** : 16 NH3 + plans Ostwald
- **Description** :

```
§7§oFritz Haber. 1908. Procede industriel le plus important du 20e siecle :
§7  N2 + 3 H2 -> 2 NH3   (450 C, 200 atm, catalyseur Fe)

§7Sans Haber-Bosch, pas d'engrais. Pas d'agriculture moderne.
§7Pas de 8 milliards d'humains. C'est ce processus la qui a permis
§7a l'humanite de se multiplier x4 en un siecle.

§7T'as besoin de :
§7  - 8 N2 (Phase 1 Q7)
§7  - 24 H2 (electrolyse, depuis Phase 1)
§7  - Un Fischer Tropsch Reactor (Mekanism Pressurized Reaction Chamber)

§e§lObjectif : §78 buckets Ammoniaque
§e§lRecompense : §716 NH3 + plans Ostwald
```

---

## Q37 : "Ostwald et l'acide nitrique"

- **ID** 237 | **Pos** (240, 810) | **Pré** : Q36
- **Type** : retrieval `8x bucket nexusabsolu:hno3`
- **Icon** : HNO3 bucket
- **Récompense** : 16 HNO3 + plans Acide sulfurique
- **Description** :

```
§7§oWilhelm Ostwald. 1902. Suite logique d'Haber-Bosch :
§7  4 NH3 + 5 O2 -> 4 NO + 6 H2O   (catalyseur Pt-Rh, 850 C)
§7  2 NO + O2 -> 2 NO2
§7  3 NO2 + H2O -> 2 HNO3 + NO

§7L'acide nitrique sert a :
§7  - Synthese Compose alpha (Q35) -- deja stocke ?
§7  - Eau Regale (3 HCl + HNO3) pour Au -> AuCl3 (Q44)
§7  - Explosifs (mais on en fait pas pour l'Age 4)

§e§lObjectif : §78 buckets HNO3
§e§lRecompense : §716 HNO3 + plans H2SO4
```

---

## Q38 : "L'acide sulfurique, l'industrie en bouteille"

- **ID** 238 | **Pos** (160, 810) | **Pré** : Q37
- **Type** : retrieval `8x bucket nexusabsolu:h2so4`
- **Icon** : H2SO4 bucket
- **Récompense** : 16 H2SO4 + plans HCl
- **Description** :

```
§7§oH2SO4. Le composant chimique le plus produit au monde
§7(245 millions de tonnes par an). C'est un §lindicateur§r§7
§7du PIB industriel d'un pays.

§7§lProcede de contact§r§7 :
§7  S + O2 -> SO2  (Procede Claus du H2S Phase 3 ?)
§7  2 SO2 + O2 -> 2 SO3  (catalyseur V2O5, 450 C)
§7  SO3 + H2O -> H2SO4

§7Pour la cartouche : Compose alpha, activation Compose beta,
§7cleaning catalyseur Pt entre cycles.

§e§lObjectif : §78 buckets H2SO4
§e§lRecompense : §716 H2SO4 + plans HCl
```

---

## Q39 : "L'acide chlorhydrique"

- **ID** 239 | **Pos** (80, 810) | **Pré** : Q38
- **Type** : retrieval `4x bucket nexusabsolu:hcl`
- **Icon** : HCl bucket
- **Récompense** : 16 HCl + plans Eau Regale
- **Description** :

```
§7§oHCl. Plus simple : H2 + Cl2 -> 2 HCl, brule a la flamme.

§7Tu en as besoin pour :
§7  - Eau Regale (3 HCl + HNO3 -> dissout Au)
§7  - Pickling des metaux (decapage avant Bessemer)
§7  - Compose beta (Phase 5 organometalliques)

§e§lObjectif : §74 buckets HCl
§e§lRecompense : §716 HCl + plans Eau Regale
```

---

## Q40 : "Eau Régale et Compose beta"

- **ID** 240 | **Pos** (0, 810) | **Pré** : Q39
- **Type** : retrieval `1x contenttweaker:compose_beta`
- **Icon** : compose_beta
- **Récompense** : 2 Compose beta + plans Acetone
- **Description** :

```
§7§oL'§lEau Regale§r§7 (regis aqua = "eau royale" en latin) :
§7  3 HCl + HNO3 -> 2 H2O + NOCl + Cl2

§7C'est le seul truc qui dissout l'or et le platine. Donc on l'utilise
§7pour activer ces metaux nobles en chlorides solubles, qui se
§7lient ensuite aux ligands organiques.

§7§lCompose beta§r§7 = AuCl3 + benzene + tributylphosphine -> bizarre
§7complexe organometallique cyan. Mort si tu le touches a main nue.

§e§lObjectif : §71 Compose beta
§e§lRecompense : §72 Compose beta + plans Acetone
```

---

## Q41 : "Le cumène et l'acétone"

- **ID** 241 | **Pos** (0, 900) | **Pré** : Q40
- **Type** : retrieval `4x bucket nexusabsolu:acetone`
- **Icon** : acetone bucket
- **Récompense** : 8 Acetone + plans Phenol
- **Description** :

```
§7§o§lProcede Cumene-Hock§r§7. Inventee 1944. Produit 90% de l'acetone
§7mondiale + 95% du phenol mondial en meme temps.

§7  Benzene + Propylene -> Cumene  (catalyseur H3PO4)
§7  Cumene + O2 -> Cumene Hydroperoxide
§7  CumOH -> Acetone + Phenol  (rupture acide)

§7Phenol : pour les composes magiques Phase 5.
§7Acetone : solvant pour la Manifoldine.

§e§lObjectif : §74 buckets Acetone
§e§lRecompense : §78 Acetone + plans Phenol
```

---

## Q42 : "L'éther étoilé"

- **ID** 242 | **Pos** (80, 900) | **Pré** : Q41
- **Type** : retrieval `2x bucket nexusabsolu:ether_etoile`
- **Icon** : ether_etoile (custom)
- **Récompense** : 4 Ether + plans Phenol substitue
- **Description** :

```
§7§oEther stabilise par exposition prolongee a la Liquid Starlight
§7(Astral Sorcery). Phase intermediaire avant Compose epsilon.

§7Recette :
§7  Acetone + Heavy Water + Liquid Starlight (Mekanism Reaction Chamber)
§7  -> Ether Etoile (1000mB output / 4000 input)

§7Spoiler : ca commence a sentir la magie. Phase 5 va etre tendue.

§e§lObjectif : §72 buckets Ether Etoile
§e§lRecompense : §74 Ether + plans Phenol substitue
```

---

## Q43 : "Phenol substitue, le précurseur"

- **ID** 243 | **Pos** (160, 900) | **Pré** : Q42
- **Type** : retrieval `4x contenttweaker:phenol_substitue`
- **Icon** : phenol_substitue (custom)
- **Récompense** : 8 Phenol substitue + plans Composes Voss
- **Description** :

```
§7§oPhenol modifie pour porter 2 groupes -OCH3 (methoxy) en para
§7(positions 2-4 du cycle benzenique).

§7Synthese : Phenol + 2 MeOH + H2SO4 catalyseur 180 C.
§7C'est tendu mais ca marche.

§7Le phenol substitue est le §lcoeur aromatique§r§7 de la Manifoldine.
§7C'est lui qui se lie au lithium dans la cyclisation finale.

§e§lObjectif : §74 phenol substitue
§e§lRecompense : §78 phenol + plans Composes Voss
```

---

## Q44 : "Le carbone activé et l'or chargé"

- **ID** 244 | **Pos** (240, 900) | **Pré** : Q43
- **Type** : retrieval `8x contenttweaker:carbone_actif_au`
- **Icon** : carbone_actif_au (custom)
- **Récompense** : 16 Carbone Au + plans Synthese Voss
- **Description** :

```
§7§oCharbon active impregne d'AuCl3 a 0.5 wt%.
§7Catalyseur ultra-selectif pour la cyclisation epsilon Phase 5.

§7Recette :
§7  Charbon + AuCl3 (Eau Regale) -> Charbon Au impregne
§7  Sechage 110 C 24h sous Argon

§7Stocke 8. La cyclisation Manifoldine va en avaler 4 d'un coup.

§e§lObjectif : §78 Carbone Actif Au
§e§lRecompense : §716 Carbone Au + plans Synthese Voss
```

---

## Q45 : "Phase 3 complete, fin de la chimie sèche"

- **ID** 245 | **Pos** (320, 900) | **Pré** : Q44
- **Type** : checkbox (lit Voss Codex chap.3)
- **Icon** : Voss Codex Patchouli book
- **Récompense** : Voss Codex + 16 pain + 4 Bouteilles XP + débloquage Phase 4
- **Description** :

```
§7§oFin de Phase 3. Tu as :
§7  - Solvant Neutre alpha
§7  - Compose alpha (super-acide stabilise)
§7  - Compose beta (organometallique Au-PR3)
§7  - Acetone + Ether Etoile + Phenol substitue
§7  - Carbone Au charge

§7Le carnet de Voss s'ouvre sur le chapitre 3 :
§7  §6"La chimie t'a fait suer. Maintenant tu vas faire chauffer
§6  des trucs dangereux. Phase 4 : nucleaire."§r

§8§o"Le H et le N et le O et le C ne sont que des poupees russes.
§8Pour cracker la simulation il faut casser le NOYAU. Bienvenue
§8dans le Theoreme V." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.3 (Chimie Seche)
§e§lRecompense : §7Phase 4 nucleaire debloquee + 4 Bouteilles XP
```

---

## ⚠️ Items custom à créer pour Phase 3

Existants dans le ZS Age4_Manifold_Content :
- ✅ `kerosene_desulfured`, `kerosene_premium`, `solvant_alpha`
- ✅ `ammoniaque`, `hno3`, `h2so4`, `hcl`
- ✅ `aqua_regia` (eau régale)
- ✅ `naphtha`, `benzene`

À CRÉER (Phase 3 nouvelles) :
- ❌ `compose_alpha` (item) — Q35
- ❌ `compose_beta` (item) — Q40
- ❌ `acetone` (fluide) — Q41
- ❌ `ether_etoile` (fluide) — Q42
- ❌ `phenol_substitue` (item) — Q43
- ❌ `carbone_actif_au` (item) — Q44

Je le ferai dans le commit suivant.
