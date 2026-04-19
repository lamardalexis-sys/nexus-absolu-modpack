package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.blocks.machines.furnaces.BlockFurnaceNexus;
import com.nexusabsolu.mod.tiles.furnaces.TileFurnaceNexus;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Upgrade Kit universel pour les Furnaces Nexus.
 *
 * Usage: shift + clic droit sur un Furnace Nexus pose pour l'AMELIORER :
 *  - Debloque la jauge RF (affichage + stockage d'energie)
 *  - Debloque les 4 slots upgrade (RF converter, IO expansion, speed, efficiency)
 *  - Active le onglet Upgrades (ouvre GUI dedie)
 *  - Active les LED ENHANCED sur la texture du bloc (overlay)
 *
 * Irreversible. Consomme a l'activation. Le flag "enhanced" est sauvegarde
 * sur le TileEntity (NBT) et transmis a l'ItemStack en cas de breakBlock,
 * permettant le craft de furnace de tier superieur en conservant le kit.
 *
 * Recipe (CraftTweaker) : Invarium + Compose A + EnderIO item_material:0
 *
 * Implementation : utilise onItemUseFirst (pattern Thermal ItemAugment.java)
 * pour capturer le shift+clic droit AVANT que Minecraft decide d'annuler
 * l'interaction avec le bloc (sneak cancel par defaut du jeu).
 */
public class ItemFurnaceUpgradeKit extends ItemBase {

    public ItemFurnaceUpgradeKit() {
        super("furnace_upgrade_kit");
        setMaxStackSize(16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("\u00A7eKit d'Amelioration Voss");
        tooltip.add("\u00A77Shift + clic droit sur un Furnace Nexus");
        tooltip.add("\u00A77pour debloquer jauge RF + 4 slots upgrade.");
        tooltip.add("\u00A78\u00A7oIrreversible. Le kit est transmis lors");
        tooltip.add("\u00A78\u00A7odes crafts de furnace superieur.");
    }

    /**
     * Pattern Thermal Expansion (ItemAugment.onItemUseFirst) :
     * Capture le shift+clic droit AVANT que Minecraft ne cancel l'interaction
     * avec le bloc a cause du sneak. Si on utilisait onBlockActivated du bloc,
     * le shift empecherait son appel.
     */
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world,
            BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ,
            EnumHand hand) {
        // Necessite shift
        if (!player.isSneaking()) {
            return EnumActionResult.PASS;
        }

        // Bloc cible doit etre un BlockFurnaceNexus
        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockFurnaceNexus)) {
            return EnumActionResult.PASS;
        }

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileFurnaceNexus)) {
            return EnumActionResult.PASS;
        }

        TileFurnaceNexus furnace = (TileFurnaceNexus) te;

        // Deja enhanced ? Rien a faire
        if (furnace.isEnhanced()) {
            if (!world.isRemote) {
                player.sendStatusMessage(
                    new TextComponentString("\u00A7cCe furnace est deja ameliore."),
                    true);
            }
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            furnace.applyEnhancement();
            // Consomme 1 kit (sauf si joueur creatif)
            ItemStack held = player.getHeldItem(hand);
            if (!player.capabilities.isCreativeMode) {
                held.shrink(1);
            }
            // Son + message
            world.playSound(null, pos,
                SoundEvents.BLOCK_ANVIL_USE,
                SoundCategory.BLOCKS,
                0.5F, 2.0F);
            player.sendStatusMessage(
                new TextComponentString(
                    "\u00A7eFurnace ameliore ! Jauge RF + upgrades debloques."),
                true);
        }
        return EnumActionResult.SUCCESS;
    }
}
