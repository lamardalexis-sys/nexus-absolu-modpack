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
        // v1.0.325 (Age 4) : overlay teinte ecran pendant injection Manifold
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.client.ManifoldOverlayHandler());
        // v1.0.326 (Age 4) : hallucinations entites — mobs en blocs aleatoires
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.client.ManifoldHallucinationHandler());
        // v1.0.327 (Age 4) : shader post-process Mandelbulb raymarching
        MinecraftForge.EVENT_BUS.register(
            new com.nexusabsolu.mod.client.ManifoldShaderHandler());

        // StateMapper custom pour le bloc fluide Diarrhee (toutes les variantes "level=X"
        // pointent vers la variante "normal" du blockstate, qui utilise forge:fluid).
        if (CommonProxy.DIARRHEE_FLUID_BLOCK != null) {
            net.minecraft.client.renderer.block.statemap.StateMap mapper =
                new net.minecraft.client.renderer.block.statemap.StateMap.Builder()
                    .ignore(net.minecraftforge.fluids.BlockFluidBase.LEVEL).build();
            net.minecraftforge.client.model.ModelLoader.setCustomStateMapper(
                CommonProxy.DIARRHEE_FLUID_BLOCK, mapper);
            // Item model: pointe vers la variante "normal" du blockstate (= forge:fluid)
            net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(
                CommonProxy.DIARRHEE_FLUID_BLOCK);
            if (item != null) {
                net.minecraft.client.renderer.block.model.ModelResourceLocation mrl =
                    new net.minecraft.client.renderer.block.model.ModelResourceLocation(
                        CommonProxy.DIARRHEE_FLUID_BLOCK.getRegistryName(), "normal");
                net.minecraft.client.renderer.ItemMeshDefinition meshDef = stack -> mrl;
                net.minecraftforge.client.model.ModelLoader.setCustomMeshDefinition(item, meshDef);
                net.minecraft.client.renderer.block.model.ModelBakery.registerItemVariants(item, mrl);
            }
        }
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
