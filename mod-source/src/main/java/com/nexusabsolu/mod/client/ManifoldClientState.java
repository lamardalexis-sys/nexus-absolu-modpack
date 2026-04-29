package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Etat cote client de l'injection Manifoldine en cours.
 *
 * Maintenu via PacketManifoldPhase (server → client). Variables statiques
 * parce qu'on a un seul player local en client. Persiste pas entre
 * deconnexions — si le joueur se reconnecte au milieu d'une injection,
 * le serveur lui renvoie un packet de sync au login.
 */
@SideOnly(Side.CLIENT)
public class ManifoldClientState {

    /** Tick world au moment de la derniere sync. */
    private static long syncTick = 0;
    /** Tick (absolu) ou la phase 2 (negative) commence. */
    private static long phase2At = 0;
    /** Tick (absolu) ou les potions epiques s'arretent (= fin phase 2). */
    private static long activeUntil = 0;
    /** Tick (absolu) ou la fatigue se termine. */
    private static long fatigueUntil = 0;

    public static void update(long syncTickIn, int phase2Delta,
                               int activeDelta, int fatigueDelta) {
        syncTick = syncTickIn;
        phase2At = (phase2Delta > 0) ? syncTickIn + phase2Delta : 0;
        activeUntil = (activeDelta > 0) ? syncTickIn + activeDelta : 0;
        fatigueUntil = (fatigueDelta > 0) ? syncTickIn + fatigueDelta : 0;
    }

    public static int getCurrentPhase(long now) {
        if (activeUntil == 0L && fatigueUntil == 0L) {
            return ManifoldEffectHandler.PHASE_NONE;
        }
        if (now < phase2At)     return ManifoldEffectHandler.PHASE_ACTIVE;
        if (now < activeUntil)  return ManifoldEffectHandler.PHASE_NEGATIVE;
        if (now < fatigueUntil) return ManifoldEffectHandler.PHASE_FATIGUE;
        return ManifoldEffectHandler.PHASE_NONE;
    }
}
