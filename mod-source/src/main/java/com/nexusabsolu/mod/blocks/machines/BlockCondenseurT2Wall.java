package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileCondenseurT2;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockCondenseurT2Wall extends Block implements IHasModel {

    /** 0 = was Nexus Wall, 1 = was Glass */
    public static final PropertyInteger ORIGINAL = PropertyInteger.create("original", 0, 1);

    public static final int TYPE_WALL = 0;
    public static final int TYPE_GLASS = 1;

    public BlockCondenseurT2Wall() {
        super(Material.IRON);
        setUnlocalizedName("condenseur_t2_wall");
        setRegistryName(Reference.MOD_ID, "condenseur_t2_wall");
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.2F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setDefaultState(blockState.getBaseState().withProperty(ORIGINAL, TYPE_WALL));
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ORIGINAL);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORIGINAL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ORIGINAL, meta & 1);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return true; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    /**
     * Drop the correct original block based on metadata.
     * meta 0 = Nexus Wall, meta 1 = Glass
     */
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (state.getValue(ORIGINAL) == TYPE_GLASS) {
            return Item.getItemFromBlock(Blocks.GLASS);
        }
        return Item.getItemFromBlock(ModBlocks.NEXUS_WALL);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos check = pos.add(dx, dy, dz);
                        TileEntity te = world.getTileEntity(check);
                        if (te instanceof TileCondenseurT2) {
                            player.openGui(NexusAbsoluMod.instance, 3, world,
                                check.getX(), check.getY(), check.getZ());
                            return true;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos check = pos.add(dx, dy, dz);
                        TileEntity te = world.getTileEntity(check);
                        if (te instanceof TileCondenseurT2) {
                            ((TileCondenseurT2) te).onStructureBroken();
                            break;
                        }
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(6) == 0) {
            double x = pos.getX() + rand.nextDouble();
            double y = pos.getY() + rand.nextDouble();
            double z = pos.getZ() + rand.nextDouble();
            world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z,
                (rand.nextDouble() - 0.5) * 0.2, rand.nextDouble() * 0.1,
                (rand.nextDouble() - 0.5) * 0.2);
        }
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
