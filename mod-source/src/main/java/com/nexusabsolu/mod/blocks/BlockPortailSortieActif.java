package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModBlocks;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * Portail de Sortie TF - active portal block.
 *
 * Spawned by ItemCatalyseurSortie when activating a valid Obsidienne Voss
 * frame (6x5, interior 4x3). When an entity collides with this block,
 * it is teleported to DIM 145 (the "overworld simulation" - Age 2 Phase 2).
 *
 * Cannot be obtained or placed directly by players. Drops nothing when
 * broken (rare, since Obsidienne Voss frame is hard to break too).
 *
 * Blockstate property AXIS = X or Z (vertical portal, oriented along
 * world X axis or Z axis like vanilla nether portal).
 *
 * Sprint 3 - v1.0.350+
 */
public class BlockPortailSortieActif extends Block implements IHasModel {

    public static final PropertyEnum<EnumFacing.Axis> AXIS =
        PropertyEnum.create("axis", EnumFacing.Axis.class,
            EnumFacing.Axis.X, EnumFacing.Axis.Z);

    /** Destination dimension: DIM 145 = overworld simulation (Age 2 Phase 2). */
    public static final int DEST_DIM = 145;

    /** Cooldown (ms) between two teleports for the same player. Prevents
     * the player from being teleported back-and-forth. */
    private static final long TP_COOLDOWN_MS = 3000L;

    public BlockPortailSortieActif() {
        super(Material.PORTAL);
        setUnlocalizedName("portail_sortie_actif");
        setRegistryName(Reference.MOD_ID, "portail_sortie_actif");
        setHardness(-1.0F);            // Unbreakable like bedrock... but we want it
        setResistance(2000.0F);         // ...mineable by creative or explosion-resistant
        setSoundType(SoundType.GLASS);
        setLightLevel(0.9F);
        setTickRandomly(true);
        // No creative tab: not obtainable directly
        setDefaultState(this.blockState.getBaseState()
            .withProperty(AXIS, EnumFacing.Axis.X));
        ModBlocks.BLOCKS.add(this);
        // Note: NO ItemBlock - this block is spawned only by the Catalyseur.
    }

    // === Block state ===

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(AXIS,
            (meta & 1) == 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AXIS) == EnumFacing.Axis.X ? 0 : 1;
    }

    // === Render properties ===

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    // === Collision: portal is walkable (no collision box) ===

    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return NULL_AABB;  // No collision - entities walk through
    }

    // === Teleport on entity contact ===

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (world.isRemote) return;
        if (!(entity instanceof EntityPlayerMP)) return;

        EntityPlayerMP player = (EntityPlayerMP) entity;

        // Cooldown: avoid bouncing
        long now = System.currentTimeMillis();
        long lastTp = player.getEntityData().getLong("nexus_tf_portal_last_tp");
        if (now - lastTp < TP_COOLDOWN_MS) return;
        player.getEntityData().setLong("nexus_tf_portal_last_tp", now);

        teleportToDim145(player, world);
    }

    private void teleportToDim145(EntityPlayerMP player, World currentWorld) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        WorldServer dest = server.getWorld(DEST_DIM);
        if (dest == null) {
            player.sendMessage(new TextComponentString(
                TextFormatting.RED + "[ERREUR] DIM " + DEST_DIM + " ne peut pas etre chargee."));
            FMLLog.log.warn("[PortailSortie] Impossible de charger DIM " + DEST_DIM);
            return;
        }

        // Spawn at (0, getHeight, 0) for now - will refine in Sprint 5/6
        // when DIM 145 has a proper spawn structure.
        int destX = 0;
        int destZ = 0;
        dest.getChunkFromBlockCoords(new BlockPos(destX, 64, destZ));
        int destY = dest.getHeight(destX, destZ);
        if (destY < 4) destY = 64;
        if (destY > 250) destY = 250;

        // Clear 3x3x3 air pocket + ensure floor (same pattern as performEscape)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    dest.setBlockToAir(new BlockPos(destX + dx, destY + dy, destZ + dz));
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = new BlockPos(destX + dx, destY - 1, destZ + dz);
                if (dest.isAirBlock(floor) || !dest.getBlockState(floor).isFullBlock()) {
                    dest.setBlockState(floor,
                        net.minecraft.init.Blocks.DIRT.getDefaultState(), 2);
                }
            }
        }

        // Cross-dim teleport
        final int fX = destX;
        final int fY = destY;
        final int fZ = destZ;
        if (player.dimension != DEST_DIM) {
            player.changeDimension(DEST_DIM, new ITeleporter() {
                @Override
                public void placeEntity(World w, Entity e, float yaw) {
                    e.setLocationAndAngles(fX + 0.5, fY, fZ + 0.5, yaw, 0);
                }
            });
        } else {
            player.setPositionAndUpdate(fX + 0.5, fY, fZ + 0.5);
        }

        dest.playSound(null, fX, fY, fZ,
            SoundEvents.BLOCK_PORTAL_TRAVEL, SoundCategory.BLOCKS, 1.0F, 0.5F);

        // Voss-flavored transition message
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "===================================="));
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "       D E U X I E M E   C I E L"));
        player.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "===================================="));
        player.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + "\"Tu es sorti de la foret. Tu crois.\""));
        player.sendMessage(new TextComponentString(
            TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "- Voss. Probablement."));
        player.sendMessage(new TextComponentString(""));
        player.sendMessage(new TextComponentString(
            TextFormatting.GREEN
            + "Age 2 - Phase 2 : Le Ciel Truque."));

        FMLLog.log.info("[PortailSortie] Player " + player.getName()
            + " teleported from DIM " + currentWorld.provider.getDimension()
            + " to DIM " + DEST_DIM);
    }

    // === Particles when player nearby ===

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        for (int i = 0; i < 4; i++) {
            double px = pos.getX() + rand.nextFloat();
            double py = pos.getY() + rand.nextFloat();
            double pz = pos.getZ() + rand.nextFloat();
            double vx = (rand.nextFloat() - 0.5) * 0.1;
            double vy = (rand.nextFloat() - 0.5) * 0.1;
            double vz = (rand.nextFloat() - 0.5) * 0.1;
            world.spawnParticle(EnumParticleTypes.PORTAL, px, py, pz, vx, vy, vz);
        }
    }

    // === Drops nothing ===

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return net.minecraft.init.Items.AIR;
    }

    @Override
    public int quantityDropped(Random rand) {
        return 0;
    }

    @Override
    public void registerModels() {
        // Pas d'item form - ce bloc n'est pas obtainable
    }
}
