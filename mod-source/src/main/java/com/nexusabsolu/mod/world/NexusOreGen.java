package com.nexusabsolu.mod.world;

import com.nexusabsolu.mod.init.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class NexusOreGen implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                        IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        int dim = world.provider.getDimension();

        // Overworld
        if (dim == 0) {
            // Vossium -- rare, deep, Y 5-20
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.VOSSIUM_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                3, 5, 20, 2); // veinSize=3, minY=5, maxY=20, chances=2
        }

        // End
        if (dim == 1) {
            // Nexium -- End only, Y 10-60
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.NEXIUM_ORE.getDefaultState(),
                Blocks.END_STONE.getDefaultState(),
                4, 10, 60, 3);
        }

        // Compact Machine dimension (144)
        if (dim == 144) {
            // Claustrite -- Compact Machine dimension only
            // Spawns in the walls near the edges
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.CLAUSTRITE_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                2, 1, 15, 1);
        }
    }

    private void generateOre(World world, Random random, int chunkX, int chunkZ,
                            IBlockState ore, IBlockState replace,
                            int veinSize, int minY, int maxY, int chancesPerChunk) {
        WorldGenMinable gen = new WorldGenMinable(ore, veinSize,
            state -> state.getBlock() == replace.getBlock());

        for (int i = 0; i < chancesPerChunk; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int y = minY + random.nextInt(Math.max(1, maxY - minY));
            int z = chunkZ * 16 + random.nextInt(16);
            gen.generate(world, random, new BlockPos(x, y, z));
        }
    }
}
