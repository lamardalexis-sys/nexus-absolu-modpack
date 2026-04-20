package com.nexusabsolu.mod.tiles.furnaces;

/**
 * Enum des categories d'upgrades pour les Furnaces Nexus.
 *
 * 4 categories, 4 slots dedies dans le GUI (un slot par categorie) :
 *  - RF_CONVERTER   : slot 0, stack max 1, pre-requis pour SPEED/IO/EFFICIENCY
 *  - IO_EXPANSION   : slot 1, stack max 1, mais 4 items distincts (IO I..IV) crafted sequentiellement
 *  - SPEED_BOOSTER  : slot 2, stack max 8, +30%/stack vitesse, +40%/stack conso
 *  - EFFICIENCY     : slot 3, stack max 10, -15%/stack conso (multiplicatif)
 *
 * Pour T7+ (nativeRF=true), une upgrade COAL_CONVERTER viendra remplacer RF_CONVERTER
 * (ajout dans phase ulterieure).
 */
public enum FurnaceUpgrade {
    RF_CONVERTER  (0, 1,  "rf_converter"),
    IO_EXPANSION  (1, 1,  "io_expansion"),
    SPEED_BOOSTER (2, 8,  "speed_booster"),
    EFFICIENCY    (3, 10, "efficiency_card");

    /** Index du slot dans le container (0-3). */
    public final int slotIndex;
    /** Taille de stack maximum dans ce slot. */
    public final int maxStackSize;
    /** Suffix registry name : nexusabsolu:upgrade_<suffix> */
    public final String registrySuffix;

    FurnaceUpgrade(int slotIndex, int maxStackSize, String registrySuffix) {
        this.slotIndex = slotIndex;
        this.maxStackSize = maxStackSize;
        this.registrySuffix = registrySuffix;
    }

    /** Nombre total de slots upgrades (= 4). */
    public static int slotCount() {
        return values().length;
    }
}
