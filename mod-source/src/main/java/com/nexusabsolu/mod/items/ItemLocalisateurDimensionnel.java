package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.NexusAbsoluMod;
import com.nexusabsolu.mod.Reference;
import com.nexusabsolu.mod.init.ModItems;
import com.nexusabsolu.mod.network.NexusPacketHandler;
import com.nexusabsolu.mod.network.PacketOpenLocalisateur;
import com.nexusabsolu.mod.util.IHasModel;
import com.nexusabsolu.mod.util.PlayerVisitedMachines;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Localisateur Dimensionnel.
 *
 * Clic-droit : ouvre une GUI listant les Compact Machines visitees par le
 * joueur. Click sur une ligne = TP direct dans la salle.
 *
 * Don automatique au joueur lors de la sortie de la CM x9 via la Cle de
 * Liberte Activee (cf. ItemCleLiberteActivee).
 *
 * Pas d'OP requis : l'item est l'autorisation. La validation cote serveur
 * (PacketTeleportToMachine.Handler) verifie que le joueur a bien l'item +
 * que la CM cible est dans sa liste visitee, donc pas d'exploit possible.
 */
public class ItemLocalisateurDimensionnel extends Item implements IHasModel {

    public ItemLocalisateurDimensionnel() {
        setRegistryName(Reference.MOD_ID, "localisateur_dimensionnel");
        setUnlocalizedName(Reference.MOD_ID + ".localisateur_dimensionnel");
        setCreativeTab(NexusAbsoluMod.CREATIVE_TAB);
        setMaxStackSize(1);
        ModItems.ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Cote client : juste accepter, le serveur fera le boulot
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (!(player instanceof EntityPlayerMP)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        EntityPlayerMP playerMP = (EntityPlayerMP) player;

        // Lit la liste des CMs visitees et envoie le packet S->C
        int[] visited = PlayerVisitedMachines.getVisited(playerMP);
        NexusPacketHandler.INSTANCE.sendTo(
            new PacketOpenLocalisateur(visited), playerMP);

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                               List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + "Clic-droit : ouvre la liste");
        tooltip.add(TextFormatting.GRAY + "des salles connues.");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_GRAY + ""
            + TextFormatting.ITALIC + "Echo du laboratoire de Voss.");
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true; // Glint enchanté permanent
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0,
            new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}
