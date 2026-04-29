package com.nexusabsolu.mod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

/**
 * Gestionnaire des phases temporelles de la Cartouche Manifold.
 *
 * Architecture en 9 STAGES (vs 2 phases avant) — basee sur la phenomenologie
 * DMT documentee (Meyer 1992, Beyer 2009, Strassman 2008, Tramacchi 2018).
 *
 * Timeline complete (8 min trip + 1 min fatigue = 9 min) :
 *
 *   00:00 → 00:30   STAGE 1   Onset (le monde s'allege)
 *   00:30 → 01:30   STAGE 2   Saturation (couleurs deviennent vives)
 *   01:30 → 02:30   STAGE 3   Geometric (fractales 2D apparaissent)
 *   02:30 → 04:00   STAGE 4   Hyperspace (3D, espaces enormes)
 *   04:00 → 05:30   STAGE 5   Entity contact (PEAK — musique, entite, voix)
 *   05:30 → 06:30   STAGE 4'  Retour Hyperspace
 *   06:30 → 07:00   STAGE 3'  Retour Geometric
 *   07:00 → 07:30   STAGE 2'  Retour Saturation
 *   07:30 → 08:00   STAGE 1'  Retour Onset
 *   08:00 → 09:00   STAGE 0   Crash + fatigue
 *
 * Au lieu d'avoir des phases discretes, on calcule UN PROGRESS [0.0..1.0]
 * et chaque effet visuel a sa propre courbe d'intensite (smoothstep) le
 * long de ce progress. Resultat : transitions douces, pas de coupures.
 *
 * BPM sync : musique Centinela = 84 BPM.
 *   1 beat = 60/84 sec = 0.714 sec = 14.3 ticks
 *   Toutes les pulsations (alpha, scale, etc.) utilisent ce temps de base.
 *
 * NBT state :
 *   - "manifold_start_tick"    : tick world au debut de l'injection
 *   - "manifold_total_ticks"   : duree totale (TOTAL_TRIP + FATIGUE)
 *   - "manifold_cooldown_until" : tick world avant lequel re-injection refusee
 */
public class ManifoldEffectHandler {

    // === DUREES (ticks, 20 ticks/sec) ===
    public static final int TRIP_DURATION    = 8 * 60 * 20;  // 8 min de trip
    public static final int FATIGUE_DURATION = 1 * 60 * 20;  // 1 min crash
    public static final int TOTAL_DURATION   = TRIP_DURATION + FATIGUE_DURATION;
    public static final int COOLDOWN_DURATION = 10 * 60 * 20;  // 10 min anti-overdose

    // === BPM (Centinela = 84 BPM, G minor) ===
    public static final float BPM = 84.0f;
    public static final float BEAT_TICKS = 60.0f * 20.0f / BPM;  // 14.29 ticks/beat

    // === STAGES (en ratio du progress 0..1 du trip) ===
    // Aller (4 min = 0.0 → 0.5 du trip)
    public static final float STAGE_1_ONSET_END        = 0.0625f;  // 0:30
    public static final float STAGE_2_SATURATION_END   = 0.1875f;  // 1:30
    public static final float STAGE_3_GEOMETRIC_END    = 0.3125f;  // 2:30
    public static final float STAGE_4_HYPERSPACE_END   = 0.5000f;  // 4:00
    public static final float STAGE_5_PEAK_END         = 0.6875f;  // 5:30
    // Retour
    public static final float STAGE_4R_HYPERSPACE_END  = 0.8125f;  // 6:30
    public static final float STAGE_3R_GEOMETRIC_END   = 0.8750f;  // 7:00
    public static final float STAGE_2R_SATURATION_END  = 0.9375f;  // 7:30
    public static final float STAGE_1R_ONSET_END       = 1.0000f;  // 8:00

    // Stage IDs (pour le client)
    public static final int STAGE_NONE        = -1;
    public static final int STAGE_1_ONSET     = 1;
    public static final int STAGE_2_SATURATION = 2;
    public static final int STAGE_3_GEOMETRIC = 3;
    public static final int STAGE_4_HYPERSPACE = 4;
    public static final int STAGE_5_PEAK      = 5;
    public static final int STAGE_FATIGUE     = 6;

    // === NBT keys ===
    public static final String NBT_START_TICK      = "manifold_start_tick";
    public static final String NBT_COOLDOWN_UNTIL  = "manifold_cooldown_until";
    public static final String NBT_FATIGUE_DONE    = "manifold_fatigue_done";
    // v1.0.329 -- Etape 1 visuel ultime : la musique demarre au Stage 1 via
    // PacketManifoldPhase (ITickableSound). Au PEAK on n'envoie plus qu'un
    // message narratif, d'ou le rename de NBT_MUSIC_PLAYED.
    public static final String NBT_PEAK_ANNOUNCED  = "manifold_peak_announced";

    // === Whisper config ===
    private static final int WHISPER_INTERVAL_TICKS = 60;  // toutes les 3s en stage 5
    private static final Random RNG = new Random();

    /**
     * Demarre l'injection.
     */
    public static void startInjection(EntityPlayerMP player) {
        long now = player.world.getTotalWorldTime();
        NBTTagCompound nbt = getPersistedNBT(player);
        nbt.setLong(NBT_START_TICK, now);
        nbt.setLong(NBT_COOLDOWN_UNTIL, now + COOLDOWN_DURATION);
        nbt.setBoolean(NBT_FATIGUE_DONE, false);
        nbt.setBoolean(NBT_PEAK_ANNOUNCED, false);

        // Apply potions cranked (durent jusqu'a la fin de TRIP_DURATION)
        applyEpicPotions(player, TRIP_DURATION);

        // Sync au client (pour le rendering + ITickableSound musique fade)
        com.nexusabsolu.mod.network.NexusPacketHandler.INSTANCE.sendTo(
            new com.nexusabsolu.mod.network.PacketManifoldPhase(
                now, now, TOTAL_DURATION),
            player);

        // v1.0.345 -- FALLBACK MUSIQUE FIABLE :
        // En plus du ITickableSound (qui peut etre tue par le SoundManager
        // ou avoir des problemes de timing), on lance aussi la musique via
        // world.playSound() qui passe par le mecanisme vanilla
        // SPacketSoundEffect -> 100% fiable cote client.
        // C'est exactement la methode du Test 2 dans /nexus_test_sound.
        // Volume 1.0, pitch 1.0, RECORDS category.
        if (com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA != null) {
            player.world.playSound(null,
                player.posX, player.posY, player.posZ,
                com.nexusabsolu.mod.init.ModSounds.MANIFOLD_CENTINELA,
                net.minecraft.util.SoundCategory.RECORDS,
                1.0F, 1.0F);
        }
    }

    private static void applyEpicPotions(EntityPlayer player, int duration) {
        player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,        duration, 4, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.SPEED,           duration, 2, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION,    duration, 3, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE,      duration, 3, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.HASTE,           duration, 4, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST,      duration, 2, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION,    duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION,      duration, 4, false, true));
    }

    private static void applyFatigue(EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,       FATIGUE_DURATION, 1, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, FATIGUE_DURATION, 1, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS,       FATIGUE_DURATION, 0, false, true));
    }

    /**
     * Tick principal — gere les transitions de stage et effets.
     */
    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();

        long now = player.world.getTotalWorldTime();
        NBTTagCompound nbt = getPersistedNBT(player);
        long startTick = nbt.getLong(NBT_START_TICK);
        if (startTick == 0L) return;

        long elapsed = now - startTick;
        if (elapsed > TOTAL_DURATION) return;

        int stage = getCurrentStage(player);
        if (stage == STAGE_NONE) return;

        // Trigger crash a la fin du trip
        if (stage == STAGE_FATIGUE && !nbt.getBoolean(NBT_FATIGUE_DONE)) {
            triggerCrash(player);
            nbt.setBoolean(NBT_FATIGUE_DONE, true);
            return;
        }

        // Stage 5 PEAK : annoncer l'arrivee du PEAK + spawner whispers.
        // (La musique Centinela demarre des le Stage 1 via ITickableSound,
        // voir ManifoldMusicTickableSound + PacketManifoldPhase.Handler.)
        if (stage == STAGE_5_PEAK) {
            if (!nbt.getBoolean(NBT_PEAK_ANNOUNCED)) {
                announcePeakArrival(player);
                nbt.setBoolean(NBT_PEAK_ANNOUNCED, true);
            }
            // Whispers pendant le PEAK seulement
            if (player.ticksExisted % WHISPER_INTERVAL_TICKS == 0) {
                playWhisper(player);
            }
        }
    }

    private static void announcePeakArrival(EntityPlayerMP player) {
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "" + TextFormatting.ITALIC
            + "Quelque chose s'approche..."));
    }

    private static void playWhisper(EntityPlayerMP player) {
        // Whisper = villager ambient sound, volume tres bas, pitch baisse
        // Position aleatoire autour du joueur (10-15 blocs) → effet "lointain"
        double angle = RNG.nextDouble() * 2 * Math.PI;
        double dist = 10.0 + RNG.nextDouble() * 5.0;
        double wx = player.posX + Math.cos(angle) * dist;
        double wy = player.posY + (RNG.nextDouble() - 0.5) * 4.0;
        double wz = player.posZ + Math.sin(angle) * dist;

        // Pitch entre 0.4 et 0.7 = grave, mysterieux
        float pitch = 0.4F + RNG.nextFloat() * 0.3F;

        player.world.playSound(null, wx, wy, wz,
            SoundEvents.ENTITY_VILLAGER_AMBIENT, SoundCategory.NEUTRAL,
            0.3F, pitch);
    }

    private static void triggerCrash(EntityPlayerMP player) {
        // Retire toutes les potions epiques
        player.removePotionEffect(MobEffects.STRENGTH);
        player.removePotionEffect(MobEffects.SPEED);
        player.removePotionEffect(MobEffects.REGENERATION);
        player.removePotionEffect(MobEffects.RESISTANCE);
        player.removePotionEffect(MobEffects.HASTE);
        player.removePotionEffect(MobEffects.JUMP_BOOST);
        player.removePotionEffect(MobEffects.NIGHT_VISION);
        player.removePotionEffect(MobEffects.WATER_BREATHING);
        player.removePotionEffect(MobEffects.FIRE_RESISTANCE);
        player.removePotionEffect(MobEffects.ABSORPTION);

        applyFatigue(player);

        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "La realite revient. Tu n'as plus de force."));
        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_RED + "" + TextFormatting.ITALIC
            + "\"Repose-toi. Ton corps t'en supplie.\""));
    }

    // === Helpers NBT ===
    public static NBTTagCompound getPersistedNBT(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound persisted;
        if (data.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persisted = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        } else {
            persisted = new NBTTagCompound();
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, persisted);
        }
        return persisted;
    }

    /**
     * Calcule le progress du trip [0.0, 1.0] depuis le NBT.
     * Renvoie -1 si pas de trip actif.
     */
    public static float getTripProgress(EntityPlayer player) {
        NBTTagCompound nbt = getPersistedNBT(player);
        long startTick = nbt.getLong(NBT_START_TICK);
        if (startTick == 0L) return -1f;

        long now = player.world.getTotalWorldTime();
        long elapsed = now - startTick;
        if (elapsed >= TRIP_DURATION) return -1f;
        if (elapsed < 0) return -1f;

        return elapsed / (float) TRIP_DURATION;
    }

    /**
     * Determine le stage actuel selon le progress.
     */
    public static int getCurrentStage(EntityPlayer player) {
        NBTTagCompound nbt = getPersistedNBT(player);
        long startTick = nbt.getLong(NBT_START_TICK);
        if (startTick == 0L) return STAGE_NONE;

        long now = player.world.getTotalWorldTime();
        long elapsed = now - startTick;
        if (elapsed >= TOTAL_DURATION) return STAGE_NONE;

        // Crash phase
        if (elapsed >= TRIP_DURATION) return STAGE_FATIGUE;

        float p = elapsed / (float) TRIP_DURATION;

        // Aller
        if (p < STAGE_1_ONSET_END)        return STAGE_1_ONSET;
        if (p < STAGE_2_SATURATION_END)   return STAGE_2_SATURATION;
        if (p < STAGE_3_GEOMETRIC_END)    return STAGE_3_GEOMETRIC;
        if (p < STAGE_4_HYPERSPACE_END)   return STAGE_4_HYPERSPACE;
        if (p < STAGE_5_PEAK_END)         return STAGE_5_PEAK;
        // Retour (mirror)
        if (p < STAGE_4R_HYPERSPACE_END)  return STAGE_4_HYPERSPACE;
        if (p < STAGE_3R_GEOMETRIC_END)   return STAGE_3_GEOMETRIC;
        if (p < STAGE_2R_SATURATION_END)  return STAGE_2_SATURATION;
        if (p < STAGE_1R_ONSET_END)       return STAGE_1_ONSET;

        return STAGE_FATIGUE;
    }

    public static long getCooldownUntil(EntityPlayer player) {
        return getPersistedNBT(player).getLong(NBT_COOLDOWN_UNTIL);
    }
}
