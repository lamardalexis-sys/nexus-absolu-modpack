package com.nexusabsolu.mod.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
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
 * Recipe (CraftTweaker) :
 *   Tech-tier: Invarium + Compose A + EnderIO Basic Capacitor + circuits
 *   Age 1 craft target. Voir scripts/Age_Furnaces.zs.
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
}
