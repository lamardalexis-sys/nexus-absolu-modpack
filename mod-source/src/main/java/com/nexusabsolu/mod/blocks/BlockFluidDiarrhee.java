package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

import java.util.Random;

/**
 * Bloc fluide pour la Diarrh33 Liquide.
 *
 * Comportement: NON-PROPAGATEUR (flaque locale).
 * - Place via seau plein -> reste exactement la ou il est verse
 * - Pas de propagation laterale (contrairement a l'eau qui s'etale sur 8 blocs)
 * - Pas de chute non plus: c'est une vraie flaque qui reste sur place
 * - Quantization 1 (= source pure, pas de niveau)
 *
 * Implementation: updateTick() est volontairement vide -> aucune logique
 * de propagation ne s'execute, donc le fluide reste statique.
 * canDisplace() refuse aussi toute substitution de bloc voisin.
 */
public class BlockFluidDiarrhee extends BlockFluidClassic {

    public BlockFluidDiarrhee(Fluid fluid) {
        super(fluid, Material.WATER);
        setRegistryName(Reference.MOD_ID, "diarrhee_liquide");
        setUnlocalizedName(Reference.MOD_ID + ".diarrhee_liquide");
        setCreativeTab(CreativeTabs.MISC);
        // tickRate eleve pour limiter les recalculs (au cas ou)
        setTickRate(20);
        // Quantization 1: pas de niveau intermediaire, juste source ou rien
        this.quantaPerBlock = 1;
        this.quantaPerBlockFloat = 1.0F;
    }

    @Override
    public boolean canDisplace(net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean displaceIfPossible(World world, BlockPos pos) {
        return false;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        // Volontairement vide: pas de propagation, le fluide reste en place.
    }
}
