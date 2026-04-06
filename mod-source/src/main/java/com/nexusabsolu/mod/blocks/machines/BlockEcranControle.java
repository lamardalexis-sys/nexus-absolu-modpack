package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.TilePortalVoss;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

public class BlockEcranControle extends Block implements IHasModel {

    public BlockEcranControle() {
        super(Material.IRON);
        setUnlocalizedName("ecran_controle");
        setRegistryName(Reference.MOD_ID, "ecran_controle");
        setHardness(8.0F);
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setLightLevel(0.6F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePortalVoss();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TilePortalVoss) {
                TilePortalVoss portal = (TilePortalVoss) te;
                portal.checkStructure();
                if (!portal.isStructureFormed()) {
                    player.sendMessage(new TextComponentString(
                        "\u00a7c[Portail Voss] Structure incomplete."));
                    player.sendMessage(new TextComponentString(
                        "\u00a77Debug: " + portal.getFirstFailure()));
                } else if (!portal.hasEnoughEnergy()) {
                    player.sendMessage(new TextComponentString(
                        "\u00a7c[Portail Voss] Energie insuffisante ("
                        + portal.getEnergyStored() + "/1,000,000 RF)"));
                } else if (!portal.hasEnoughFluid()) {
                    player.sendMessage(new TextComponentString(
                        "\u00a7c[Portail Voss] Diarrhee insuffisante ("
                        + portal.getFluidStored() + "/10,000 mB)"));
                } else {
                    player.sendMessage(new TextComponentString(
                        "\u00a7d[Portail Voss] \u00a7lPortail pret. \u00a77Activation..."));
                    portal.activate(player);
                }
            }
        }
        return true;
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
