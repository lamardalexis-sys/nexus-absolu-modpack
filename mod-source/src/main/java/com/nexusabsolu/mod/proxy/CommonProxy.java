package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.GuiHandler;
import com.nexusabsolu.mod.scavenging.ScavengeEventHandler;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.world.NexusOreGen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ScavengeEventHandler());
        GameRegistry.registerTileEntity(TileCondenseur.class,
            new ResourceLocation(Reference.MOD_ID, "condenseur"));
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new NexusOreGen(), 3);
        NetworkRegistry.INSTANCE.registerGuiHandler(NexusAbsoluMod.instance, new GuiHandler());
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
