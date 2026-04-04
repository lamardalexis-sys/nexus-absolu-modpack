package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.gui.GuiHandler;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileMachineKRDA;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockMachineKRDA extends Block implements IHasModel {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockMachineKRDA() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".machine_krda");
        setRegistryName(Reference.MOD_ID, "machine_krda");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(25.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 2);
        setDefaultState(blockState.getBaseState()
            .withProperty(FACING, EnumFacing.NORTH));
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
    @Override
    public IBlockState getStateForPlacement(World w, BlockPos p,
            EnumFacing f, float hx, float hy, float hz,
            int meta, EntityLivingBase placer) {
        return getDefaultState()
            .withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }
    @Override
    public int getMetaFromState(IBlockState s) {
        return s.getValue(FACING).getHorizontalIndex();
    }
    @Override
    public IBlockState getStateFromMeta(int m) {
        return getDefaultState()
            .withProperty(FACING, EnumFacing.getHorizontal(m));
    }
    @Override
    public boolean hasTileEntity(IBlockState s) { return true; }
    @Override
    public TileEntity createTileEntity(World w, IBlockState s) {
        return new TileMachineKRDA();
    }
    @Override
    public boolean onBlockActivated(World w, BlockPos p, IBlockState s,
            EntityPlayer player, EnumHand hand, EnumFacing f,
            float hx, float hy, float hz) {
        if (!w.isRemote) {
            player.openGui(NexusAbsoluMod.instance,
                GuiHandler.MACHINE_KRDA_GUI,
                w, p.getX(), p.getY(), p.getZ());
        }
        return true;
    }
    @Override
    public void breakBlock(World w, BlockPos p, IBlockState s) {
        TileEntity te = w.getTileEntity(p);
        if (te instanceof TileMachineKRDA) {
            TileMachineKRDA t = (TileMachineKRDA) te;
            for (int i = 0; i < t.getSizeInventory(); i++) {
                ItemStack stack = t.getStackInSlot(i);
                if (!stack.isEmpty())
                    InventoryHelper.spawnItemStack(w, p.getX(), p.getY(), p.getZ(), stack);
            }
        }
        super.breakBlock(w, p, s);
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World w,
            List<String> tip, ITooltipFlag flag) {
        tip.add("\u00a77Transmute le Signalum via la Diarrhee");
        tip.add("\u00a78Produit: Lingot de Signalhee");
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
