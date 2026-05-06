# Phase 6 — Polish (Carnet Voss Vol IV + textures + sons ambient)

> Date : Mai 2026
> Statut : ✅ **CORE COMPLET** (extensions futures possibles)

---

## 🎯 Résumé

Polish Age 4 fini en 3 sous-tâches :

| Sous-tâche | Statut | Détail |
|---|---|---|
| **6.A** Carnet Voss Vol IV (Patchouli) | ✅ Complet | 1 catégorie + 8 entries (FR + EN) |
| **6.B** Textures items HD (composés finaux) | ✅ Complet | 11 textures 32×32 générées |
| **6.C** Sons ambient multiblocs | ✅ Bases en place | 5 nouveaux sound events |

---

## 6.A — Carnet Voss Vol IV (Patchouli book `voss_codex`)

Nouvelle catégorie ajoutée : **`vol_iv`** (sortnum 2, après "Le Portail Voss").

### Structure narrative (8 entries)

| Entry | Sortnum | Pages | Sujet |
|---|---|---|---|
| `intro_vol_iv` | 1 | 3 | Préambule, les 5 théorèmes, intro Cartouche |
| `theoreme_1_conservation` | 2 | 3 | Eau pure, L1 Pétrochimie, L2 Hydro |
| `theoreme_2_organique` | 3 | 3 | 16 champignons, MB-ALAMBIC, Essence Chromatique |
| `theoreme_3_stellaire` | 4 | 3 | Liquid Starlight, MB-CYCLISATEUR (conditions nuit) |
| `theoreme_4_sanguine` | 5 | 2 | Tryptamide-M, MB-AROMATIC |
| `theoreme_5_brisure` | 6 | 2 | Mycelium Activé, flux neutronique NC |
| `synthese_finale` | 7 | 4 | M1 Mélangeur, casing, cartouche_chargee, Bio-Réacteur |
| `epilogue` | 8 | 3 | L'injection, les 9 stages, le réveil Age 5 |

### Style narratif

Tonalité **mystérieuse + dark humor + chimie IRL** (cohérence avec `00-vision.md`). Les entries sont écrites comme des notes manuscrites de Voss, avec des moments d'épigraphes en `$(o)italique$(/o)`.

### Fichiers créés

```
mod-source/src/main/resources/assets/nexusabsolu/patchouli_books/voss_codex/
├── fr_fr/
│   ├── categories/vol_iv.json              ← nouveau
│   └── entries/
│       ├── intro_vol_iv.json               ← nouveau
│       ├── theoreme_1_conservation.json    ← nouveau
│       ├── theoreme_2_organique.json       ← nouveau
│       ├── theoreme_3_stellaire.json       ← nouveau
│       ├── theoreme_4_sanguine.json        ← nouveau
│       ├── theoreme_5_brisure.json         ← nouveau
│       ├── synthese_finale.json            ← nouveau
│       └── epilogue.json                   ← nouveau
└── en_us/                                  ← copies des FR (book i18n=true)
    └── (idem)
```

---

## 6.B — Textures items HD (Phase 3+4)

11 textures 32×32 PNG générées via Pillow procédural. Tous les items finaux Phase 3+4 ont maintenant leur visuel propre.

| Item | Style | Couleur |
|---|---|---|
| `compose_alpha` | Fiole liquide | Jaune acide (super-acide H2SO4 + HNO3) |
| `compose_beta` | Fiole liquide | Cyan (organométallique Au) |
| `compose_gamma1` | Fiole liquide | Vert (UF6 enrichi) |
| `compose_gamma2` | Fiole liquide | Violet (Pu-Be Borate) |
| `compose_gamma3` | Fiole liquide | Noir + glow violet (LiT Tritiure) |
| `compose_delta` | Fiole liquide | Pourpre (bio-actif) |
| `tryptamide_m_capsule` | Capsule scellée | Violet dégradé + sceau doré |
| `cristal_manifoldine` | Cristal hexagonal | Pourpre + facettes glow |
| `matrix_pigmentary` | Matrice 4×4 | 16 couleurs Botania |
| `casing_titane_iridium` | Anneau métallique | Argenté + accents cyan |
| `cartouche_chargee` | Variante cartouche | Gradient pourpre→cyan (semi-actif) |

Script générateur : `scripts-tools/generate_phase6_textures.py` (152 lignes, 5 helpers + 11 items).

---

## 6.C — Sons ambient multiblocs

### Sound events ajoutés à `sounds.json`

5 nouveaux sound events utilisant des sons vanilla Minecraft (mappés avec ajustements volume/pitch pour cohérence ambient) :

| Sound event | Source | Usage suggéré |
|---|---|---|
| `machine.multiblock_humming` | `block.beacon.ambient` (vol 0.3, pitch 0.7) | Multibloc actif (générique) |
| `machine.multiblock_complete` | `block.beacon.activate` (vol 0.5, pitch 1.2) | Cycle terminé |
| `machine.haber_pressure` | `block.fire.extinguish` (vol 0.6, pitch 0.6) | MB-HABER pressurisation |
| `machine.cyclisateur_stellaire` | `block.portal.ambient` (vol 0.4, pitch 1.5) | MB-CYCLO résonance stellaire |
| `machine.kroll_argon` | `block.fire.ambient` (vol 0.4, pitch 0.8) | MB-KROLL flush argon |

### Comment les jouer en jeu

Les sound events sont enregistrés et **immédiatement utilisables** via :

1. **CraftTweaker / ZenScript** :
   ```zenscript
   server.commandManager.executeCommand(server, server,
     "/playsound nexusabsolu:machine.multiblock_humming block @a");
   ```

2. **Modular Machinery JSON** : ajouter dans recipes le champ `"sound": "nexusabsolu:machine.haber_pressure"` (custom field si supporté par ce fork de MM).

3. **Java mod (TileEntity)** : `world.playSound(null, x, y, z, ModSounds.MULTIBLOCK_HUMMING, SoundCategory.BLOCKS, 1.0f, 1.0f);` après ajout dans `ModSounds.java`.

### Extensions futures possibles

- Créer `.ogg` natifs Nexus Absolu pour chaque multibloc (sons custom enregistrés)
- Ajouter ITickableSound pour boucler le humming pendant que le multibloc est actif
- Intégrer AmbientSounds avec un `provider` custom qui détecte la proximité d'un multibloc actif

Pour l'instant, les sons vanilla mappés donnent une expérience décente sans effort de création audio.

---

## ✅ Bilan Phase 6

```
✅ 6.A Carnet Voss Vol IV : 8 entries Patchouli FR+EN, lore complet
✅ 6.B Textures items HD : 11 textures 32×32 procédurales
✅ 6.C Sons ambient : 5 sound events ready-to-use

  Polish Age 4 = ÉTAT STABLE pour release alpha
```

Pour **release beta complète**, ajouter (futures sessions) :
- Sons `.ogg` custom pour chaque multibloc (à enregistrer)
- Textures HD 64×64 ou 128×128 pour les items les plus visibles
- Particules custom autour des multiblocs actifs (ParticleManager Java)
- Easter eggs Voss dans les descriptions de quêtes obscures
