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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
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

    public BlockCondenseurT2Wall() {
        super(Material.IRON);
        setUnlocalizedName("condenseur_t2_wall");
        setRegistryName(Reference.MOD_ID, "condenseur_t2_wall");
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.2F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // Find the T2 master in a 3-block radius and open its GUI
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
            // Breaking a formed wall breaks the T2 structure
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
        // Purple particles on formed walls
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
