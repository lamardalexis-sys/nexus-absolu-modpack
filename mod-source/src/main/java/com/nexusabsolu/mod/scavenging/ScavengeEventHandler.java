package com.nexusabsolu.mod.scavenging;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.ItemPioche;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ScavengeEventHandler {

    private static final Random rand = new Random();

    // Ticks between each scavenging drop
    private static final int INTERVAL_PIOCHE = 10;  // 0.5 sec
    private static final int INTERVAL_BARE = 16;     // 0.8 sec

    // If no heartbeat for this many ticks, stop mining
    private static final int HEARTBEAT_TIMEOUT = 8;

    // Safety: max continuous mining (5 min)
    private static final int MAX_DURATION = 6000;

    // Active miners: UUID -> MiningState
    private static final HashMap<UUID, MiningState> activeMiners = new HashMap<UUID, MiningState>();

    private static class MiningState {
        int ticksSinceLastDrop;
        int ticksSinceHeartbeat;
        int ticksActive;

        MiningState() {
            this.ticksSinceLastDrop = 0;
            this.ticksSinceHeartbeat = 0;
            this.ticksActive = 0;
        }
    }

    /**
     * Called from PacketMiningHeartbeat handler on the main server thread.
     * Refreshes or creates the mining state for this player.
     */
    public static void refreshHeartbeat(UUID uuid) {
        MiningState state = activeMiners.get(uuid);
        if (state == null) {
            state = new MiningState();
            activeMiners.put(uuid, state);
        }
        state.ticksSinceHeartbeat = 0;
    }

    // --- Tick handler: continuous scavenging ---
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.side.isClient()) return;

        EntityPlayer player = event.player;
        UUID uuid = player.getUniqueID();

        MiningState state = activeMiners.get(uuid);
        if (state == null) return;

        state.ticksActive++;
        state.ticksSinceLastDrop++;
        state.ticksSinceHeartbeat++;

        // Stop if no heartbeat received recently (player released click)
        if (state.ticksSinceHeartbeat > HEARTBEAT_TIMEOUT) {
            activeMiners.remove(uuid);
            return;
        }

        // Safety: stop after max duration
        if (state.ticksActive > MAX_DURATION) {
            activeMiners.remove(uuid);
            return;
        }

        // Server-side raycast validation (never trust client)
        RayTraceResult ray = rayTrace(player, 5.0);
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            activeMiners.remove(uuid);
            return;
        }

        BlockPos lookPos = ray.getBlockPos();
        World world = player.world;
        Block lookBlock = world.getBlockState(lookPos).getBlock();

        if (lookBlock.getRegistryName() == null ||
            !lookBlock.getRegistryName().toString().equals("compactmachines3:wall")) {
            activeMiners.remove(uuid);
            return;
        }

        // Check distance
        if (player.getDistanceSq(lookPos) > 36.0) {
            activeMiners.remove(uuid);
            return;
        }

        // Determine tool and interval
        ItemStack tool = player.getHeldItem(EnumHand.MAIN_HAND);
        boolean hasPioche = !tool.isEmpty() && tool.getItem() instanceof ItemPioche;
        int interval = hasPioche ? INTERVAL_PIOCHE : INTERVAL_BARE;

        if (state.ticksSinceLastDrop < interval) return;
        state.ticksSinceLastDrop = 0;

        // Execute scavenging
        if (hasPioche) {
            executePiocheMining(world, player, lookPos, tool);
        } else {
            executeBareHandMining(world, player, lookPos);
        }
    }

    private void executePiocheMining(World world, EntityPlayer player, BlockPos pos, ItemStack tool) {
        ItemPioche pioche = (ItemPioche) tool.getItem();
        int multiplier = pioche.getDustMultiplier();

        tool.damageItem(1, player);
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
            1.0F, 0.8F + rand.nextFloat() * 0.4F);
        player.swingArm(EnumHand.MAIN_HAND);

        double r = rand.nextDouble();

        if (multiplier <= 1) {
            // Pioche Fragmentee: wall_dust + bonus
            spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1 + rand.nextInt(2)));
            if (r < 0.30)      spawnDrop(world, player, new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
            else if (r < 0.50) spawnDrop(world, player, new ItemStack(Items.FLINT, 1));
            else if (r < 0.65) spawnDrop(world, player, new ItemStack(Items.CLAY_BALL, 1));
        } else {
            // Pioche Renforcee: grits + compose + wall_dust
            if (r < 0.12)       spawnDrop(world, player, new ItemStack(ModItems.IRON_GRIT, 1));
            else if (r < 0.25)  spawnDrop(world, player, new ItemStack(ModItems.COPPER_GRIT, 1));
            else if (r < 0.35)  spawnDrop(world, player, new ItemStack(ModItems.TIN_GRIT, 1));
            else if (r < 0.45)  spawnDrop(world, player, new ItemStack(Items.COAL, 1));
            else if (r < 0.53)  spawnDrop(world, player, new ItemStack(Items.REDSTONE, 1));
            else if (r < 0.58)  spawnDrop(world, player, new ItemStack(ModItems.NICKEL_GRIT, 1));
            else if (r < 0.635) spawnDrop(world, player, new ItemStack(ModItems.COMPOSE_A, 1));
            else                spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1));
        }

        player.addExhaustion(0.5F);

        // Stop if tool broke
        if (tool.isEmpty()) {
            activeMiners.remove(player.getUniqueID());
        }
    }

    private void executeBareHandMining(World world, EntityPlayer player, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
            0.7F, 0.6F + rand.nextFloat() * 0.3F);
        player.swingArm(EnumHand.MAIN_HAND);

        double r = rand.nextDouble();

        if (r < 0.18)      spawnDrop(world, player, new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
        else if (r < 0.43) spawnDrop(world, player, new ItemStack(Items.STICK, 1));
        else if (r < 0.63) spawnDrop(world, player, new ItemStack(Blocks.PLANKS, 1));
        else if (r < 0.75) spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1));

        player.addExhaustion(0.8F);
    }

    private RayTraceResult rayTrace(EntityPlayer player, double reach) {
        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d lookVec = player.getLook(1.0F);
        Vec3d endPos = eyePos.addVector(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);
        return player.world.rayTraceBlocks(eyePos, endPos, false, false, true);
    }

    private void spawnDrop(World world, EntityPlayer player, ItemStack stack) {
        EntityItem entity = new EntityItem(world,
            player.posX, player.posY + 0.5, player.posZ, stack);
        entity.setNoPickupDelay();
        world.spawnEntity(entity);
    }
}
