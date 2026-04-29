# Age 4 — Phase 4 détaillée (Q46-Q55 : Le Feu Nucléaire)

> Quatrième batch — uranium, fission, plutonium, tritium, neutrons. **10 quêtes** (questIDs 246-255).
> Pré-requis Phase 3 complete (Q45).
> Voir : `docs/age4-cartouche-manifold/lines/L5-nucleaire.md`

*"Voss a touché à des choses qu'il ne fallait pas toucher. Tu vas faire pareil."*

---

## 📐 Layout

Phase 4 commence à `y=1040`. **Plus court : 10 quêtes** = 2 rangées de 5 :

```
   Q46 ── Q47 ── Q48 ── Q49 ── Q50
  (0,1040) ...                  (320,1040)
                                          │
   Q55 ── Q54 ── Q53 ── Q52 ── Q51
  (320,1130) ...                 (0,1130)
```

---

## Q46 : "Le réacteur fission, ce monstre"

- **ID** 246 | **Pos** (0, 1040) | **Pré** : Q45
- **Type** : crafting `1x mekanism:reactorglass` (placeholder pour reactor controller)
- **Icon** : Reactor controller block
- **Récompense** : Plans Centrifugeuse + 2 Bouteilles XP
- **Description** :

```
§7§oTu as 30 elements, des acides, des ethers magiques.
§7Maintenant tu vas casser le NOYAU. Sujet 47 : welcome to nuclear physics.

§7Construis un reacteur Mekanism Fission ou un Big Reactor :
§7  - Coeur : barres uranium
§7  - Moderateur : Heavy Water (D2O, Phase 1 Q13)
§7  - Refroidissement : Resonant Cells / Sodium liquide
§7  - Blindage : Lead (Phase 2 Q26)

§7Sortie : 50M+ RF/t en pic. Tu en auras besoin pour la Phase 5
§7(Liquid Starlight + Botania ne pousse pas avec rien).

§e§lObjectif : §7Crafte 1 Reactor Glass / Controller Mekanism
§e§lRecompense : §7Plans Centrifugeuse + 2 Bouteilles XP
```

---

## Q47 : "L'enrichissement de l'uranium"

- **ID** 247 | **Pos** (80, 1040) | **Pré** : Q46
- **Type** : retrieval `8x bucket nexusabsolu:uf6_gas`
- **Icon** : UF6 gas bucket
- **Récompense** : 4 UF6 + plans Pu
- **Description** :

```
§7§oUF6 = uranium hexafluoride. Solide a 56 C, gazeux au-dessus.
§7C'est le seul compose d'uranium volatile, donc le seul qu'on
§7peut centrifuger pour separer U-235 de U-238.

§7Synthese :
§7  Yellowcake (U3O8, Phase 2 Q27) + 6 HF -> UF6 + UO2 + ...

§7Le HF (acide fluorhydrique) tu l'as deja (F2 + H2 -> 2 HF, Phase 1).

§7§4§lDANGER§r §7: l'UF6 attaque le verre normal. Reservoirs Lead/Iridium only.

§e§lObjectif : §78 buckets UF6 gazeux
§e§lRecompense : §7Plans enrichissement Pu + 2 Bouteilles XP
```

---

## Q48 : "La fission et son plutonium"

- **ID** 248 | **Pos** (160, 1040) | **Pré** : Q47
- **Type** : retrieval `4x ingotPlutonium`
- **Icon** : Plutonium ingot
- **Récompense** : 2 Pu Plates + plans Be source
- **Description** :

```
§7§oPu-239 = ton vrai isotope d'interet. Pourquoi ?
§7  - Section efficace fission x2 par rapport a U-235
§7  - Demi-vie 24000 ans (assez stable pour l'utiliser)
§7  - Cree dans le reacteur fission par capture neutronique :
§7    U-238 + n -> U-239 -> Np-239 -> Pu-239

§7Tu as besoin de 4 ingots de Pu pour la capsule Pu-Be (Q49).
§7Recyclage des barres usagees du reacteur Mekanism IsotopicCentrifuge.

§8§o"Le plutonium n'existe pas dans la nature. C'est un metal
§8artificiel. C'est nous qui l'avons cree. C'est le premier
§8signe que nous sommes des Createurs." — E.V.

§e§lObjectif : §74 Plutonium ingots
§e§lRecompense : §72 Pu Plates + plans capsule Be
```

---

## Q49 : "Capsule Pu-Be — la source neutronique"

- **ID** 249 | **Pos** (240, 1040) | **Pré** : Q48
- **Type** : retrieval `1x contenttweaker:capsule_pube`
- **Icon** : capsule_pube (custom)
- **Récompense** : Plans Tritium pur + 2 Bouteilles XP
- **Description** :

```
§7§oUne capsule Pu-Be = source neutronique scellee.

§7Recette :
§7  4 Pu (Q48) + 4 Be (Phase 2 Q29) + 1 capsule blindee Lead
§7  -> Capsule Pu-Be qui emet ~10E6 neutrons/sec pendant 100 ans

§7Tu vas l'utiliser pour irradier le 6Li -> Tritium (Q50).
§7Et dans le Mycelium Activator (Q51) — flux neutronique sur
§7Mycelium pour activer la magie (jonction Theoreme IV+V).

§7§4§lDANGER§r §7: tiens-la dans un Lead Tank pendant le transport.

§e§lObjectif : §71 Capsule Pu-Be synthese
§e§lRecompense : §7Plans Tritium pur + 2 Bouteilles XP
```

---

## Q50 : "Tritium pur, le carburant des etoiles"

- **ID** 250 | **Pos** (320, 1040) | **Pré** : Q49
- **Type** : retrieval `4x bucket nexusabsolu:tritium`
- **Icon** : tritium bucket
- **Récompense** : 8 Tritium + plans 6LiT
- **Description** :

```
§7§oFin Phase 1 Q14 tu avais pre-stocke 100mB de Tritium.
§7Maintenant tu en stockes 4 buckets (4000mB).

§7Avec ta capsule Pu-Be tu peux maintenant produire industriellement :
§7  6Li + n -> 4He + Tritium + 4.8 MeV

§7Le Tritium pur sert a :
§7  - Compose gamma3 = 6LiT (lithium tritide)
§7  - Fusion D-T des reacteurs (50% des chambres reaction Phase 5)
§7  - Maintien plasma stellaire reaction Phase 6

§e§lObjectif : §74 buckets Tritium pur
§e§lRecompense : §7Plans 6LiT + 2 Bouteilles XP
```

---

## Q51 : "Mycélium activé, premier croisement"

- **ID** 251 | **Pos** (320, 1130) | **Pré** : Q50
- **Type** : retrieval `8x contenttweaker:mycelium_active`
- **Icon** : mycelium_active (custom)
- **Récompense** : 16 mycelium activé + plans Compose gamma
- **Description** :

```
§7§oUne pelle de mycelium normal + 1 capsule Pu-Be 100 ms d'irradiation
§7-> Mycelium Active.

§7C'est de la science-fiction mais Voss avait vu juste : le flux
§7neutronique active la replication ARN champignonique (Botania
§7ne fonctionne pas sur du mycelium normal).

§8§o"L'irradiation neutronique a faible dose ouvre les portes du vivant.
§8Phase 5 vous demandera 16 champignons. Ils ne pousseront que
§8sur Mycelium Active." — E.V.

§7§4§lWARNING§r §7: 8 mycelium = 800ms d'irradiation. Ta capsule en aura.

§e§lObjectif : §78 Mycelium Active
§e§lRecompense : §716 mycelium bonus + plans Compose gamma
```

---

## Q52 : "Composé gamma1 — sodium borate"

- **ID** 252 | **Pos** (240, 1130) | **Pré** : Q51
- **Type** : retrieval `2x contenttweaker:compose_gamma1`
- **Icon** : compose_gamma1
- **Récompense** : 4 gamma1 + plans gamma2
- **Description** :

```
§7§oCompose gamma1 = Borate de sodium dope au lithium.

§7Recette : Na2B4O7 + 2 Li + Heavy Water reaction chamber
§7-> Na2B4O7-Li dope (cristal vert luminescent)

§7Sert a moderer les neutrons rapides du reacteur Phase 4 vers
§7des neutrons thermiques (lents, plus efficaces pour fission Pu).

§e§lObjectif : §72 Compose gamma1
§e§lRecompense : §74 gamma1 + plans gamma2
```

---

## Q53 : "Composé gamma2 — l'iridium activé"

- **ID** 253 | **Pos** (160, 1130) | **Pré** : Q52
- **Type** : retrieval `2x contenttweaker:compose_gamma2`
- **Icon** : compose_gamma2
- **Récompense** : 4 gamma2 + plans gamma3
- **Description** :

```
§7§oIr-191 + n -> Ir-192 + gamma. Demi-vie Ir-192 = 73.8 jours.
§7C'est ta source de rayons gamma pour activer le Compose epsilon
§7(Phase 5 cyclisation).

§7Recette :
§7  1 Ir ingot (Phase 2 Q24) + capsule Pu-Be 5s d'exposition
§7  -> Compose gamma2 (Iridium-192 active)

§7Half-life 73 jours -> tu as 2 mois Minecraft pour finir l'Age 4.
§7Apres ca decline. Pas tres grave si tu te depeches.

§e§lObjectif : §72 Compose gamma2
§e§lRecompense : §74 gamma2 + plans gamma3
```

---

## Q54 : "Composé gamma3 — le 6LiT"

- **ID** 254 | **Pos** (80, 1130) | **Pré** : Q53
- **Type** : retrieval `1x contenttweaker:compose_gamma3`
- **Icon** : compose_gamma3
- **Récompense** : 2 gamma3 + plans Carnet Brisure
- **Description** :

```
§7§oLithium-6 Tritide (6LiT). Le compose gamma de plus haute densite
§7energetique connu.

§7Recette :
§7  6Li (separe par Mekanism IsotopicCentrifuge) + Tritium pur
§7  -> 6LiT (poudre noire, 1.04 g/cm3)

§7§5§l/!\\ ENERGIE LIBEREE §r§7 si compresse a 100 atm = 4.7 MJ/kg.
§7C'est ce qui declenche la cyclisation finale Phase 6 du Solvant
§7epsilon dans la chambre stellaire.

§e§lObjectif : §71 Compose gamma3
§e§lRecompense : §72 gamma3 + Carnet Voss chap.5 (Brisure)
```

---

## Q55 : "Phase 4 complete, le feu nucléaire dompté"

- **ID** 255 | **Pos** (0, 1130) | **Pré** : Q54
- **Type** : checkbox (lit Voss Codex chap.5 Brisure)
- **Icon** : Voss Codex
- **Récompense** : Voss Codex + 16 pain + 4 Bouteilles XP + débloquage Phase 5
- **Description** :

```
§7§oTu as :
§7  - Reacteur fission stable (50M+ RF/t)
§7  - UF6 + Pu enrichis
§7  - Capsule Pu-Be source neutronique
§7  - Tritium pur
§7  - Mycelium Active (jonction Theoreme IV+V)
§7  - Composes gamma1, gamma2, gamma3

§7Tu as franchi le §lTheoreme V (Brisure)§r§7. Le carnet de Voss
§7s'ouvre sur le chapitre 5, le plus dangereux.

§8§o"Tu as transmute la Terre. Tu as transmute les molecules.
§8Tu as transmute les NOYAUX. Maintenant tu vas transmuter
§8le VIVANT. Et Phase 5 va te ramener vers la magie. Au sens
§8technique du terme cette fois." — E.V.

§e§lObjectif : §7Lis Voss Codex chap.5 (Brisure Nucleaire)
§e§lRecompense : §7Phase 5 (Vivant et Etoile) debloquee + 4 Bouteilles XP
```

---

## ⚠️ Items custom Phase 4

À CRÉER :
- ❌ `nexusabsolu:uf6_gas` (fluide existe déjà comme `uf6_gas` ligne 94 ZS) — Q47 ✅
- ❌ `contenttweaker:capsule_pube` (item) — Q49
- ❌ `contenttweaker:mycelium_active` (item) — Q51
- ❌ `contenttweaker:compose_gamma1` (item) — Q52
- ❌ `contenttweaker:compose_gamma2` (item) — Q53
- ❌ `contenttweaker:compose_gamma3` (item) — Q54

Pour `ingotPlutonium` (Q48) : devrait exister via Mekanism ou Big Reactors. Sinon créer alias OreDict.
