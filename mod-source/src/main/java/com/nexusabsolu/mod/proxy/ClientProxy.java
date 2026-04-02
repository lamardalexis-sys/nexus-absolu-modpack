package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.client.ClientMiningHandler;
import com.nexusabsolu.mod.render.TESRCondenseur;
import com.nexusabsolu.mod.render.TESRCondenseurT2;
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
        MinecraftForge.EVENT_BUS.register(new ClientMiningHandler());
    }
}
