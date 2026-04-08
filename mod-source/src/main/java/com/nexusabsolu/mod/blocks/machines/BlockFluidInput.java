package com.nexusabsolu.mod.blocks.machines;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.tiles.TileFluidInput;
import com.nexusabsolu.mod.util.IHasModel;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.FMLLog;

public class BlockFluidInput extends Block implements IHasModel {

    public BlockFluidInput() {
        super(Material.IRON);
        setUnlocalizedName("fluid_input");
        setRegistryName(Reference.MOD_ID, "fluid_input");
        setHardness(5.0F);
        setResistance(10.0F);
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        ModBlocks.BLOCKS.add(this);
        ModItems.ITEMS.add(new ItemBlock(this).setRegistryName(getRegistryName()));
    }

    @Override
    public boolean hasTileEntity(IBlockState state) { return true; }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFluidInput();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                    EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) return true;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileFluidInput)) return true;
        TileFluidInput fluidTile = (TileFluidInput) te;

        // === Try Forge's standard bucket/container interaction ===
        boolean forgeOk = false;
        try {
            forgeOk = FluidUtil.interactWithFluidHandler(player, hand,
                world, pos, facing);
        } catch (Throwable t) {
            FMLLog.log.warn("[BlockFluidInput] FluidUtil threw: " + t.getMessage());
        }
        if (forgeOk) {
            FMLLog.log.info("[BlockFluidInput] FluidUtil bucket interaction OK");
            return true;
        }

        // === Fallback: manual bucket handling ===
        // FluidUtil can silently fail for a bunch of reasons (inventory full,
        // stacked buckets, capability quirks). Detect a UniversalBucket in
        // the player's hand and fill the tank manually.
        ItemStack held = player.getHeldItem(hand);
        if (!held.isEmpty() && held.getItem() instanceof UniversalBucket) {
            UniversalBucket bucket = (UniversalBucket) held.getItem();
            FluidStack contained = bucket.getFluid(held);
            if (contained != null && contained.amount > 0) {
                int filled = fluidTile.fillTank(contained.copy(), false);
                FMLLog.log.info("[BlockFluidInput] Manual bucket fill simulation: "
                    + filled + "/" + contained.amount + " mB of "
                    + contained.getFluid().getName());
                if (filled >= contained.amount) {
                    fluidTile.fillTank(contained.copy(), true);
                    // Replace the bucket with an empty one
                    if (!player.capabilities.isCreativeMode) {
                        held.shrink(1);
                        ItemStack empty = new ItemStack(Items.BUCKET);
                        if (held.isEmpty()) {
                            player.setHeldItem(hand, empty);
                        } else if (!player.inventory.addItemStackToInventory(empty)) {
                            player.dropItem(empty, false);
                        }
                    }
                    player.sendMessage(new TextComponentString(
                        TextFormatting.GREEN + "[Fluid Input] +"
                        + contained.amount + " mB "
                        + contained.getFluid().getLocalizedName(contained)));
                    return true;
                } else {
                    player.sendMessage(new TextComponentString(
                        TextFormatting.RED + "[Fluid Input] Pas assez de place ("
                        + filled + "/" + contained.amount + " mB)"));
                    return true;
                }
            }
        }

        // === No bucket in hand: open the master's GUI if T2 ===
        BlockPos masterPos = fluidTile.getMasterPos();
        if (masterPos != null) {
            TileEntity masterTe = world.getTileEntity(masterPos);
            if (masterTe instanceof com.nexusabsolu.mod.tiles.TileCondenseurT2) {
                player.openGui(NexusAbsoluMod.instance, 3, world,
                    masterPos.getX(), masterPos.getY(), masterPos.getZ());
                return true;
            }
        }

        // === Still no match: show current tank status ===
        int current = fluidTile.getFluidAmount();
        int capacity = fluidTile.getTank().getCapacity();
        String fluidName = fluidTile.getTank().getFluid() != null
            ? fluidTile.getTank().getFluid().getFluid().getLocalizedName(
                fluidTile.getTank().getFluid())
            : "vide";
        player.sendMessage(new TextComponentString(
            TextFormatting.LIGHT_PURPLE + "[Fluid Input] "
            + current + "/" + capacity + " mB (" + fluidName + ")"));
        return true;
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
