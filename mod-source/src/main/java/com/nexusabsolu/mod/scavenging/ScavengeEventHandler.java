package com.nexusabsolu.mod.scavenging;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.items.ItemPioche;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

public class ScavengeEventHandler {

    private static final Random rand = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        IBlockState state = event.getState();
        Block block = state.getBlock();

        // Check if it's a Nexus Wall or a Compact Machine wall
        boolean isNexusWall = (block == ModBlocks.NEXUS_WALL);
        boolean isCMWall = block.getRegistryName() != null &&
            block.getRegistryName().toString().equals("compactmachines3:wall");

        if (!isNexusWall && !isCMWall) return;

        EntityPlayer player = event.getHarvester();
        if (player == null) return;

        // Clear default drops
        event.getDrops().clear();

        ItemStack tool = player.getHeldItemMainhand();
        int dustMultiplier = 1;
        int harvestLevel = -1;

        // Check for custom pioche
        if (!tool.isEmpty() && tool.getItem() instanceof ItemPioche) {
            ItemPioche pioche = (ItemPioche) tool.getItem();
            dustMultiplier = pioche.getDustMultiplier();
            harvestLevel = pioche.getHarvestLevel(tool, "pickaxe", player, state);
        } else if (!tool.isEmpty() && tool.getItem().getToolClasses(tool).contains("pickaxe")) {
            harvestLevel = tool.getItem().getHarvestLevel(tool, "pickaxe", player, state);
        }

        // Always drop wall dust (multiplied by pioche bonus)
        event.getDrops().add(new ItemStack(ModItems.WALL_DUST, 2 * dustMultiplier));

        // Fist or no pickaxe
        if (harvestLevel < 0) {
            return; // just wall dust
        }

        double r = rand.nextDouble();

        // Wood pickaxe / Pioche Fragmentee (harvest 0)
        if (harvestLevel == 0) {
            if (r < 0.40)      addDrop(event, ModItems.COBBLESTONE_FRAGMENT, 1 * dustMultiplier);
            else if (r < 0.70) addDrop(event, ModItems.WALL_DUST, 2 * dustMultiplier);
            else if (r < 0.90) addDrop(event, Items.FLINT, 1);
            else               addDrop(event, Items.CLAY_BALL, 1);
        }
        // Stone pickaxe (harvest 1)
        else if (harvestLevel == 1) {
            if (r < 0.35)      addDrop(event, ModItems.IRON_GRIT, 1 * dustMultiplier);
            else if (r < 0.65) addDrop(event, ModItems.COPPER_GRIT, 1 * dustMultiplier);
            else if (r < 0.90) addDrop(event, ModItems.TIN_GRIT, 1 * dustMultiplier);
            else               addDrop(event, Items.COAL, 1);
        }
        // Iron pickaxe / Pioche Renforcee (harvest 2)
        else if (harvestLevel == 2) {
            if (r < 0.30)      addDrop(event, ModItems.SILVER_GRIT, 1 * dustMultiplier);
            else if (r < 0.60) addDrop(event, ModItems.NICKEL_GRIT, 1 * dustMultiplier);
            else if (r < 0.85) addDrop(event, ModItems.LEAD_GRIT, 1 * dustMultiplier);
            else               addDrop(event, Items.REDSTONE, 1);
        }
        // Diamond pickaxe (harvest 3+)
        else {
            if (r < 0.30)      addDrop(event, ModItems.GOLD_GRIT, 1 * dustMultiplier);
            else if (r < 0.55) addDrop(event, ModItems.OSMIUM_GRIT, 1 * dustMultiplier);
            else if (r < 0.75) addDrop(event, ModItems.DIAMOND_FRAGMENT, 1);
            else if (r < 0.90) addDrop(event, Items.REDSTONE, 3);
            else               addDrop(event, ModItems.ENDER_PEARL_FRAGMENT, 1);
        }
    }

    private void addDrop(BlockEvent.HarvestDropsEvent event, Item item, int count) {
        event.getDrops().add(new ItemStack(item, count));
    }
}
