package com.nexusabsolu.mod.archives.blocks;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.archives.tiles.TileCompresseurEau;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
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
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Compresseur d'Eau Voss.
 *
 * <p>Machine single-block. Cycle frigorifique (contre-intuitif mais simplifie
 * pour gameplay) :
 * <ul>
 *   <li><b>Input</b> : Eau Voss Chaude (cycle) OU Water vanilla (appoint)</li>
 *   <li><b>Input</b> : RF</li>
 *   <li><b>Output</b> : Eau Voss Froide</li>
 *   <li><b>Perte</b> : 50 mB par cycle (force appoint d'eau vanilla regulier)</li>
 * </ul>
 *
 * <p>Consommation : 50 RF/tick quand actif. Vitesse : 1 cycle = 100 ticks (5s).
 * Chaque cycle convertit 200 mB entree -> 150 mB sortie (50 mB perte).
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class BlockCompresseurEau extends Block implements ITileEntityProvider, IHasModel {

    public BlockCompresseurEau() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".compresseur_eau");
        setRegistryName(Reference.MOD_ID, "compresseur_eau");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(3.0F);
        setResistance(10.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCompresseurEau();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        // Sprint 1 : on utilise le GuiHandler standard pour ouvrir le GUI du compresseur.
        // La constante est declaree dans GuiHandler (ajoutee plus bas).
        player.openGui(
            NexusAbsoluMod.instance,
            com.nexusabsolu.mod.gui.GuiHandler.COMPRESSEUR_EAU_GUI,
            world, pos.getX(), pos.getY(), pos.getZ()
        );
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
