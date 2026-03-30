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
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockCompose extends Block implements IHasModel {

    private final String tier;
    private final int baseRF;
    private final float particleR, particleG, particleB;

    public BlockCompose(String name, String tier, int baseRF,
                        float pR, float pG, float pB) {
        super(Material.ROCK);
        this.tier = tier;
        this.baseRF = baseRF;
        this.particleR = pR;
        this.particleG = pG;
        this.particleB = pB;
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(3.0F);
        setResistance(15.0F);
        setSoundType(SoundType.GLASS);
        setLightLevel(0.5F);
        setHarvestLevel("pickaxe", 1);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    public String getTier() { return tier; }
    public int getBaseRF() { return baseRF; }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        // Sparkle like redstone ore
        for (int i = 0; i < 3; i++) {
            double px = pos.getX() + rand.nextDouble();
            double py = pos.getY() + rand.nextDouble();
            double pz = pos.getZ() + rand.nextDouble();
            world.spawnParticle(EnumParticleTypes.REDSTONE,
                px, py, pz, particleR, particleG, particleB);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
