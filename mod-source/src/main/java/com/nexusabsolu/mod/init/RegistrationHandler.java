package com.nexusabsolu.mod.init;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class RegistrationHandler {

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
        ModBlocks.registerItemBlocks(event);
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for (Item item : ModItems.ITEMS) {
            if (item instanceof IHasModel) {
                ((IHasModel) item).registerModels();
            }
        }
        for (Block block : ModBlocks.BLOCKS) {
            if (block instanceof IHasModel) {
                ((IHasModel) block).registerModels();
            }
        }
    }

    /**
     * v1.0.268 : remappe les anciens IDs furnace_invar vers furnace_invarium
     * pour ne pas perdre les fours deja poses dans les saves existants apres
     * le renommage.
     */
    @SubscribeEvent
    public static void onMissingBlockMappings(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> m : event.getAllMappings()) {
            if (m.key.toString().equals(Reference.MOD_ID + ":furnace_invar")) {
                m.remap(ModBlocks.FURNACE_INVARIUM);
            }
        }
    }

    @SubscribeEvent
    public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> m : event.getAllMappings()) {
            if (m.key.toString().equals(Reference.MOD_ID + ":furnace_invar")) {
                m.remap(Item.getItemFromBlock(ModBlocks.FURNACE_INVARIUM));
            }
        }
    }
}
