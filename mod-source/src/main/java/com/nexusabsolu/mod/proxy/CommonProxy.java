package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.scavenging.ScavengeEventHandler;
import com.nexusabsolu.mod.world.NexusOreGen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ScavengeEventHandler());
    }

    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new NexusOreGen(), 3);
    }

    public void postInit(FMLPostInitializationEvent event) {
    }
}
