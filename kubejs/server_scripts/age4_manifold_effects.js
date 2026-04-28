// =============================================================================
// AGE 4 — CARTOUCHE MANIFOLD — Effets d'injection (V1 minimaliste)
// =============================================================================
// KubeJS 1.12.2 (1.1.0.65) utilise Rhino ES5. Donc :
//   - PAS de const/let → var partout
//   - PAS d'arrow functions => → function() {} partout
//   - PAS de template literals `${x}` → string concat avec '+'
//
// Cette V1 fait UN SEUL truc : right-click → applique les potions vanilla cranked.
// Si ça marche, on rajoutera Lifesteal/Lightning Chain/Bullet Time/Hard Reset
// progressivement en V2/V3 (chaque feature est isolable).
//
// Namespace items : "contenttweaker:" (default ContentTweaker) — PAS "nexusabsolu:"
// qui est reserve aux items du mod source Java.
// =============================================================================

// ---------------------------------------------------------------------------
// CONFIG
// ---------------------------------------------------------------------------
var CARTOUCHE_ID = 'contenttweaker:cartouche_manifold';
var CARTOUCHE_USED_ID = 'contenttweaker:cartouche_used';
var DURATION_TICKS = 3600; // 3 minutes
var COOLDOWN_TICKS = 6000; // 5 minutes

// ---------------------------------------------------------------------------
// EVENT — Right-click sur la Cartouche
// ---------------------------------------------------------------------------
// NB: si "events.listen" ne marche pas, essayer "onEvent" a la place.
events.listen('item.right_click', function(event) {
    var player = event.player;
    var stack = event.item;

    // Filtre : seulement notre cartouche
    if (!stack || stack.id !== CARTOUCHE_ID) return;

    // Cooldown check via NBT persistent data
    var data = player.persistentData;
    if (data.getInt('manifold_cooldown') > 0) {
        player.tell('§4Ton corps n\'a pas encore metabolise la derniere injection.');
        return;
    }

    // INJECTION
    injectCartouche(player);

    // Consomme la cartouche, donne la cartouche usee
    stack.shrink(1);
    player.inventory.add(CARTOUCHE_USED_ID);

    // Lock cooldown
    data.putInt('manifold_active_until', player.age + DURATION_TICKS);
    data.putInt('manifold_cooldown', COOLDOWN_TICKS);
});

function injectCartouche(player) {
    // Sound + message
    player.playSound('minecraft:entity.evocation_illager.cast_spell', 1.5, 0.5);
    player.playSound('minecraft:entity.player.levelup', 1.0, 2.0);
    player.tell('§5§l[Manifoldine] §rTu te souviens de quelque chose. Quelque chose qui n\'etait pas la avant.');

    // Vanilla potions cranked - amplifier (0 = level I, 1 = II, 2 = III, etc.)
    var d = DURATION_TICKS;
    player.potionEffects.add('minecraft:strength', d, 4, false, false);       // X
    player.potionEffects.add('minecraft:speed', d, 2, false, false);          // III
    player.potionEffects.add('minecraft:regeneration', d, 3, false, false);   // IV
    player.potionEffects.add('minecraft:resistance', d, 3, false, false);     // IV
    player.potionEffects.add('minecraft:haste', d, 4, false, false);          // V
    player.potionEffects.add('minecraft:jump_boost', d, 2, false, false);     // III
    player.potionEffects.add('minecraft:night_vision', d, 0, false, false);
    player.potionEffects.add('minecraft:water_breathing', d, 0, false, false);
    player.potionEffects.add('minecraft:fire_resistance', d, 0, false, false);
}

// ---------------------------------------------------------------------------
// EVENT — Decrementer le cooldown chaque tick
// ---------------------------------------------------------------------------
events.listen('player.tick', function(event) {
    var player = event.player;
    if (player.age % 20 !== 0) return; // 1x/sec pour economiser CPU

    var data = player.persistentData;
    var cooldown = data.getInt('manifold_cooldown');
    if (cooldown > 0) {
        data.putInt('manifold_cooldown', Math.max(0, cooldown - 20));
    }
});

// =============================================================================
// NOTES IMPLEMENTATION
// =============================================================================
// V1 = juste les potions vanilla cranked. C'est suffisant pour valider que :
//   1. KubeJS lit le fichier sans erreur de parse
//   2. events.listen('item.right_click') existe en 1.12.2
//   3. events.listen('player.tick') existe en 1.12.2
//   4. player.potionEffects.add() est la bonne API
//   5. player.persistentData lit/ecrit du NBT correctement
//
// Si V1 marche, ajouter en V2 :
//   - Lifesteal absolu (event 'entity.hurt')
//   - Aura toxique (event 'player.tick' avec entity scan)
//   - Particules autour du joueur
//
// Si V2 marche, ajouter en V3 :
//   - Lightning Chain (chain entity damage)
//   - Bullet Time (sneak right-click)
//   - Hard Reset (event 'player.death' avec cancel)
//
// Si KubeJS 1.12.2 ne supporte pas certains de ces events, fallback :
//   - CraftTweaker + EventTweaker addon
//   - OU mod source Java dans mod-source/src/main/java/com/nexusabsolu/mod/
// =============================================================================
