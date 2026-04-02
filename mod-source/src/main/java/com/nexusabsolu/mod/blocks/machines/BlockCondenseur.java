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

    // Try all 4 orientations of the 2x2x2 structure
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

            BlockPos p1 = pos.add(dx, 0, 0);
            BlockPos p2 = pos.add(0, 0, dz);
            BlockPos p3 = pos.add(dx, 0, dz);
            BlockPos t0 = pos.add(0, 1, 0);
            BlockPos t1 = pos.add(dx, 1, 0);
            BlockPos t2 = pos.add(0, 1, dz);
            BlockPos t3 = pos.add(dx, 1, dz);

            // Bottom: 2x Nexus Wall + 1x Redstone Block (energy input)
            int walls = 0; int redstone = 0;
            BlockPos[] bottoms = {p1, p2, p3};
            for (BlockPos bp : bottoms) {
                if (isNexusWall(world, bp)) walls++;
                else if (isRedstone(world, bp)) redstone++;
            }
            if (walls != 2 || redstone != 1) continue;

            // Top: 3x Glass + 1x Nexus Wall
            int glass = 0; int wall = 0;
            BlockPos[] tops = {t0, t1, t2, t3};
            for (BlockPos tp : tops) {
                if (isGlass(world, tp)) glass++;
                else if (isNexusWall(world, tp)) wall++;
            }
            if (glass != 3 || wall != 1) continue;

            // VALID! Form the multiblock
            formMultiblock(world, pos, dx, dz, p1, p2, p3, t0, t1, t2, t3);
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

        // Position 0 = master (condenseur location)
        world.setBlockState(master, formed.getDefaultState()
            .withProperty(BlockCondenseurFormed.POSITION, 0), 2);

        // Bottom slaves: position 3 = redstone (energy), 1-2 = nexus wall
        BlockPos[] bots = {b1, b2, b3};
        int wallIdx = 1;
        for (BlockPos bp : bots) {
            if (isRedstone(world, bp)) {
                world.setBlockState(bp, formed.getDefaultState()
                    .withProperty(BlockCondenseurFormed.POSITION, 3), 2);
            } else {
                world.setBlockState(bp, formed.getDefaultState()
                    .withProperty(BlockCondenseurFormed.POSITION, wallIdx), 2);
                wallIdx++;
            }
        }

        // Top layer: positions 4,5,6 = glass, 7 = nexus wall
        BlockPos[] topBlocks = {t0, t1, t2, t3};
        int glassIdx = 4;
        for (int i = 0; i < 4; i++) {
            BlockPos tp = topBlocks[i];
            if (isGlass(world, tp)) {
                world.setBlockState(tp, formed.getDefaultState()
                    .withProperty(BlockCondenseurFormed.POSITION, glassIdx), 2);
                glassIdx++;
            } else {
                world.setBlockState(tp, formed.getDefaultState()
                    .withProperty(BlockCondenseurFormed.POSITION, 7), 2);
            }
        }

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
