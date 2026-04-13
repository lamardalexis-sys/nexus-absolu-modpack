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

        // Run the full escape sequence (kept for backward compat: players
        // who already had this item in their inventory before v1.0.150
        // can still use it the old 2-clic way).
        boolean ok = performEscape((EntityPlayerMP) player, world);
        if (ok) {
            stack.shrink(1);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    /**
     * Public static escape sequence - shared by ItemCleLiberteActivee.onItemRightClick
     * (legacy 2-click flow) and BlockEcranControle.onBlockActivated (new 1-click flow
     * where the player clicks the screen with a Cle de Liberte and is teleported
     * directly).
     *
     * Performs all 6 steps of the Age 1 -> Age 2 transition:
     * 1. Identify the player's current CM room id (must be called BEFORE teleport)
     * 2. Compute a destination point 500 blocks from the overworld spawn
     * 3. Clear a 3x3x3 air pocket and ensure solid floor
     * 4. Place a CM x9 block linked to the player's current room
     * 5. Teleport the player to the destination (cross-dimensional if needed)
     * 6. Play sounds + display the epic LIBRE message
     *
     * Returns true on success, false on critical error (overworld unloaded,
     * server null, etc.). The caller is responsible for consuming/transforming
     * any item involved.
     */
    public static boolean performEscape(EntityPlayerMP playerMP, World currentWorld) {
        MinecraftServer server = playerMP.getServer();
        if (server == null) return false;

        WorldServer overworld = server.getWorld(0);
        if (overworld == null) {
            playerMP.sendMessage(new TextComponentString(
                TextFormatting.RED + "[ERREUR] Impossible de charger l'overworld."));
            return false;
        }

        // === STEP 1: Identify the CM room the player is currently in ===
        // Must happen BEFORE teleport, while the player is still inside DIM 144.
        net.minecraftforge.fml.common.FMLLog.log.info(
            "[CleLiberte] performEscape: player=" + playerMP.getName()
            + " dim=" + playerMP.dimension
            + " pos=" + playerMP.getPosition());

        int currentRoomId = com.nexusabsolu.mod.compat.CM3Bridge.getIdForPos(
            currentWorld, playerMP.getPosition());
        String bridgeReason = com.nexusabsolu.mod.compat.CM3Bridge.getLastFailureReason();
        net.minecraftforge.fml.common.FMLLog.log.info(
            "[CleLiberte] getIdForPos returned " + currentRoomId
            + " (bridge reason: " + bridgeReason + ")");

        // === STEP 2: Compute destination in overworld ===
        // Exactly 500 blocks from spawn, random angle
        BlockPos spawn = overworld.getSpawnPoint();
        Random rand = currentWorld.rand;
        double angle = rand.nextDouble() * 2.0 * Math.PI;
        int destX = spawn.getX() + (int) Math.round(Math.cos(angle) * 500.0);
        int destZ = spawn.getZ() + (int) Math.round(Math.sin(angle) * 500.0);

        // Load the destination chunk and find the ground level
        overworld.getChunkFromBlockCoords(new BlockPos(destX, 64, destZ));
        int destY = overworld.getHeight(destX, destZ);
        if (destY < 4) destY = 64;
        if (destY > 250) destY = 250;

        // === STEP 3: Clear a 3x3x3 safe zone above ground ===
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    overworld.setBlockToAir(new BlockPos(destX + dx, destY + dy, destZ + dz));
                }
            }
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floor = new BlockPos(destX + dx, destY - 1, destZ + dz);
                if (overworld.isAirBlock(floor) || !overworld.getBlockState(floor).isFullBlock()) {
                    overworld.setBlockState(floor,
                        net.minecraft.init.Blocks.DIRT.getDefaultState(), 2);
                }
            }
        }

        // === STEP 4: Place the linked CM x9 block next to the player ===
        BlockPos cmBlockPos = new BlockPos(destX + 1, destY, destZ);
        boolean cmPlaced = false;
        String placeFailReason = null;

        if (!com.nexusabsolu.mod.compat.CM3Bridge.isAvailable()) {
            placeFailReason = "CM3Bridge indisponible: "
                + com.nexusabsolu.mod.compat.CM3Bridge.getLastFailureReason();
        } else if (currentRoomId < 0) {
            placeFailReason = "Pas dans une salle CM (id=-1). Bridge reason: "
                + com.nexusabsolu.mod.compat.CM3Bridge.getLastFailureReason();
        } else {
            cmPlaced = com.nexusabsolu.mod.compat.CM3Bridge.placeLinkedMachine(
                overworld, cmBlockPos,
                com.nexusabsolu.mod.compat.CM3Bridge.SIZE_LARGE,
                currentRoomId,
                false); // ne PAS écraser l'exit target — on garde la matryoshka
            if (!cmPlaced) {
                placeFailReason = com.nexusabsolu.mod.compat.CM3Bridge.getLastFailureReason();
            }
        }
        net.minecraftforge.fml.common.FMLLog.log.info(
            "[CleLiberte] Placement result: placed=" + cmPlaced
            + " reason=" + placeFailReason);

        // === STEP 5: Teleport the player ===
        final int fDestX = destX;
        final int fDestY = destY;
        final int fDestZ = destZ;
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

        // === STEP 6: Dramatic sounds + epic message ===
        overworld.playSound(null, fDestX, fDestY, fDestZ,
            SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 0.5F);
        overworld.playSound(null, fDestX, fDestY, fDestZ,
            SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, 0.6F, 1.2F);

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

        if (cmPlaced) {
            playerMP.sendMessage(new TextComponentString(
                TextFormatting.LIGHT_PURPLE
                + "Ta Compact Machine est a cote de toi."));
        } else {
            playerMP.sendMessage(new TextComponentString(
                TextFormatting.RED
                + "[Attention] Pas de Compact Machine placee:"));
            playerMP.sendMessage(new TextComponentString(
                TextFormatting.GRAY + "  " + placeFailReason));
        }
        playerMP.sendMessage(new TextComponentString(""));
        playerMP.sendMessage(new TextComponentString(
            TextFormatting.GREEN + "Age 1 termine. Age 2 : La Surface."));

        return true;
    }
}
