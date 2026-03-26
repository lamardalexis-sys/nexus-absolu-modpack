package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

import java.util.Random;

public class BlockNexusWall extends Block implements IHasModel {

    public BlockNexusWall() {
        super(Material.ROCK);
        setTranslationKey("nexus_wall");
        setRegistryName(Reference.MOD_ID, "nexus_wall");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(2.5F);
        setResistance(10.0F);
        setSoundType(SoundType.STONE);
        setHarvestLevel("pickaxe", 0);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    // No normal drops — the ScavengeEventHandler handles drops
    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    // Particle effect when mined
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            // Spawn crack particles
            for (int i = 0; i < 5; i++) {
                double x = pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                double z = pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.8;
                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, x, y, z,
                    0, 0, 0, Block.getStateId(state));
            }
        }
        return false;
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
