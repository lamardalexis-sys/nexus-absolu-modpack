package com.nexusabsolu.mod.archives.blocks;

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
 * Thermal core block of the Archives Voss multiblock.
 *
 * <p>Placement in the multiblock : bottom layer center, 3 positions between
 * the Input port (left) and Output port (right) of the lower layer.
 * Purely cosmetic/structural - no tick, no TileEntity.
 *
 * <p>Narrative : these blocks contain the "resonance coils" through which
 * the Eau Voss Froide circulates to dissipate the heat of quantum storage.
 * Visual should suggest piping/coils, rust patina coherent with Fourneaux.
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class BlockArchiveThermalCore extends Block implements IHasModel {

    public BlockArchiveThermalCore() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".archive_thermal_core");
        setRegistryName(Reference.MOD_ID, "archive_thermal_core");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(3.5F);
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
