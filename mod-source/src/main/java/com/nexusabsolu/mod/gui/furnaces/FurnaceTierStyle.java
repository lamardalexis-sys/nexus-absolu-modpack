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
            case DARK_ASTRAL:    return "Fourneau Dark Astral";
            case GAIA_LUDICRITE: return "Fourneau Gaia Ludicrite";
            case PALLANUTRO:     return "Fourneau Pallanutro";
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
            case DARK_ASTRAL:    return 0xFF8866FF;  // violet sombre astral
            case GAIA_LUDICRITE: return 0xFFFF88DD;  // rose Ludicrite
            case PALLANUTRO:     return 0xFFFFCC55;  // or flamboyant
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
            case DARK_ASTRAL:    return 0xFF5632A0;
            case GAIA_LUDICRITE: return 0xFFCC5099;
            case PALLANUTRO:     return 0xFFDD9020;
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
            case DARK_ASTRAL:    return 0xFFAA88FF;
            case GAIA_LUDICRITE: return 0xFFFFB0DD;
            case PALLANUTRO:     return 0xFFFFDD66;
            default:         return 0xFF9090FF;
        }
    }
}
