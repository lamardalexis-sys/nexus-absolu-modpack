package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.tiles.TileCondenseurEnergy;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockCondenseurFormed extends Block implements IHasModel {

    // POSITION property: 0=master(condenseur), 1-3=bottom slaves, 4-7=top layer
    public static final PropertyInteger POSITION = PropertyInteger.create("position", 0, 7);

    public BlockCondenseurFormed() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".condenseur_formed");
        setRegistryName(Reference.MOD_ID, "condenseur_formed");
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.625F);
        setDefaultState(blockState.getBaseState().withProperty(POSITION, 0));
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, POSITION);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(POSITION);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(POSITION, meta & 7);
    }

    // Only glass position (4) is see-through
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        int pos = state.getValue(POSITION);
        return pos != 4; // only glass is transparent
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        int pos = state.getValue(POSITION);
        return pos != 4;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world,
            BlockPos pos, EnumFacing side) {
        int position = state.getValue(POSITION);
        // Hide faces between top wall blocks (5,6,7) for seamless look
        if (position >= 5 && position <= 7) {
            IBlockState neighbor = world.getBlockState(pos.offset(side));
            if (neighbor.getBlock() == this) {
                int nPos = neighbor.getValue(POSITION);
                if (nPos >= 5 && nPos <= 7) return false;
            }
        }
        // Glass: hide face toward adjacent top blocks
        if (position == 4) {
            IBlockState neighbor = world.getBlockState(pos.offset(side));
            if (neighbor.getBlock() == this) {
                int nPos = neighbor.getValue(POSITION);
                if (nPos >= 4 && nPos <= 7) return false;
            }
        }
        return super.shouldSideBeRendered(state, world, pos, side);
    }

    // Only position 0 (master) has a TileEntity
    @Override
    public boolean hasTileEntity(IBlockState state) {
        int pos = state.getValue(POSITION);
        return pos == 0 || pos == 3;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        int pos = state.getValue(POSITION);
        if (pos == 0) {
            return new TileCondenseur();
        }
        if (pos == 3) {
            return new TileCondenseurEnergy();
        }
        return null;
    }

    // Right-click on ANY block of the multibloc opens the master GUI
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BlockPos masterPos = findMaster(world, pos, state);
            if (masterPos != null) {
                TileEntity te = world.getTileEntity(masterPos);
                if (te instanceof TileCondenseur) {
                    player.openGui(NexusAbsoluMod.instance, 0, world,
                        masterPos.getX(), masterPos.getY(), masterPos.getZ());
                }
            }
        }
        return true;
    }

    // Find the master (position 0) from any slave position
    private BlockPos findMaster(World world, BlockPos pos, IBlockState state) {
        int position = state.getValue(POSITION);
        if (position == 0) return pos;

        // Search 2x2x2 area around this block for position 0
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 0; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos check = pos.add(dx, dy, dz);
                    IBlockState s = world.getBlockState(check);
                    if (s.getBlock() == this && s.getValue(POSITION) == 0) {
                        return check;
                    }
                }
            }
        }
        return null;
    }

    // Breaking ANY block breaks the entire multibloc
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            BlockPos masterPos = findMaster(world, pos, state);

            // Drop inventory from master
            if (masterPos != null) {
                TileEntity te = world.getTileEntity(masterPos);
                if (te instanceof TileCondenseur) {
                    TileCondenseur condenseur = (TileCondenseur) te;
                    for (int i = 0; i < condenseur.getSizeInventory(); i++) {
                        if (!condenseur.getStackInSlot(i).isEmpty()) {
                            Block.spawnAsEntity(world, masterPos, condenseur.getStackInSlot(i));
                        }
                    }
                }
            }

            // Replace all formed blocks with original blocks
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos check = pos.add(dx, dy, dz);
                        if (check.equals(pos)) continue;
                        IBlockState s = world.getBlockState(check);
                        if (s.getBlock() == this) {
                            int p = s.getValue(POSITION);
                            if (p == 0) {
                                world.setBlockState(check, ModBlocks.CONDENSEUR.getDefaultState());
                            } else if (p == 1 || p == 2) {
                                world.setBlockState(check, ModBlocks.NEXUS_WALL.getDefaultState());
                            } else if (p == 3) {
                                // Energy input = redstone block
                                world.setBlockState(check, net.minecraft.init.Blocks.REDSTONE_BLOCK.getDefaultState());
                            } else {
                                world.setBlockState(check, net.minecraft.init.Blocks.GLASS.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    // Particles inside the multibloc - visible through glass
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        int position = state.getValue(POSITION);

        // Glass blocks (4-6): always show subtle idle particles inside
        if (position >= 4 && position <= 6) {
            // Subtle portal swirl inside - always active
            if (rand.nextInt(4) == 0) {
                double x = pos.getX() + 0.2 + rand.nextDouble() * 0.6;
                double y = pos.getY() + 0.1 + rand.nextDouble() * 0.8;
                double z = pos.getZ() + 0.2 + rand.nextDouble() * 0.6;
                world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z,
                    (rand.nextDouble() - 0.5) * 0.3, rand.nextDouble() * 0.2, (rand.nextDouble() - 0.5) * 0.3);
            }
            // Enchantment sparkle
            if (rand.nextInt(8) == 0) {
                double x = pos.getX() + 0.2 + rand.nextDouble() * 0.6;
                double y = pos.getY() + 0.2 + rand.nextDouble() * 0.6;
                double z = pos.getZ() + 0.2 + rand.nextDouble() * 0.6;
                world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, y, z, 0, 0.1, 0);
            }
        }

        // Master block (0): intense effects when processing
        if (position == 0) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileCondenseur && ((TileCondenseur) te).isProcessing()) {
                // Intense portal burst above
                for (int i = 0; i < 5; i++) {
                    double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 1.5;
                    double y = pos.getY() + 1.2 + rand.nextDouble() * 0.8;
                    double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 1.5;
                    world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z,
                        (rand.nextDouble() - 0.5) * 0.5, 0.2 + rand.nextDouble() * 0.3, (rand.nextDouble() - 0.5) * 0.5);
                }
                // Drip from glass ceiling
                if (rand.nextInt(2) == 0) {
                    double x = pos.getX() + rand.nextDouble() * 2;
                    double y = pos.getY() + 1.95;
                    double z = pos.getZ() + rand.nextDouble() * 2;
                    world.spawnParticle(EnumParticleTypes.DRIP_LAVA, x, y, z, 0, 0, 0);
                }
                // Smoke from bottom
                if (rand.nextInt(3) == 0) {
                    double x = pos.getX() + rand.nextDouble() * 2;
                    double y = pos.getY() + 0.5;
                    double z = pos.getZ() + rand.nextDouble() * 2;
                    world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0, 0.05, 0);
                }
                // Redstone sparkle on edges
                world.spawnParticle(EnumParticleTypes.REDSTONE,
                    pos.getX() + rand.nextInt(2) * 2.0, pos.getY() + rand.nextDouble() * 2,
                    pos.getZ() + rand.nextInt(2) * 2.0, 0.5, 0.0, 1.0);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        for (int i = 0; i < 8; i++) {
            ModelLoader.setCustomModelResourceLocation(
                Item.getItemFromBlock(this), i,
                new ModelResourceLocation(getRegistryName(), "position=" + i));
        }
    }
}
