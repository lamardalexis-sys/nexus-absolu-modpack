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

public class BlockCondenseur extends Block implements IHasModel {

    public BlockCondenseur() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".condenseur");
        setRegistryName(Reference.MOD_ID, "condenseur");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(5.0F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.5F); // Subtle glow
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
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileCondenseur) {
                player.openGui(NexusAbsoluMod.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
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

    // Purple particles when processing
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileCondenseur && ((TileCondenseur) te).isProcessing()) {
            // Purple portal particles
            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 1.2;
                double y = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 1.2;
                double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 1.2;
                world.spawnParticle(EnumParticleTypes.PORTAL, x, y, z, 0, 0.1, 0);
            }
            // Drip particles (the "goop dripping" effect)
            if (rand.nextInt(3) == 0) {
                double x = pos.getX() + 0.2 + rand.nextDouble() * 0.6;
                double y = pos.getY() + 0.9;
                double z = pos.getZ() + 0.2 + rand.nextDouble() * 0.6;
                world.spawnParticle(EnumParticleTypes.DRIP_LAVA, x, y, z, 0, 0, 0);
            }
            // Enchantment particles
            if (rand.nextInt(4) == 0) {
                double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 2;
                double y = pos.getY() + rand.nextDouble() * 2;
                double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 2;
                world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, y, z,
                    (pos.getX() + 0.5 - x), (pos.getY() + 0.5 - y), (pos.getZ() + 0.5 - z));
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
