package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.client.ClientMiningHandler;
import com.nexusabsolu.mod.render.TESRAutoScavenger;
import com.nexusabsolu.mod.render.TESRCondenseur;
import com.nexusabsolu.mod.render.TESRCondenseurT2;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileCondenseur.class, new TESRCondenseur());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCondenseurT2.class, new TESRCondenseurT2());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAutoScavenger.class, new TESRAutoScavenger());
        // v1.0.306 Archives Voss Sprint 1 polish : shell 3D du multiblock
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.nexusabsolu.mod.archives.tiles.TileArchiveController.class,
            new com.nexusabsolu.mod.archives.render.TESRArchiveController());
        MinecraftForge.EVENT_BUS.register(new ClientMiningHandler());
    }
}
