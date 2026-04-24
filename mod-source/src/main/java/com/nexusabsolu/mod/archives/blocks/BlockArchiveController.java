package com.nexusabsolu.mod.archives.blocks;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.archives.tiles.TileArchiveController;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Controller block of the Archives Voss multiblock.
 *
 * <p>Placement : top-center of the multiblock (center of the upper 3-block
 * layer). One per multiblock. Holds the logic, the network, and the GUI.
 *
 * <p>Behavior :
 * <ul>
 *   <li>When placed : triggers structure scan after 1 tick delay
 *       (via TileEntity update)</li>
 *   <li>Right-click : opens GUI (only if multiblock valid)</li>
 *   <li>Right-click when multiblock invalid : displays chat message
 *       "Structure incomplete" with details</li>
 *   <li>When broken : releases network, disconnects all nodes</li>
 * </ul>
 *
 * @since v1.0.302 (Archives Voss Sprint 1)
 */
public class BlockArchiveController extends Block implements ITileEntityProvider, IHasModel {

    public BlockArchiveController() {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + ".archive_controller");
        setRegistryName(Reference.MOD_ID, "archive_controller");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(5.0F);  // plus dur que les frame/thermal, c'est le coeur
        setResistance(20.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 2);  // tier IRON requis
        setLightLevel(0.3F);  // leger glow (ambiance active)
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileArchiveController();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;  // tout traite cote serveur

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileArchiveController)) return false;

        TileArchiveController controller = (TileArchiveController) te;
        if (!controller.isStructureFormed()) {
            // Message d'aide pour le joueur qui n'a pas encore valide la structure
            player.sendMessage(new TextComponentString(
                TextFormatting.RED + "Structure incomplete"
                + TextFormatting.GRAY + " - il manque des blocs au multiblock Archives."
            ));
            return true;
        }

        // TODO Sprint 4 : ouvrir le GUI Controller (liste items + autocraft patterns)
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "[Archives Voss] " + TextFormatting.GRAY
            + "Multiblock valide. GUI a venir (Sprint 4)."
        ));
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileArchiveController) {
            ((TileArchiveController) te).onControllerBroken();
        }
        super.breakBlock(world, pos, state);
    }

    /**
     * Appele quand un bloc voisin change (placement, destruction, modification).
     * Delegue au TileEntity pour requeter un rescan de la structure au prochain tick.
     * Important : on rescan paresseux (pas immediatement) pour eviter de multiples
     * scans sequentiels quand plusieurs blocs sont poses en chaine (ex: chargement
     * de chunk qui charge les 8 blocs a la fois).
     */
    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos,
                                 Block blockIn, BlockPos fromPos) {
        if (world.isRemote) return;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileArchiveController) {
            ((TileArchiveController) te).requestRescan();
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
