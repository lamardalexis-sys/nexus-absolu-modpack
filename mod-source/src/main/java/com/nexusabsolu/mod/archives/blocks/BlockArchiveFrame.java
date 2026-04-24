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
 * Frame block of the Archives Voss multiblock.
 *
 * <p>Placement in the multiblock : top layer, 2 positions (left and right of
 * the Controller on the upper layer). Purely cosmetic/structural - no tick,
 * no TileEntity.
 *
 * <p>Visual theme : industriel-vintage rust patina, matching the Fourneaux
 * Voss aesthetic. Placeholder texture until final art.
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class BlockArchiveFrame extends Block implements IHasModel {

    public BlockArchiveFrame() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".archive_frame");
        setRegistryName(Reference.MOD_ID, "archive_frame");
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
