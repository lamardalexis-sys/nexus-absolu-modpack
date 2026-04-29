package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Etat client de l'injection Manifold.
 *
 * Sync via PacketManifoldPhase. Stocke startTick + totalTicks, le client
 * calcule progress + stage chaque frame avec la meme logique que le serveur
 * (ManifoldEffectHandler.getCurrentStage / getTripProgress).
 *
 * Helper BPM-sync : getBeatPhase(now) renvoie [0..1] selon le BPM 84.
 */
@SideOnly(Side.CLIENT)
public class ManifoldClientState {

    private static long syncTick = 0L;
    private static long startTick = 0L;
    private static int totalTicks = 0;

    public static void update(long syncTickIn, long startTickIn, int totalTicksIn) {
        syncTick = syncTickIn;
        startTick = startTickIn;
        totalTicks = totalTicksIn;
    }

    /** Renvoie le tick world du debut du trip (ou 0 si pas actif). */
    public static long getStartTick() {
        return startTick;
    }

    /**
     * Calcule le progress [0..1] du trip ou -1 si fini/inactif.
     */
    public static float getTripProgress(long now) {
        if (startTick == 0L || totalTicks == 0) return -1f;
        long elapsed = now - startTick;
        if (elapsed < 0) return -1f;
        if (elapsed >= ManifoldEffectHandler.TRIP_DURATION) return -1f;
        return elapsed / (float) ManifoldEffectHandler.TRIP_DURATION;
    }

    /**
     * Stage actuel (mirror logique du serveur).
     */
    public static int getCurrentStage(long now) {
        if (startTick == 0L || totalTicks == 0) {
            return ManifoldEffectHandler.STAGE_NONE;
        }
        long elapsed = now - startTick;
        if (elapsed >= totalTicks || elapsed < 0) {
            return ManifoldEffectHandler.STAGE_NONE;
        }
        if (elapsed >= ManifoldEffectHandler.TRIP_DURATION) {
            return ManifoldEffectHandler.STAGE_FATIGUE;
        }

        float p = elapsed / (float) ManifoldEffectHandler.TRIP_DURATION;

        if (p < ManifoldEffectHandler.STAGE_1_ONSET_END)        return ManifoldEffectHandler.STAGE_1_ONSET;
        if (p < ManifoldEffectHandler.STAGE_2_SATURATION_END)   return ManifoldEffectHandler.STAGE_2_SATURATION;
        if (p < ManifoldEffectHandler.STAGE_3_GEOMETRIC_END)    return ManifoldEffectHandler.STAGE_3_GEOMETRIC;
        if (p < ManifoldEffectHandler.STAGE_4_HYPERSPACE_END)   return ManifoldEffectHandler.STAGE_4_HYPERSPACE;
        if (p < ManifoldEffectHandler.STAGE_5_PEAK_END)         return ManifoldEffectHandler.STAGE_5_PEAK;
        if (p < ManifoldEffectHandler.STAGE_4R_HYPERSPACE_END)  return ManifoldEffectHandler.STAGE_4_HYPERSPACE;
        if (p < ManifoldEffectHandler.STAGE_3R_GEOMETRIC_END)   return ManifoldEffectHandler.STAGE_3_GEOMETRIC;
        if (p < ManifoldEffectHandler.STAGE_2R_SATURATION_END)  return ManifoldEffectHandler.STAGE_2_SATURATION;
        if (p < ManifoldEffectHandler.STAGE_1R_ONSET_END)       return ManifoldEffectHandler.STAGE_1_ONSET;
        return ManifoldEffectHandler.STAGE_FATIGUE;
    }

    /**
     * smoothstep(edge0, edge1, x) -- courbe d'interpolation Hermite cubique
     * (smooth aux 2 extremites mais transition rapide au milieu).
     */
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    /**
     * smootherstep(edge0, edge1, x) -- courbe quintic de Ken Perlin (2002),
     * derivee 1ere ET 2eme nulles aux extremites -> transition encore plus
     * douce que smoothstep, particulierement perceptible quand l'oeil suit
     * une variation continue (cas de notre fade in/out de stage).
     *
     * Formule : 6t^5 - 15t^4 + 10t^3
     *
     * v1.0.339 (Etape 8) : utilisee pour les transitions entre stages
     * pour donner une sensation de "flow" plus organique.
     */
    public static float smootherstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * t * (t * (t * 6f - 15f) + 10f);
    }

    /**
     * Calcule l'intensite de chaque layer visuel selon le progress du trip.
     * Chaque layer "monte" pendant son stage et "descend" pendant le mirror.
     *
     * v1.0.339 (Etape 8) -- TRANSITIONS ELARGIES + courbe quintic :
     *   - Fenetres de fade-in/out elargies (chevauchement plus long entre
     *     stages adjacents -> sensation de flow continu, pas de coupure)
     *   - smootherstep() au lieu de smoothstep() -> derivee 2eme nulle aux
     *     extremites, transition imperceptible visuellement
     *
     * @return tableau [stage1, stage2, stage3, stage4, stage5] intensities [0..1]
     */
    public static float[] getLayerIntensities(float progress) {
        float[] r = new float[5];
        if (progress < 0) return r;

        // Stage 1 (onset) : transitions elargies a 8% du trip
        // Aller : 0..0.10 monte (au lieu de 0..0.0625) ; Retour : 0.90..1.0 descend
        r[0] = Math.max(
            smootherstep(0f, 0.10f, progress),
            1f - smootherstep(0.90f, 1.0f, progress)
        );

        // Stage 2 (saturation) : chevauche 5% avec stage 1 et stage 3
        // Aller : 0.04..0.22 monte ; Retour : 0.84..0.96 descend
        r[1] = Math.min(
            smootherstep(0.04f, 0.22f, progress),
            1f - smootherstep(0.84f, 0.96f, progress)
        );
        r[1] = Math.max(0f, r[1]);

        // Stage 3 (geometric) : chevauche 5% avec stages adjacents
        // Aller : 0.16..0.36 monte ; Retour : 0.78..0.88 descend
        r[2] = Math.min(
            smootherstep(0.16f, 0.36f, progress),
            1f - smootherstep(0.78f, 0.88f, progress)
        );
        r[2] = Math.max(0f, r[2]);

        // Stage 4 (hyperspace) : chevauche 5% avec stage 3 et stage 5 PEAK
        // Aller : 0.28..0.52 monte ; Retour : 0.66..0.84 descend
        r[3] = Math.min(
            smootherstep(0.28f, 0.52f, progress),
            1f - smootherstep(0.66f, 0.84f, progress)
        );
        r[3] = Math.max(0f, r[3]);

        // Stage 5 (peak) : fade-in moyen (pas instantane mais pas etale a
        // l'absurde non plus) -> 0.48..0.53 (~5s pour atteindre max).
        // C'est important pour 2 raisons :
        //   1. L'entite doit etre visible des le debut du PEAK pour qu'on voie
        //      le morphing iris->crack->faces->Salviadroid qui se passe sur 45s.
        //   2. Trop rapide = saut visuel, trop lent = on rate le morphing.
        // Fade-out : 0.65..0.6875 inchange (assez vif pour finir le PEAK net).
        r[4] = Math.min(
            smootherstep(0.48f, 0.53f, progress),
            1f - smootherstep(0.65f, 0.6875f, progress)
        );
        r[4] = Math.max(0f, r[4]);

        return r;
    }

    /**
     * Phase du beat actuel [0..1] selon BPM 84.
     * 0 = on est sur le kick, 1 = juste avant le prochain kick.
     */
    public static float getBeatPhase(long now) {
        float beatTicks = ManifoldEffectHandler.BEAT_TICKS;
        return (float)((now % (long) beatTicks) / beatTicks);
    }

    /**
     * Pulsation "kick" : remonte vite a 1.0 sur le beat puis descend en exp.
     */
    public static float getBeatKick(long now) {
        float phase = getBeatPhase(now);
        // Attaque rapide (premiers 10%) puis decay en exp
        if (phase < 0.1f) {
            return phase / 0.1f;  // 0 → 1
        }
        return (float) Math.exp(-(phase - 0.1f) * 4.0);
    }
}
