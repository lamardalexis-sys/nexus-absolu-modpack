package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.init.ModItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

/**
 * Minerai Tutuosss - Overworld uniquement.
 * Drop du tuosss_row (comme le diamond ore drop des diamonds).
 * Rarete comparable au diamant: veins de 6 max, Y 5-16.
 *
 * Hardness 3.0, harvest level 2 (iron pickaxe), exp 3-7.
 * Bonus fortune comme le diamant.
 */
public class BlockTutuosssOre extends BlockNexusOre {

    public BlockTutuosssOre() {
        super("tutuosss_ore", 3.0F, 2, 3, 7);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.TUOSSS_ROW;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune > 0) {
            int bonus = random.nextInt(fortune + 2) - 1;
            if (bonus < 0) bonus = 0;
            return 1 + bonus;
        }
        return 1;
    }
}
