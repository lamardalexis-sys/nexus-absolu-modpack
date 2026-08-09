package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.machines.MachineRegistry;
import com.nexusabsolu.mod.machines.tile.TileMachineBase;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Controleur de multibloc Nexus.
 *
 * Ce bloc appartient au mod et n'est jamais remplace. Quand une definition
 * existe dans assets/nexusabsolu/machines/ pour son identifiant, il recoit une
 * TileMachineBase qui valide la structure autour de lui. Sinon il reste un
 * bloc decoratif, sans TileEntity et sans cout de tick -- c'est le cas des
 * machines pas encore migrees.
 *
 * Historique : ces 27 blocs venaient de ContentTweaker, puis ont ete migres
 * dans le mod (commit 2ad8268). Ils sont restes decoratifs tant que le moteur
 * reposait sur Modular Machinery, dont TileMachineController ecrasait la
 * position du controleur par son propre bloc des qu'une structure etait
 * reconnue.
 */
public class BlockMachineController extends Block implements IHasModel {

    /**
     * Cache de la question "ce bloc a-t-il une definition de machine ?".
     * Reste null tant que le registre n'est pas charge, pour ne pas figer un
     * "non" pendant l'enregistrement des blocs, qui precede le preInit.
     */
    private Boolean machineBacked = null;

    public BlockMachineController(String name, float hardness, float resistance,
                                  int toolLevel, int lightValue) {
        super(Material.IRON);
        setUnlocalizedName(Reference.MOD_ID + "." + name);
        setRegistryName(Reference.MOD_ID, name);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(hardness);
        setResistance(resistance);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", toolLevel);
        setLightLevel(lightValue / 15.0F);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    /** True si une machine est declaree pour cet identifiant de bloc. */
    private boolean isMachineBacked() {
        if (machineBacked == null) {
            if (!MachineRegistry.isLoaded()) return false;
            ResourceLocation rl = getRegistryName();
            machineBacked = Boolean.valueOf(
                rl != null && MachineRegistry.getByController(rl.toString()) != null);
        }
        return machineBacked.booleanValue();
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return isMachineBacked();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return isMachineBacked() ? new TileMachineBase() : null;
    }

    /**
     * Clic droit : rapport de structure. En phase 1 c'est le seul retour au
     * joueur ; en phase 3 la GUI prendra le relais quand la structure est
     * formee, et ce rapport restera le diagnostic quand elle ne l'est pas.
     */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!isMachineBacked()) return false;
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileMachineBase) {
                ((TileMachineBase) te).sendDiagnostic(player);
            }
        }
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
