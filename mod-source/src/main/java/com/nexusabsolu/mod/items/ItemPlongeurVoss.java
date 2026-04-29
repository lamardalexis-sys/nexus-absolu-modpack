package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.compat.CM3Bridge;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Plongeur Voss : outil custom Voss permettant de descendre dans une
 * Compact Machine en cliquant dessus.
 *
 * Contrairement au PSD vanilla CM3 qui sort systematiquement quand on est
 * deja en DIM 144, le Plongeur Voss permet la descente matryoshka sans
 * limite : clic-droit sur un bloc CM et on tp dedans, peu importe la
 * profondeur actuelle.
 *
 * Pour sortir : utiliser le PSD vanilla normalement.
 */
public class ItemPlongeurVoss extends Item implements IHasModel {

    private static final int ROOM_FLOOR_Y = 40;
    private static final int SPAWN_Y = ROOM_FLOOR_Y + 1;
    private static final int SPAWN_OFFSET = 4;

    public ItemPlongeurVoss() {
        setRegistryName(Reference.MOD_ID, "plongeur_voss");
        setUnlocalizedName(Reference.MOD_ID + ".plongeur_voss");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxStackSize(1);
        ModItems.ITEMS.add(this);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        // Server-side seulement
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }

        // Verifie que le bloc cible est bien une Compact Machine
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        if (block.getRegistryName() == null
                || !"compactmachines3:machine".equals(block.getRegistryName().toString())) {
            // Pas un bloc CM, message d'aide
            if (player instanceof EntityPlayerMP) {
                TextComponentString msg = new TextComponentString(
                    "Le Plongeur Voss ne fonctionne que sur les Compact Machines.");
                msg.getStyle().setColor(TextFormatting.GRAY);
                player.sendMessage(msg);
            }
            return EnumActionResult.PASS;
        }

        // Lit le roomId de la TE
        TileEntity te = world.getTileEntity(pos);
        int roomId = CM3Bridge.getRoomIdFromTE(te);
        if (roomId < 0) {
            if (player instanceof EntityPlayerMP) {
                TextComponentString msg = new TextComponentString(
                    "Cette Compact Machine n'a pas encore ete initialisee. "
                    + "Utilise le PSD une fois pour la creer, puis reessaie.");
                msg.getStyle().setColor(TextFormatting.YELLOW);
                player.sendMessage(msg);
            }
            return EnumActionResult.PASS;
        }

        // TP le joueur dans la sous-CM
        if (player instanceof EntityPlayerMP) {
            descendInto((EntityPlayerMP) player, roomId);
        }
        return EnumActionResult.SUCCESS;
    }

    private void descendInto(EntityPlayerMP player, int roomId) {
        double targetX = 1024.0 * roomId + SPAWN_OFFSET + 0.5;
        double targetY = SPAWN_Y;
        double targetZ = SPAWN_OFFSET + 0.5;

        FMLLog.log.info("[PlongeurVoss] " + player.getName()
            + " descending into room " + roomId
            + " at (" + targetX + ", " + targetY + ", " + targetZ + ")");

        // Si le joueur n'est pas en DIM 144, le tp doit changer de dimension
        if (player.dimension != 144) {
            // Cross-dimensional teleport vers DIM 144
            net.minecraft.server.MinecraftServer server = player.getServer();
            if (server == null) return;
            net.minecraft.world.WorldServer dim144 = server.getWorld(144);
            if (dim144 == null) {
                FMLLog.log.warn("[PlongeurVoss] DIM 144 not loaded - cannot descend");
                return;
            }
            final double fx = targetX, fy = targetY, fz = targetZ;
            player.changeDimension(144, new net.minecraftforge.common.util.ITeleporter() {
                @Override
                public void placeEntity(World w, net.minecraft.entity.Entity entity, float yaw) {
                    entity.setLocationAndAngles(fx, fy, fz, yaw, 0);
                }
            });
        } else {
            // Deja en DIM 144, tp interne
            player.connection.setPlayerLocation(
                targetX, targetY, targetZ,
                player.rotationYaw, player.rotationPitch);
        }

        // Petit son de descente (whoosh)
        player.world.playSound(null, player.posX, player.posY, player.posZ,
            net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT,
            SoundCategory.PLAYERS, 0.5F, 1.5F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
