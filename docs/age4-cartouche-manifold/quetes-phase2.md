# Age 4 — Phase 2 détaillée (Q16-Q30 : Les Métaux)

> Deuxième batch — la pyrométallurgie. 15 quêtes (questIDs 216-230).
> Pré-requis Phase 1 complete (Q15).
> Voir : `docs/age4-cartouche-manifold/lines/L4-pyrometallurgie.md`

*"Voss avait raison sur le Théorème I. Maintenant prouve-le sur les métaux."*

---

## 📐 Layout

Phase 2 commence à `y=400` (sous Phase 1 qui finit à y=260) et serpente :

```
   Q16 ── Q17 ── Q18 ── Q19 ── Q20
  (0,400) (80,400) (160,400) (240,400) (320,400)
                                          │
   Q25 ── Q24 ── Q23 ── Q22 ── Q21
  (320,490)
   │
   Q26 ── Q27 ── Q28 ── Q29 ── Q30
  (0,580) (80,580) (160,580) (240,580) (320,580) ──► Phase 3 unlock
```

---

## Q16 : "Le fer ment moins que l'eau"

- **ID** 216 | **Pos** (0, 400) | **Pré** : Q215 (Q15)
- **Type** : retrieval `64x ingotIron` (ore dict)
- **Icon** : Iron ingot
- **Récompense** : Mekanism Bessemer plans + 32 iron ingots
- **Description** :

```
§7§oOK. La chimie du gaz c'est fait. Maintenant les §lmetaux§r§7.

§7Spoiler : la chimie c'est rien sans les metaux.
§7Les §lcatalyseurs§r§7 c'est des metaux.
§7Les §lparois de reacteurs§r§7 c'est des metaux.
§7L'§lampoule§r§7 de la cartouche c'est de l'iridium platine.

§7On commence simple : 64 lingots de fer pur.
§7Tu vas vouloir un Pulverizer + Induction Smelter pour x2 yield.

§e§lObjectif : §764 Iron ingots
§e§lRecompense : §7Mekanism Bessemer plans + 32 iron
```

---

## Q17 : "Le cuivre conduit"

- **ID** 217 | **Pos** (80, 400) | **Pré** : Q16
- **Type** : retrieval `64x ingotCopper`
- **Icon** : Copper ingot
- **Récompense** : 32 Copper Plates + plans Cuivre Pur
- **Description** :

```
§7§oCu. 8.96 g/cm3. Conduit l'electricite mieux que tout sauf l'argent.

§7Tu vas en avoir besoin pour :
§7  - Les bobines des Energy Conduits
§7  - Le Bessemer (tubes anodiques)
§7  - L'electrolyse de l'eau (electrodes)

§7Les Apatites de Forestry te donneront du phosphore en sous-produit.
§7Plus tard. Pas maintenant.

§e§lObjectif : §764 Copper ingots
§e§lRecompense : §732 Copper Plates + plans
```

---

## Q18 : "L'aluminium et le procédé Bayer"

- **ID** 218 | **Pos** (160, 400) | **Pré** : Q17
- **Type** : retrieval `32x ingotAluminium`
- **Icon** : Aluminium ingot
- **Récompense** : Hall-Heroult plans + 16 Cryolite
- **Description** :

```
§7§oAl. Le metal qui n'existait pas a l'etat pur sur Terre avant 1825.
§7Pourquoi ? Parce qu'il fallait un courant electrique pour le faire.

§7§lProcede Bayer§r§7 : Bauxite + NaOH chaud -> NaAlO2 (aluminate) + boue rouge
§7§lProcede Hall-Heroult§r§7 : NaAlO2 + Cryolite fondue + 4V -> Al + O2

§7T'as deja le NaOH (Phase 1 Q8). Manque la cryolite (Q19).

§e§lObjectif : §732 Aluminium ingots
§e§lRecompense : §7Plans Hall-Heroult + 16 Cryolite
```

---

## Q19 : "La cryolite ouvre la voie"

- **ID** 219 | **Pos** (240, 400) | **Pré** : Q18
- **Type** : retrieval `64x contenttweaker:cryolite_dust` ou Mekanism cryolite
- **Icon** : Cryolite dust
- **Récompense** : 16 Aluminium Plates + plans Titane (Kroll)
- **Description** :

```
§7§oNa3AlF6. La cryolite. Trouvee naturellement au Groenland en quantite
§7tellement faible qu'a la fin du 20e siecle on a du la synthetiser.

§7Synthese : 6 NaF + Al(OH)3 + 3/2 O2 -> Na3AlF6 + ...
§7Tu as deja le NaOH. Tu as deja le F2 (Phase 1 Q4 -> Cryo-distillation).

§7Stocke 64 dust. Tu vas en bruler beaucoup au Hall-Heroult.

§e§lObjectif : §764 Cryolite dust
§e§lRecompense : §716 Aluminium + plans Kroll
```

---

## Q20 : "Le titane, ce difficile"

- **ID** 220 | **Pos** (320, 400) | **Pré** : Q19
- **Type** : retrieval `16x ingotTitanium`
- **Icon** : Titanium ingot
- **Récompense** : 8 Titanium Plates + plans Tungstene
- **Description** :

```
§7§oTi. Resistance/poids champion. Indispensable pour la cartouche
§7(le casing externe doit resister a 800 C cyclisation).

§7§lProcede Kroll§r§7 :
§7  TiO2 + 2 Cl2 + C -> TiCl4 + CO2  (chloruration)
§7  TiCl4 + 2 Mg -> Ti + 2 MgCl2     (reduction sous Argon)

§7Pour ca il te faut :
§7  - Cl2 (Phase 1 cryo-distillation Saumure)
§7  - Argon (Phase 1 Q6, atmosphere inerte)
§7  - Mg (a faire sur Magnesite + reduction Pidgeon)

§e§lObjectif : §716 Titanium ingots
§e§lRecompense : §78 Ti Plates + plans Tungstene
```

---

## Q21 : "Le tungstène, le plus dur des metaux"

- **ID** 221 | **Pos** (320, 490) | **Pré** : Q20
- **Type** : retrieval `8x ingotTungsten`
- **Icon** : Tungsten ingot
- **Récompense** : 4 W Plates + plans Cobalt
- **Description** :

```
§7§oW (de Wolfram en allemand). Point de fusion 3422 C. Champion absolu.

§7Tu vas en avoir besoin pour :
§7  - Catalyseur CoMo (HDS petrochimie Phase 3)
§7  - Filaments des cables d'arc Hall-Heroult
§7  - Casing chambre cyclisation Manifold

§7Spoiler : tu vas en cramer beaucoup.

§e§lObjectif : §78 Tungsten ingots
§e§lRecompense : §74 W Plates + plans Cobalt
```

---

## Q22 : "Le cobalt et le Mo, ces deux la"

- **ID** 222 | **Pos** (240, 490) | **Pré** : Q21
- **Type** : retrieval `8x ingotCobalt + 8x ingotMolybdenum`
- **Icon** : Cobalt ingot
- **Récompense** : Catalyseur CoMo (item custom) + plans Pt/Pd
- **Description** :

```
§7§oCo et Mo. Les deux pour faire le §lcatalyseur CoMo§r§7.

§7Le CoMo (cobalt-molybdene sulfure) c'est ce qui rend la
§7§lhydrodesulfuration§r§7 possible :
§7  Petrole + H2 + CoMo -> petrole desulfure + H2S

§7Sans ce truc tu peux pas raffiner ton kerosene Phase 3.
§7Et sans kerosene raffine pas de Solvant Neutre alpha.
§7Et sans Solvant Neutre alpha pas de cartouche.

§7Tu vois la chaine de dependance ?

§e§lObjectif : §78 Co + 8 Mo
§e§lRecompense : §71 Catalyseur CoMo (custom) + plans Pt/Pd
```

---

## Q23 : "Platine et palladium en quetes paralleles"

- **ID** 223 | **Pos** (160, 490) | **Pré** : Q22
- **Type** : retrieval `4x ingotPlatinum + 4x ingotPalladium`
- **Icon** : Platinum ingot
- **Récompense** : 2 Pt Plates + 2 Pd Plates + plans Iridium
- **Description** :

```
§7§oPt et Pd. Metaux nobles ultra rares.

§7Tu en auras besoin pour :
§7  - Pot d'echappement Bessemer (Pt comme catalyseur Ostwald)
§7  - Membrane de cyclisation (Pd selectif H2)
§7  - Encartouchage (electrodes inertes)

§7Tu peux les obtenir via Mekanism Tier 4 ore processing
§7(double dust + chemical washer + dissolution + crystallizer).

§e§lObjectif : §74 Pt + 4 Pd
§e§lRecompense : §72 Plates each + plans Iridium
```

---

## Q24 : "L'iridium, l'élément le plus dense"

- **ID** 224 | **Pos** (80, 490) | **Pré** : Q23
- **Type** : retrieval `4x ingotIridium`
- **Icon** : Iridium ingot
- **Récompense** : 2 Iridium Plates + plans Or pur
- **Description** :

```
§7§oIr. 22.56 g/cm3. Le plus dense apres l'osmium. Si tu fais tomber
§7un cube de 1 cm3 sur ton pied il pese 22 grammes mais te casse l'os.

§7L'iridium c'est l'§lampoule§r§7 de la cartouche. Il faut qu'elle
§7soit assez resistante pour ne pas reagir avec les composes
§7delta et epsilon de la Phase 5 (qui tueraient n'importe quel metal).

§7Iridium = Mekanism T4 + AE2 sieve sur Mining Dimension Crushed Ores.
§7Bonne chasse.

§e§lObjectif : §74 Iridium ingots
§e§lRecompense : §72 Ir Plates + plans Or
```

---

## Q25 : "L'or, le metal noble"

- **ID** 225 | **Pos** (320, 490) | ⚠️ Position Q21 = (320,490). **Pos** : (0, 490) | **Pré** : Q24
- **Type** : retrieval `32x ingotGold`
- **Icon** : Gold ingot
- **Récompense** : 8 Gold Plates + plans Eau Regale
- **Description** :

```
§7§oAu. Le metal de l'humanite. Brillant. Stable. Conducteur.

§7Pour la cartouche tu en as besoin pour :
§7  - Contacts electriques inertes
§7  - Solvant Eau Regale (3 HCl + 1 HNO3 -> dissout l'or)
§7    qu'on utilisera Phase 3 pour activer Au -> AuCl3

§e§lObjectif : §732 Gold ingots
§e§lRecompense : §78 Au Plates + plans Eau Regale
```

---

## Q26 : "Le plomb, ce vieux con"

- **ID** 226 | **Pos** (0, 580) | **Pré** : Q25
- **Type** : retrieval `64x ingotLead`
- **Icon** : Lead ingot
- **Récompense** : 32 Lead Plates + plans blindage Phase 4
- **Description** :

```
§7§oPb. Le metal qui a empoisonne l'Empire Romain.
§7Densite 11.3 g/cm3. Conduit pas grand chose. Mais !
§7Il bloque les rayons gamma comme personne.

§7Tu vas en avoir besoin Phase 4 :
§7  - Blindage du reacteur fission
§7  - Conteneurs UF6 (uranium hexafluoride)
§7  - Doublure capsule Pu-Be

§7Stocke 64. La Phase 4 te demandera 200 plates en pic.

§e§lObjectif : §764 Lead ingots
§e§lRecompense : §732 Pb Plates + plans blindage
```

---

## Q27 : "L'uranium est jaune"

- **ID** 227 | **Pos** (80, 580) | **Pré** : Q26
- **Type** : retrieval `4x dustYellowcake` ou `4x ingotUranium`
- **Icon** : Uranium ingot
- **Récompense** : 1 capsule Yellowcake + plans UF6
- **Description** :

```
§7§oU. 92 protons. Element le plus lourd que tu trouves a l'etat naturel.

§7Le minerai s'appelle "yellowcake" (U3O8). On va le fluorer en UF6
§7gazeux Phase 4 pour le centrifuger et l'enrichir.

§7Pour l'instant on stocke 4 dust.
§7Le minage te tue ? Active la radiation immunity (potion).

§8§o"L'uranium est le seul element naturel qui souviens
§8de la nucleosynthese stellaire qui l'a cree." — E.V.

§e§lObjectif : §74 Uranium ingots/dust
§e§lRecompense : §71 Yellowcake + plans UF6
```

---

## Q28 : "Le lithium dans la roche"

- **ID** 228 | **Pos** (160, 580) | **Pré** : Q27
- **Type** : retrieval `32x ingotLithium`
- **Icon** : Lithium ingot
- **Récompense** : 16 Li Plates + plans Tritium production
- **Description** :

```
§7§oLi. 3 protons. L'element le plus leger qui soit solide a 25 C.

§7Tu l'as deja croise (Phase 1 Q14 prestock Tritium).
§7Maintenant tu en stockes 32 ingots pour :
§7  - Compose gamma3 = 6Li + Tritium = 6LiT
§7  - Source neutronique reacteur fission (Li-6 + n -> T + 4He)
§7  - Beryllium-Li alloys pour le pot reactif Phase 5

§e§lObjectif : §732 Lithium ingots
§e§lRecompense : §716 Li Plates + plans Tritium
```

---

## Q29 : "Beryllium et magnesium, les jumeaux"

- **ID** 229 | **Pos** (240, 580) | **Pré** : Q28
- **Type** : retrieval `16x ingotBeryllium + 32x ingotMagnesium`
- **Icon** : Beryllium ingot
- **Récompense** : 8 Be + 16 Mg Plates + plans Pu-Be
- **Description** :

```
§7§oBe (4) et Mg (12). Group 2 du tableau periodique.
§7Tous les deux ultra-reactifs avec H2O et O2. Tu les stockes
§7sous Argon (Phase 1 Q6).

§7Pour la cartouche :
§7  - Be -> Capsule Pu-Be (source neutronique gamma2)
§7  - Mg -> Reduction Kroll TiCl4 (etape Q20 Titane)

§7Si t'as pas encore stocke 32 Mg, ton procede Kroll est a sec.

§e§lObjectif : §716 Be + 32 Mg
§e§lRecompense : §7Plates each + plans Pu-Be
```

---

## Q30 : "30 elements, fin de Phase 2"

- **ID** 230 | **Pos** (320, 580) | **Pré** : Q29
- **Type** : checkbox (lit le carnet chap.2)
- **Icon** : `nexusabsolu:voss_codex` Patchouli book
- **Récompense** : Voss Codex + 16 pain + 4 Bouteilles XP + débloquage Phase 3
- **Description** :

```
§7§oCompte tes lingots, sujet 47.

§7Fe, Cu, Al, Ti, W, Co, Mo, Pt, Pd, Ir, Au, Pb, U, Li, Be, Mg.
§7Plus tous les natifs (Sn, Ag, Ni, Zn, Bi, Mn, Cr, V, Si, P, S, Na, Cl, F, K).

§7Tu as les 30 elements. La Phase 2 est terminee.

§8§o"Tu as transmute la Terre. Maintenant tu vas transmuter
§8les molecules. Phase 3 : la chimie organique." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.2 (Pyrometallurgie)
§e§lRecompense : §7Phase 3 debloquee + 4 Bouteilles XP
```

---

## ⚠️ Items custom à créer pour Phase 2

- ✅ Tous les ingots existent dans le modpack (Mekanism + Thermal + EnderIO + IE)
- ❌ `contenttweaker:cryolite_dust` — **TODO** Q19 (ou utiliser Mekanism cryolite si existe)
- ❌ `contenttweaker:catalyseur_como` (item custom) — **TODO** Q22
- ❌ `contenttweaker:yellowcake_dust` — **TODO** Q27 (sinon utiliser dust uranium classique)

Aussi : `ingotMolybdenum`, `ingotPalladium`, `ingotIridium`, `ingotBeryllium` peut-être pas dans le modpack vanilla, vérifier oredict.
