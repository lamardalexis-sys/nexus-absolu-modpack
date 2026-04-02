# QUESTS_AGE1_AGE2.md — Design complet des quetes

---

## AGE 1 — La Mecanique Brute (50 quetes : Q47-Q96)
CM 5x5 → 7x7 | Mods : Thermal, IE, EnderIO, AA, Tinkers

---

### PHASE 1 — Installation 5x5 (Q47-Q56) [VALIDE]

Q47 : "Respire" — checkbox
  "L'air est different ici. Tu peux lever les bras sans toucher le plafond."
  Prereq: Q40 (fin Age 0). Reward: furnace

Q48 : "Du sol sous les pieds" — craft 1 crafting table
  Prereq: Q47. Reward: 1 furnace

Q49 : "Le ronronnement" — detect 1 Convertisseur pose
  "Le bruit te manquait, avoue."
  Prereq: Q48. Reward: 4 compose A

Q50 : "Ca chauffe" — craft 1 Stirling Dynamo
  Prereq: Q49. Reward: 16 coal

Q51 : "Les arteres" — craft 4 Leadstone Fluxducts
  Prereq: Q50. Reward: 4 fluxducts

Q52 : "Toujours des murs" — obtenir 16 iron ingots
  "Plus d'espace. Plus de besoins."
  Prereq: Q51. Reward: 8 iron

Q53 : "Le doubleur" — craft 1 Pulverizer
  Prereq: Q52. Reward: 4 copper dust

Q54 : "Le four intelligent" — craft 1 Redstone Furnace
  Prereq: Q52. Reward: 8 redstone

Q55 : "La fusion" — craft 1 Induction Smelter
  Prereq: Q53 + Q54

Q56 : "Le coke" — craft 1 Coke Oven IE
  Prereq: Q52

---

### PHASE 2 — Atelier + Acier + Vossium-II (Q57-Q66) [VALIDE]

Q57 : "L'atelier ressuscite" — craft 1 Atelier du Dr. Voss
  "Des plans griffonnes partout. Voss etait la avant toi."
  Prereq: Q48

Q58 : "Le bois traite" — obtenir 16 Treated Wood
  Prereq: Q56

Q59 : "L'etabli d'ingenieur" — craft 1 Engineer's Workbench
  Prereq: Q58

Q60 : "Le blast furnace" — construire 1 Blast Furnace IE
  "Ca prend de la place. Tant mieux."
  Prereq: Q59

Q61 : "L'acier" — obtenir 8 lingots d'acier
  Prereq: Q60. Reward: 4 acier

Q62 : "Le Compose B" — obtenir 4 Compose B
  Prereq: Q61 + Q52

Q63 : "Le Vossium evolue" — craft Vossium-II au Alloy Kiln
  "Le Vossium chante."
  Prereq: Q62 + Q46 (Vossium Age 0)

Q64 : "Le bloc de Vossium II" — craft 1 Vossium Block II
  Prereq: Q63

Q65 : "Parler aux murs" — craft 1 Connecteur Dimensionnel a l'Atelier
  "Voss avait compris comment parler aux dimensions."
  Prereq: Q57 + Q61

Q66 : "Les tunnels" — craft 1 tunnel items + 1 tunnel energie CM
  Prereq: Q65

---

### PHASE 3 — Vers le Condenseur T2 (Q67-Q73)

Q67 : "L'assistant mecanique" — craft 1 Auto-Scavenger
  "Voss aussi en avait marre de se salir les mains."
  Prereq: Q51 + Q50. Consomme 15 RF/t + pioche inside.

Q68 : "L'upgrade" — craft 1 Hardened Upgrade Thermal
  Prereq: Q55

Q69 : "L'engrenage de Compose B" — craft 1 Compose Gear B
  "Le Vossium-II vibre quand tu l'entoures de Compose B."
  Prereq: Q63

Q70 : "La reconstructrice" — craft 1 Atomic Reconstructor AA
  Prereq: Q51

Q71 : "Les cristaux" — obtenir 4 cristaux AA
  Prereq: Q70

Q72 : "L'armature du Condenseur" — craft les pieces du T2
  Necessite: acier + cristaux AA + Vossium Block II
  Prereq: Q71 + Q61 + Q64

Q73 : "Le Condenseur Tier 2" — construire le multibloc 3x3x3
  "La dimension tremble. Elle sait ce que tu fais."
  Prereq: Q72 + Q69

---

### PHASE 4 — Euphorie 7x7 (Q74-Q80)

Q74 : "La deuxieme fissure" — fusionner 2x CM 5x5 → 1x CM 7x7
  "Plus grand. Toujours plus grand. Mais cette fois tu souris pour de vrai."
  Prereq: Q73

Q75 : "L'air libre" — checkbox
  "Tu pourrais presque oublier que c'est une boite."
  Prereq: Q74

Q76 : "Le magma" — craft 1 Crucible + produire 1 bucket de lave
  Prereq: Q75

Q77 : "Le feu liquide" — craft 1 Magmatic Dynamo
  "Le feu ne s'eteint plus."
  Prereq: Q76 + Q68

Q78 : "La fonderie" — construire 1 Smeltery Tinkers
  Prereq: Q75

Q79 : "L'outil parfait" — craft 1 pickaxe Tinkers tete acier
  Prereq: Q78

Q80 : "L'alliage sombre" — craft 8 Dark Steel
  Prereq: Q55 + Q61

---

### PHASE 5 — Automation (Q81-Q88)

Q81 : "L'armure des ombres" — craft 1 piece Dark Steel Armor
  Prereq: Q80

Q82 : "Les tiroirs" — craft 4 Storage Drawers + 1 Controller
  "L'ordre. Enfin l'ordre."
  Prereq: Q75

Q83 : "Le jardin mecanique" — craft 1 Garden Cloche IE
  "La nature obeit aussi a l'ingenierie."
  Prereq: Q58 + Q77

Q84 : "Le reseau de conduits" — craft 8 Energy + 8 Fluid Conduit EnderIO
  Prereq: Q80

Q85 : "Le transposeur" — craft 1 Fluid Transposer Thermal
  Prereq: Q68

Q86 : "Le premier vol" — craft 1 Jetpack Simply Jetpacks
  "Voler dans une cage. Voss aurait ri."
  Prereq: Q85

Q87 : "L'automation totale" — checkbox (ore processing full auto)
  "Tu croises les bras. La machine respire."
  Prereq: Q84 + Q82

Q88 : "Le million" — checkbox (1M RF stocke)
  "Un million. Voss avait mis 3 ans. Toi, quelques jours."
  Prereq: Q77

---

### PHASE 6 — Montee en puissance (Q89-Q93)

Q89 : "Le broyeur geant" — construire 1 IE Crusher
  Prereq: Q61 + Q75

Q90 : "Le Vibrant Alloy" — obtenir 8 Vibrant Alloy EnderIO
  "Ce metal est vivant. Presque."
  Prereq: Q80

Q91 : "Le Capacitor Bank" — craft 1 Capacitor Bank EnderIO
  Prereq: Q90

Q92 : "Dix millions" — checkbox (10M RF)
  "Le Compose B pulse dans toute la salle."
  Prereq: Q88 + Q91

Q93 : "La pioche specialiste" — craft 1 pioche avancee pour Auto-Scavenger
  "Les murs ont encore des secrets."
  Prereq: Q67 + Q78

---

### PHASE 7 — Le Fragment (Q94-Q96)

Q94 : "Le Fragment Mecanique" — craft 1 Fragment Mecanique
  Necessite: Compose Gear B + Circuit Stabilise + Bobine d'Induction + Vossium-II
  "Il pulse. Tu le sens dans tes os. Le premier fragment existe.
  Voss en revait. Tu l'as fait. Plus que huit."
  Prereq: Q69 + Q92 + Q57

Q95 : "Les notes de Voss" — checkbox
  "Si vous lisez ceci, le premier Fragment est assemble.
  Le prochain necessite quelque chose que je n'ai jamais maitrise.
  La vie organique. Le mana. Le sang.
  Bonne chance.
  — Dr. E. Voss, Carnet n6"
  Prereq: Q94

Q96 : "Fin de l'Age 1" — checkbox
  "La Mecanique Brute est derriere toi. Devant : l'impossible."
  Prereq: Q95

---
---

## AGE 2 — Le Paradoxe Organique (30 quetes : Q97-Q126)
CM 7x7 → 9x9 | Mods : Botania, Astral Sorcery, Blood Magic, Mystical Agriculture

Ton narratif : La technologie seule ne suffit plus. Voss l'avait decouvert.
Le Fragment Organique necessite de la magie. Le joueur decouvre que
la science et la magie ne sont pas opposees — elles sont complementaires.

---

### PHASE 1 — L'eveil magique (Q97-Q103)

Q97 : "Le paradoxe" — checkbox
  "La technologie a des limites. Voss les a trouvees.
  Il a ecrit : 'La matiere obeit aux equations.
  Mais la VIE obeit a autre chose.'
  L'Age du Paradoxe commence."
  Prereq: Q96

Q98 : "La graine impossible" — obtenir 1 Mystical Agriculture seed tier 1
  Le joueur decouvre que des graines poussent du... metal? De l'essence?
  "Ca ne devrait pas exister. Mais ca pousse."
  Prereq: Q97

Q99 : "La fleur vivante" — obtenir 1 fleur Botania (Daybloomer ou Endoflame)
  Premier contact avec le mana. La fleur genere de l'energie a partir de... rien? De la lumiere?
  "Elle respire. La fleur respire."
  Prereq: Q97

Q100 : "Le Mana Pool" — craft 1 Mana Pool Botania
  Le joueur stocke du mana. Un liquide qui n'est pas un liquide.
  "Voss appelait ca 'l'energie qui refuse d'etre mesuree'."
  Prereq: Q99

Q101 : "Les pétales" — craft 1 Petal Apothecary Botania
  La table de craft Botania. Fleurs + eau → nouvelles fleurs.
  Prereq: Q100

Q102 : "L'agriculture impossible" — craft 4 Inferium Essence seeds
  Mystical Agriculture tier 1. Le joueur farm de l'essence qui pousse comme du ble.
  Prereq: Q98 + Q83 (garden cloche pour automatiser)

Q103 : "Le Spreader" — craft 1 Mana Spreader Botania
  Le mana circule. Comme le RF mais... different. Vivant.
  "Le mana ne circule pas dans des cables. Il vole."
  Prereq: Q100

---

### PHASE 2 — Les etoiles (Q104-Q110)

Q104 : "Le ciel dans la boite" — craft 1 Luminous Crafting Table Astral Sorcery
  Le joueur voit des etoiles. Dans une Compact Machine. C'est impossible et pourtant.
  "Le plafond n'est pas un plafond. C'est un ciel."
  Prereq: Q97

Q105 : "Le telescope" — craft 1 Looking Glass Astral Sorcery
  Le joueur observe les constellations depuis sa boite. Surrealiste.
  Prereq: Q104

Q106 : "Le cristal de roche" — obtenir 4 Rock Crystals Astral Sorcery
  Les cristaux amplifient tout. Machines Thermal + cristaux = boost.
  "Voss avait decouvert que les cristaux 'ecoutent' les machines."
  Prereq: Q105

Q107 : "L'infusion stellaire" — craft 1 Starlight Infusion (item infuse)
  Le starlight transforme la matiere. Premier craft Astral.
  Prereq: Q106

Q108 : "Le Compose C" — obtenir 4 Compose C
  Le Compose C necessite du mana ET du starlight. Premier craft inter-magie.
  Prereq: Q103 (mana) + Q107 (starlight)

Q109 : "Le Vossium-III" — craft Vossium-III au Alloy Kiln
  Vossium-II + Compose C. La troisieme couche s'ouvre.
  "Les canaux tertiaires. Le Vossium ne chante plus. Il hurle."
  Prereq: Q108 + Q63 (Vossium-II)

Q110 : "Le bloc de Vossium III" — craft 1 Vossium Block III
  Prereq: Q109

---

### PHASE 3 — Le sang (Q111-Q117)

Q111 : "L'autel" — craft 1 Blood Altar Blood Magic
  Le joueur sacrifie sa vie pour du pouvoir. Litteralement.
  "Le sang a une memoire. Chaque goutte se souvient."
  Prereq: Q97

Q112 : "Le sacrifice" — obtenir 1 Weak Blood Orb
  Premier sacrifice. Le joueur perd des coeurs pour remplir l'orbe.
  "Ca fait mal. Mais ca marche."
  Prereq: Q111

Q113 : "Les runes" — craft 4 Blood Runes
  L'autel monte en puissance. Plus de runes = plus de capacite.
  Prereq: Q112

Q114 : "Le Slate" — obtenir 8 Blank Slate
  Materiau de base Blood Magic. Necessite un autel avec du sang.
  Prereq: Q113

Q115 : "Le rituel" — activer 1 rituel Blood Magic
  Le joueur dessine un rituel au sol. C'est sombre, mystique, puissant.
  "Les equations ne fonctionnent plus ici. Seul le sang repond."
  Prereq: Q114

Q116 : "Le Compose D" — obtenir 4 Compose D
  Necessite du sang (Blood Magic) + mana (Botania) + starlight (Astral).
  Les trois magies fusionnent. Le joueur comprend enfin le plan de Voss.
  "Trois forces. Un seul canal. Voss avait vu juste."
  Prereq: Q115 + Q103 + Q107

Q117 : "Le Vossium-IV" — craft Vossium-IV
  Vossium-III + Compose D. Les canaux dimensionnels stabilisent la magie.
  "Le Vossium accepte la magie comme il acceptait l'energie. Sans juger."
  Prereq: Q116 + Q109

---

### PHASE 4 — La fusion (Q118-Q126)

Q118 : "Le Condenseur Tier 3" — construire le multibloc 4x4x4
  Plus grand. Necessite Vossium Block III. Le joueur est un batisseur maintenant.
  Prereq: Q110 + Q92 (10M RF de l'Age 1 carry over)

Q119 : "La cle de la 9x9" — craft Cle d'Expansion 9x9
  Prereq: Q118

Q120 : "La troisieme fissure" — fusionner vers CM 9x9
  "L'espace se plie sans resister. Il te connait maintenant."
  Prereq: Q119

Q121 : "Le Terrasteel" — craft 1 Terrasteel Ingot Botania
  Le materiau ultime de Botania. Necessite le rituel de la plaque de terre.
  Prereq: Q120 + Q103

Q122 : "Le Mystical Agriculture tier 3" — craft seeds tier 3
  Les essences deviennent serieuses. Diamond essence, emerald essence.
  Prereq: Q102 + Q120

Q123 : "L'Attunement" — s'attuner a une constellation Astral Sorcery
  Le joueur choisit une constellation. Ca change ses pouvoirs.
  Prereq: Q106 + Q120

Q124 : "Le Fragment Organique" — craft 1 Fragment Organique
  Deuxieme des neuf fragments. Mana + sang + starlight + Vossium-IV.
  "Le metal et la magie. La machine et le vivant.
  Le paradoxe n'est pas un probleme. C'est la reponse."
  Prereq: Q117 + Q121 + Q115

Q125 : "Le Fragment Stellaire" — craft 1 Fragment Stellaire
  Troisieme fragment. Astral Sorcery pur + Vossium-IV + cristaux.
  "Les etoiles se souviennent de Voss. Elles se souviennent de tout."
  Prereq: Q123 + Q117

Q126 : "La trinite" — checkbox (posseder les 3 premiers fragments)
  "Mecanique. Organique. Stellaire.
  Trois sur neuf. Le Nexus prend forme.
  Voss a ecrit : 'Quand j'ai tenu le troisieme fragment,
  j'ai compris que l'univers n'est pas fait de matiere.
  Il est fait de connexions.'
  La Cle du Laboratoire est proche.
  — Dr. E. Voss, Carnet n7"
  Prereq: Q124 + Q125

---

## RESUME

| Age | Quetes | Range | CM | Mods principaux |
|-----|--------|-------|----|-----------------|
| 0 | 44 | Q0-Q46 | 3x3→5x5 | Ex Nihilo, Tinkers, Bonsai, Pam's |
| 1 | 50 | Q47-Q96 | 5x5→7x7 | Thermal, IE, EnderIO, AA, Tinkers |
| 2 | 30 | Q97-Q126 | 7x7→9x9 | Botania, Astral Sorcery, Blood Magic, Mystical Ag |
