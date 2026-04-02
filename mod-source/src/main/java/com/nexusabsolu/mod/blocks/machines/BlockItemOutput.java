package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileItemOutput;
import com.nexusabsolu.mod.util.IHasModel;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

public class BlockItemOutput extends Block implements IHasModel {

    public BlockItemOutput() {
        super(Material.IRON);
        setUnlocalizedName("item_output");
        setRegistryName(Reference.MOD_ID, "item_output");
        setHardness(5.0F);
        setResistance(10.0F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileItemOutput();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileItemOutput) {
                BlockPos masterPos = ((TileItemOutput) te).getMasterPos();
                if (masterPos != null) {
                    TileEntity masterTe = world.getTileEntity(masterPos);
                    if (masterTe instanceof com.nexusabsolu.mod.tiles.TileCondenseurT2) {
                        player.openGui(NexusAbsoluMod.instance, 3, world,
                            masterPos.getX(), masterPos.getY(), masterPos.getZ());
                        return true;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileItemOutput) {
            TileItemOutput output = (TileItemOutput) te;
            for (int i = 0; i < output.getSizeInventory(); i++) {
                if (!output.getStackInSlot(i).isEmpty()) {
                    InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(),
                        output.getStackInSlot(i));
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
