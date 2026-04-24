# Archives Voss — Synthèse de recherche (pré-Sprint 1)

**Date** : 2026-04-24 (v1.0.300+)
**Objectif** : Capturer toutes les ressources et bonnes pratiques identifiées avant de démarrer le code.

---

## 1. Ressources étudiées

### Skills / Frameworks méthodologiques

**`obra/superpowers-skills`** (clone `/home/claude/research/superpowers-skills/`) — Community-editable skills. 8 catégories :
- `collaboration/writing-plans` — Plans détaillés tâches 2-5 min, TDD enforced
- `collaboration/executing-plans` — Stratégie 2-phase code review
- `collaboration/brainstorming` — Before planning, design discussion
- `collaboration/subagent-driven-development` — Parallel agents isolated git worktrees
- `debugging/systematic-debugging` — Iron Law: "NO FIXES WITHOUT ROOT CAUSE INVESTIGATION FIRST"
- `debugging/root-cause-tracing` — Trace backward through call stack
- `debugging/defense-in-depth` — Fix at source + add defensive checks
- `debugging/verification-before-completion` — Verify before marking done
- `problem-solving/when-stuck` — Dispatch vers la bonne technique selon stuck-type
- `problem-solving/simplification-cascades` — Quand complexity spirale
- `problem-solving/inversion-exercise` — Quand bloqué par hypothèses
- `testing/test-driven-development` — TDD strict

### Références code Minecraft 1.12.2

**`refinedmods/refinedstorage` (v1.x, 1.12 branch)** — clone `/home/claude/rs-src/refinedstorage/`
- 641 fichiers Java, 55 164 lignes
- Architecture 4 concepts : INetwork / INetworkNode / IStorage / IGrid
- Packages clés : `api/network/`, `api/storage/disk/`, `apiimpl/`

**`tth05/morerefinedstorage`** — clone `/home/claude/research/morerefinedstorage/`
- **Fork RS 1.12.2 focus performance** ← BIEN PLUS pertinent pour nous
- 1163 fichiers, optimisations et fixes accumulés après années de feedback
- Même version Forge que nous (1.12.2) = code directement applicable

**`mekanism/Mekanism` (1.12 branch)** — clone `/home/claude/mek-12/`
- Pattern Capability propriétaire (LOGISTICAL_TRANSPORTER_CAPABILITY)
- Référence pour gérer cas edge (déjà utilisé dans v1.0.300 MekanismIntegration)

### Documentation Forge

**`docs.minecraftforge.net`** + **`forge.gemwire.uk/wiki/Capabilities`**
- Capabilities : ForgeCapabilities#ITEM_HANDLER
- AttachCapabilitiesEvent pour objets "foreign"
- ICapabilityProvider pour TileEntities custom
- `ItemStackHandler` = default impl de IItemHandler

**`williewillus` Capabilities Primer** (gist github)
- `ItemHandlerHelper` = helper pour insertion/extraction safe
- `SlotItemHandler` = Slot basé sur IItemHandler pour Containers
- **Important** : mauvais guard insertItem/extractItem casse SlotItemHandler

---

## 2. Pièges (pitfalls) identifiés à éviter

### Multiblock / TileEntity

1. **shouldRefresh()** : Par défaut, block state change = TileEntity replaced.
   → Override pour ne pas perdre l'état NBT du Controller multiblock.
2. **ITickable sur N blocs** : 8 blocs du multiblock ticking = 8 TE qui roulent.
   → **Seul le Controller doit être ITickable**, les autres (frame, thermal_core) juste storage d'état.
3. **Performance RS** : network lag à 89% du serveur thread quand Network Receiver dans chunk unloaded (issue #3468).
   → Toujours checker `world.isChunkLoaded()` avant scan réseau.
4. **onLoad / onChunkUnload / invalidate** pour capability :
   → Invalider les caches au chunk unload pour éviter crash.

### Capabilities

5. **hasCapability fallback to super** : Toujours `|| super.hasCapability(...)`, sinon on casse IItemHandler inherit.
6. **IItemHandler non exposé** par certains mods proprietary (Mekanism Transporter) → besoin de reflection fallback.
7. **Storage Drawers** : expose IItemHandler mais certains blocs (drawer trim, compacting drawers) ont des quirks sur `insertItem` avec NBT.

### NBT / Save

8. **NBT keys collisions** : en 1.12.2 pas d'auto-validation, utiliser prefix mod ID (`voss_archive_...`).
9. **Migration NBT** : quand on change la structure, fallback read old keys pour backwards compat (comme on a fait pour cookProgress int→float v1.0.290).

### GUI / Client sync

10. **detectAndSendChanges spam** : si on re-send tout chaque tick = lag 1000 TE actifs.
    → ContainerListener + delta sync (ne sync que si valeur changée, pattern RS).
11. **Client-side optimistic UI** : toggle côté client pour feedback immédiat + packet serveur. Pattern déjà utilisé dans nos fours (face config clicks).

### Dimensions / Compact Machines

12. **TileEntity.getWorld()** retourne le monde du CM, pas l'Overworld.
    → Pour cross-dim link, stocker `dimensionId` + `BlockPos` en NBT, récupérer world via DimensionManager.getWorld(dimId).
13. **Chunks non-loaded dans CM** : la CM peut être "vide" côté client.
    → Préférer NBT stocké cross-dim plutôt que live TE access.

---

## 3. Best Practices adoptées pour Archives Voss

### Architecture

**Séparation en 3 couches** (inspirée RS) :
```
api/              interfaces pures (INexusNetwork, INexusNetworkNode, etc.)
apiimpl/          implémentations (NexusNetwork, etc.)
blocks/           BlockXxx + TileEntityXxx
```

**Master-Slave pour multiblock** :
- 1 seul Controller avec logique + ITickable
- 7 autres blocs = "dumb blocks" qui ne font que signaler au Controller
- Controller fait le scan via BFS au placement/destruction

**Scan incrémental** plutôt que re-scan complet :
- Bloc ajouté : Controller ajoute juste ce bloc + ses voisins non encore dans le réseau
- Bloc retiré : invalidate la branche concernée seulement
- Évite le bug "89% server tick time" reporté dans RS issue #3468

### TDD adopté pour Archives Voss

Pour les composants critiques (réseau, sérialisation NBT, autocraft) :
1. Écrire test unitaire qui échoue
2. Implémenter minimum pour passer
3. Refactor
4. Commit

Pour les blocs GUI / rendering : tests manuels in-game (Minecraft ne supporte pas facilement l'headless).

### Code review 2-phase (Superpowers)

À chaque fin de Sprint :
1. **Phase 1** : Spec compliance review (est-ce que ça match le design doc ?)
2. **Phase 2** : Code quality review (brace balance, patterns, perf, DRY, YAGNI)

Ensuite seulement commit + push.

### Bite-sized tasks (Superpowers Writing Plans)

Chaque task = 2-5 minutes d'action :
- "Write failing test" - 1 step
- "Run it to verify it fails" - 1 step
- "Implement minimal code" - 1 step
- "Run test to verify pass" - 1 step
- "Commit" - 1 step

---

## 4. Mods du pack à cibler pour intégration

Déjà identifié dans `minecraftinstance.json` (202 mods) :

**Compatibles IItemHandler standard** (marcheront direct) :
- Hoppers vanilla
- Storage Drawers (déjà testé par Alexis)
- Ender Storage
- Iron Chests
- QuantumStorage
- Thermal Dynamics Itemducts
- EnderIO Item Conduits
- Industrial Foregoing Black Hole Unit

**Nécessitent intégration reflection** (comme v1.0.300) :
- Mekanism Logistical Transporter (✅ déjà fait)
- XNet Connectors (à tester avant de supposer)

**Pour auto-craft Interface** :
- Fourneaux Voss (notre code) — priorité 1
- Mekanism Factories (Enrichment, Purification) — priorité 2
- Thermal Expansion machines — priorité 2
- Immersive Engineering Blast Furnace — priorité 3
- NuclearCraft processors — priorité 3

---

## 5. Risques identifiés

| Risque | Probabilité | Impact | Mitigation |
|---|---|---|---|
| Réseau lag à grande échelle (>200 blocs) | Élevée | Fort | Scan incrémental + unload safety |
| NBT bloat (disques T4 avec 62k items × 255 types) | Moyenne | Moyen | Compression via bitmap indexing (ItemId → uint16) |
| Crash cross-dim link quand CM unloaded | Élevée | Fort | NBT-only access pour linked networks |
| GUI lag à 10k+ items uniques | Moyenne | Moyen | Delta packets + client-side cache |
| Autocraft loop infinite (pattern A utilise A) | Moyenne | Fort | Detection cycle dans pattern validator |
| Conflict item IDs avec autres mods | Faible | Faible | Prefix `voss_` rigoureux |

---

## 6. Plan révisé pour Archives Voss (post-research)

Tout reste dans `NEXUS_STORAGE_SYSTEM_DESIGN.md` v2, mais on ajoute au **Sprint 1** :

**Sprint 0 (nouveau, avant Sprint 1)** — Setup & Infrastructure (~150 lignes)
- Créer branch `feature/archives-voss`
- Créer package `com.nexusabsolu.mod.archives.*`
- Écrire `INexusNetworkNode` interface minimale
- Setup TDD : framework JUnit 4 (déjà dans mod-source) + 1 test passant
- Commit initial pour vérifier que la branche build

**Sprint 1** (inchangé) — Multiblock & Fluides custom
**Sprint 2** (inchangé) — Network + Câbles + IO filtres
**Sprint 3** (inchangé) — Disques + Drive
**Sprint 4** (inchangé) — Terminal Stockage RS-style
**Sprint 5** (inchangé) — Autocraft + Interface Machines
**Sprint 6** (inchangé) — Télécommande + Inter-CM
**Sprint 7** (inchangé) — Polish

Total avec Sprint 0 : ~5450 lignes (vs 5300 sans Sprint 0).

---

## 7. Conclusion

Je suis maintenant **significativement mieux armé** pour Archives Voss :
- Études sources : RS 1.x + morerefinedstorage + Mekanism 1.12
- Patterns Forge 1.12.2 connus et pièges identifiés
- Méthodo Superpowers actualisée (TDD + bite-sized + code review 2-phase)
- Interdépendances pack identifiées (202 mods)
- Risques cartographiés avec mitigations

**Prochaine étape** : Alexis dit "GO Sprint 0" et on commence.
