# Age 4 — Phase 1 détaillée (Q-INTRO + Q1-Q15)

> Premier batch pour validation avant injection BQ.
> Tag : #age4 #betterquesting #phase1
> Voir : `03-progression-quetes.md` pour le plan global

---

## 📐 Conventions

- **questID** : 158 (Q-INTRO) → 173 (Q15). Range Age 4 réservée 158-237.
- **Layout** : x croit horizontalement (80px par quête), y par phase (90px par rangée).
- **Icon** : item de récompense ou item central de la quête.
- **Pattern desc** : intro narrative → corps technique avec humour → citation Voss → Objectif → Récompense.
- **Récompense systématique** : item-clé pour suite + 1 bouteille XP + parfois pain.

---

## Q-INTRO : "Tu te crois libre ?"

- **ID** 158 | **Pos** (0, 0) | **Pré-requis** : Q-fin-Age-3 (ou Q-fin-Age-2 si on saute Age 3)
- **Type** : checkbox (lit le carnet, valide manuellement)
- **Icon** : `nexusabsolu:voss_codex` ou `patchouli:guide_book` taggué
- **Récompense** : Carnet Voss Vol IV (Patchouli) + 4 pain + 1 bouteille XP
- **Description** :

```
§7§oTu sors de ta base. Tu regardes le ciel. Tu te dis "j'ai fait un truc bien".§r

§7Sauf qu'un livre vient d'apparaitre sur ta table de craft.
§7Tu ne l'as pas pose la.
§7Personne d'autre n'est la.

§8§o"Sujet 47.
§8Tu as franchi trois paliers. Tu es sorti des Compact Machines
§8a la fin de l'Age 1. Tu as explore l'overworld pendant
§8les Ages 2 et 3. Tu te crois libre.

§8Tu te trompes.

§8Cette ligne d'horizon que tu vois — c'est un mur."§r
§8— E.V., Carnet Vol. IV — Preface

§e§lObjectif : §7Lis le carnet. Comprends que ce n'est pas fini.
§e§lRecompense : §7La liberte n'est pas pour aujourd'hui.
```

---

## Q1 : "L'eau qui ment"

- **ID** 159 | **Pos** (0, 80) | **Pré** : Q-INTRO (158)
- **Type** : retrieval `1x distilled_water (1000mB Mekanism)`
- **Icon** : bucket Distilled Water
- **Récompense** : 4 buckets vides + 1 Resin Block (custom) en avance + plans Filtre Ionique (note papier)
- **Description** :

```
§7§oVoss commence par l'eau. Evidemment.§r

§7Tu pensais qu'un seau d'eau de riviere c'etait de l'eau ?
§7§lFaux.§r §7C'est de l'eau qui ment.
§7Elle contient 2.4 millions de bacteries, 14 ions differents,
§7du calcaire, du fluor, et probablement les souvenirs
§7d'au moins trois grenouilles decedees.

§7Pour la Cartouche tu vas avoir besoin d'§lEau Tridistillee§r§7.
§7Tu vas la faire en 3 etapes.

§8§o"L'eau pure n'existe pas dans la nature, Sujet 47.
§8L'eau pure est une obsession humaine. Adopte-la." — E.V.

§e§lObjectif : §7Distille ton premier 1000mB d'eau (Thermal Evap Plant)
§e§lRecompense : §7Plans du Filtre Ionique
```

---

## Q2 : "Le filtre qui sépare le bon grain de l'ivraie ionique"

- **ID** 160 | **Pos** (80, 80) | **Pré** : Q1
- **Type** : crafting `1x nexusabsolu:resine_echangeuse_block` (custom)
- **Icon** : resine_echangeuse_block
- **Récompense** : 4 Resine Block + plans Osmose Inverse
- **Description** :

```
§7§oCe que ton seau d'eau distillee a encore en lui c'est des §lions§r§7.
§7Cations. Anions. Tu sais. Les particules chargees qui font
§7que ton serum exploserait au lieu de te transformer.

§7Ce qu'il te faut c'est une §lresine echangeuse d'ions§r§7.
§7Silice + acide sulfonique. Tu vas devoir synthetiser ca.
§7Et oui ca veut dire que tu as deja besoin d'acide sulfurique
§7avant meme d'avoir commence l'usine. Bienvenue dans la chimie.

§8§o"Tout depend de tout. C'est l'essence du Theoreme I." — E.V.

§e§lObjectif : §7Crafte 1 bloc de Resine Echangeuse
§e§lRecompense : §7Plans de l'Osmose Inverse
```

---

## Q3 : "L'eau au cube"

- **ID** 161 | **Pos** (160, 80) | **Pré** : Q2
- **Type** : retrieval `4000mB nexusabsolu:tridistilled_water`
- **Icon** : bucket Tridistilled Water (nouveau fluide custom)
- **Récompense** : Schema Cryo-Distillateur + 8 buckets Tridist
- **Description** :

```
§7§oTroisieme passage. Membrane silicium dope.
§7Pression osmotique inverse. T'es en mode labo blanc maintenant.

§7Cette eau-la elle ment plus.
§7Elle est si pure qu'elle conduit MOINS l'electricite que l'air.
§7Si tu en bois t'as litteralement aucun sel dans le corps.
§7Donc bois pas. T'as pas signe pour mourir d'hyponatremie aujourd'hui.

§e§lObjectif : §7Produis 4000mB d'Eau Tridistillee
§e§lRecompense : §7Schema du Cryo-Distillateur Atmospherique
```

---

## Q4 : "L'air aussi est sale"

- **ID** 162 | **Pos** (240, 80) | **Pré** : Q3
- **Type** : crafting `1x nexusabsolu:cryo_distillateur_controller` (multibloc)
- **Icon** : cryo_distillateur_controller
- **Récompense** : 1 bucket Argon (premier coup gratuit) + 4 Cryotheum bucket
- **Description** :

```
§7§oL'air que tu respires c'est :
§7  - 78% azote (qui sert a rien pour respirer mais joli)
§7  - 21% oxygene (le truc utile)
§7  - 0.93% argon (le gaz noble dont t'auras besoin)
§7  - 0.04% CO2 (le truc qui rechauffe la planete)
§7  - 0.03% autres conneries

§7Pour la Cartouche il te faut isoler l'§lArgon§r§7.
§7Spoiler : tu peux pas le crafter. Tu peux que le §lcondenser a -186 deg§r§7.
§7Et tu vas avoir besoin de §lGelid Cryotheum§r§7.
§7Beaucoup de Gelid Cryotheum.

§8§o"L'argon est le mensonge le plus discret de l'atmosphere.
§8Personne ne le voit. Mais sans lui ton serum oxyde et meurt." — E.V.

§e§lObjectif : §7Construis le Cryo-Distillateur Atmospherique (3x6x3)
§e§lRecompense : §7Bucket d'Argon (premier coup gratuit)
```

---

## Q5 : "Le froid intelligent"

- **ID** 163 | **Pos** (320, 80) | **Pré** : Q4
- **Type** : retrieval `32x cryotheum bucket`
- **Icon** : Gelid Cryotheum bucket
- **Récompense** : Resonant Tank (Thermal) + 8 cryotheum bucket
- **Description** :

```
§7§oFais des stocks de Cryotheum. Serieux.
§7T'en as besoin pour le Cryo-Distillateur. T'en auras besoin
§7pour le Melangeur Cryogenique. T'en auras besoin pour le
§7Bio-Reacteur Manifold final.

§7Recette : Blizz Powder + Snowball + Redstone + Niter
§7Tu connais. Tu connais.

§7Si tu connais pas c'est que t'as seche les ages 2 et 3.
§7Honte a toi. Ouvre le JEI.

§e§lObjectif : §7Stocke 32 buckets de Gelid Cryotheum
§e§lRecompense : §7Resonant Tank pour les stocker
```

---

## Q6 : "Le ciel s'est ouvert"

- **ID** 164 | **Pos** (320, 170) | **Pré** : Q5
- **Type** : retrieval `8x bucket nexusabsolu:argon`
- **Icon** : argon bucket (custom)
- **Récompense** : 4 Resonant Glass + plans encartouchage Argon
- **Description** :

```
§7§oArgon. 0.93% de l'atmosphere. Le moins glorieux des gaz nobles.

§7Sauf que c'est le seul gaz inerte que tu peux faire
§7sans casquer 200 buckets de Cryotheum par mB.
§7L'helium serait mieux mais bon courage pour en condenser
§7a -269 deg dans Minecraft.

§7Stocke 8 buckets. Tu vas en cramer un en encartouchage final.
§7Le reste te servira pour le Kroll TiCl4 (Phase 2).

§e§lObjectif : §7Stocke 8 buckets d'Argon
§e§lRecompense : §74 Resonant Glass + plans encartouchage
```

---

## Q7 : "L'azote pour le pivot"

- **ID** 165 | **Pos** (240, 170) | **Pré** : Q6
- **Type** : retrieval `10x bucket nexusabsolu:n2`
- **Icon** : n2 bucket
- **Récompense** : Pump Mekanism + 16 buckets vides
- **Description** :

```
§7§oN2. 78% de ton atmosphere. C'est ce qui prend le plus de place
§7dans tes poumons sans servir a respirer.

§7Tu vas l'utiliser pour le §lprocede Haber-Bosch§r§7 (Phase 3) :
§7  N2 + 3 H2 -> 2 NH3 (ammoniaque)
§7Et l'ammoniaque c'est la base de toute la chimie organique azotee.

§7Stocke 10 buckets. Phase 3 va te demander 6.

§e§lObjectif : §7Stocke 10 buckets de N2
§e§lRecompense : §7Mekanism Pump (utile pour la suite)
```

---

## Q8 : "L'oxygène pour brûler"

- **ID** 166 | **Pos** (160, 170) | **Pré** : Q7
- **Type** : retrieval `10x bucket nexusabsolu:o2`
- **Icon** : o2 bucket
- **Récompense** : Resonant Tank + 8 buckets vides
- **Description** :

```
§7§oO2. Le truc qui te tient en vie. Et qui fait bruler tout ce
§7que tu mets dedans.

§7Pour la Cartouche tu vas l'utiliser pour :
§7  - Bessemer (Phase 2 fer pur)
§7  - Claus (Phase 3 soufre)
§7  - Ostwald (Phase 3 acide nitrique)

§7Stocke 10 buckets. C'est la base.

§e§lObjectif : §7Stocke 10 buckets d'O2
§e§lRecompense : §7Resonant Tank pour stockage
```

---

## Q9 : "Et la lumière fut"

- **ID** 167 | **Pos** (80, 170) | **Pré** : Q8
- **Type** : retrieval `1x energy_meter (Mekanism Cardboard Box ou energy reader)` ou checkbox manuel
- **Icon** : Mekanism Bin or RF battery
- **Récompense** : 8 Energy Conduit Resonant + Carnet Voss Vol IV ch.1
- **Description** :

```
§7§o5M RF/t. Voila ton premier jalon energie.

§7Tu vas pas y arriver avec des fours a charbon. Faut soit :
§7  - 2 Big Reactors taille moyenne
§7  - 1 Mekanism Fission Reactor T1
§7  - 4 EnderIO Capacitor Banks Resonant + 16 generateurs Industrial Foregoing

§7Spoiler : Phase 4 nucleaire va te demander 50M RF/t en pic.
§7Donc 5M c'est juste l'echauffement.

§8§o"L'energie est la signature des Createurs.
§8Plus tu en consommes plus tu te rapproches d'eux." — E.V.

§e§lObjectif : §7Atteins 5M RF/t de production stable
§e§lRecompense : §78 Energy Conduit Resonant + Carnet Voss Vol IV ch.1
```

---

## Q10 : "L'usine sait pomper"

- **ID** 168 | **Pos** (0, 170) | **Pré** : Q9
- **Type** : crafting `1x mekanism:electric_pump`
- **Icon** : Mekanism Electric Pump
- **Récompense** : 4 Mekanism Pumps + 8 Mekanism Tubes (gas)
- **Description** :

```
§7§oTu vas pas continuer a remplir des seaux a la main.
§7Tu te crois encore en age 1 ?

§7§lMekanism Electric Pump§r§7. Source d'eau infinie tant que t'as
§7une source d'eau dessous. Roxe pour quand tu auras besoin
§7de 30 buckets/seconde pour la pretrochimie (Phase 3).

§e§lObjectif : §7Crafte ta premiere Mekanism Electric Pump
§e§lRecompense : §74 Pumps supplementaires + 8 Gas Tubes
```

---

## Q11 : "Tu te crois encore en âge 1"

- **ID** 169 | **Pos** (0, 260) | **Pré** : Q10
- **Type** : crafting `1x appliedenergistics2:controller`
- **Icon** : AE2 Controller
- **Récompense** : 16 ME Cable Smart + 4 Storage Cell 16k + Crafting CPU plans
- **Description** :

```
§7§oArrete avec tes coffres en bois. Serieusement.

§7Pour l'Age 4 il te faut un §lAE2 ME System§r§7. C'est non-negociable.
§7Tu vas avoir 50 fluides differents, 80 items custom, 16 champis,
§7et il faudra qu'AE2 trie tout ca pendant que tu reflechis a la chimie.

§7Si tu galeres avec AE2 c'est normal. Ouvre le JEI ou Patchouli AE2.
§7Le ME Controller + 8 Storage Drive 16k + 1 Pattern Provider
§7suffira pour l'Age 4 entier.

§e§lObjectif : §7Crafte un ME Controller AE2
§e§lRecompense : §7Storage 16k + Crafting CPU plans
```

---

## Q12 : "L'OpenComputer t'aidera"

- **ID** 170 | **Pos** (80, 260) | **Pré** : Q11
- **Type** : crafting `1x opencomputers:case3` ou `computercraft:computer_advanced`
- **Icon** : OC Case T3 / CC Computer
- **Récompense** : OC GPU T3 + Screen T3 + 4 RAM Tier 3 ou CC Disk Drive + 4 floppies
- **Description** :

```
§7§oQuand tu auras 8 lignes industrielles qui tournent en parallele
§7tu vas vouloir un §lecran de monitoring§r§7.

§7Tu peux ecrire un script Lua qui affiche pour chaque ligne :
§7  - Niveau des reservoirs
§7  - Production / minute
§7  - Alertes si < seuil
§7  - Nombre d'items synthetises

§7Si tu connais pas Lua c'est le moment d'apprendre.
§7Sinon tu prends CC:Tweaked qui est plus simple.

§e§lObjectif : §7Crafte un PC OC T3 ou CC Computer Advanced
§e§lRecompense : §7Hardware T3 + plans monitoring
```

---

## Q13 : "L'eau lourde rampe"

- **ID** 171 | **Pos** (160, 260) | **Pré** : Q12
- **Type** : retrieval `1x bucket mekanism:heavywater` (1000mB)
- **Icon** : Mekanism Heavy Water bucket
- **Récompense** : 4 Heavy Water + 1 Plate Lead + plans Tritium
- **Description** :

```
§7§oD2O. L'eau lourde. 0.015% de l'eau ordinaire.

§7Mekanism te permet d'en extraire avec :
§7  Electrolytic Separator + 1000mB H2O -> 200mB Heavy Water + 800mB Sodium

§7Tu en auras besoin pour :
§7  - Moderateur du reacteur Phase 4
§7  - Cyclisation Manifoldine Phase 5

§e§lObjectif : §7Stocke 1000mB d'Eau Lourde
§e§lRecompense : §74 Heavy Water + plans Tritium
```

---

## Q14 : "Le tritium et la stratégie"

- **ID** 172 | **Pos** (240, 260) | **Pré** : Q13
- **Type** : retrieval `100mB nexusabsolu:tritium`
- **Icon** : tritium bucket (custom)
- **Récompense** : 8 Lithium dust + Bouteille XP x2
- **Description** :

```
§7§oTritium. T2. Hydrogene avec 2 neutrons en plus.
§7Tu vas en avoir besoin pour le compose gamma3 (6LiT) Phase 4.

§7Recette : 6Li + flux neutronique -> 4He + Tritium + 4.8 MeV

§7Donc t'as besoin du reacteur de Phase 4 pour le faire.
§7§lMais§r §7tu peux deja produire 100mB en pre-stock avec
§7une Mekanism Fusion Reactor Mini (D-T fusion side product).

§7Le but de cette quete = preparer Phase 4 a l'avance.

§e§lObjectif : §7Stocke 100mB de Tritium
§e§lRecompense : §78 Lithium dust + 2 Bouteilles XP
```

---

## Q15 : "Tu peux le faire"

- **ID** 173 | **Pos** (320, 260) | **Pré** : Q14
- **Type** : checkbox (lit le carnet)
- **Icon** : Carnet Voss Vol IV (Patchouli)
- **Récompense** : Carnet Voss Vol IV chap.1 (signed book) + 8 pain + 4 Bouteilles XP + débloquage Phase 2
- **Description** :

```
§7§oFin de Phase 1. Tu as :
§7  - Eau Tridistillee a la pelle
§7  - Argon, N2, O2 stockes
§7  - 5M RF/t stable
§7  - AE2 + OC qui ronronnent
§7  - Pre-stock D2O et Tritium

§7Ouvre le Carnet Voss Vol IV chapitre 1.
§7Tu vas y trouver le plan complet de la Phase 2 — les §lmetaux§r§7.

§8§o"Tu as construit les fondations. Maintenant tu vas chercher
§8les materiaux. Trente elements. Aucun substitut accepte." — E.V.

§e§lObjectif : §7Lis le Carnet Voss Vol IV — Chapitre 1
§e§lRecompense : §7Carnet chap.1 + Phase 2 debloquee + 4 Bouteilles XP
```

---

## 📊 Layout Phase 1

```
              Q-INTRO (158)
                  │
                  ▼
   Q1 ─── Q2 ─── Q3 ─── Q4 ─── Q5
  (159)  (160)  (161)  (162)  (163)
                                │
   Q10 ── Q9 ── Q8 ── Q7 ── Q6
  (168)  (167) (166) (165) (164)
   │
   ▼
   Q11 ── Q12 ── Q13 ── Q14 ── Q15
  (169)  (170)  (171)  (172)  (173) ──► Phase 2 unlock
```

Serpentin classique BetterQuesting comme Q139→Q144 fin Age 1.

---

## ⚠️ Items custom requis (script ZS Age4_Manifold_Content.zs)

Pour que ces 16 quêtes marchent, il faut ces items/fluides custom registered (certains existent déjà dans le ZS) :

- ✅ `nexusabsolu:tridistilled_water` (fluide) — Q1, Q3
- ❌ `nexusabsolu:resine_echangeuse_block` — Q2 — **TODO**
- ❌ `nexusabsolu:cryo_distillateur_controller` — Q4 — **TODO**
- ✅ `nexusabsolu:argon` (fluide) — Q4, Q6
- ✅ `nexusabsolu:n2` (fluide) — Q7
- ✅ `nexusabsolu:o2` (fluide) — Q8
- ❌ `nexusabsolu:tritium` (fluide) — Q14 — **TODO**
- ❌ `nexusabsolu:voss_codex` (Patchouli book) — Q-INTRO, Q15 — **TODO**

Les **TODO** seront créés dans `Age4_Manifold_Content.zs` et `Age4_Items.zs` en parallèle.
