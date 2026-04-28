package com.nexusabsolu.mod.events;

import com.nexusabsolu.mod.proxy.CommonProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Quand un joueur entre/reste dans le fluide Diarrhee Liquide:
 *  - Effet Nausea (vision qui ondule) - 6s
 *  - Effet Hunger (faim qui descend vite) - 4s
 *  - Particules ITEM_CRACK marron depuis la tete (= vomi)
 *  - Son rot/burp aleatoirement
 *
 * Re-applique les effets toutes les 40 ticks (2s) pour eviter le spam
 * tout en maintenant l'etat actif tant que le joueur est dans le fluide.
 */
public class DiarrheeImmersionHandler {

    private static final int CHECK_INTERVAL_TICKS = 40; // 2 secondes
    private static final int NAUSEA_DURATION_TICKS = 120; // 6 secondes
    private static final int HUNGER_DURATION_TICKS = 80;  // 4 secondes
    private static final int VOMIT_PARTICLES_PER_PULSE = 8;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;

        // Pulse toutes les 40 ticks (et phase decalee selon entityId pour eviter
        // que tous les joueurs vomissent en meme temps)
        if ((world.getTotalWorldTime() + player.getEntityId()) % CHECK_INTERVAL_TICKS != 0) {
            return;
        }

        if (!isInDiarrhee(player)) return;

        // Effets de potion (server-side seulement)
        if (!world.isRemote) {
            player.addPotionEffect(new PotionEffect(
                MobEffects.NAUSEA, NAUSEA_DURATION_TICKS, 0, false, false));
            player.addPotionEffect(new PotionEffect(
                MobEffects.HUNGER, HUNGER_DURATION_TICKS, 0, false, false));

            // Son de rot a 50% pour pas trop spammer
            if (world.rand.nextFloat() < 0.5f) {
                world.playSound(null, player.posX, player.posY, player.posZ,
                    SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                    0.6f, 0.7f + world.rand.nextFloat() * 0.3f);
            }
        }

        // Particules vomi (client + server pour synchro)
        spawnVomitParticles(world, player);
    }

    /**
     * Le joueur est-il en contact avec le fluide Diarrhee?
     * On verifie les blocs qui intersectent la bbox du joueur.
     */
    private boolean isInDiarrhee(EntityPlayer player) {
        if (CommonProxy.DIARRHEE_FLUID_BLOCK == null) return false;

        AxisAlignedBB bb = player.getEntityBoundingBox().shrink(0.001);
        int minX = (int) Math.floor(bb.minX);
        int maxX = (int) Math.floor(bb.maxX);
        int minY = (int) Math.floor(bb.minY);
        int maxY = (int) Math.floor(bb.maxY);
        int minZ = (int) Math.floor(bb.minZ);
        int maxZ = (int) Math.floor(bb.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    pos.setPos(x, y, z);
                    if (player.world.getBlockState(pos).getBlock()
                            == CommonProxy.DIARRHEE_FLUID_BLOCK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Spawn des particules ITEM_CRACK avec un item marron (cocoa/dirt)
     * a partir de la bouche du joueur, vers l'avant.
     * Effet: simule le vomi qui sort.
     */
    private void spawnVomitParticles(World world, EntityPlayer player) {
        // Position de la "bouche" (yeux - 0.2)
        double mouthY = player.posY + player.getEyeHeight() - 0.2;
        Vec3d look = player.getLookVec();

        // On utilise la metadata d'ItemStack (Items.DYE = poudre marron via meta=3 cocoa)
        // Pour ITEM_CRACK la metadata se passe via le 4eme argument (ou par
        // l'array d'arguments qui contient l'itemId)
        int cocoaItemId = net.minecraft.item.Item.getIdFromItem(Items.DYE);

        for (int i = 0; i < VOMIT_PARTICLES_PER_PULSE; i++) {
            // Position de spawn: legerement devant la bouche
            double sx = player.posX + look.x * 0.4 + (world.rand.nextDouble() - 0.5) * 0.2;
            double sy = mouthY + (world.rand.nextDouble() - 0.5) * 0.1;
            double sz = player.posZ + look.z * 0.4 + (world.rand.nextDouble() - 0.5) * 0.2;

            // Velocite: vers l'avant + un peu de gravite/etalement
            double vx = look.x * 0.4 + (world.rand.nextDouble() - 0.5) * 0.15;
            double vy = 0.1 + world.rand.nextDouble() * 0.15; // un peu vers le haut
            double vz = look.z * 0.4 + (world.rand.nextDouble() - 0.5) * 0.15;

            // ITEM_CRACK avec id de l'item DYE et meta=3 (cocoa = marron)
            world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                sx, sy, sz, vx, vy, vz, cocoaItemId, 3);
        }
    }
}
