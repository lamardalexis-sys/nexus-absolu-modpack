package com.nexusabsolu.mod.scavenging;

import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.ItemPioche;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
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
    private static final long COOLDOWN_MS = 500L;

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

        // Must hold a custom pioche
        ItemStack tool = player.getHeldItem(EnumHand.MAIN_HAND);
        if (tool.isEmpty() || !(tool.getItem() instanceof ItemPioche)) return;

        // Cooldown check
        UUID uuid = player.getUniqueID();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid)) {
            if (now - cooldowns.get(uuid) < COOLDOWN_MS) return;
        }
        cooldowns.put(uuid, now);

        ItemPioche pioche = (ItemPioche) tool.getItem();
        int multiplier = pioche.getDustMultiplier();

        // Damage the tool
        tool.damageItem(1, player);

        // Play sound
        world.playSound(null, pos, SoundEvents.BLOCK_STONE_HIT, SoundCategory.BLOCKS,
            1.0F, 0.8F + rand.nextFloat() * 0.4F);

        // Swing arm
        player.swingArm(EnumHand.MAIN_HAND);

        // Always drop wall_dust
        spawnDrop(world, player, new ItemStack(ModItems.WALL_DUST, 1 + rand.nextInt(2) * multiplier));

        // Bonus drops based on pioche tier
        double r = rand.nextDouble();

        if (multiplier <= 1) {
            // Pioche Fragmentee (wood tier)
            if (r < 0.30)      spawnDrop(world, player, new ItemStack(ModItems.COBBLESTONE_FRAGMENT, 1));
            else if (r < 0.50) spawnDrop(world, player, new ItemStack(Items.FLINT, 1));
            else if (r < 0.65) spawnDrop(world, player, new ItemStack(Items.CLAY_BALL, 1));
            // else: nothing extra
        } else {
            // Pioche Renforcee (iron tier)
            if (r < 0.25)      spawnDrop(world, player, new ItemStack(ModItems.IRON_GRIT, 1));
            else if (r < 0.45) spawnDrop(world, player, new ItemStack(ModItems.COPPER_GRIT, 1));
            else if (r < 0.60) spawnDrop(world, player, new ItemStack(ModItems.TIN_GRIT, 1));
            else if (r < 0.75) spawnDrop(world, player, new ItemStack(Items.COAL, 1));
            else if (r < 0.85) spawnDrop(world, player, new ItemStack(Items.REDSTONE, 1));
            // else: nothing extra
        }

        // Add exhaustion (hunger cost)
        player.addExhaustion(0.5F);
    }

    private void spawnDrop(World world, EntityPlayer player, ItemStack stack) {
        EntityItem entity = new EntityItem(world,
            player.posX, player.posY + 0.5, player.posZ, stack);
        entity.setNoPickupDelay();
        world.spawnEntity(entity);
    }
}
