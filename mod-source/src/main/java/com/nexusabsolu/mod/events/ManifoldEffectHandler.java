package com.nexusabsolu.mod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Gestionnaire des effets temporels post-injection de la Cartouche Manifold.
 *
 * Timeline complete (5 minutes 30 secondes au total) :
 *   t=0s     INJECTION : potions cranked appliquees, particules chaos demarrent
 *   t=0-4min PHASE 1 (haut) : violet/cyan, 9 effets cranked, particules chaos
 *   t=4-5min PHASE 2 (vrille) : overlay passe en negatif, le trip vrille
 *   t=5min   CRASH : potions disparaissent, fatigue 1 min appliquee
 *   t=5-6min PHASE 3 (fatigue) : Slowness II + Mining Fatigue II + Weakness
 *   t=6min   RETOUR NORMAL
 *
 * NBT state stocke dans player.PERSISTED_NBT_TAG :
 *   - "manifold_active_until"   (long) : tick world a partir duquel les effets s'arretent
 *   - "manifold_phase2_at"      (long) : tick world ou commence la phase 2 (negatif)
 *   - "manifold_crash_at"       (long) : tick world du crash (declenche fatigue)
 *   - "manifold_fatigue_until"  (long) : tick world ou la fatigue se termine
 *   - "manifold_cooldown_until" (long) : tick world avant lequel re-injection refusee
 *
 * Le handler ne fait QUE deux choses :
 *  1. Spawn les particules chaos (server-side via SPacketParticles vers tous les joueurs autour)
 *  2. Declencher la phase 3 (crash + fatigue) au bon moment
 *
 * Tout le reste (overlay teinte) est client-side dans ManifoldOverlayHandler.
 */
public class ManifoldEffectHandler {

    // === Durees (ticks) ===
    public static final int PHASE1_DURATION   = 4 * 60 * 20;  // 4 min
    public static final int PHASE2_DURATION   = 1 * 60 * 20;  // 1 min (negatif)
    public static final int FATIGUE_DURATION  = 1 * 60 * 20;  // 1 min crash
    public static final int COOLDOWN_DURATION = 5 * 60 * 20;  // 5 min anti-overdose

    public static final int TOTAL_ACTIVE = PHASE1_DURATION + PHASE2_DURATION;  // 5 min

    // === NBT keys ===
    public static final String NBT_ACTIVE_UNTIL   = "manifold_active_until";
    public static final String NBT_PHASE2_AT      = "manifold_phase2_at";
    public static final String NBT_CRASH_AT       = "manifold_crash_at";
    public static final String NBT_FATIGUE_UNTIL  = "manifold_fatigue_until";
    public static final String NBT_COOLDOWN_UNTIL = "manifold_cooldown_until";
    public static final String NBT_CRASH_TRIGGERED = "manifold_crash_done";

    // === Phase enum (lu cote client pour overlay) ===
    public static final int PHASE_NONE     = 0;
    public static final int PHASE_ACTIVE   = 1;  // 0-4min, violet/cyan
    public static final int PHASE_NEGATIVE = 2;  // 4-5min, negatif
    public static final int PHASE_FATIGUE  = 3;  // 5-6min, fatigue

    // === Particule chaos config ===
    private static final int PARTICLE_INTERVAL_TICKS = 5;  // spawn toutes les 0.25s

    /**
     * Demarre l'injection. Appele depuis ItemCartoucheManifold.onItemRightClick().
     */
    public static void startInjection(EntityPlayerMP player) {
        long now = player.world.getTotalWorldTime();
        NBTTagCompound nbt = getPersistedNBT(player);
        nbt.setLong(NBT_ACTIVE_UNTIL, now + TOTAL_ACTIVE);
        nbt.setLong(NBT_PHASE2_AT, now + PHASE1_DURATION);
        nbt.setLong(NBT_CRASH_AT, now + TOTAL_ACTIVE);
        nbt.setLong(NBT_FATIGUE_UNTIL, now + TOTAL_ACTIVE + FATIGUE_DURATION);
        nbt.setLong(NBT_COOLDOWN_UNTIL, now + COOLDOWN_DURATION);
        nbt.setBoolean(NBT_CRASH_TRIGGERED, false);

        // Apply potions cranked (durent jusqu'a la fin de phase 2 = TOTAL_ACTIVE)
        applyEpicPotions(player, TOTAL_ACTIVE);

        // Sync timestamps au client pour l'overlay teinte ecran
        com.nexusabsolu.mod.network.NexusPacketHandler.INSTANCE.sendTo(
            new com.nexusabsolu.mod.network.PacketManifoldPhase(
                now, PHASE1_DURATION, TOTAL_ACTIVE, TOTAL_ACTIVE + FATIGUE_DURATION),
            player);
    }

    private static void applyEpicPotions(EntityPlayer player, int duration) {
        // amplifier 0=I, 1=II, 2=III, 3=IV, 4=V
        player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,        duration, 4, false, true));  // V
        player.addPotionEffect(new PotionEffect(MobEffects.SPEED,           duration, 2, false, true));  // III
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION,    duration, 3, false, true));  // IV
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE,      duration, 3, false, true));  // IV
        player.addPotionEffect(new PotionEffect(MobEffects.HASTE,           duration, 4, false, true));  // V
        player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST,      duration, 2, false, true));  // III
        player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION,    duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, duration, 0, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION,      duration, 4, false, true));  // V
    }

    private static void applyFatigue(EntityPlayer player) {
        // Leger : Slowness II + Mining Fatigue II + Weakness
        player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,         FATIGUE_DURATION, 1, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE,   FATIGUE_DURATION, 1, false, true));
        player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS,         FATIGUE_DURATION, 0, false, true));
    }

    /**
     * Tick toutes les 0.25s (5 ticks) pour spawn particules + checker phase change.
     */
    @SubscribeEvent
    public void onPlayerTick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        if (player.ticksExisted % PARTICLE_INTERVAL_TICKS != 0) return;

        int phase = getCurrentPhase(player);
        if (phase == PHASE_NONE) return;

        // Trigger crash si on vient juste de finir la phase 2
        if (phase == PHASE_FATIGUE) {
            NBTTagCompound nbt = getPersistedNBT(player);
            if (!nbt.getBoolean(NBT_CRASH_TRIGGERED)) {
                triggerCrash(player);
                nbt.setBoolean(NBT_CRASH_TRIGGERED, true);
            }
            return;  // pas de particules en phase fatigue
        }

        // Phases 1 et 2 : particules chaos
        spawnChaosParticles(player, phase);
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

        // Applique la fatigue
        applyFatigue(player);

        // Message immersif
        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "La realite revient. Tu n'as plus de force."));
        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_RED + "" + TextFormatting.ITALIC
            + "\"Repose-toi. Ton corps t'en supplie.\""));
    }

    /**
     * Spawn des particules chaos autour du joueur :
     *  - Tesseract 4D projete en 3D (8 sommets qui tournent)
     *  - Glyphes Voss (cercles de particules portales avec lightning entre)
     *  - Aura magique (END_ROD + ENCHANTMENT_TABLE)
     */
    private static void spawnChaosParticles(EntityPlayerMP player, int phase) {
        WorldServer world = (WorldServer) player.world;
        long t = world.getTotalWorldTime();
        double px = player.posX;
        double py = player.posY + 1.0;
        double pz = player.posZ;

        // === LAYER 1 : Tesseract 4D ===
        // Un tesseract a 16 sommets en 4D, projetes en 3D ils donnent 16 points
        // qui dansent de facon impossible. Rotation continue dans le plan w-x.
        double angleW = (t * 0.04) % (2.0 * Math.PI);
        double cosW = Math.cos(angleW);
        double sinW = Math.sin(angleW);
        double scale = 1.5;

        for (int i = 0; i < 16; i++) {
            // 16 sommets du tesseract : (x,y,z,w) chacun ±0.5
            double x4 = ((i & 1) != 0 ? 0.5 : -0.5);
            double y4 = ((i & 2) != 0 ? 0.5 : -0.5);
            double z4 = ((i & 4) != 0 ? 0.5 : -0.5);
            double w4 = ((i & 8) != 0 ? 0.5 : -0.5);

            // Rotation 4D dans le plan w-x
            double xRot = x4 * cosW - w4 * sinW;
            double wRot = x4 * sinW + w4 * cosW;

            // Projection perspective 4D -> 3D (plus le w est grand, plus c'est loin)
            double persp = 2.0 / (2.0 - wRot);
            double finalX = xRot * persp * scale;
            double finalY = y4 * persp * scale;
            double finalZ = z4 * persp * scale;

            EnumParticleTypes ptype = (phase == PHASE_NEGATIVE)
                ? EnumParticleTypes.DRAGON_BREATH
                : EnumParticleTypes.END_ROD;

            world.spawnParticle(ptype,
                px + finalX, py + finalY, pz + finalZ,
                1, 0, 0, 0, 0);
        }

        // === LAYER 2 : Glyphes Voss (cercles concentriques avec rotation) ===
        // 3 cercles a hauteurs differentes, rotations differentes
        double[] yOffsets = { -0.5, 0.0, 0.5 };
        double[] radii = { 1.8, 2.2, 1.8 };
        double[] speeds = { 0.08, -0.06, 0.10 };

        for (int ring = 0; ring < 3; ring++) {
            double angleBase = (t * speeds[ring]) % (2.0 * Math.PI);
            int count = 8;
            for (int i = 0; i < count; i++) {
                double a = angleBase + (i * 2.0 * Math.PI / count);
                double rx = Math.cos(a) * radii[ring];
                double rz = Math.sin(a) * radii[ring];

                EnumParticleTypes ptype = (phase == PHASE_NEGATIVE)
                    ? EnumParticleTypes.SPELL_WITCH
                    : EnumParticleTypes.PORTAL;

                world.spawnParticle(ptype,
                    px + rx, py + yOffsets[ring], pz + rz,
                    1, 0, 0, 0, 0);
            }
        }

        // === LAYER 3 : Lightning chain entre les sommets du tesseract ===
        // (en realite : lignes de particules CRIT_MAGIC entre 2 sommets aleatoires)
        if (t % 10 == 0) {
            double a1 = (t * 0.13) % (2.0 * Math.PI);
            double a2 = a1 + Math.PI * 0.7;
            double r = 1.5;
            double x1 = px + Math.cos(a1) * r;
            double z1 = pz + Math.sin(a1) * r;
            double x2 = px + Math.cos(a2) * r;
            double z2 = pz + Math.sin(a2) * r;
            int steps = 12;
            for (int s = 0; s <= steps; s++) {
                double f = s / (double) steps;
                double lx = x1 + (x2 - x1) * f;
                double ly = py + Math.sin(f * Math.PI) * 0.3;
                double lz = z1 + (z2 - z1) * f;
                world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                    lx, ly, lz, 1, 0, 0, 0, 0);
            }
        }

        // === LAYER 4 : Aura ENCHANTMENT_TABLE qui converge vers le joueur ===
        if (t % 4 == 0) {
            double rx = (Math.random() - 0.5) * 4.0;
            double ry = (Math.random() - 0.5) * 2.0;
            double rz = (Math.random() - 0.5) * 4.0;
            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                px + rx, py + ry, pz + rz,
                1,
                -rx * 0.1, -ry * 0.1, -rz * 0.1,  // motion vers le joueur
                0);
        }
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
     * Determine la phase actuelle en fonction des ticks NBT vs world time.
     */
    public static int getCurrentPhase(EntityPlayer player) {
        NBTTagCompound nbt = getPersistedNBT(player);
        long now = player.world.getTotalWorldTime();
        long activeUntil = nbt.getLong(NBT_ACTIVE_UNTIL);
        long phase2At = nbt.getLong(NBT_PHASE2_AT);
        long fatigueUntil = nbt.getLong(NBT_FATIGUE_UNTIL);

        if (activeUntil == 0L && fatigueUntil == 0L) return PHASE_NONE;

        if (now < phase2At)    return PHASE_ACTIVE;
        if (now < activeUntil) return PHASE_NEGATIVE;
        if (now < fatigueUntil) return PHASE_FATIGUE;
        return PHASE_NONE;
    }

    public static long getCooldownUntil(EntityPlayer player) {
        return getPersistedNBT(player).getLong(NBT_COOLDOWN_UNTIL);
    }
}
