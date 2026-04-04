package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.blocks.BlockCompose;
import com.nexusabsolu.mod.blocks.BlockNexusOre;
import com.nexusabsolu.mod.blocks.BlockNexusWall;
import com.nexusabsolu.mod.blocks.BlockVossiumII;
import com.nexusabsolu.mod.blocks.machines.BlockAtelier;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseur;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurFormed;
import com.nexusabsolu.mod.blocks.machines.BlockAutoScavenger;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurT2;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurT2Wall;
import com.nexusabsolu.mod.blocks.machines.BlockConvertisseur;
import com.nexusabsolu.mod.blocks.machines.BlockEnergyInput;
import com.nexusabsolu.mod.blocks.machines.BlockItemInput;
import com.nexusabsolu.mod.blocks.machines.BlockItemOutput;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();

    // === SCAVENGING ===
    public static final BlockNexusWall NEXUS_WALL = new BlockNexusWall();

    // === CUSTOM ORES ===
    public static final Block VOSSIUM_ORE = new BlockNexusOre("vossium_ore", 4.0F, 2, 3, 7);
    public static final Block NEXIUM_ORE = new BlockNexusOre("nexium_ore", 5.0F, 3, 5, 10);
    public static final Block CLAUSTRITE_ORE = new BlockNexusOre("claustrite_ore", 3.0F, 1, 2, 5);

    // === COMPOSE BLOCKS (energy source, sparkle like redstone) ===
    public static final Block COMPOSE_BLOCK_A = new BlockCompose("compose_block_a", "A", 25, 0.8F, 0.3F, 1.0F);
    public static final Block COMPOSE_BLOCK_B = new BlockCompose("compose_block_b", "B", 75, 0.7F, 0.6F, 0.9F);
    public static final Block COMPOSE_BLOCK_C = new BlockCompose("compose_block_c", "C", 150, 0.5F, 0.8F, 0.7F);
    public static final Block COMPOSE_BLOCK_D = new BlockCompose("compose_block_d", "D", 300, 0.3F, 0.9F, 0.5F);
    public static final Block COMPOSE_BLOCK_E = new BlockCompose("compose_block_e", "E", 500, 0.2F, 1.0F, 0.3F);

    // === MACHINES ===
    public static final BlockCondenseur CONDENSEUR = new BlockCondenseur();
    public static final BlockCondenseurFormed CONDENSEUR_FORMED = new BlockCondenseurFormed();
    public static final BlockAtelier ATELIER = new BlockAtelier();
    public static final BlockConvertisseur CONVERTISSEUR = new BlockConvertisseur();

    // === CONDENSEUR T2 (3x3x3) ===
    public static final BlockCondenseurT2 CONDENSEUR_T2 = new BlockCondenseurT2();
    public static final BlockCondenseurT2Wall CONDENSEUR_T2_WALL = new BlockCondenseurT2Wall();
    public static final BlockItemInput ITEM_INPUT = new BlockItemInput();
    public static final BlockItemOutput ITEM_OUTPUT = new BlockItemOutput();
    public static final BlockAutoScavenger AUTO_SCAVENGER = new BlockAutoScavenger();
    public static final BlockEnergyInput ENERGY_INPUT = new BlockEnergyInput();
    public static final BlockVossiumII VOSSIUM_II_BLOCK = new BlockVossiumII();

    public static void registerItemBlocks(RegistryEvent.Register<Block> event) {
    }
}
