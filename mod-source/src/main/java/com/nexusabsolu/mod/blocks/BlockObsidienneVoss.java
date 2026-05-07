package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;

/**
 * Obsidienne Voss - frame block for the Age 2 Phase 1 -> Phase 2 portal.
 *
 * Forms the 6x5 vertical frame (interior 4x3) that the player must build
 * inside the Twilight Forest to escape towards DIM 145 ("the overworld
 * simulation"). When activated by an ItemCatalyseurSortie, the interior
 * is filled with BlockPortailSortieActif blocks.
 *
 * Resistant to creeper/wither explosions (like vanilla obsidian).
 *
 * Sprint 3 - v1.0.350+
 */
public class BlockObsidienneVoss extends Block implements IHasModel {

    public BlockObsidienneVoss() {
        super(Material.ROCK);
        setUnlocalizedName("obsidienne_voss");
        setRegistryName(Reference.MOD_ID, "obsidienne_voss");
        setHardness(50.0F);       // Same as vanilla obsidian
        setResistance(2000.0F);   // Same as vanilla obsidian
        setSoundType(SoundType.STONE);
        setLightLevel(0.1F);      // Faint glow
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHarvestLevel("pickaxe", 3);  // Diamond tier
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
