package com.nexusabsolu.mod.client;

import com.nexusabsolu.mod.events.ManifoldEffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Musique Centinela avec volume dynamique pendant le trip Manifold.
 *
 * world.playSound() ne permet pas de modifier le volume d'un son en cours.
 * Solution : ITickableSound (via MovingSound qui l'implemente). update() est
 * appele chaque tick par SoundManager, on y recalcule le volume selon le
 * progress du trip lu sur ManifoldClientState.
 *
 * Courbe de volume (V1.0.329, voir BRIEF_VISUEL_ULTIME etape 1) :
 *
 *   progress  | stage         | volume          | description
 *   ----------|---------------|-----------------|------------------------
 *   0.0000    | Stage 1 start | 0.0             | injection demarre
 *   0.0625    | Stage 2 start | 0.4             | fin fade-in
 *   0.5000    | Stage 5 start | 0.4             | ambient (S2-S4)
 *   0.6875    | Stage 5 end   | 0.8             | PEAK boost
 *   1.0000    | trip end      | 0.0             | fade-out total
 *
 * Spatialisation : NONE -- musique "dans la tete" du joueur, pas dans le
 * monde. La position est neanmoins mise a jour pour suivre le joueur (au
 * cas ou un mod tiers la lit).
 */
@SideOnly(Side.CLIENT)
public class ManifoldMusicTickableSound extends MovingSound {

    public ManifoldMusicTickableSound(SoundEvent sound) {
        super(sound, SoundCategory.MUSIC);
        this.repeat = false;
        this.repeatDelay = 0;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.attenuationType = ISound.AttenuationType.NONE;
    }

    @Override
    public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) {
            this.donePlaying = true;
            return;
        }

        long now = mc.world.getTotalWorldTime();
        float progress = ManifoldClientState.getTripProgress(now);

        // -1 = trip fini/inactif -> on coupe le son
        if (progress < 0.0F) {
            this.volume = 0.0F;
            this.donePlaying = true;
            return;
        }

        this.volume = computeVolume(progress);

        // Suivre le joueur (pour les mods tiers qui lisent xyz)
        EntityPlayer player = mc.player;
        if (player != null) {
            this.xPosF = (float) player.posX;
            this.yPosF = (float) player.posY;
            this.zPosF = (float) player.posZ;
        }
    }

    /**
     * Calcule le volume [0..1] selon le progress du trip [0..1].
     *
     * Bornes alignees sur les ratios STAGE_X de ManifoldEffectHandler :
     *   - STAGE_1_ONSET_END   = 0.0625
     *   - STAGE_4_HYPERSPACE_END = 0.5000
     *   - STAGE_5_PEAK_END    = 0.6875
     */
    public static float computeVolume(float p) {
        if (p < 0.0F) return 0.0F;

        // Phase 1 -- Fade in (Stage 1) : 0.0..0.0625 -> 0.0..0.4
        if (p < ManifoldEffectHandler.STAGE_1_ONSET_END) {
            return 0.4F * (p / ManifoldEffectHandler.STAGE_1_ONSET_END);
        }

        // Phase 2 -- Ambient (Stage 2-4) : 0.0625..0.5 -> 0.4 constant
        if (p < ManifoldEffectHandler.STAGE_4_HYPERSPACE_END) {
            return 0.4F;
        }

        // Phase 3 -- PEAK boost (Stage 5) : 0.5..0.6875 -> 0.4..0.8
        if (p < ManifoldEffectHandler.STAGE_5_PEAK_END) {
            float t = (p - ManifoldEffectHandler.STAGE_4_HYPERSPACE_END)
                    / (ManifoldEffectHandler.STAGE_5_PEAK_END - ManifoldEffectHandler.STAGE_4_HYPERSPACE_END);
            return 0.4F + 0.4F * t;
        }

        // Phase 4 -- Fade out (retour 4'/3'/2'/1') : 0.6875..1.0 -> 0.8..0.0
        if (p < 1.0F) {
            float t = (p - ManifoldEffectHandler.STAGE_5_PEAK_END)
                    / (1.0F - ManifoldEffectHandler.STAGE_5_PEAK_END);
            return 0.8F * (1.0F - t);
        }

        return 0.0F;
    }
}
