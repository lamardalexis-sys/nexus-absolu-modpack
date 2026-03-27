package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.blocks.BlockNexusOre;
import com.nexusabsolu.mod.blocks.BlockNexusWall;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseur;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurFormed;
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

    // === MACHINES ===
    public static final BlockCondenseur CONDENSEUR = new BlockCondenseur();
    public static final BlockCondenseurFormed CONDENSEUR_FORMED = new BlockCondenseurFormed();

    public static void registerItemBlocks(RegistryEvent.Register<Block> event) {
    }
}
