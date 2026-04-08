package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Cle de Liberte Activee - the consumable teleport item.
 *
 * Obtained by right-clicking the Ecran de Controle of an active Portail
 * Voss while holding a plain Cle de Liberte. The portal drains its
 * energy and fluid in the process.
 *
 * On right-click (anywhere, any dimension):
 * 1. Teleport the player to the overworld at a random point exactly
 *    500 blocks from the world spawn (top solid block + 1).
 * 2. Consume the item.
 * 3. Display the AGE 2 transition message.
 *
 * Single-use. Stack size = 1. Enchanted glint for visual feedback.
 */
public class ItemCleLiberteActivee extends ItemBase {

    public ItemCleLiberteActivee() {
        super("cle_liberte_activee");
        setMaxStackSize(1);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;  // Enchanted glint to distinguish from inactive key
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.LIGHT_PURPLE + "La cle resonne. Elle veut partir.");
        tooltip.add(TextFormatting.DARK_GRAY + "\"Le ciel t'attend.\"");
        tooltip.add("");
        tooltip.add(TextFormatting.YELLOW + "Clic droit pour t'echapper.");
        tooltip.add(TextFormatting.DARK_GRAY + "Usage unique.");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player,
                                                     EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Client side: just return success so the server also triggers
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (!(player instanceof EntityPlayerMP)) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        MinecraftServer server = playerMP.getServer();
        if (server == null) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        WorldServer overworld = server.getWorld(0);
        if (overworld == null) {
            player.sendMessage(new TextComponentString(
                TextFormatting.RED + "[ERREUR] Impossible de charger l'overworld."));
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        // Compute destination: random direction, exactly 500 blocks from spawn
        BlockPos spawn = overworld.getSpawnPoint();
        Random rand = world.rand;
        double angle = rand.nextDouble() * 2.0 * Math.PI;
        int destX = spawn.getX() + (int) Math.round(Math.cos(angle) * 500.0);
        int destZ = spawn.getZ() + (int) Math.round(Math.sin(angle) * 500.0);

        // Load the destination chunk so getHeight() works reliably
        overworld.getChunkFromBlockCoords(new BlockPos(destX, 64, destZ));
        int destY = overworld.getHeight(destX, destZ);
        // Safety clamp in case getHeight returns something weird
        if (destY < 4) destY = 64;
        if (destY > 250) destY = 250;

        final int fDestX = destX;
        final int fDestY = destY;
        final int fDestZ = destZ;

        // Teleport (cross-dimensional if needed)
        if (playerMP.dimension != 0) {
            playerMP.changeDimension(0, new ITeleporter() {
                @Override
                public void placeEntity(World w, Entity entity, float yaw) {
                    entity.setLocationAndAngles(
                        fDestX + 0.5, fDestY, fDestZ + 0.5, yaw, 0);
                }
            });
        } else {
            playerMP.setPositionAndUpdate(fDestX + 0.5, fDestY, fDestZ + 0.5);
        }

        // Play a dramatic sound at the arrival location
        overworld.playSound(null, fDestX, fDestY, fDestZ,
            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.5F);
        overworld.playSound(null, fDestX, fDestY, fDestZ,
            SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 0.6F, 1.2F);

        // Epic arrival message
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "===================================="));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "              L I B R E"));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GOLD + "" + TextFormatting.BOLD
            + "===================================="));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GRAY + "" + TextFormatting.ITALIC
            + "\"Tu vois le ciel pour la premiere fois.\""));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC
            + "- Toi, premier journal en surface."));
        playerMP.sendMessage(new TextComponentString(""));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Age 1 termine. Age 2 : La Surface."));

        // Consume the item
        stack.shrink(1);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
