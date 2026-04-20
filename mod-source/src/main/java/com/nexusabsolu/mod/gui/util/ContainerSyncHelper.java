package com.nexusabsolu.mod.gui.util;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

/**
 * Helper pour synchroniser des valeurs int > 32767 entre serveur et client
 * via sendWindowProperty, en contournant la limite short signe (-32768..32767).
 *
 * =============================================================================
 * CONTEXTE : limite de sendWindowProperty en Minecraft 1.12.2
 * =============================================================================
 *
 * IContainerListener.sendWindowProperty(container, id, data) envoie data
 * comme un SHORT SIGNE. Si data > 32767 (ex: RF storage d'une machine de
 * 100k RF), le cast silencieux cote serveur tronque la valeur et le client
 * recoit une valeur negative ou erronee.
 *
 * Pattern utilise par Mekanism et Thermal Expansion :
 *   - Split chaque int en 2 shorts (low 16 bits + high 16 bits)
 *   - Cote client, combiner les 2 shorts pour retrouver l'int exact
 *   - Chaque int coute 2 ids dans le protocole window property
 *
 * =============================================================================
 * UTILISATION TYPE
 * =============================================================================
 *
 * class MyContainer extends Container {
 *     private final ContainerSyncHelper sync;
 *
 *     public MyContainer(...) {
 *         // 3 fields a sync : progress, energy, maxEnergy
 *         sync = new ContainerSyncHelper(3);
 *     }
 *
 *     @Override
 *     public void addListener(IContainerListener listener) {
 *         super.addListener(listener);
 *         sync.sendInitial(this, listener, fetchFields());
 *     }
 *
 *     @Override
 *     public void detectAndSendChanges() {
 *         super.detectAndSendChanges();
 *         sync.detectChanges(this, listeners, fetchFields());
 *     }
 *
 *     @Override
 *     public void updateProgressBar(int id, int data) {
 *         int fieldIdx = sync.receiveProperty(id, data);
 *         if (fieldIdx >= 0) {
 *             int value = sync.getField(fieldIdx);
 *             // Appliquer au tile local ex: switch(fieldIdx) { ... }
 *         }
 *     }
 *
 *     private int[] fetchFields() {
 *         return new int[] { tile.getProgress(), tile.getEnergy(), tile.getMaxEnergy() };
 *     }
 * }
 */
public final class ContainerSyncHelper {

    /** Nombre de champs int a sync. */
    private final int fieldCount;

    /** Cache serveur : derniere valeur envoyee par champ (pour detecter les changements). */
    private final int[] cachedFields;

    /** Buffer client : low bits recus en attente de high bits pour assembler l'int. */
    private final int[] pendingFields;

    /**
     * @param fieldCount nombre de valeurs int a sync. Chaque champ utilise
     *                   2 ids consecutifs dans le protocole window property.
     */
    public ContainerSyncHelper(int fieldCount) {
        if (fieldCount <= 0) throw new IllegalArgumentException("fieldCount must be > 0");
        this.fieldCount = fieldCount;
        this.cachedFields = new int[fieldCount];
        this.pendingFields = new int[fieldCount];
    }

    /**
     * Envoie les valeurs initiales a un listener qui vient de s'attacher,
     * pour que le GUI s'affiche immediatement avec les bonnes valeurs au lieu
     * d'avoir 0 pendant 1 tick.
     *
     * @param container       le Container qui envoie
     * @param listener        le nouveau listener (typiquement EntityPlayerMP)
     * @param currentValues   les valeurs actuelles dans l'ordre des fields
     */
    public void sendInitial(Container container, IContainerListener listener, int[] currentValues) {
        validateFieldArray(currentValues);
        for (int i = 0; i < fieldCount; i++) {
            sendSplit(container, listener, i, currentValues[i]);
        }
    }

    /**
     * Detecte les changements par rapport au cache serveur et envoie a tous
     * les listeners. Met a jour le cache apres avoir envoye a TOUS les
     * listeners (sinon seul le premier recevrait le delta).
     *
     * @param container     le Container qui envoie
     * @param listeners     tous les listeners attaches (typiquement container.listeners)
     * @param currentValues les valeurs actuelles dans l'ordre des fields
     */
    public void detectChanges(Container container,
                               java.util.List<IContainerListener> listeners,
                               int[] currentValues) {
        validateFieldArray(currentValues);
        for (IContainerListener listener : listeners) {
            for (int i = 0; i < fieldCount; i++) {
                if (cachedFields[i] != currentValues[i]) {
                    sendSplit(container, listener, i, currentValues[i]);
                }
            }
        }
        // Update cache APRES la boucle pour que TOUS les listeners recoivent
        // le delta (sinon le 2e listener verrait deja la nouvelle valeur dans
        // le cache et ne recevrait rien)
        System.arraycopy(currentValues, 0, cachedFields, 0, fieldCount);
    }

    /**
     * Recoit un paquet sendWindowProperty cote client. Buffer les low bits
     * et quand les high bits arrivent, assemble la valeur finale dans le
     * pendingFields[].
     *
     * @param id   id recu (paire = low bits du champ, impaire = high bits)
     * @param data short signe recu (encode en int mais limite a 16 bits)
     * @return l'index du champ si la valeur est complete (low+high recus),
     *         -1 sinon (on a juste recu les low bits, on attend les high)
     */
    public int receiveProperty(int id, int data) {
        int unsigned = data & 0xFFFF;
        int fieldIdx = id / 2;
        boolean isHighBits = (id % 2) == 1;

        if (fieldIdx < 0 || fieldIdx >= fieldCount) return -1;

        if (isHighBits) {
            pendingFields[fieldIdx] = (pendingFields[fieldIdx] & 0x0000FFFF) | (unsigned << 16);
            return fieldIdx;
        } else {
            pendingFields[fieldIdx] = (pendingFields[fieldIdx] & 0xFFFF0000) | unsigned;
            return -1;
        }
    }

    /**
     * Retourne la valeur complete d'un champ apres assemblage low+high.
     * A utiliser apres receiveProperty quand celle-ci retourne un index >= 0.
     */
    public int getField(int fieldIdx) {
        if (fieldIdx < 0 || fieldIdx >= fieldCount) return 0;
        return pendingFields[fieldIdx];
    }

    private void sendSplit(Container container, IContainerListener listener, int fieldIdx, int value) {
        listener.sendWindowProperty(container, fieldIdx * 2, value & 0xFFFF);
        listener.sendWindowProperty(container, fieldIdx * 2 + 1, (value >>> 16) & 0xFFFF);
    }

    private void validateFieldArray(int[] values) {
        if (values.length != fieldCount) {
            throw new IllegalArgumentException(
                "Expected " + fieldCount + " fields, got " + values.length);
        }
    }
}
