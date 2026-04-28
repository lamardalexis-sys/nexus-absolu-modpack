package com.nexusabsolu.mod.proxy;

import com.nexusabsolu.mod.client.ClientMiningHandler;
import com.nexusabsolu.mod.client.NexusLangInjector;
import com.nexusabsolu.mod.render.TESRAutoScavenger;
import com.nexusabsolu.mod.render.TESRCondenseur;
import com.nexusabsolu.mod.render.TESRCondenseurT2;
import com.nexusabsolu.mod.tiles.TileAutoScavenger;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.bindTileEntitySpecialRenderer(TileCondenseur.class, new TESRCondenseur());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCondenseurT2.class, new TESRCondenseurT2());
        ClientRegistry.bindTileEntitySpecialRenderer(TileAutoScavenger.class, new TESRAutoScavenger());
        MinecraftForge.EVENT_BUS.register(new ClientMiningHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // Bulldozer: force le chargement des traductions du mod dans LanguageMap.
        // Voir NexusLangInjector pour le contexte du bug.
        NexusLangInjector injector = new NexusLangInjector();
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                .registerReloadListener(injector);
        // Inject une fois immediatement (le listener s'occupe des reloads ulterieurs)
        NexusLangInjector.injectAll();
    }
}
