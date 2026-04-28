// =============================================================================
// AGE 4 — CARTOUCHE MANIFOLD — Effets d'injection
// =============================================================================
// Ce script gère ce qui se passe quand le joueur right-click la Cartouche.
//
// IMPORTANT — KubeJS 1.12.2 (1.1.0.65 dans Nexus Absolu) est une version EARLY.
// La syntaxe est différente de KubeJS moderne. Certains events utilisés ici
// peuvent ne pas exister tels quels — à valider en jeu et adapter si besoin.
//
// ALTERNATIVE : si KubeJS 1.12.2 ne supporte pas tout, déplacer les effets
// les plus complexes (Bullet Time, Hard Reset) vers le mod source Java
// dans mod-source/src/main/java/com/nexusabsolu/mod/items/ItemCartoucheManifold.java
//
// Référence design : docs/age4-cartouche-manifold/00-vision.md § "Identité visuelle"
//                    docs/age4-cartouche-manifold/01-lore-integration.md § "Théorème IV"
// =============================================================================

// CONFIGURATION
// -----------------------------------------------------------------------------
const CARTOUCHE_DURATION_TICKS = 3600;        // 3 minutes (20 ticks/sec × 180s)
const COOLDOWN_AFTER_USE_TICKS = 6000;        // 5 minutes cooldown (overdose risk)
const HARD_RESET_USES = 1;                     // 1 résurrection par cartouche

const VANILLA_EFFECTS_CRANKED = [
  // [potion_id, amplifier]  — durée = full cartridge duration
  ['minecraft:strength', 4],          // Strength X (5x dmg)
  ['minecraft:speed', 2],             // Speed III
  ['minecraft:regeneration', 3],      // Regen IV
  ['minecraft:resistance', 3],        // Resistance IV
  ['minecraft:haste', 4],             // Haste V
  ['minecraft:jump_boost', 2],        // Jump Boost III
  ['minecraft:night_vision', 0],      // Night Vision
  ['minecraft:water_breathing', 0],   // Water Breathing
  ['minecraft:fire_resistance', 0]    // Fire Resistance
];

// =============================================================================
// EVENT 1 — Right-click sur la Cartouche → Injection
// =============================================================================
events.listen('item.right_click', event => {
  const player = event.player;
  const stack = event.item;

  if (stack.id !== 'nexusabsolu:cartouche_manifold') return;
  if (player.world.isClient()) return;

  const data = player.persistentData;

  // Vérification cooldown
  if (data.getInt('manifold_cooldown') > 0) {
    player.tell('§4Ton corps n\'a pas encore métabolisé la dernière injection. Attends.');
    event.cancel();
    return;
  }

  // INJECTION — déclenchement
  injectCartouche(player);

  // Consomme la cartouche → cartouche_used
  stack.shrink(1);
  player.inventory.add('nexusabsolu:cartouche_used');

  // Lock animation 1.5s
  data.putInt('manifold_lock_until', player.age + 30);
  data.putInt('manifold_active_until', player.age + CARTOUCHE_DURATION_TICKS);
  data.putInt('manifold_cooldown', COOLDOWN_AFTER_USE_TICKS);
  data.putInt('manifold_hard_reset_charges', HARD_RESET_USES);

  event.cancel();
});

function injectCartouche(player) {
  // Sons + particules
  player.playSound('minecraft:entity.evocation_illager.cast_spell', 1.5, 0.5);
  player.playSound('minecraft:entity.player.levelup', 1.0, 2.0);

  // Annonce épique
  player.tell('§5§l[Manifoldine] §rTu te souviens de quelque chose. Quelque chose qui n\'était pas là avant.');

  // Apply vanilla effects cranked
  VANILLA_EFFECTS_CRANKED.forEach(eff => {
    player.potionEffects.add(eff[0], CARTOUCHE_DURATION_TICKS, eff[1], false, false);
  });

  // Glow effect (vanilla 1.12 doesn't have it natively — proxy via particles)
  // Custom KubeJS effect "manifoldine_active" if registered as potion
  // player.potionEffects.add('nexusabsolu:manifoldine_active', CARTOUCHE_DURATION_TICKS, 0, false, false);
}

// =============================================================================
// EVENT 2 — Tick joueur → entretien des effets custom + signature
// =============================================================================
events.listen('player.tick', event => {
  const player = event.player;
  if (player.world.isClient()) return;
  if (player.age % 4 !== 0) return; // 5x/sec

  const data = player.persistentData;
  const activeUntil = data.getInt('manifold_active_until');
  const cooldown = data.getInt('manifold_cooldown');

  // Decrement cooldown
  if (cooldown > 0) {
    data.putInt('manifold_cooldown', Math.max(0, cooldown - 4));
  }

  // Si Cartouche active : effets signature
  if (activeUntil > player.age) {
    // Particules visuelles autour du joueur (effet "aura Manifoldine")
    spawnAuraParticles(player);

    // SIGNATURE 1 — Lifesteal absolu : 50% des dégâts infligés guérissent
    // (implémenté dans event.entity_hurt ci-dessous)

    // SIGNATURE 2 — Aura Toxique passive : entités hostiles dans 3 blocs prennent Wither
    applyToxicAura(player);
  }
});

function spawnAuraParticles(player) {
  // KubeJS 1.12.2 : à valider. Sinon utiliser /particle command.
  for (let i = 0; i < 3; i++) {
    const angle = (player.age * 0.3 + i * 120) * Math.PI / 180;
    const x = player.x + Math.cos(angle) * 1.0;
    const z = player.z + Math.sin(angle) * 1.0;
    const y = player.y + 1.0;
    player.world.spawnParticle('portal', x, y, z, 0, 0, 0);
  }
}

function applyToxicAura(player) {
  const range = 3.0;
  // Get nearby hostile entities
  const entities = player.world.getEntitiesInArea(
    player.x - range, player.y - range, player.z - range,
    player.x + range, player.y + range, player.z + range
  );
  entities.forEach(e => {
    if (e === player) return;
    if (e.isHostile && e.isHostile()) {
      e.potionEffects.add('minecraft:wither', 60, 0, true, true);
      e.potionEffects.add('minecraft:poison', 60, 1, true, true);
    }
  });
}

// =============================================================================
// EVENT 3 — Joueur attaque entité → SIGNATURE 1 (Lifesteal) + 4 (Lightning Chain)
// =============================================================================
events.listen('entity.hurt', event => {
  if (!event.source || !event.source.player) return;
  const player = event.source.player;
  const data = player.persistentData;
  const activeUntil = data.getInt('manifold_active_until');

  if (activeUntil <= player.age) return;

  const damage = event.amount;

  // SIGNATURE 1 — LIFESTEAL ABSOLU
  const heal = damage * 0.5;
  player.heal(heal);

  // SIGNATURE 4 — LIGHTNING CHAIN
  // Tes attaques arcent sur jusqu'à 5 mobs autour dans 5 blocs
  const target = event.entity;
  if (target && !target.isPlayer) {
    chainLightning(player, target, damage * 0.3, 5, 5.0);
  }
});

function chainLightning(source, firstTarget, damage, maxJumps, range) {
  let current = firstTarget;
  let hit = [firstTarget];
  for (let i = 0; i < maxJumps; i++) {
    const candidates = current.world.getEntitiesInArea(
      current.x - range, current.y - range, current.z - range,
      current.x + range, current.y + range, current.z + range
    ).filter(e =>
      e !== source &&
      !hit.includes(e) &&
      e.isAlive &&
      !e.isPlayer
    );
    if (candidates.length === 0) break;
    const next = candidates[0];
    next.attack(damage);
    // visual: lightning particle line (à custom)
    next.world.spawnParticle('end_rod', next.x, next.y + 1, next.z, 0.1, 0.1, 0.1);
    hit.push(next);
    current = next;
    damage *= 0.7; // damage falloff
  }
}

// =============================================================================
// EVENT 4 — Joueur meurt → SIGNATURE 3 (Hard Reset) si charges restantes
// =============================================================================
events.listen('player.death', event => {
  const player = event.entity;
  const data = player.persistentData;
  const charges = data.getInt('manifold_hard_reset_charges');
  const activeUntil = data.getInt('manifold_active_until');

  if (activeUntil <= player.age) return; // Cartouche pas active
  if (charges <= 0) return; // Plus de charges

  // SIGNATURE 3 — HARD RESET
  event.cancel(); // Annule la mort
  player.heal(player.maxHealth); // Full HP
  data.putInt('manifold_hard_reset_charges', charges - 1);

  player.playSound('minecraft:block.beacon.activate', 2.0, 0.5);
  player.tell('§5§l[Manifoldine] §rLa simulation refuse ta mort. Pas encore. Pas comme ça.');

  // Bonus : effets persistants 30s
  player.potionEffects.add('minecraft:resistance', 600, 4, false, false);
  player.potionEffects.add('minecraft:regeneration', 600, 3, false, false);
  player.potionEffects.add('minecraft:absorption', 600, 4, false, false);
});

// =============================================================================
// SIGNATURE 2 — BULLET TIME (right-click sneak avec cartouche active)
// =============================================================================
events.listen('item.right_click', event => {
  const player = event.player;
  const data = player.persistentData;
  const activeUntil = data.getInt('manifold_active_until');

  if (activeUntil <= player.age) return;
  if (!player.isSneaking()) return;

  // Bullet Time : applique slowness IV à toutes les entités hostiles dans 8 blocs pendant 4s
  // Et speed II + jump boost III au joueur pendant 4s
  const range = 8.0;
  const entities = player.world.getEntitiesInArea(
    player.x - range, player.y - range, player.z - range,
    player.x + range, player.y + range, player.z + range
  );
  entities.forEach(e => {
    if (e === player) return;
    e.potionEffects.add('minecraft:slowness', 80, 4, true, true); // Slowness V
    e.potionEffects.add('minecraft:weakness', 80, 1, true, true);
  });

  player.potionEffects.add('minecraft:speed', 80, 2, false, false);
  player.potionEffects.add('minecraft:jump_boost', 80, 2, false, false);

  player.playSound('minecraft:item.elytra.flying', 1.0, 0.5);
  player.tell('§5§l[Bullet Time] §rTu vois entre les ticks.');
});

// =============================================================================
// NOTES D'IMPLÉMENTATION
// =============================================================================
// 1. KubeJS 1.12.2 (1.1.0.65) ne supporte pas forcément tous ces events tels
//    quels — `events.listen` avec ces noms exacts est à valider.
//    Si problème, alternative : ItemTweaker addon ou code Java.
//
// 2. Les effets vanilla cranked (Strength X, Speed V, etc) sont tous dispo
//    en 1.12.2 — aucun souci.
//
// 3. Particules custom : pour un effet "rainbow trail" ou "manifoldine glow"
//    spécifique, il faut un mod source Java avec particle factory. À faire en
//    Phase 6 finale.
//
// 4. La SIGNATURE 3 "Hard Reset" peut nécessiter un PlayerDeathEvent hook côté
//    Forge plutôt que KubeJS (le `event.cancel()` sur player.death peut ne pas
//    fonctionner via KubeJS 1.12.2). À tester en jeu.
//
// 5. Pour la cinématique de fin Âge 4 (sortie de simulation), prévoir un
//    advancement Vanilla qui se trigger à la première utilisation de la
//    cartouche → trigger commande /tp vers la "vraie" dimension Âge 5.
//    À implémenter dans worldprimer ou via OpenComputers Lua script.
//
// =============================================================================
