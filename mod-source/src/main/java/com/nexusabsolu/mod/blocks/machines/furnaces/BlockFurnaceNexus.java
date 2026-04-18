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
        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    public FurnaceTier getTier() { return tier; }

    // === BlockState / Facing ===

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getFront(meta);
        if (facing.getAxis() == EnumFacing.Axis.Y) facing = EnumFacing.NORTH;
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
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

    // === GUI on right-click ===

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // Sprint A: pas de GUI encore. Les upgrades + interface arrivent Sprint C.
            // En attendant, on peut tester via hoppers (capability ItemHandler).
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileFurnaceNexus) {
                TileFurnaceNexus furnace = (TileFurnaceNexus) te;
                player.sendStatusMessage(
                    new net.minecraft.util.text.TextComponentString(
                        "Furnace " + furnace.getTier().registryName
                        + " | Progress: " + furnace.getCookProgress() + "/" + furnace.getMaxCookTime()
                        + " | Fuel: " + furnace.getFuelRemaining()
                        + " | RF: " + furnace.getEnergyStored() + "/" + furnace.getMaxEnergy()),
                    false);
            }
        }
        return true;
    }

    // === DROP MEKANISM-STYLE : conserve tout dans NBT ===

    /**
     * Quand le joueur casse le bloc, on genere un ItemStack unique avec
     * un tag "BlockEntityTag" contenant l'etat complet de la TileEntity.
     * Minecraft applique automatiquement ce tag au placement.
     */
    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess worldIn, BlockPos pos,
            IBlockState state, int fortune) {
        ItemStack stack = new ItemStack(Item.getItemFromBlock(this));

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileFurnaceNexus) {
            NBTTagCompound teTag = te.writeToNBT(new NBTTagCompound());
            // Wrap dans "BlockEntityTag" -- nom reserve Minecraft qui sera
            // automatiquement re-applique a la TileEntity au placement
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setTag("BlockEntityTag", teTag);
            stack.setTagCompound(itemTag);
        }

        drops.add(stack);
    }

    /**
     * Override harvestBlock pour skip le drop vanilla par defaut
     * (sinon double-drop : 1 via getDrops + 1 via inventaire spille).
     * On ne veut QUE le drop getDrops qui a le NBT.
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
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

    // === Model registration ===

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
