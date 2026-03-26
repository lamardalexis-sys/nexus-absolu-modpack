package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.blocks.BlockNexusOre;
import com.nexusabsolu.mod.blocks.BlockNexusWall;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

import java.util.ArrayList;
import java.util.List;

public class ModBlocks {
    public static final List<Block> BLOCKS = new ArrayList<>();

    // === SCAVENGING ===
    public static final BlockNexusWall NEXUS_WALL = new BlockNexusWall();

    // === CUSTOM ORES ===
    // Vossium --- Overworld rare, deep. Lie au Dr. Voss.
    public static final Block VOSSIUM_ORE = new BlockNexusOre(
        "vossium_ore", 4.0F, 2, 3, 7
    );

    // Nexium --- End only. Necessaire pour les fragments avances.
    public static final Block NEXIUM_ORE = new BlockNexusOre(
        "nexium_ore", 5.0F, 3, 5, 10
    );

    // Claustrite --- Compact Machine dimension exclusive.
    public static final Block CLAUSTRITE_ORE = new BlockNexusOre(
        "claustrite_ore", 3.0F, 1, 2, 5
    );

    public static void registerItemBlocks(RegistryEvent.Register<Block> event) {
        // ItemBlocks are registered in block constructors via ModItems.ITEMS.add()
    }
}
