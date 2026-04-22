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
    /** Tier 1..4 pour IO_EXPANSION (slots 3/5/7/9). 0 pour les autres categories. */
    private final int tier;

    public ItemFurnaceUpgrade(String name, FurnaceUpgrade category) {
        this(name, category, 0);
    }

    public ItemFurnaceUpgrade(String name, FurnaceUpgrade category, int tier) {
        super(name);
        this.category = category;
        this.tier = tier;
        // Max stack size de la categorie
        setMaxStackSize(category.maxStackSize);
    }

    public FurnaceUpgrade getCategory() { return category; }

    /** 1..4 pour IO_EXPANSION, 0 pour les autres. */
    public int getTier() { return tier; }

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
            case IO_EXPANSION:  return "\u00A7bExtension I/O " + toRoman(tier);
            case SPEED_BOOSTER: return "\u00A7cAccelerateur";
            case EFFICIENCY:    return "\u00A7aCarte d'Efficience";
            default:            return category.registrySuffix;
        }
    }

    private String getTooltipLine2() {
        switch (category) {
            case RF_CONVERTER:  return "Active mode RF : consomme energie au lieu de coal";
            case IO_EXPANSION:  return getIOExpansionDesc();
            case SPEED_BOOSTER: return "+30% vitesse par item (cumul. conso x1.30 par item)";
            case EFFICIENCY:    return "-20% conso par item (multiplicatif cumul.)";
            default:            return "";
        }
    }

    /** Description dynamique selon le tier IO Expansion. */
    private String getIOExpansionDesc() {
        int slots = 1 + 2 * tier;  // tier 0 = 1, tier 1 = 3, tier 2 = 5, tier 3 = 7, tier 4 = 9
        return slots + " slots in + " + slots + " slots out (cuisson parallele)";
    }

    /** Convertit 1..4 en chiffre romain. */
    private String toRoman(int n) {
        switch (n) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            default: return "";
        }
    }
}
