package com.nexusabsolu.mod.blocks.machines.furnaces;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Block generique pour les 9 tiers de Furnace Nexus Absolu.
 *
 * Parametre par FurnaceTier : speed, conso, RF natif, age gating.
 * Tous les tiers partagent la meme classe block + meme TileEntity,
 * seule l'enum change leur comportement.
 *
 * Drop: "Mekanism-style" -- le block casse drop un ItemStack unique
 * dont le NBT contient l'etat complet de la TileEntity (inventaire,
 * upgrades, energie, progress). Au placement, le NBT est restaure.
 */
public class BlockFurnaceNexus extends Block implements IHasModel {

    public static final PropertyDirection FACING = PropertyDirection.create(
        "facing", EnumFacing.Plane.HORIZONTAL);
    // v1.0.212 : property calculee depuis la TileEntity via getActualState
    // (pas stockee en meta - le meta reste facing-only, 2 bits)
    public static final PropertyBool ENHANCED = PropertyBool.create("enhanced");
    // v1.0.216 : true si le furnace cuit activement (flamme visible).
    // Permet au blockstate de choisir entre:
    //   - base : texture neutre (pas de lumiere, LED grise si enhanced)
    //   - active : front_on+top avec flamme + LED cyan brillante si enhanced
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    private final FurnaceTier tier;

    public BlockFurnaceNexus(FurnaceTier tier) {
        super(Material.IRON);
        this.tier = tier;
        String registryId = "furnace_" + tier.registryName;
        setUnlocalizedName(Reference.MOD_ID + "." + registryId);
        setRegistryName(Reference.MOD_ID, registryId);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setHardness(4.5F);
        setResistance(15.0F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setDefaultState(this.blockState.getBaseState()
            .withProperty(FACING, EnumFacing.NORTH)
            .withProperty(ENHANCED, Boolean.FALSE)
            .withProperty(ACTIVE, Boolean.FALSE));
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    public FurnaceTier getTier() { return tier; }

    // === BlockState / Facing ===

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ENHANCED, ACTIVE);
    }

    /**
     * v1.0.212/216 : lit les flags isEnhanced + isActivelyCooking depuis la
     * TileEntity pour que le blockstate JSON puisse choisir le bon modele :
     *   - active=false : texture eteinte (LED grise si enhanced)
     *   - active=true  : texture allumee (LED cyan brillante si enhanced)
     */
    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        boolean enhanced = false;
        boolean active = false;
        if (te instanceof TileFurnaceNexus) {
            TileFurnaceNexus furnace = (TileFurnaceNexus) te;
            enhanced = furnace.isEnhanced();
            active = furnace.isActivelyCooking();
        }
        return state.withProperty(ENHANCED, enhanced).withProperty(ACTIVE, active);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getFront(meta);
        if (facing.getAxis() == EnumFacing.Axis.Y) facing = EnumFacing.NORTH;
        // ENHANCED et ACTIVE ne sont PAS dans le meta - toujours false au
        // getStateFromMeta, getActualState les remplace depuis la TileEntity
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        // meta = facing seulement, ENHANCED et ACTIVE viennent de la TileEntity
        return state.getValue(FACING).getIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // === TileEntity ===

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFurnaceNexus(tier);
    }

    // === Render ===

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return true; }

    @Override
    public boolean isFullCube(IBlockState state) { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() { return BlockRenderLayer.CUTOUT; }

    // === GUI on right-click (l'Upgrade Kit est gere par
    //     ItemFurnaceUpgradeKit.onItemUseFirst pour capturer le shift+clic
    //     avant que Minecraft cancel l'interaction bloc a cause du sneak) ===

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileFurnaceNexus) {
                // Ouvre le GUI Furnace (le GUI s'adapte selon tile.isEnhanced())
                player.openGui(NexusAbsoluMod.instance,
                    com.nexusabsolu.mod.gui.GuiHandler.FURNACE_NEXUS_GUI,
                    world, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    // === DROP MEKANISM-STYLE : conserve tout dans NBT ===

    /**
     * v1.0.220 : Utilise ThreadLocal pour passer la reference TileEntity
     * de harvestBlock vers getDrops. En effet, MC invalide la TileEntity
     * entre removedByPlayer et getDrops, donc world.getTileEntity() peut
     * retourner null dans getDrops, resultant en un ItemStack sans NBT.
     *
     * Pattern vanilla : BlockShulkerBox fait exactement ca en 1.12.2.
     */
    private static final ThreadLocal<TileFurnaceNexus> CAPTURED_TE = new ThreadLocal<>();

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess worldIn, BlockPos pos,
            IBlockState state, int fortune) {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(this));

        // Essaye d'abord la TE capturee par harvestBlock (chemin normal :
        // joueur casse le bloc avec harvest=true)
        TileFurnaceNexus captured = CAPTURED_TE.get();
        TileEntity te = captured != null ? captured : worldIn.getTileEntity(pos);

        if (te instanceof TileFurnaceNexus) {
            NBTTagCompound teTag = te.writeToNBT(new NBTTagCompound());
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setTag("BlockEntityTag", teTag);
            stack.setTagCompound(itemTag);
        }

        drops.add(stack);
    }

    /**
     * v1.0.220 : override breakBlock pour NE PAS dumper l'inventaire (pattern
     * Mekanism). Sans ca, MC vanilla Block.breakBlock() appelle
     * InventoryHelper.dropInventoryItems() qui aurait drope les items de
     * l'IInventory comme ItemEntities dans le monde, causant des duplicates
     * ou au contraire la perte si on a deja dropee l'ItemStack avec NBT.
     */
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // On ne fait RIEN (skip InventoryHelper.dropInventoryItems du super).
        // Le drop ItemStack avec NBT est gere par harvestBlock/getDrops.
        // On delegue juste a Block.breakBlock qui removeTileEntity.
        super.breakBlock(world, pos, state);
    }

    /**
     * Override removedByPlayer pour retarder la suppression du bloc jusqu'apres
     * harvestBlock. Pattern vanilla BlockShulkerBox pour preserver la TE pendant
     * getDrops.
     */
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
            EntityPlayer player, boolean willHarvest) {
        if (willHarvest) return true;
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state,
            TileEntity te, ItemStack tool) {
        // v1.0.220 : capture la TE dans ThreadLocal pour que getDrops y ait
        // acces, meme si world.getTileEntity(pos) retourne null entre-temps.
        if (te instanceof TileFurnaceNexus) {
            CAPTURED_TE.set((TileFurnaceNexus) te);
        }
        try {
            super.harvestBlock(world, player, pos, state, te, tool);
        } finally {
            CAPTURED_TE.remove();
        }
        // Finalise la suppression du bloc (removedByPlayer a retourne true sans
        // le retirer, c'est a nous de le faire maintenant que les drops sont
        // generes)
        world.setBlockToAir(pos);
    }

    // === Model registration ===

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        // Properties ORDRE ALPHABETIQUE : active, enhanced, facing
        // MC 1.12.2 compose la cle de variant dans cet ordre.
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(),
                "active=false,enhanced=false,facing=north"));
    }
}
