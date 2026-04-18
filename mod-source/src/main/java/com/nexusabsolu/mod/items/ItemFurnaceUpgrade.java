package com.nexusabsolu.mod.items;

import com.nexusabsolu.mod.tiles.furnaces.FurnaceUpgrade;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item upgrade pour les Furnaces Nexus. Un item par categorie FurnaceUpgrade.
 *
 * La categorie determine:
 *  - dans quel slot upgrade du GUI il peut aller (via Container.isItemValid)
 *  - le max stack size (via getItemStackLimit)
 *  - le tooltip affiche
 *  - l'effet dans TileFurnaceNexus (logique de vitesse/conso/RF mode)
 */
public class ItemFurnaceUpgrade extends ItemBase {

    private final FurnaceUpgrade category;

    public ItemFurnaceUpgrade(String name, FurnaceUpgrade category) {
        super(name);
        this.category = category;
        // Max stack size de la categorie
        setMaxStackSize(category.maxStackSize);
    }

    public FurnaceUpgrade getCategory() { return category; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world,
                                List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(getTooltipLine1());
        tooltip.add("\u00A77" + getTooltipLine2());
        tooltip.add("\u00A78Max stack: " + category.maxStackSize);
    }

    private String getTooltipLine1() {
        switch (category) {
            case RF_CONVERTER:  return "\u00A7eConvertisseur RF";
            case IO_EXPANSION:  return "\u00A7bExtension I/O";
            case SPEED_BOOSTER: return "\u00A7cAccelerateur";
            case EFFICIENCY:    return "\u00A7aCarte d'Efficience";
            default:            return category.registrySuffix;
        }
    }

    private String getTooltipLine2() {
        switch (category) {
            case RF_CONVERTER:  return "Active mode RF : consomme energie au lieu de coal";
            case IO_EXPANSION:  return "Augmente les slots in/out (reserve Sprint B2)";
            case SPEED_BOOSTER: return "+30% vitesse par item (cumul. conso x1.40)";
            case EFFICIENCY:    return "-8% conso par item (multiplicatif)";
            default:            return "";
        }
    }
}
