package com.nexusabsolu.mod.scavenging;

import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.minecraft.item.Item;
import java.util.Random;

/**
 * The Scavenging System -- core mechanic of Nexus Absolu.
 * When a Nexus Wall is broken, drops depend on the tool used.
 * Different pickaxe harvest levels yield different grits.
 */
public class ScavengeEventHandler {

    private static final Random rand = new Random();

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.HarvestDropsEvent event) {
        IBlockState state = event.getState();
        if (state.getBlock() != ModBlocks.NEXUS_WALL) return;

        EntityPlayer player = event.getHarvester();
        if (player == null) return;

        // Clear default drops
        event.getDrops().clear();

        // Always drop wall dust (to re-craft nexus walls)
        event.getDrops().add(new ItemStack(ModItems.WALL_DUST, 2));

        ItemStack tool = player.getHeldItemMainhand();
        int harvestLevel = -1;

        if (!tool.isEmpty() && tool.getItem().getToolClasses(tool).contains("pickaxe")) {
            harvestLevel = tool.getItem().getHarvestLevel(tool, "pickaxe", player, state);
        }

        // Fist or no pickaxe
        if (harvestLevel < 0) {
            event.getDrops().add(new ItemStack(ModItems.WALL_DUST, 2));
            return;
        }

        double r = rand.nextDouble();

        // Wood pickaxe (harvest 0)
        if (harvestLevel == 0) {
            if (r < 0.40)      addDrop(event, ModItems.COBBLESTONE_FRAGMENT, 1);
            else if (r < 0.70) addDrop(event, ModItems.WALL_DUST, 2);
            else if (r < 0.90) addDrop(event, Items.FLINT, 1);
            else               addDrop(event, Items.CLAY_BALL, 1);
        }
        // Stone pickaxe (harvest 1)
        else if (harvestLevel == 1) {
            if (r < 0.35)      addDrop(event, ModItems.IRON_GRIT, 1);
            else if (r < 0.65) addDrop(event, ModItems.COPPER_GRIT, 1);
            else if (r < 0.90) addDrop(event, ModItems.TIN_GRIT, 1);
            else               addDrop(event, Items.COAL, 1);
        }
        // Iron pickaxe (harvest 2)
        else if (harvestLevel == 2) {
            if (r < 0.30)      addDrop(event, ModItems.SILVER_GRIT, 1);
            else if (r < 0.60) addDrop(event, ModItems.NICKEL_GRIT, 1);
            else if (r < 0.85) addDrop(event, ModItems.LEAD_GRIT, 1);
            else               addDrop(event, Items.REDSTONE, 1);
        }
        // Diamond pickaxe (harvest 3+)
        else {
            if (r < 0.30)      addDrop(event, ModItems.GOLD_GRIT, 1);
            else if (r < 0.55) addDrop(event, ModItems.OSMIUM_GRIT, 1);
            else if (r < 0.75) addDrop(event, ModItems.DIAMOND_FRAGMENT, 1);
            else if (r < 0.90) addDrop(event, Items.REDSTONE, 3);
            else               addDrop(event, ModItems.ENDER_PEARL_FRAGMENT, 1);
        }
    }

    private void addDrop(BlockEvent.HarvestDropsEvent event, Item item, int count) {
        event.getDrops().add(new ItemStack(item, count));
    }
}
