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

public class BlockNexusWallT2 extends Block implements IHasModel {

    public BlockNexusWallT2() {
        super(Material.IRON);
        setUnlocalizedName("nexus_wall_t2");
        setRegistryName(Reference.MOD_ID, "nexus_wall_t2");
        setHardness(10.0F);
        setResistance(25.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.3F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
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
