package com.nexusabsolu.mod.gui.furnaces;

import com.nexusabsolu.mod.tiles.furnaces.FurnaceTier;

/**
 * Utilitaires d'affichage pour les FurnaceTier : nom francais, couleurs
 * de titre et progress bar par tier.
 *
 * Factorise depuis GuiFurnaceNexus pour etre reutilisable (JEI, tooltips,
 * quests UI potentielles, etc.) et separer les concerns visuels du GUI
 * proprement dit.
 *
 * Toutes les methodes sont statiques.
 */
public final class FurnaceTierStyle {

    private FurnaceTierStyle() {}

    /**
     * Nom d'affichage francais du tier. Pour les tiers non encore implementes
     * (Dark Astral, Gaia, etc.), retourne "Fourneau <registryName>".
     */
    public static String getDisplayName(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return "Fourneau de Fer";
            case GOLD:       return "Fourneau d'Or";
            case INVARIUM:   return "Fourneau d\u0027Invarium";
            case EMERADIC:   return "Fourneau Emeradic";
            case VOSSIUM_IV: return "Fourneau Vossium IV";
            default:         return "Fourneau " + tier.registryName;
        }
    }

    /** Couleur ARGB du titre dans le GUI. Reflete l'ambiance du tier. */
    public static int getTitleColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFFCCCCCC;  // gris acier
            case GOLD:       return 0xFFFFDD60;  // dore
            case INVARIUM:   return 0xFFBBDDBB;  // vert-gris
            case EMERADIC:   return 0xFF80E690;  // emeraude
            case VOSSIUM_IV: return 0xFFC070FF;  // violet Vossium
            default:         return 0xFFDD88FF;  // rose default
        }
    }

    /** Couleur principale (remplissage) de la progress bar de cuisson. */
    public static int getProgressColor(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFF808090;
            case GOLD:       return 0xFFC8A032;
            case INVARIUM:   return 0xFF96A596;
            case EMERADIC:   return 0xFF46AA5A;
            case VOSSIUM_IV: return 0xFF783CAA;
            default:         return 0xFF5050A0;
        }
    }

    /** Couleur "shine" (trait brillant 1px en haut) de la progress bar. */
    public static int getProgressBright(FurnaceTier tier) {
        switch (tier) {
            case IRON:       return 0xFFDDDDEE;
            case GOLD:       return 0xFFFFE680;
            case INVARIUM:   return 0xFFDDEEDD;
            case EMERADIC:   return 0xFF80FF90;
            case VOSSIUM_IV: return 0xFFCC80FF;
            default:         return 0xFF9090FF;
        }
    }
}
