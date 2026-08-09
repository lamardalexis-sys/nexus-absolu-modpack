package com.nexusabsolu.mod.machines.block;

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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Casing structurel des multiblocs Nexus.
 *
 * Un seul bloc pour les trente machines : c'est le controleur qui porte
 * l'identite visuelle, pas la carcasse. Meme parti pris qu'Immersive
 * Engineering, et le joueur n'a qu'un bloc a fabriquer en volume.
 */
public class BlockMachineCasing extends Block implements IHasModel {

    public BlockMachineCasing() {
        super(Material.IRON);
        String name = "machine_casing";
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
