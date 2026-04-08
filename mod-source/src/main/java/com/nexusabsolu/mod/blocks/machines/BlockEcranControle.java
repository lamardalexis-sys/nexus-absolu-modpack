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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

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

                // Is the player holding a Cle de Liberte (inactive)?
                ItemStack held = player.getHeldItem(hand);
                boolean holdingKey = !held.isEmpty()
                    && held.getItem() == ModItems.CLE_LIBERTE;

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
                } else if (!holdingKey) {
                    // Everything is ready but no key in hand - tell the player
                    player.sendMessage(new TextComponentString(
                        "\u00a7d[Portail Voss] \u00a7lLe portail est stable."));
                    player.sendMessage(new TextComponentString(
                        "\u00a77Tiens une \u00a7dCle de Liberte\u00a77 en main et clique a nouveau."));
                } else {
                    // ALL GOOD: drain the portal AND teleport in one go
                    portal.activate(player);

                    // Consume the inactive key
                    held.shrink(1);

                    // Run the full escape sequence (TP + place CM x9 + messages)
                    if (player instanceof net.minecraft.entity.player.EntityPlayerMP) {
                        net.minecraft.entity.player.EntityPlayerMP playerMP =
                            (net.minecraft.entity.player.EntityPlayerMP) player;
                        com.nexusabsolu.mod.items.ItemCleLiberteActivee.performEscape(
                            playerMP, world);

                        // Auto-complete Q149 "L I B R E" via BetterQuesting admin command
                        net.minecraft.server.MinecraftServer server = playerMP.getServer();
                        if (server != null) {
                            try {
                                server.commandManager.executeCommand(server,
                                    "/bq_admin complete 149 " + playerMP.getName());
                                net.minecraftforge.fml.common.FMLLog.log.info(
                                    "[PortailVoss] bq_admin complete 149 "
                                    + playerMP.getName() + " executed");
                            } catch (Throwable t) {
                                net.minecraftforge.fml.common.FMLLog.log.warn(
                                    "[PortailVoss] bq_admin complete failed: "
                                    + t.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TilePortalVoss)) return;
        TilePortalVoss portal = (TilePortalVoss) te;

        if (portal.isStructureFormed()) {
            // Enchantment particles swirling around the EC
            for (int i = 0; i < 3; i++) {
                double px = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 1.5;
                double py = pos.getY() + 0.5 + (rand.nextDouble() - 0.5) * 1.5;
                double pz = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 1.5;
                world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                    px, py, pz,
                    (pos.getX() + 0.5 - px) * 0.3,
                    (pos.getY() + 0.5 - py) * 0.3,
                    (pos.getZ() + 0.5 - pz) * 0.3);
            }

            // Portal particles in the gate zone (layers 3-4, between the pillars)
            // Pillars are at dx=-1 and dx=+1, portal at dx=0, dy=-2 and dy=-1
            int rotation = portal.getActiveRotation();
            if (rotation >= 0) {
                for (int dy = -2; dy <= -1; dy++) {
                    for (int i = 0; i < 4; i++) {
                        double localX = (rand.nextDouble() - 0.5) * 0.8;
                        double localY = rand.nextDouble();
                        double localZ = (rand.nextDouble() - 0.5) * 0.8;

                        // Rotate to match structure orientation
                        double wx = pos.getX() + 0.5 + rotX(localX, localZ, rotation);
                        double wy = pos.getY() + dy + localY;
                        double wz = pos.getZ() + 0.5 + rotZ(localX, localZ, rotation);

                        world.spawnParticle(EnumParticleTypes.PORTAL,
                            wx, wy, wz,
                            (rand.nextDouble() - 0.5) * 0.1,
                            rand.nextDouble() * 0.2,
                            (rand.nextDouble() - 0.5) * 0.1);
                    }
                }

                // Purple dust particles rising from the base
                if (rand.nextInt(3) == 0) {
                    double bx = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 5;
                    double bz = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 5;
                    world.spawnParticle(EnumParticleTypes.SPELL_WITCH,
                        bx, pos.getY() - 5.0 + rand.nextDouble(), bz,
                        0, 0.05, 0);
                }
            }
        }

        if (portal.isPortalActive()) {
            // Dense portal effect when activated
            for (int i = 0; i < 8; i++) {
                double px = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 3;
                double py = pos.getY() - 2.0 + rand.nextDouble() * 3;
                double pz = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 3;
                world.spawnParticle(EnumParticleTypes.PORTAL,
                    px, py, pz,
                    (rand.nextDouble() - 0.5) * 0.5,
                    rand.nextDouble() * 0.5,
                    (rand.nextDouble() - 0.5) * 0.5);
            }
        }
    }

    private static double rotX(double x, double z, int rotation) {
        switch (rotation) {
            case 0: return x;
            case 1: return z;
            case 2: return -x;
            case 3: return -z;
            default: return x;
        }
    }

    private static double rotZ(double x, double z, int rotation) {
        switch (rotation) {
            case 0: return z;
            case 1: return -x;
            case 2: return -z;
            case 3: return x;
            default: return z;
        }
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
