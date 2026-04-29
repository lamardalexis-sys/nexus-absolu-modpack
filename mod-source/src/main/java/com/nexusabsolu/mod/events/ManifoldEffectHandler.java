package com.nexusabsolu.mod.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
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
    private static final int PARTICLE_INTERVAL_TICKS = 8;  // spawn toutes les 0.4s (moins spam)

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
     * Spawn des particules chaos autour du joueur — pattern phenomenologique DMT :
     *  - Mandala "Chrysanthemum veil" (8 petales, 4 couleurs alternees, theoreme 4-couleurs)
     *  - Sierpinski tetraedre 3D (fractale recursive niveau 3)
     *  - Tesseract 4D reduit (50% de la fréquence d'avant)
     *
     * Spawn frequency : toutes les 8 ticks (vs 5 avant) pour pas spammer.
     */
    private static void spawnChaosParticles(EntityPlayerMP player, int phase) {
        WorldServer world = (WorldServer) player.world;
        long t = world.getTotalWorldTime();
        double px = player.posX;
        double py = player.posY + 1.0;
        double pz = player.posZ;

        boolean negative = (phase == PHASE_NEGATIVE);

        // === LAYER 1 : MANDALA "Chrysanthemum veil" (8 petales, 4 couleurs) ===
        // Pattern phenomenologique DMT documente : mandala radial avec 4 couleurs
        // distinctes suivant le theoreme des 4 couleurs (aucune couleur adjacente
        // n'est la meme). On utilise 4 types de particules differentes pour les
        // 4 couleurs (en l'absence de coloration custom : magenta, cyan, lime, or).
        EnumParticleTypes[] mandalaPalette = negative ?
            new EnumParticleTypes[] {
                EnumParticleTypes.SPELL_MOB,         // sombre
                EnumParticleTypes.SPELL_WITCH,       // violet
                EnumParticleTypes.DRAGON_BREATH,     // gris-violet
                EnumParticleTypes.SMOKE_NORMAL       // sombre
            } :
            new EnumParticleTypes[] {
                EnumParticleTypes.HEART,             // proche du magenta vif
                EnumParticleTypes.END_ROD,           // cyan/blanc
                EnumParticleTypes.HAPPY_VILLAGER,    // vert lime
                EnumParticleTypes.FLAME              // or/orange
            };

        double mandalaRadius = 2.5;
        double mandalaAngle = (t * 0.05) % (2.0 * Math.PI);
        // 8 petales — alternance des 4 couleurs (theoreme 4 couleurs)
        for (int i = 0; i < 8; i++) {
            double a = mandalaAngle + (i * Math.PI / 4.0);
            double rx = Math.cos(a) * mandalaRadius;
            double rz = Math.sin(a) * mandalaRadius;
            // chaque petale a 3 particules en ligne radiale (effet chrysantheme)
            for (int p = 0; p < 3; p++) {
                double f = 0.6 + p * 0.2;
                EnumParticleTypes ptype = mandalaPalette[(i + p) % 4];
                world.spawnParticle(ptype,
                    px + rx * f, py, pz + rz * f,
                    1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // === LAYER 2 : SIERPINSKI TETRAEDRE 3D (fractale recursive) ===
        // 4 sommets initiaux d'un tetraedre, on subdivise recursivement niveau 3.
        // Chaque iteration : on prend les milieux des 4 cotes et on garde 4 sous-tetraedres.
        // Niveau 3 = 4^3 = 64 points (mais on les anime pour pas trop charger).
        double scale = 2.0;
        double sierAngle = (t * 0.03) % (2.0 * Math.PI);
        double cosA = Math.cos(sierAngle);
        double sinA = Math.sin(sierAngle);

        // 4 sommets du tetraedre regulier (centre origine)
        double[][] vertices = {
            { 1.0,  1.0,  1.0},
            { 1.0, -1.0, -1.0},
            {-1.0,  1.0, -1.0},
            {-1.0, -1.0,  1.0}
        };
        spawnSierpinskiPoints(world, px, py, pz, vertices, scale, 3,
            cosA, sinA, t, negative);

        // === LAYER 3 : TESSERACT 4D (reduit, 1 frame sur 2) ===
        if ((t / 8) % 2 == 0) {
            double angleW = (t * 0.04) % (2.0 * Math.PI);
            double cosW = Math.cos(angleW);
            double sinW = Math.sin(angleW);
            double tessScale = 1.5;

            for (int i = 0; i < 16; i++) {
                double x4 = ((i & 1) != 0 ? 0.5 : -0.5);
                double y4 = ((i & 2) != 0 ? 0.5 : -0.5);
                double z4 = ((i & 4) != 0 ? 0.5 : -0.5);
                double w4 = ((i & 8) != 0 ? 0.5 : -0.5);

                double xRot = x4 * cosW - w4 * sinW;
                double wRot = x4 * sinW + w4 * cosW;
                double persp = 2.0 / (2.0 - wRot);
                double finalX = xRot * persp * tessScale;
                double finalY = y4 * persp * tessScale;
                double finalZ = z4 * persp * tessScale;

                EnumParticleTypes ptype = negative
                    ? EnumParticleTypes.DRAGON_BREATH
                    : EnumParticleTypes.END_ROD;

                world.spawnParticle(ptype,
                    px + finalX, py + finalY, pz + finalZ,
                    1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // === LAYER 4 : Aura discrete qui converge vers le joueur (reduite) ===
        if (t % 8 == 0) {
            double rx = (Math.random() - 0.5) * 4.0;
            double ry = (Math.random() - 0.5) * 2.0;
            double rz = (Math.random() - 0.5) * 4.0;
            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                px + rx, py + ry, pz + rz,
                1,
                -rx * 0.1, -ry * 0.1, -rz * 0.1,
                0.0);
        }
    }

    /**
     * Recursion Sierpinski 3D : prend 4 sommets, si depth=0 spawn une particule
     * au centroid, sinon subdivise en 4 sous-tetraedres (un par sommet, base
     * milieux). Niveau 3 = 64 particules theoriques mais on stagger dans le temps.
     */
    private static void spawnSierpinskiPoints(WorldServer world,
                                                double cx, double cy, double cz,
                                                double[][] verts, double scale,
                                                int depth,
                                                double cosA, double sinA,
                                                long t, boolean negative) {
        if (depth == 0) {
            // Centroid du tetraedre actuel
            double sx = 0, sy = 0, sz = 0;
            for (double[] v : verts) {
                sx += v[0]; sy += v[1]; sz += v[2];
            }
            sx /= 4.0; sy /= 4.0; sz /= 4.0;
            // Rotation Y axis
            double rotX = sx * cosA - sz * sinA;
            double rotZ = sx * sinA + sz * cosA;
            // Stagger dans le temps : skip 50% des points par frame (look organique)
            int hash = (int)((rotX * 100 + rotZ * 100 + sy * 100));
            if ((hash + (int)(t / 4)) % 2 != 0) return;

            EnumParticleTypes ptype = negative
                ? EnumParticleTypes.SPELL_WITCH
                : EnumParticleTypes.PORTAL;
            world.spawnParticle(ptype,
                cx + rotX * scale, cy + sy * scale, cz + rotZ * scale,
                1, 0.0, 0.0, 0.0, 0.0);
            return;
        }
        // Subdivise : pour chaque sommet, cree un sous-tetraedre avec
        // les milieux entre ce sommet et les 3 autres
        for (int i = 0; i < 4; i++) {
            double[][] subVerts = new double[4][3];
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    subVerts[j] = verts[i].clone();
                } else {
                    subVerts[j] = new double[] {
                        (verts[i][0] + verts[j][0]) * 0.5,
                        (verts[i][1] + verts[j][1]) * 0.5,
                        (verts[i][2] + verts[j][2]) * 0.5
                    };
                }
            }
            spawnSierpinskiPoints(world, cx, cy, cz, subVerts, scale,
                depth - 1, cosA, sinA, t, negative);
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
