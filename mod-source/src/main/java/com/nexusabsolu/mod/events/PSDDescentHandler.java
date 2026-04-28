package com.nexusabsolu.mod.events;

import com.nexusabsolu.mod.compat.CM3Bridge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Permet la "descente matryoshka" : depuis l'interieur d'une CM (DIM 144),
 * un clic-droit avec le PSD sur un bloc Compact Machine plonge le joueur
 * dans cette sous-CM, au lieu d'etre ejecte vers l'exterieur.
 *
 * Probleme vanilla CM3 : le PSD a un comportement binaire "dim==144 ?
 * sortir : entrer". Donc clic-droit dans DIM 144 = sortie systematique,
 * meme si on cible un bloc CM enfant. Resultat : impossible de descendre
 * dans une sous-CM via le PSD une fois qu'on est deja dans une CM.
 *
 * Solution : on intercepte PlayerInteractEvent.RightClickBlock AVANT que
 * le PSD vanilla soit consulte, et si toutes les conditions sont reunies
 * (joueur en DIM 144, item = PSD, bloc cible = CM), on tp manuellement
 * vers la sous-CM puis on cancel l'event pour empecher la sortie vanilla.
 *
 * La sortie standard via PSD reste fonctionnelle (clic-droit sur l'air ou
 * sur un bloc non-CM = comportement vanilla = sortie via WSDM exit target).
 */
public class PSDDescentHandler {

    // Y du sol des rooms CM dans DIM 144 (constante CM3 legacy)
    private static final int ROOM_FLOOR_Y = 40;
    // Spawn legerement au-dessus du sol et offset positif (centre approximatif
    // dans une room large 9x9, marge sure pour les autres tailles)
    private static final int SPAWN_Y = ROOM_FLOOR_Y + 1;
    private static final int SPAWN_X_OFFSET = 4;
    private static final int SPAWN_Z_OFFSET = 4;

    private static final String CM_BLOCK_REGISTRY = "compactmachines3:machine";
    private static final String PSD_REGISTRY = "compactmachines3:psd";

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) return;

        // Cote serveur uniquement (le tp doit etre fait server-side)
        if (player.world.isRemote) return;

        // Conditions de declenchement
        if (player.dimension != 144) return; // pas dans DIM 144 = vanilla normal
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!isPSD(stack)) return; // pas le PSD = pas notre affaire
        if (!isCMBlock(player.world, event.getPos())) return;

        // Le joueur veut descendre dans la sous-CM
        TileEntity te = player.world.getTileEntity(event.getPos());
        int targetRoomId = CM3Bridge.getRoomIdFromTE(te);

        if (targetRoomId < 0) {
            // Bloc CM mais pas encore initialise (jamais utilise) -> laisser
            // le vanilla CM3 le gerer (il va creer la room a la 1ere entree).
            // Mais comme on est en DIM 144, le PSD vanilla ferait sortir au
            // lieu d'initialiser. Cas limite, on laisse passer pour pas
            // empirer le bug.
            FMLLog.log.info("[PSDDescent] CM block at " + event.getPos()
                + " has no roomId yet (uninitialized) - letting vanilla handle");
            return;
        }

        FMLLog.log.info("[PSDDescent] " + player.getName()
            + " clicked CM block at " + event.getPos()
            + " (target roomId=" + targetRoomId + ") - descending");

        // Cancel l'event pour bloquer le PSD vanilla qui ferait sortir
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);

        // TP le joueur vers la sous-CM
        if (player instanceof EntityPlayerMP) {
            descendInto((EntityPlayerMP) player, targetRoomId);
        }
    }

    private boolean isPSD(ItemStack stack) {
        if (stack.getItem().getRegistryName() == null) return false;
        return PSD_REGISTRY.equals(stack.getItem().getRegistryName().toString());
    }

    private boolean isCMBlock(net.minecraft.world.World world, BlockPos pos) {
        net.minecraft.block.Block block = world.getBlockState(pos).getBlock();
        if (block.getRegistryName() == null) return false;
        return CM_BLOCK_REGISTRY.equals(block.getRegistryName().toString());
    }

    private void descendInto(EntityPlayerMP player, int roomId) {
        // Position de la room cible : NW corner = (1024 * roomId, 40, 0)
        // Spawn un peu au-dessus du sol, avec un offset pour eviter de
        // spawner dans le mur NW
        double targetX = 1024.0 * roomId + SPAWN_X_OFFSET + 0.5;
        double targetY = SPAWN_Y;
        double targetZ = SPAWN_Z_OFFSET + 0.5;

        // Le joueur reste dans DIM 144 (descente matryoshka), pas besoin
        // de changeDimension. Juste un setLocation simple.
        player.connection.setPlayerLocation(
            targetX, targetY, targetZ,
            player.rotationYaw, player.rotationPitch);

        FMLLog.log.info("[PSDDescent] Teleported " + player.getName()
            + " to room " + roomId + " at (" + targetX + ", " + targetY + ", " + targetZ + ")");
    }
}
