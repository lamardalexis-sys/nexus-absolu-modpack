package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.blocks.BlockCompose;
import com.nexusabsolu.mod.blocks.BlockNexusOre;
import com.nexusabsolu.mod.blocks.BlockNexusWall;
import com.nexusabsolu.mod.blocks.BlockNexusWallT2;
import com.nexusabsolu.mod.blocks.BlockObsidienneVoss;
import com.nexusabsolu.mod.blocks.BlockPortailSortieActif;
import com.nexusabsolu.mod.blocks.BlockTutuosssOre;
import com.nexusabsolu.mod.blocks.BlockVossiumII;
import com.nexusabsolu.mod.blocks.BlockVossiumIII;
import com.nexusabsolu.mod.blocks.BlockVossiumIV;
import com.nexusabsolu.mod.blocks.machines.BlockAtelier;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseur;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurFormed;
import com.nexusabsolu.mod.blocks.machines.BlockAutoScavenger;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurT2;
import com.nexusabsolu.mod.blocks.machines.BlockCondenseurT2Wall;
import com.nexusabsolu.mod.blocks.machines.BlockConvertisseur;
import com.nexusabsolu.mod.blocks.machines.BlockEcranControle;
import com.nexusabsolu.mod.blocks.machines.BlockMachineHumaine;
import com.nexusabsolu.mod.blocks.machines.BlockMachineKRDA;
import com.nexusabsolu.mod.blocks.machines.BlockEnergyInput;
import com.nexusabsolu.mod.blocks.machines.BlockFluidInput;
import com.nexusabsolu.mod.blocks.machines.BlockMachineController;
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
    public static final Block TUTUOSSS_ORE = new BlockTutuosssOre();

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
    public static final BlockMachineHumaine MACHINE_HUMAINE = new BlockMachineHumaine();
    public static final BlockMachineKRDA MACHINE_KRDA = new BlockMachineKRDA();
    public static final BlockEnergyInput ENERGY_INPUT = new BlockEnergyInput();
    public static final BlockFluidInput FLUID_INPUT = new BlockFluidInput();
    public static final BlockVossiumII VOSSIUM_II_BLOCK = new BlockVossiumII();
    public static final BlockVossiumIII VOSSIUM_III_BLOCK = new BlockVossiumIII();
    public static final BlockVossiumIV VOSSIUM_IV_BLOCK = new BlockVossiumIV();

    // === PORTAIL VOSS ===
    public static final BlockNexusWallT2 NEXUS_WALL_T2 = new BlockNexusWallT2();
    public static final BlockEcranControle ECRAN_CONTROLE = new BlockEcranControle();

    // === PORTAIL DE SORTIE TF (Age 2 Phase 1 -> Phase 2) ===
    public static final BlockObsidienneVoss OBSIDIENNE_VOSS = new BlockObsidienneVoss();
    public static final BlockPortailSortieActif PORTAIL_SORTIE_ACTIF = new BlockPortailSortieActif();

    // === FURNACES NEXUS (T1-T8 implementes ; T9 Infinite reste multiblock a faire) ===
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_IRON =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.IRON);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_GOLD =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.GOLD);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_INVARIUM =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.INVARIUM);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_EMERADIC =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.EMERADIC);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_VOSSIUM_IV =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.VOSSIUM_IV);
    // v1.0.272 : nouveaux tiers phase avancee
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_DARK_ASTRAL =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.DARK_ASTRAL);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_GAIA_LUDICRITE =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.GAIA_LUDICRITE);
    public static final com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus FURNACE_PALLANUTRO =
        new com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus(
            com.nexusabsolu.mod.tiles.furnaces.FurnaceTier.PALLANUTRO);

    public static void registerItemBlocks(RegistryEvent.Register<Block> event) {
    }

    // === CONTROLEURS DE MULTIBLOCS AGE 4 ===
    // Migres depuis ContentTweaker (Age4_L*_Multiblocs.zs).
    // Facade visuelle : MM forme ses structures autour de son propre
    // blockcontroller, aucun machinery JSON ne declare de binding custom.
    public static final Block ALAMBIC_MANAIC_CONTROLLER = new BlockMachineController(
        "alambic_manaic_controller", 6.0F, 20.0F, 2, 10);
    public static final Block AQUA_REGIA_CELL_CONTROLLER = new BlockMachineController(
        "aqua_regia_cell_controller", 6.0F, 18.0F, 3, 5);
    public static final Block AROMATIC_REACTOR_CONTROLLER = new BlockMachineController(
        "aromatic_reactor_controller", 6.0F, 20.0F, 3, 10);
    public static final Block BIOREACTEUR_CONTROLLER = new BlockMachineController(
        "bioreacteur_controller", 8.0F, 20.0F, 3, 8);
    public static final Block CK_CELL_CONTROLLER = new BlockMachineController(
        "ck_cell_controller", 7.0F, 20.0F, 3, 6);
    public static final Block CONTACT_TOWER_CONTROLLER = new BlockMachineController(
        "contact_tower_controller", 7.0F, 22.0F, 3, 6);
    public static final Block CRYO_DISTILLATEUR_CONTROLLER = new BlockMachineController(
        "cryo_distillateur_controller", 4.0F, 8.0F, 2, 8);
    public static final Block CUMENE_REACTOR_CONTROLLER = new BlockMachineController(
        "cumene_reactor_controller", 6.0F, 18.0F, 3, 8);
    public static final Block CYCLISATEUR_STELLAIRE_CONTROLLER = new BlockMachineController(
        "cyclisateur_stellaire_controller", 8.0F, 25.0F, 3, 12);
    public static final Block ELECTRIC_FURNACE_CONTROLLER = new BlockMachineController(
        "electric_furnace_controller", 8.0F, 25.0F, 3, 14);
    public static final Block EVAPORATOR_CONTROLLER = new BlockMachineController(
        "evaporator_controller", 4.0F, 10.0F, 2, 6);
    public static final Block FERMENTER_CONTROLLER = new BlockMachineController(
        "fermenter_controller", 4.0F, 10.0F, 2, 3);
    public static final Block FLUORITE_CELL_CONTROLLER = new BlockMachineController(
        "fluorite_cell_controller", 6.0F, 18.0F, 3, 5);
    public static final Block GAMMA_FORGE_CONTROLLER = new BlockMachineController(
        "gamma_forge_controller", 8.0F, 30.0F, 3, 10);
    public static final Block HABER_REACTOR_CONTROLLER = new BlockMachineController(
        "haber_reactor_controller", 10.0F, 40.0F, 3, 12);
    public static final Block HALL_HEROULT_CELL_CONTROLLER = new BlockMachineController(
        "hall_heroult_cell_controller", 8.0F, 25.0F, 3, 14);
    public static final Block HDS_TOWER_CONTROLLER = new BlockMachineController(
        "hds_tower_controller", 7.0F, 20.0F, 3, 6);
    public static final Block KROLL_REACTOR_CONTROLLER = new BlockMachineController(
        "kroll_reactor_controller", 7.0F, 22.0F, 3, 8);
    public static final Block LIT_CHAMBER_CONTROLLER = new BlockMachineController(
        "lit_chamber_controller", 7.0F, 25.0F, 3, 8);
    public static final Block MANA_ENCHANTER_CONTROLLER = new BlockMachineController(
        "mana_enchanter_controller", 5.0F, 15.0F, 2, 14);
    public static final Block MELANGEUR_CRYOGENIQUE_CONTROLLER = new BlockMachineController(
        "melangeur_cryogenique_controller", 10.0F, 40.0F, 3, 4);
    public static final Block OSMOSE_INVERSE_CONTROLLER = new BlockMachineController(
        "osmose_inverse_controller", 5.0F, 15.0F, 2, 4);
    public static final Block OSTWALD_TOWER_CONTROLLER = new BlockMachineController(
        "ostwald_tower_controller", 7.0F, 22.0F, 3, 8);
    public static final Block SOXHLET_EXTRACTOR_CONTROLLER = new BlockMachineController(
        "soxhlet_extractor_controller", 5.0F, 15.0F, 2, 4);
    public static final Block THERMAL_CRACKER_CONTROLLER = new BlockMachineController(
        "thermal_cracker_controller", 7.0F, 20.0F, 3, 12);
    public static final Block TRITIUM_BREEDER_CONTROLLER = new BlockMachineController(
        "tritium_breeder_controller", 8.0F, 30.0F, 3, 8);
    public static final Block VACUUM_CHAMBER_CONTROLLER = new BlockMachineController(
        "vacuum_chamber_controller", 5.0F, 15.0F, 2, 4);
}
