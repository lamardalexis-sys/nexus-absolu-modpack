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
     * smoothstep(edge0, edge1, x) — fonction d'interpolation lisse.
     * Renvoie 0 si x<=edge0, 1 si x>=edge1, courbe S entre les deux.
     */
    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    /**
     * Calcule l'intensite de chaque layer visuel selon le progress du trip.
     * Chaque layer "monte" pendant son stage et "descend" pendant le mirror.
     *
     * @return tableau [stage1, stage2, stage3, stage4, stage5] intensities [0..1]
     */
    public static float[] getLayerIntensities(float progress) {
        float[] r = new float[5];
        if (progress < 0) return r;

        // Stage 1 (onset) : 0..0.06 monte, 0.94..1.0 descend, 1.0 entre les deux
        r[0] = Math.max(
            smoothstep(0f, 0.0625f, progress),
            1f - smoothstep(0.9375f, 1.0f, progress)
        );

        // Stage 2 (saturation) : 0.06..0.19 monte, 0.875..0.9375 descend
        r[1] = Math.min(
            smoothstep(0.0625f, 0.1875f, progress),
            1f - smoothstep(0.875f, 0.9375f, progress)
        );
        r[1] = Math.max(0f, r[1]);

        // Stage 3 (geometric) : 0.19..0.31 monte, 0.8125..0.875 descend
        r[2] = Math.min(
            smoothstep(0.1875f, 0.3125f, progress),
            1f - smoothstep(0.8125f, 0.875f, progress)
        );
        r[2] = Math.max(0f, r[2]);

        // Stage 4 (hyperspace) : 0.31..0.5 monte, 0.6875..0.8125 descend
        r[3] = Math.min(
            smoothstep(0.3125f, 0.5f, progress),
            1f - smoothstep(0.6875f, 0.8125f, progress)
        );
        r[3] = Math.max(0f, r[3]);

        // Stage 5 (peak) : 0.5..0.51 monte (rapide, 1% du trip = ~5s pour
        // que l'entite soit visible quasi instantanement et qu'on voie bien
        // le morphing iris->crack->3faces->Salviadroid qui se passe sur
        // 0.5..0.5625), reste a fond, descend 0.65..0.6875.
        // v1.0.336 BUGFIX : avant le fade-in s'etalait sur 0.5..0.59 (43s),
        // ce qui rendait toute la phase morphing quasi invisible.
        r[4] = Math.min(
            smoothstep(0.5f, 0.51f, progress),
            1f - smoothstep(0.65f, 0.6875f, progress)
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
