package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TileCondenseur;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCondenseur extends Block implements IHasModel {

    public BlockCondenseur() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".condenseur");
        setRegistryName(Reference.MOD_ID, "condenseur");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.5F);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileCondenseur();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // Try to form multibloc first
            if (tryFormMultiblock(world, pos)) {
                return true;
            }
            // If not formed, open the GUI anyway (shows "Structure incomplete")
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileCondenseur) {
                player.openGui(NexusAbsoluMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    // Try all 4 orientations of the 2x2x2 structure.
    // Spatial layout (relative to master at pos):
    //   POS 0 = master                  pos
    //   POS 1 = nexus wall              pos + (dx, 0, 0)
    //   POS 2 = nexus wall              pos + (0, 0, dz)
    //   POS 3 = redstone block          pos + (dx, 0, dz)   (diagonal from master)
    //   POS 4 = glass                   pos + (0, 1, 0)     (above master)
    //   POS 5 = glass                   pos + (dx, 1, 0)
    //   POS 6 = glass                   pos + (0, 1, dz)
    //   POS 7 = nexus wall (top)        pos + (dx, 1, dz)   (above redstone)
    public boolean tryFormMultiblock(World world, BlockPos pos) {
        int[][] directions = {
            {1, 1},   // +X +Z
            {-1, 1},  // -X +Z
            {1, -1},  // +X -Z
            {-1, -1}  // -X -Z
        };

        for (int[] dir : directions) {
            int dx = dir[0];
            int dz = dir[1];

            BlockPos b1 = pos.add(dx, 0, 0);   // POS 1
            BlockPos b2 = pos.add(0, 0, dz);   // POS 2
            BlockPos b3 = pos.add(dx, 0, dz);  // POS 3
            BlockPos t0 = pos.add(0, 1, 0);    // POS 4
            BlockPos t1 = pos.add(dx, 1, 0);   // POS 5
            BlockPos t2 = pos.add(0, 1, dz);   // POS 6
            BlockPos t3 = pos.add(dx, 1, dz);  // POS 7

            // Strict spatial validation (exact shape required)
            if (!isNexusWall(world, b1)) continue;
            if (!isNexusWall(world, b2)) continue;
            if (!isRedstone(world, b3)) continue;
            if (!isGlass(world, t0))    continue;
            if (!isGlass(world, t1))    continue;
            if (!isGlass(world, t2))    continue;
            if (!isNexusWall(world, t3)) continue;

            // VALID! Form the multiblock with deterministic spatial positions
            formMultiblock(world, pos, dx, dz, b1, b2, b3, t0, t1, t2, t3);
            return true;
        }
        return false;
    }

    private void formMultiblock(World world, BlockPos master, int dx, int dz,
            BlockPos b1, BlockPos b2, BlockPos b3,
            BlockPos t0, BlockPos t1, BlockPos t2, BlockPos t3) {

        // Save TileEntity data before replacing blocks
        TileEntity te = world.getTileEntity(master);
        NBTTagCompound savedData = null;
        if (te instanceof TileCondenseur) {
            savedData = te.writeToNBT(new NBTTagCompound());
        }

        Block formed = ModBlocks.CONDENSEUR_FORMED;

        // SPATIAL POSITION ASSIGNMENT (deterministic, see tryFormMultiblock comment for layout)
        // Each (block, POS) pair is fixed by spatial role, not by block type ordering.
        world.setBlockState(master, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 0), 2);  // master
        world.setBlockState(b1, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 1), 2);  // wall +X
        world.setBlockState(b2, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 2), 2);  // wall +Z
        world.setBlockState(b3, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 3), 2);  // redstone diagonal
        world.setBlockState(t0, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 4), 2);  // glass above master
        world.setBlockState(t1, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 5), 2);  // glass above b1
        world.setBlockState(t2, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 6), 2);  // glass above b2
        world.setBlockState(t3, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 7), 2);  // wall_top above redstone

        // Restore TileEntity data to the master position
        if (savedData != null) {
            TileEntity newTe = world.getTileEntity(master);
            if (newTe instanceof TileCondenseur) {
                newTe.readFromNBT(savedData);
                ((TileCondenseur) newTe).setStructureFormed(true);
                ((TileCondenseur) newTe).setMultiDirection(dx, dz);
                // Sync to client so TESR can render the screen
                newTe.markDirty();
                IBlockState newState = world.getBlockState(master);
                world.notifyBlockUpdate(master, newState, newState, 3);
            }
        }
    }

    private boolean isNexusWall(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == ModBlocks.NEXUS_WALL;
    }

    private boolean isGlass(World world, BlockPos pos) {
        Block b = world.getBlockState(pos).getBlock();
        return b == Blocks.GLASS || b == Blocks.STAINED_GLASS;
    }

    private boolean isRedstone(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.REDSTONE_BLOCK;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCondenseur) {
            TileCondenseur condenseur = (TileCondenseur) te;
            for (int i = 0; i < condenseur.getSizeInventory(); i++) {
                if (!condenseur.getStackInSlot(i).isEmpty()) {
                    Block.spawnAsEntity(world, pos, condenseur.getStackInSlot(i));
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
