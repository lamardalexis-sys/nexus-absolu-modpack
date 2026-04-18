# FURNACES_RECIPES_DESIGN.md
> Recipes des 5 Furnaces Nexus T1-T5, validees par Alexis (session v1.0.180).
> **A implementer au Sprint D** -- PAS encore code.

---

## Principes

- Symetrie du pattern (MODPACK_RECIPES_SKILL.md)
- Center = le tier precedent (chain upgrade) OU un core element
- 2-3 mods impliques par craft (force cross-mod)
- Progression coherente avec l'Age de debloquage

---

## T1 — Iron Furnace (Age 0, CM 3x3)

**Rationale** : Age 0 = pas encore de smeltery Tinkers, le joueur a juste des
`nexusabsolu:iron_grit` via scavenging. Ce craft utilise ce qu'il a.

```
[iron_grit]   [cobblestone]   [iron_grit]
[cobblestone] [vanilla furnace][cobblestone]
[iron_grit]   [wall_dust]     [iron_grit]
```

Mods : **Minecraft + Nexus**

---

## T2 — Gold Furnace (Age 1, CM 5x5+)

```
[gold_ingot]           [gold_ingot]         [gold_ingot]
[thermal:machine_frame][FURNACE_IRON]       [thermal:machine_frame]
[gold_ingot]           [gold_ingot]         [gold_ingot]
```

Mods : **Thermal + Nexus**

---

## T3 — Invar Furnace (Age 1, parallele au Gold)

Voie "tech" = alternative au Gold. Meme +40% vitesse, cout different.

```
[invar_ingot]                [invar_ingot]   [invar_ingot]
[ie:heavy_engineering]       [FURNACE_IRON]  [ie:heavy_engineering]
[invar_ingot]                [coal_block]    [invar_ingot]
```

Mods : **Immersive Engineering + Nexus**

---

## T4 — Emeradic Crystal Furnace (Age 1 tard)

```
[emeradic_crystal]              [emeradic_crystal]   [emeradic_crystal]
[enderio:framed_machine_chassis][FURNACE_INVAR ou _GOLD][enderio:framed_machine_chassis]
[emeradic_crystal]              [redstone_block]     [emeradic_crystal]
```

Mods : **Environmental Tech + EnderIO + Nexus**

**A verifier au moment du code** :
- Item ID exact Emeradic Crystal (ET a plusieurs crystals : Litherite, Erodium,
  Kyronite, Pladium, Ionite, Aethium, Emeradic). Probablement
  `environmentaltech:emeradic_crystal` mais confirmer via JEI dump.
- `enderio:framed_machine_chassis` = nom confirme dans EnderIO 5.3.72.

---

## T5 — Vossium IV Furnace (Age 2)

Saut de puissance +100% -> +200%. Force les 3 magic mods Age 2
(Astral + Draconic + Blood Magic). Tres en ligne avec philosophie
"Nexus Absolu = convergence de tous les mods".

```
[vossium_iv_ingot]              [astralsorcery:attunedcrystal]    [vossium_iv_ingot]
[draconicevolution:wyvern_core] [FURNACE_EMERADIC]                [draconicevolution:wyvern_core]
[vossium_iv_ingot]              [bloodmagic:sanguine_core]        [vossium_iv_ingot]
```

Mods : **Nexus + Astral + Draconic + Blood Magic**

**A verifier au moment du code** :
- `astralsorcery:itemattunedrockcrystal` vs autre nom exact
- `draconicevolution:wyvern_core` dispo Age 2 ? Sinon downgrade
  a `draconicevolution:draconic_core` (fallback) ou item plus bas
- `bloodmagic:sanguine_core` : existe peut-etre pas sous ce nom
  dans BM 2.4.3. Fallback : `bloodmagic:slate:5` (Ethereal Slate)
  ou `bloodmagic:activation_crystal:2` (Demonic Will Crystal)

---

## Checklist a cocher quand Sprint D demarre

- [ ] Dump JEI pour verifier tous les item IDs exacts
- [ ] Creer `scripts/Furnaces_Recipes.zs`
- [ ] 5 recipes `recipes.addShaped()`
- [ ] Ajouter remove des furnaces Mystical Agriculture
  (`mysticalagriculture:*_furnace`)
- [ ] Verifier que remove Mystical Agr furnaces n'impacte pas
  une quete existante qui pointe dessus
- [ ] Test JEI in-game : chaque recipe doit apparaitre
- [ ] Test craft in-game : chaque furnace doit etre craftable
  dans l'Age correspondant (ItemStages si gating necessaire)

---

## Notes de design

- **Tier chain forcing** : T2/T3/T4 utilisent le tier precedent au centre.
  Force le joueur a crafter dans l'ordre (pas skip direct au T4).
- **T5 NE reprend PAS FURNACE_EMERADIC au centre** dans le proposal
  actuel mais peut-etre devrait -- a rediscuter avec Alexis.
  Si on le met au centre : force chain complete. Si pas au centre :
  permet jump direct si joueur a toutes les ressources.
- **Philosophie "remake"** : on pourrait envisager que chaque furnace
  t1 -> t+1 **consume le tier precedent sans le rendre**
  (via `recipes.addShaped` qui ne retourne pas l'ancien). C'est le
  pattern E2E. A decider.
