package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.GuiHandler;
import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.scavenging.ScavengeEventHandler;
import com.nexusabsolu.mod.world.DimensionChangeHandler;
import com.nexusabsolu.mod.tiles.TileAtelier;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileCondenseurEnergy;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileConvertisseur;
import com.nexusabsolu.mod.tiles.TileEnergyInput;
import com.nexusabsolu.mod.tiles.TileFluidInput;
import com.nexusabsolu.mod.tiles.TilePortalVoss;
import com.nexusabsolu.mod.tiles.TileItemInput;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import com.nexusabsolu.mod.tiles.TileMachineHumaine;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import com.nexusabsolu.mod.world.NexusOreGen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

    public static Fluid DIARRHEE_FLUID;
    public static net.minecraft.block.Block DIARRHEE_FLUID_BLOCK;

    public void preInit(FMLPreInitializationEvent event) {
        // Fluid registration
        FluidRegistry.enableUniversalBucket();
        DIARRHEE_FLUID = new Fluid("diarrhee_liquide",
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_still"),
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_flow"))
            .setDensity(1200).setViscosity(3000)
            .setColor(0xFFB8843E); // marron clair-fonce (cafe au lait sale)
        FluidRegistry.registerFluid(DIARRHEE_FLUID);
        FluidRegistry.addBucketForFluid(DIARRHEE_FLUID);

        // Bloc fluide placeable (le seau de Diarrhee place le bloc au sol)
        DIARRHEE_FLUID_BLOCK = new com.nexusabsolu.mod.blocks.BlockFluidDiarrhee(DIARRHEE_FLUID);
        net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS
            .register(DIARRHEE_FLUID_BLOCK);
        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS
            .register(new net.minecraft.item.ItemBlock(DIARRHEE_FLUID_BLOCK)
                .setRegistryName(DIARRHEE_FLUID_BLOCK.getRegistryName()));

        NexusPacketHandler.init();
        MinecraftForge.EVENT_BUS.register(new ScavengeEventHandler());
        MinecraftForge.EVENT_BUS.register(new DimensionChangeHandler());
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.events.FurnaceCraftTransmissionHandler());
        // v1.0.297 : bloque l'interaction sur les 6 furnaces Mystical Agriculture
        // (ils restent craftables pour chaine progression -> Infinite Furnace Nexus,
        // mais le right-click pour ouvrir GUI est cancel).
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.events.MAFurnaceBlockerHandler());
        // v1.0.316 : effet "vomi" + nausee + faim quand le joueur est dans
        // le fluide Diarrhee Liquide
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.events.DiarrheeImmersionHandler());
        // v1.0.324 : PSDDescentHandler (override clic-droit PSD) DESACTIVE
        // car il interferait avec le PSD vanilla. Remplace par l'item
        // dedie ItemPlongeurVoss en v1.0.325.
        // MinecraftForge.EVENT_BUS.register(
        //     new com.nexusabsolu.mod.events.PSDDescentHandler());
        // v1.0.327 : ecrit le NBT parent_room_id sur les blocs CM places
        // en DIM 144 (pour navigation matryoshka via Localisateur Voss)
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.events.CMPlacementHandler());
        // v1.0.325 (Age 4) : Cartouche Manifold — gestion phases + particules
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.events.ManifoldEffectHandler());
        GameRegistry.registerTileEntity(TileCondenseur.class,
            new ResourceLocation(Reference.MOD_ID, "condenseur"));
        GameRegistry.registerTileEntity(TileCondenseurEnergy.class,
            new ResourceLocation(Reference.MOD_ID, "condenseur_energy"));
        GameRegistry.registerTileEntity(TileAtelier.class,
            new ResourceLocation(Reference.MOD_ID, "atelier_voss"));
        GameRegistry.registerTileEntity(TileConvertisseur.class,
            new ResourceLocation(Reference.MOD_ID, "convertisseur_voss"));
        GameRegistry.registerTileEntity(TileCondenseurT2.class,
            new ResourceLocation(Reference.MOD_ID, "condenseur_t2"));
        GameRegistry.registerTileEntity(TileItemInput.class,
            new ResourceLocation(Reference.MOD_ID, "item_input"));
        GameRegistry.registerTileEntity(TileItemOutput.class,
            new ResourceLocation(Reference.MOD_ID, "item_output"));
        GameRegistry.registerTileEntity(TileAutoScavenger.class,
            new ResourceLocation(Reference.MOD_ID, "auto_scavenger"));
        GameRegistry.registerTileEntity(TileEnergyInput.class,
            new ResourceLocation(Reference.MOD_ID, "energy_input"));
        GameRegistry.registerTileEntity(TileFluidInput.class,
            new ResourceLocation(Reference.MOD_ID, "fluid_input"));
        GameRegistry.registerTileEntity(TilePortalVoss.class,
            new ResourceLocation(Reference.MOD_ID, "portal_voss"));
        GameRegistry.registerTileEntity(TileMachineHumaine.class,
            new ResourceLocation(Reference.MOD_ID, "machine_humaine"));
        GameRegistry.registerTileEntity(TileMachineKRDA.class,
            new ResourceLocation(Reference.MOD_ID, "machine_krda"));
        GameRegistry.registerTileEntity(com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus.class,
            new ResourceLocation(Reference.MOD_ID, "furnace_nexus"));
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new NexusOreGen(), 3);
        NetworkRegistry.INSTANCE.registerGuiHandler(NexusAbsoluMod.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
