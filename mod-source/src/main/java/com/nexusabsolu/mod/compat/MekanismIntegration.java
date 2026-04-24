package com.nexusabsolu.mod.compat;

import java.lang.reflect.Method;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;

/**
 * Helper pour l'integration Mekanism, en particulier les Logistical Transporters.
 *
 * === PROBLEME RESOLU (v1.0.300) ===
 *
 * Les Logistical Transporter de Mekanism (cables a items) n'exposent PAS la
 * capability Forge standard ITEM_HANDLER_CAPABILITY. Ils exposent uniquement
 * leur propre capability proprietaire LOGISTICAL_TRANSPORTER_CAPABILITY.
 *
 * Donc, notre doAutoIO() dans TileFurnaceNexus voyait neighborHandler == null
 * et skippait l'insertion. Aucun item ne sortait du four vers un transporter.
 *
 * === SOLUTION (reflection, zero hard-depend sur Mekanism) ===
 *
 * Au chargement :
 *   - Si Mekanism est present (Loader.isModLoaded("mekanism")) :
 *       - Charge via reflection les classes mekanism.* necessaires
 *       - Cache les Methods pour performance (appels frequents)
 *   - Sinon : MEK_AVAILABLE = false, tous les appels no-op
 *
 * A l'execution :
 *   - tryInsertIntoTransporter(source, target, face, stack)
 *     -> cast target en ILogisticalTransporter via capability
 *     -> cree TransitRequest.getFromStack(stack)
 *     -> appelle TransporterUtils.insert(...)
 *     -> lit TransitResponse.getRejected(stack) = les items non-acceptes
 *     -> retourne les items non-acceptes (pour que le four les garde)
 *
 * === AVANTAGES ===
 *
 * - Pas de hard-depend Mekanism (pack sans Mekanism = pas de crash)
 * - Pas de build-time depend (on ne compile pas contre mekanism.jar)
 * - Extension facile : si besoin de supporter d'autres mods de cables
 *   (Thermal Itemduct, EnderIO Item Conduit), meme pattern.
 */
public final class MekanismIntegration {

    private static boolean INITIALIZED = false;
    private static boolean MEK_AVAILABLE = false;

    // Capability cachee (Capability<ILogisticalTransporter>)
    private static Capability<?> LOGISTICAL_TRANSPORTER_CAPABILITY = null;

    // Methods cachees pour performance
    private static Method METHOD_TRANSIT_REQUEST_GET_FROM_STACK = null;
    private static Method METHOD_TRANSPORTER_UTILS_INSERT = null;
    private static Method METHOD_TRANSIT_RESPONSE_GET_REJECTED = null;
    private static Method METHOD_TRANSIT_RESPONSE_IS_EMPTY = null;

    // Classes cachees
    private static Class<?> CLASS_ENUM_COLOR = null;

    private MekanismIntegration() { /* static helper */ }

    /**
     * Init idempotent. Appele au premier usage.
     * Echoue silencieusement si Mekanism n'est pas present.
     */
    private static void init() {
        if (INITIALIZED) return;
        INITIALIZED = true;

        if (!Loader.isModLoaded("mekanism")) {
            // Pas de Mekanism, rien a faire
            return;
        }

        try {
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            LOGISTICAL_TRANSPORTER_CAPABILITY = (Capability<?>)
                capabilitiesClass.getField("LOGISTICAL_TRANSPORTER_CAPABILITY").get(null);

            Class<?> transitRequestClass = Class.forName("mekanism.common.content.transporter.TransitRequest");
            Class<?> transitResponseClass = Class.forName("mekanism.common.content.transporter.TransitRequest$TransitResponse");
            Class<?> transporterUtilsClass = Class.forName("mekanism.common.util.TransporterUtils");
            Class<?> logisticalTransporterClass = Class.forName("mekanism.common.base.ILogisticalTransporter");
            CLASS_ENUM_COLOR = Class.forName("mekanism.api.EnumColor");

            METHOD_TRANSIT_REQUEST_GET_FROM_STACK =
                transitRequestClass.getMethod("getFromStack", ItemStack.class);

            // public static TransitResponse insert(TileEntity outputter, ILogisticalTransporter transporter,
            //                                       TransitRequest request, EnumColor color, boolean doEmit, int min)
            METHOD_TRANSPORTER_UTILS_INSERT = transporterUtilsClass.getMethod("insert",
                TileEntity.class,
                logisticalTransporterClass,
                transitRequestClass,
                CLASS_ENUM_COLOR,
                boolean.class,
                int.class);

            METHOD_TRANSIT_RESPONSE_GET_REJECTED =
                transitResponseClass.getMethod("getRejected", ItemStack.class);
            METHOD_TRANSIT_RESPONSE_IS_EMPTY =
                transitResponseClass.getMethod("isEmpty");

            MEK_AVAILABLE = true;
        } catch (Exception e) {
            // Log mais continue sans Mekanism
            System.err.println("[Nexus Absolu] Mekanism integration init failed: " + e.getMessage());
            MEK_AVAILABLE = false;
        }
    }

    /**
     * Returns true si le TileEntity voisin est un Logistical Transporter.
     * Appel peu cher, safe si Mek absent (retourne false).
     */
    public static boolean isLogisticalTransporter(TileEntity neighbor, EnumFacing sideOfNeighbor) {
        init();
        if (!MEK_AVAILABLE || neighbor == null) return false;
        return neighbor.hasCapability(LOGISTICAL_TRANSPORTER_CAPABILITY, sideOfNeighbor);
    }

    /**
     * Tente d'inserer un stack dans un Logistical Transporter voisin.
     *
     * @param sourceTile Le TileEntity source (le four qui ejecte)
     * @param targetTile Le TileEntity cible (le transporter)
     * @param sideOfTarget La face du transporter qui touche la source
     * @param stack Le stack a inserer (une copie)
     * @return Le stack restant (non accepte). ItemStack.EMPTY si tout accepte.
     *         Retourne stack inchange si integration indisponible.
     */
    public static ItemStack tryInsertIntoTransporter(TileEntity sourceTile, TileEntity targetTile,
                                                      EnumFacing sideOfTarget, ItemStack stack) {
        init();
        if (!MEK_AVAILABLE || stack.isEmpty()) return stack;

        try {
            Object transporter = targetTile.getCapability(LOGISTICAL_TRANSPORTER_CAPABILITY, sideOfTarget);
            if (transporter == null) return stack;

            // TransitRequest request = TransitRequest.getFromStack(stack);
            Object request = METHOD_TRANSIT_REQUEST_GET_FROM_STACK.invoke(null, stack);

            // TransitResponse response = TransporterUtils.insert(sourceTile, transporter, request, null, true, 0);
            // color = null (pas de filtre couleur), doEmit = true (reel, pas simulate), min = 0
            Object response = METHOD_TRANSPORTER_UTILS_INSERT.invoke(null,
                sourceTile, transporter, request, null, Boolean.TRUE, Integer.valueOf(0));

            // Verifier si reponse vide (transporter plein/bloque)
            boolean responseEmpty = (Boolean) METHOD_TRANSIT_RESPONSE_IS_EMPTY.invoke(response);
            if (responseEmpty) return stack;  // rien accepte, on garde tout

            // response.getRejected(stack) = items pas acceptes
            ItemStack rejected = (ItemStack) METHOD_TRANSIT_RESPONSE_GET_REJECTED.invoke(response, stack);
            return rejected == null ? ItemStack.EMPTY : rejected;

        } catch (Exception e) {
            // Erreur runtime : on ne casse pas le jeu, on dit que rien n'a ete insere
            System.err.println("[Nexus Absolu] Error inserting into Mekanism transporter: " + e.getMessage());
            return stack;
        }
    }
}
