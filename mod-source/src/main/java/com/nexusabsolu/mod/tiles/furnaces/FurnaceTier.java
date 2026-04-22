package com.nexusabsolu.mod.tiles.furnaces;

/**
 * Enum des 9 tiers de Furnace du Nexus Absolu.
 *
 * Chaque tier definit :
 *  - registryName : id minecraft (nexusabsolu:furnace_<name>)
 *  - speedMultiplier : multiplicateur vitesse vs vanilla furnace (200 ticks = x1.0)
 *  - baseCoalPerOp : fraction de coal consommee par operation (vanilla = 1 coal = 8 ops = 0.125)
 *  - baseRfPerTick : conso RF quand RF Converter installe
 *  - nativeRF : si true, le furnace est nativement RF (T7+), sinon coal par defaut
 *  - age : Age de debloquage pour ItemStages
 *
 * T1-T5 implementes dans la premiere phase.
 * T6-T9 declares mais non-register dans ModBlocks (a activer quand pret).
 */
public enum FurnaceTier {
    //                   id              speed   coal    rf/t   nativeRF  age
    IRON      ("iron",       1.2f,  0.10f,  20,   false, 0),
    GOLD      ("gold",       1.4f,  0.11f,  30,   false, 1),
    INVARIUM  ("invarium",   1.7f,  0.12f,  40,   false, 1),     // v1.0.268 : invar -> invarium, stats boostees pour etre meilleur que gold
    EMERADIC  ("emeradic",   2.0f,  0.12f,  60,   false, 1),
    VOSSIUM_IV("vossium_iv", 3.0f,  0.15f,  120,  false, 2),
    DARK_ASTRAL("dark_astral", 6.0f, 0.20f, 300,  false, 3),   // phase ulterieure
    GAIA_LUDICRITE("gaia_ludicrite", 21.0f, 0f, 800, true, 4), // phase ulterieure, RF natif
    PALLANUTRO("pallanutro", 56.0f, 0f,    2000, true,  5),    // phase ulterieure, RF natif
    INFINITE  ("infinite",  101.0f, 0f,    5000, true,  6);    // phase ulterieure, multiblock

    public final String registryName;
    public final float speedMultiplier;
    public final float baseCoalPerOp;
    public final int baseRfPerTick;
    public final boolean nativeRF;
    public final int age;

    FurnaceTier(String registryName, float speedMultiplier, float baseCoalPerOp,
                int baseRfPerTick, boolean nativeRF, int age) {
        this.registryName = registryName;
        this.speedMultiplier = speedMultiplier;
        this.baseCoalPerOp = baseCoalPerOp;
        this.baseRfPerTick = baseRfPerTick;
        this.nativeRF = nativeRF;
        this.age = age;
    }

    /** Temps de cuisson de base pour ce tier, en ticks. Vanilla = 200. */
    public int baseCookTime() {
        return Math.max(1, (int)(200f / speedMultiplier));
    }

    /**
     * Capacite energie de base. Proportionnelle a la conso pour ~100s
     * d'autonomie a conso de base, soit ~10s avec Speed + auto-sort.
     *
     * v1.0.266 : formule × 10 (baseRfPerTick * 2000 au lieu de 200).
     * Avant : Vossium IV = 24k RF = 10s d'autonomie de base, se vide en
     * ~330ms avec Speed+auto-sort 9 paires. Maintenant : 240k RF = 3-5s
     * minimum meme dans le pire cas, donc jouable.
     *
     * Minimum 50000 RF pour que les tiers bas aient une reserve confortable.
     */
    public int baseEnergyCapacity() {
        return Math.max(50000, baseRfPerTick * 2000);
    }

    /** Tiers implementes dans la phase actuelle (T1-T5). */
    public boolean isImplemented() {
        return this.ordinal() <= VOSSIUM_IV.ordinal();
    }

    public static FurnaceTier byName(String name) {
        // v1.0.268 : alias legacy pour saves existants (invar -> invarium)
        if ("invar".equals(name)) return INVARIUM;
        for (FurnaceTier t : values()) {
            if (t.registryName.equals(name)) return t;
        }
        return IRON;
    }
}
