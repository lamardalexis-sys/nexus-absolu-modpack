package com.nexusabsolu.mod.scavenging;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.ItemPioche;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ScavengeEventHandler {

    private static final Random rand = new Random();
    private static final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_PIOCHE = 500L;
    private static final long COOLDOWN_BARE = 800L;

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        World world = event.getWorld();
        if (world.isRemote) return;

        EntityPlayer player = event.getEntityPlayer();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Only works on Compact Machine walls
        if (block.getRegistryName() == null) return;
        String regName = block.getRegistryName().toString();
        if (!regName.equals("compactmachines3:wall")) return;

        UUID uuid = player.getUniqueID();
        long now = System.currentTimeMillis();

        ItemStack tool = player.getHeldItem(EnumHand.MAIN_HAND);
        boolean hasPioche = !tool.isEmpty() && tool.getItem() instanceof ItemPioche;

        if (hasPioche) {
            // === PIOCHE MINING ===
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_PIOCHE) return;
            cooldowns.put(uuid, now);

            ItemPioche pioche = (ItemPioche) tool.getItem();
            int multiplier = pioche.getDustMultiplier();

            tool.damageItem(1, player);
            world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                1.0F, 0.8F + rand.nextFloat() * 0.4F);
            player.swingArm(EnumHand.MAIN_HAND);

            double r = rand.nextDouble();

            if (multiplier <= 1) {
                // Pioche Fragmentee: always wall_dust + bonus
                spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1 + rand.nextInt(2)));
                if (r < 0.30)      spawnDrop(world, player, new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
                else if (r < 0.50) spawnDrop(world, player, new ItemStack(Items.FLINT, 1));
                else if (r < 0.65) spawnDrop(world, player, new ItemStack(Items.CLAY_BALL, 1));
            } else {
                // Pioche Renforcee: 60% grits, 5.5% compose, 34.5% wall_dust
                if (r < 0.15)       spawnDrop(world, player, new ItemStack(ModItems.IRON_GRIT, 1));
                else if (r < 0.30)  spawnDrop(world, player, new ItemStack(ModItems.COPPER_GRIT, 1));
                else if (r < 0.42)  spawnDrop(world, player, new ItemStack(ModItems.TIN_GRIT, 1));
                else if (r < 0.52)  spawnDrop(world, player, new ItemStack(Items.COAL, 1));
                else if (r < 0.60)  spawnDrop(world, player, new ItemStack(Items.REDSTONE, 1));
                else if (r < 0.655) spawnDrop(world, player, new ItemStack(ModItems.COMPOSE_A, 1));
                else                spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1));
            }

            player.addExhaustion(0.5F);

        } else {
            // === BARE HANDS ===
            if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN_BARE) return;
            cooldowns.put(uuid, now);

            world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
                0.7F, 0.6F + rand.nextFloat() * 0.3F);
            player.swingArm(EnumHand.MAIN_HAND);

            double r = rand.nextDouble();

            if (r < 0.20)      spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1));
            else if (r < 0.45) spawnDrop(world, player, new ItemStack(Items.STICK, 1));
            else if (r < 0.65) spawnDrop(world, player, new ItemStack(Blocks.PLANKS, 1));
            else if (r < 0.75) spawnDrop(world, player, new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
            // else: nothing

            player.addExhaustion(0.8F);
        }
    }

    private void spawnDrop(World world, EntityPlayer player, ItemStack stack) {
        EntityItem entity = new EntityItem(world,
            player.posX, player.posY + 0.5, player.posZ, stack);
        entity.setNoPickupDelay();
        world.spawnEntity(entity);
    }
}
