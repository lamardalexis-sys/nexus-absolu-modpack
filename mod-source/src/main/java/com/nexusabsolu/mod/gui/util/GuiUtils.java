package com.nexusabsolu.mod.gui.util;

import net.minecraft.client.gui.Gui;

/**
 * Utilitaires GUI reutilisables pour les machines Nexus.
 *
 * Factorise les helpers qui etaient inline dans GuiFurnaceNexus afin qu'ils
 * puissent etre reutilises par d'autres machines (Condenseur, Atelier, etc.)
 * et que le GUI principal reste focalise sur son layout specifique.
 *
 * Toutes les methodes sont statiques.
 */
public final class GuiUtils {

    private GuiUtils() {}

    /**
     * Dessine une barre horizontale qui se remplit de gauche vers droite,
     * avec un effet de "shine" (trait plus clair) en haut si largeur >2px.
     *
     * @param bx    x de depart de la barre
     * @param by    y de depart de la barre
     * @param bw    largeur totale de la barre
     * @param bh    hauteur de la barre
     * @param value valeur courante
     * @param max   valeur maximale (0 ou negative = ne dessine rien)
     * @param color couleur ARGB de remplissage principal
     * @param shine couleur ARGB du trait brillant en haut (effet 3D)
     */
    public static void fillBarHorizontal(int bx, int by, int bw, int bh,
                                          int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillW = (int) (bw * ratio);
        Gui.drawRect(bx, by, bx + fillW, by + bh, color);
        if (fillW > 2) {
            Gui.drawRect(bx, by, bx + fillW, by + 1, shine);
        }
    }

    /**
     * Dessine une barre verticale qui se remplit du BAS vers le HAUT avec un
     * degrade rouge fonce -> jaune vif (style Thermal Expansion).
     *
     * Le degrade est calcule pixel par pixel : le bas de la barre est rouge
     * fonce, le haut du remplissage (la partie la plus haute atteinte) est
     * jaune brillant. Un trait blanc-jaune supplementaire marque le sommet.
     *
     * Les params color/shine sont ignores pour l'instant, gardes pour la
     * signature similaire a fillBarHorizontal (potentielle customisation
     * future).
     *
     * @param bx    x de depart
     * @param by    y de depart (coin haut-gauche de la zone totale)
     * @param bw    largeur
     * @param bh    hauteur totale de la zone remplissable
     * @param value valeur courante
     * @param max   valeur maximale
     * @param color non-utilise (reserve pour API future)
     * @param shine non-utilise (reserve pour API future)
     */
    public static void fillBarVertical(int bx, int by, int bw, int bh,
                                        int value, int max, int color, int shine) {
        if (max <= 0 || value <= 0) return;
        float ratio = Math.min(1.0F, (float) value / max);
        int fillH = (int) (bh * ratio);
        if (fillH <= 0) return;

        // Degrade rouge (bas) -> jaune (haut) pixel par pixel
        for (int dy = 0; dy < fillH; dy++) {
            float t = (float) dy / Math.max(1, fillH - 1);
            int r = 255;
            int g = (int) (40 + (220 - 40) * t);
            int b = (int) (40 * (1 - t));
            int col = 0xFF000000 | (r << 16) | (g << 8) | b;
            int py = by + bh - 1 - dy;
            Gui.drawRect(bx, py, bx + bw, py + 1, col);
        }

        // Shine brillant au sommet du remplissage
        if (fillH > 2) {
            int topY = by + bh - fillH;
            Gui.drawRect(bx, topY, bx + bw, topY + 1, 0xFFFFFFAA);
        }
    }

    /**
     * Hit-test : verifie si le curseur (mx,my) est dans le rectangle (rx,ry,rw,rh).
     * Inclusif sur les bords comme les autres hit-tests vanilla GuiContainer.
     */
    public static boolean inRect(int mx, int my, int rx, int ry, int rw, int rh) {
        return mx >= rx && mx <= rx + rw && my >= ry && my <= ry + rh;
    }

    /**
     * Eclaircit une couleur ARGB en interpolant vers blanc.
     *
     * @param color  couleur d'origine (ARGB 0xAARRGGBB)
     * @param factor 0.0 = pas de changement, 1.0 = blanc pur
     * @return nouvelle couleur ARGB avec alpha=0xFF
     */
    public static int brighten(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) + (255 - ((color >> 16) & 0xFF)) * factor);
        int g = (int) (((color >> 8) & 0xFF) + (255 - ((color >> 8) & 0xFF)) * factor);
        int b = (int) ((color & 0xFF) + (255 - (color & 0xFF)) * factor);
        return 0xFF000000 | (Math.min(255, r) << 16) | (Math.min(255, g) << 8) | Math.min(255, b);
    }

    /**
     * Assombrit une couleur ARGB en reduisant chaque composante.
     *
     * @param color  couleur d'origine
     * @param factor 0.0 = pas de changement, 1.0 = noir pur
     * @return nouvelle couleur ARGB avec alpha=0xFF
     */
    public static int darken(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * (1 - factor));
        int g = (int) (((color >> 8) & 0xFF) * (1 - factor));
        int b = (int) ((color & 0xFF) * (1 - factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * Formatte un nombre d'unites energetiques (RF) pour lisibilite GUI :
     *   0..999      -> "N"
     *   1000..999k  -> "N.Nk"
     *   1M+         -> "N.NM"
     *
     * @param value valeur RF (doit etre >= 0)
     * @return chaine compacte adaptee au tooltip
     */
    public static String formatRf(int value) {
        if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000.0F);
        if (value >= 1_000)     return String.format("%.1fk", value / 1_000.0F);
        return String.valueOf(value);
    }
}
