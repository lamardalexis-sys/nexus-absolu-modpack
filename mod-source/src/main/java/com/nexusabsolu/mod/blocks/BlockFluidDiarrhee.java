package com.nexusabsolu.mod.blocks;

import com.nexusabsolu.mod.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Bloc fluide pour la Diarrh33 Liquide.
 * Utilise BlockFluidClassic standard de Forge:
 * - placeable au sol via clic droit avec un seau plein
 * - se propage comme l'eau (mais avec viscosite/densite custom du Fluid)
 * - matiere WATER pour comportement fluide standard (gravite, push entites)
 * - tickRate 8 (plus visqueux que l'eau qui est tickRate 5)
 */
public class BlockFluidDiarrhee extends BlockFluidClassic {

    public BlockFluidDiarrhee(Fluid fluid) {
        super(fluid, Material.WATER);
        setRegistryName(Reference.MOD_ID, "diarrhee_liquide");
        setUnlocalizedName(Reference.MOD_ID + ".diarrhee_liquide");
        setCreativeTab(CreativeTabs.MISC);
        // Plus visqueux que l'eau (tickRate 5) - se propage plus lentement
        setTickRate(8);
    }
}
