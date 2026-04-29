package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.events.CMPlacementHandler;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.util.IHasModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Localisateur Voss : item OP-only qui permet de retracer la chaine
 * matryoshka des Compact Machines.
 *
 * Usage:
 *  - Clic-droit sur un bloc CM : lit son NBT 'parent_room_id' et affiche
 *    la room parente. Si OP, propose le tp direct.
 *  - Clic-droit en l'air en DIM 144 : affiche la roomId courante.
 *
 * L'item peut etre tenu par n'importe quel joueur (recette accessible)
 * mais ne fonctionne que pour les operateurs (permission >= 2).
 */
public class ItemLocalisateurVoss extends Item implements IHasModel {

    public ItemLocalisateurVoss() {
        setRegistryName(Reference.MOD_ID, "localisateur_voss");
        setUnlocalizedName(Reference.MOD_ID + ".localisateur_voss");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxStackSize(1);
        ModItems.ITEMS.add(this);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
                                      EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {
        if (world.isRemote) return EnumActionResult.SUCCESS;
        if (!(player instanceof EntityPlayerMP)) return EnumActionResult.PASS;

        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // OP check
        if (!playerMP.canUseCommand(2, "voss_locate")) {
            sendMsg(playerMP, TextFormatting.RED,
                "Le Localisateur Voss requiert des privileges d'operateur (OP).");
            return EnumActionResult.FAIL;
        }

        // Verifie que le bloc cible est bien une Compact Machine
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        if (block.getRegistryName() == null
                || !"compactmachines3:machine".equals(block.getRegistryName().toString())) {
            sendMsg(playerMP, TextFormatting.GRAY,
                "Cible une Compact Machine pour voir sa chaine de parents.");
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            sendMsg(playerMP, TextFormatting.RED, "TileEntity manquant.");
            return EnumActionResult.FAIL;
        }

        NBTTagCompound nbt = te.getTileData();

        // Affiche les infos
        sendMsg(playerMP, TextFormatting.GOLD, "=== Localisateur Voss ===");
        sendMsg(playerMP, TextFormatting.GRAY, "Bloc: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        sendMsg(playerMP, TextFormatting.GRAY, "Dim courante: " + world.provider.getDimension());

        // Lit le roomId de ce bloc CM (sa propre room)
        int ownRoomId = com.nexusabsolu.mod.compat.CM3Bridge.getRoomIdFromTE(te);
        if (ownRoomId >= 0) {
            sendMsg(playerMP, TextFormatting.AQUA, "Room ID de ce bloc: " + ownRoomId);
        } else {
            sendMsg(playerMP, TextFormatting.YELLOW,
                "Ce bloc n'est pas encore initialise (jamais utilise).");
        }

        // Lit le NBT parent
        if (nbt.hasKey(CMPlacementHandler.NBT_PARENT_ROOM_ID)) {
            int parentRoomId = nbt.getInteger(CMPlacementHandler.NBT_PARENT_ROOM_ID);
            sendMsg(playerMP, TextFormatting.GREEN,
                "Room parente (ou ce bloc a ete pose): #" + parentRoomId);
            sendMsg(playerMP, TextFormatting.GRAY,
                "Pour y aller: /voss_goto " + parentRoomId);

            // Tp direct via shift-clic
            if (player.isSneaking()) {
                tpToRoom(playerMP, parentRoomId);
                sendMsg(playerMP, TextFormatting.GREEN,
                    "TP vers la room parente #" + parentRoomId);
            } else {
                sendMsg(playerMP, TextFormatting.GRAY,
                    "(Shift+clic pour tp directement)");
            }
        } else {
            sendMsg(playerMP, TextFormatting.YELLOW,
                "Pas de room parente enregistree (CM ancienne ou posee en overworld).");
            sendMsg(playerMP, TextFormatting.GRAY,
                "Utilise /voss_goto <id> pour explorer manuellement.");
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public net.minecraft.util.ActionResult<net.minecraft.item.ItemStack>
            onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new net.minecraft.util.ActionResult<>(EnumActionResult.SUCCESS,
                player.getHeldItem(hand));
        }
        if (!(player instanceof EntityPlayerMP)) {
            return new net.minecraft.util.ActionResult<>(EnumActionResult.PASS,
                player.getHeldItem(hand));
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // OP check
        if (!playerMP.canUseCommand(2, "voss_locate")) {
            sendMsg(playerMP, TextFormatting.RED,
                "Le Localisateur Voss requiert des privileges d'operateur (OP).");
            return new net.minecraft.util.ActionResult<>(EnumActionResult.FAIL,
                player.getHeldItem(hand));
        }

        // Affiche infos sur la position courante
        sendMsg(playerMP, TextFormatting.GOLD, "=== Localisateur Voss ===");
        sendMsg(playerMP, TextFormatting.GRAY,
            "Position: " + (int) playerMP.posX + ", " + (int) playerMP.posY + ", " + (int) playerMP.posZ);
        sendMsg(playerMP, TextFormatting.GRAY, "Dim: " + playerMP.dimension);

        if (playerMP.dimension == 144) {
            int roomId = com.nexusabsolu.mod.compat.CM3Bridge.getIdForPos(
                world, playerMP.getPosition());
            sendMsg(playerMP, TextFormatting.AQUA,
                "Room courante: #" + roomId);
            sendMsg(playerMP, TextFormatting.GRAY,
                "Astuce: clique sur un bloc CM pour voir sa room parente.");
            sendMsg(playerMP, TextFormatting.GRAY,
                "Pour explorer une room par ID: /voss_goto <id>");
        } else {
            sendMsg(playerMP, TextFormatting.GRAY,
                "Tu n'es pas dans une CM. Clique sur un bloc CM pour info.");
        }

        return new net.minecraft.util.ActionResult<>(EnumActionResult.SUCCESS,
            player.getHeldItem(hand));
    }

    private void tpToRoom(EntityPlayerMP player, int roomId) {
        final double targetX = 1024.0 * roomId + 4.5;
        final double targetY = 41.0;
        final double targetZ = 4.5;

        FMLLog.log.info("[Localisateur] " + player.getName()
            + " tp to room #" + roomId);

        if (player.dimension != 144) {
            player.changeDimension(144, new ITeleporter() {
                @Override
                public void placeEntity(World w, net.minecraft.entity.Entity entity, float yaw) {
                    entity.setLocationAndAngles(targetX, targetY, targetZ, yaw, 0);
                }
            });
        } else {
            player.connection.setPlayerLocation(targetX, targetY, targetZ,
                player.rotationYaw, player.rotationPitch);
        }

        player.world.playSound(null, player.posX, player.posY, player.posZ,
            net.minecraft.init.SoundEvents.ENTITY_ENDERMEN_TELEPORT,
            SoundCategory.PLAYERS, 0.5F, 1.0F);
    }

    private void sendMsg(EntityPlayerMP player, TextFormatting color, String msg) {
        TextComponentString text = new TextComponentString(msg);
        text.getStyle().setColor(color);
        player.sendMessage(text);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
