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

    // v1.0.302 (Archives Voss Sprint 1) : 2 fluides pour le cycle de refroidissement.
    //   EAU_VOSS_FROIDE : sortie du Compresseur, alimente les Archives
    //   EAU_VOSS_CHAUDE : sortie des Archives (a recycler via Compresseur)
    // Cycle ferme avec 50 mB de perte par cycle (force appoint d'eau vanilla).
    public static Fluid EAU_VOSS_FROIDE;
    public static Fluid EAU_VOSS_CHAUDE;

    public void preInit(FMLPreInitializationEvent event) {
        // Fluid registration
        FluidRegistry.enableUniversalBucket();
        DIARRHEE_FLUID = new Fluid("diarrhee_liquide",
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_still"),
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_flow"))
            .setDensity(1200).setViscosity(3000)
            .setColor(0xFF8B4513); // marron - teinte JEI + in-world + bucket universel
        FluidRegistry.registerFluid(DIARRHEE_FLUID);
        FluidRegistry.addBucketForFluid(DIARRHEE_FLUID);

        // v1.0.302 : Eau Voss Froide (bleu clair vif, utile)
        EAU_VOSS_FROIDE = new Fluid("eau_voss_froide",
            new ResourceLocation(Reference.MOD_ID, "blocks/eau_voss_froide_still"),
            new ResourceLocation(Reference.MOD_ID, "blocks/eau_voss_froide_flow"))
            .setDensity(1000).setViscosity(900)
            .setColor(0xFF64C8FF); // RGB 100, 200, 255 = bleu clair vif
        FluidRegistry.registerFluid(EAU_VOSS_FROIDE);
        FluidRegistry.addBucketForFluid(EAU_VOSS_FROIDE);

        // v1.0.302 : Eau Voss Chaude (bleu grise/ternie, a recycler)
        EAU_VOSS_CHAUDE = new Fluid("eau_voss_chaude",
            new ResourceLocation(Reference.MOD_ID, "blocks/eau_voss_chaude_still"),
            new ResourceLocation(Reference.MOD_ID, "blocks/eau_voss_chaude_flow"))
            .setDensity(1100).setViscosity(1200).setTemperature(373)
            .setColor(0xFF3C6482); // RGB 60, 100, 130 = bleu grise/ternie (eau chaude)
        FluidRegistry.registerFluid(EAU_VOSS_CHAUDE);
        FluidRegistry.addBucketForFluid(EAU_VOSS_CHAUDE);

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
        // v1.0.302 (Archives Voss Sprint 1) : new TileEntities
        GameRegistry.registerTileEntity(
            com.nexusabsolu.mod.archives.tiles.TileArchiveController.class,
            new ResourceLocation(Reference.MOD_ID, "archive_controller"));
        GameRegistry.registerTileEntity(
            com.nexusabsolu.mod.archives.tiles.TileCompresseurEau.class,
            new ResourceLocation(Reference.MOD_ID, "compresseur_eau"));
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
