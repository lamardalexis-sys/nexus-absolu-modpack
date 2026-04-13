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

        // === OVERWORLD: Tutuosss Ore (force le minage manuel pour Age 2+) ===
        // Rarete comparable au diamant: vein 6 max, Y 5-16, 1 chance/chunk
        if (dim == 0) {
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.TUTUOSSS_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                6, 5, 16, 1);
            return;
        }

        // Only spawn on planets (not overworld, nether, end, or compact machines)
        // Galacticraft: Moon=-28, Mars=-29, Venus=-31, Asteroids=-30
        // Advanced Rocketry: typically dim 100+
        // Skip vanilla dims and compact machines (144)
        if (dim == -1 || dim == 1 || dim == 144) return;

        // Vossium -- Moon & Mars, rare, deep
        if (dim == -28 || dim == -29) {
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.VOSSIUM_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                4, 5, 40, 3);
        }

        // Nexium -- Venus & Asteroids, very rare
        if (dim == -31 || dim == -30) {
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.NEXIUM_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                3, 10, 60, 2);
        }

        // Claustrite -- Advanced Rocketry planets (dim 100+)
        if (dim >= 100 && dim != 144) {
            generateOre(world, random, chunkX, chunkZ,
                ModBlocks.CLAUSTRITE_ORE.getDefaultState(),
                Blocks.STONE.getDefaultState(),
                2, 5, 30, 2);
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
