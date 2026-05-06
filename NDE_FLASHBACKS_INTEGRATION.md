# NDE_FLASHBACKS_INTEGRATION.md

> Guide pour brancher les phases C + E du Sprint 2 étape 2 NDE flashbacks.
> Version : 1.0.363 (06 mai 2026)
> Status : Phases A/B/D **terminées**, phases C/E à brancher.

---

## 📦 Ce qui est déjà fait (phases A, B, D)

Trois nouveaux fichiers Java prêts à l'emploi, **aucune modification du code existant** sauf NexusPacketHandler (+2 lignes) :

| Fichier | Rôle |
|---|---|
| `manifold/PlayerMemorySnapshot.java` | Data class capture stats joueur (mobs/items/biomes/dimensions/temps) |
| `network/PacketSyncMemorySnapshot.java` | Packet server→client + Handler |
| `client/ManifoldClientMemory.java` | Storage côté client du snapshot reçu |
| `client/ManifoldFlashbackRenderer.java` | Renderer 250 lignes pour afficher les flashbacks |

Test rapide : tout compile et passe les vérifs syntaxiques (braces/parens équilibrés).

---

## 🔌 Phase C — Hook capture + envoi snapshot au start du trip (à brancher)

**Où** : dans le code qui démarre un trip cartouche. Probablement :
- `tiles/TileMachineManifold.java` (si elle existe), méthode `activate()` ou `tryStartTrip()`
- `items/ItemCartoucheManifold.java` méthode `onItemRightClick()` ou `onItemUse()`

**Code à insérer** (au moment où le trip démarre) :

```java
// Sprint 2 etape 2 : capture stats joueur et envoi au client
if (player instanceof EntityPlayerMP) {
    com.nexusabsolu.mod.manifold.PlayerMemorySnapshot snap =
        com.nexusabsolu.mod.manifold.PlayerMemorySnapshot.captureFromPlayer(player);
    
    com.nexusabsolu.mod.network.NexusPacketHandler.INSTANCE.sendTo(
        new com.nexusabsolu.mod.network.PacketSyncMemorySnapshot(snap),
        (EntityPlayerMP) player);
}
```

**Effet** : au moment où le joueur active le cartouche, son snapshot est capturé côté serveur (lecture des stats) et envoyé immédiatement au client. Le `ManifoldClientMemory.setSnapshot()` est appelé dans le handler.

**Note** : la capture lit les stats vanilla disponibles. Pour ajouter biomes/dimensions visités, il faudra plus tard un BiomeTracker custom (Sprint 2.5).

---

## 🎨 Phase E — Intégration du renderer dans ManifoldOverlayHandler (à brancher)

**Où** : dans `client/ManifoldOverlayHandler.java`, méthode `onRenderOverlay()`, après les couches existantes mais **AVANT** le fade to black final (qui doit rester en dernier).

**Code à insérer** (chercher la couche FINALE - fade to black, et insérer JUSTE AVANT) :

```java
// === COUCHE FLASHBACKS NDE -- v1.0.363 Sprint 2 etape 2 ===
// Affichage references a la save du joueur pendant la NDE.
// Skip si pas de snapshot (le joueur n'a pas encore lance de trip
// avec la phase C branchee).
if (com.nexusabsolu.mod.client.ManifoldClientMemory.hasSnapshot()
    && progress > 0.625f && progress < 0.99f) {
    com.nexusabsolu.mod.client.ManifoldFlashbackRenderer.render(
        mc, w, h, now, progress,
        com.nexusabsolu.mod.client.ManifoldClientMemory.getSnapshot());
}

// === COUCHE FINALE -- v1.0.359 : FADE TO BLACK definitif ===
// (existant, ne pas toucher)
```

**Effet** : pendant la NDE :
- 40 icones d'items collectés/craftés défilent à l'écran (mouvement vertical)
- Tailles variées (0.5x à 2x) pour effet de profondeur
- Alpha animée : fade in 0.625-0.70, plateau 0.70-0.85, fade out 0.85-0.99
- Texte mobs (blanc) et biomes (vert) en surimpression sur certaines positions
- Le renderer skip automatiquement si pas de snapshot

---

## 🧹 Phase F (optionnelle) — Cleanup au end of trip

Pour libérer la mémoire à la fin du trip, ajouter dans la même méthode où le trip se termine :

```java
// Cleanup snapshot client a la fin du trip
com.nexusabsolu.mod.client.ManifoldClientMemory.clear();
```

Pas critique : le snapshot occupe ~quelques KB, mais c'est plus propre.

---

## 🧪 Test step-by-step

1. **Build** : `bash mod-source/build.sh`. Si erreur de compilation, vérifier que les imports `EntityPlayerMP`, etc. sont corrects.

2. **Test phase C seule** (sans phase E) :
   - Activer un cartouche → vérifier dans les logs serveur : `[Nexus] PlayerMemorySnapshot captured X entries`
   - Vérifier dans les logs client : `[Nexus] Snapshot received from server`
   - Note : pas de visuel pendant la NDE encore (phase E non branchée).

3. **Test phases C + E** :
   - Activer un cartouche, attendre la phase NDE (5:00 min in trip)
   - Voir les icones d'items défilent + textes mobs/biomes
   - Vérifier que ça fade out à la fin

4. **Edge cases** :
   - Premier trip après un nouveau monde : peu d'items collectés → display vide ou minimal
   - Trip après long jeu : 100+ items → rotation parmi le pool de 40 affichés
   - Trip multiplayer : chaque joueur a son propre snapshot

---

## 🐛 Troubleshooting

**Pas de logs "Snapshot received"** :
- Vérifier que `PacketSyncMemorySnapshot` est bien dans `NexusPacketHandler.init()`
- Vérifier que la phase C est bien branchée (instanceof EntityPlayerMP, sendTo)

**Items ne s'affichent pas** :
- Vérifier que `ManifoldClientMemory.hasSnapshot()` retourne true (debug log)
- Vérifier les couches dans ManifoldOverlayHandler (l'appel doit être AVANT le fade to black)
- Si `cachedItems.isEmpty()` → seul le texte sera affiché

**Crash au render item** :
- Le renderer a un try/catch sur `renderItemAndEffectIntoGUI`, devrait skip les items qui crashent
- Si crash GL state, vérifier que `RenderHelper.disableStandardItemLighting()` est bien appelé

---

## 📊 Bilan technique

| Métrique | Valeur |
|---|---|
| Nouveaux fichiers Java | 4 |
| Lignes ajoutées | ~810 |
| Modifications fichiers existants | 1 fichier, 2 lignes (NexusPacketHandler) |
| Tests requis | Build + run en jeu avec un cartouche |
| Risque de régression | **Faible** - tout est dans des fichiers séparés, désactivé sans hook |

---

*Sprint 2 étape 2 phases A/B/D complètes. Phases C+E = 2 hooks Java à brancher.*
