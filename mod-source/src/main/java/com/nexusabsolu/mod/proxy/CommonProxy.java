package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.GuiHandler;
import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.scavenging.ScavengeEventHandler;
import com.nexusabsolu.mod.tiles.TileAtelier;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileCondenseurEnergy;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.tiles.TileConvertisseur;
import com.nexusabsolu.mod.tiles.TileEnergyInput;
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

    public void preInit(FMLPreInitializationEvent event) {
        // Fluid registration
        FluidRegistry.enableUniversalBucket();
        DIARRHEE_FLUID = new Fluid("diarrhee_liquide",
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_still"),
            new ResourceLocation(Reference.MOD_ID, "blocks/diarrhee_flow"))
            .setDensity(1200).setViscosity(3000);
        FluidRegistry.registerFluid(DIARRHEE_FLUID);
        FluidRegistry.addBucketForFluid(DIARRHEE_FLUID);

        NexusPacketHandler.init();
        MinecraftForge.EVENT_BUS.register(new ScavengeEventHandler());
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
        GameRegistry.registerTileEntity(TileMachineHumaine.class,
            new ResourceLocation(Reference.MOD_ID, "machine_humaine"));
        GameRegistry.registerTileEntity(TileMachineKRDA.class,
            new ResourceLocation(Reference.MOD_ID, "machine_krda"));
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new NexusOreGen(), 3);
        NetworkRegistry.INSTANCE.registerGuiHandler(NexusAbsoluMod.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
