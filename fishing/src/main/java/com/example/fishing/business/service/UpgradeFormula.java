package com.example.fishing.business.service;

public final class UpgradeFormula {

    private UpgradeFormula() {}

    public static long flatPerClick(int level) {
        // e.g. level 1 -> 1 fish, level 100 -> 100 fish
        return level;
    }

    public static double luckRate(int level) {
        // e.g. level 10 -> 5.0
        return level / 2.0;
    }

    public static double luckMultiplier(int level) {
        // e.g. level 10 -> 2.0 ( +100% )
        return 1.0 + level / 10.0;
    }

    public static double masteryMultiplier(int level) {
        if (level < 1) {
            return 1.0;
        }
        return (double) level;
    }

    public static double passiveFishRate(int level) {
        double slope = (3000.0 - 30000.0) / 99.0; // ≈ -272.727...
        return 30000.0 + slope * (level - 1);
    }

    public static double passiveFishAmount(int level) {
        // e.g. level 1 -> 1 fish, level 100 -> 100 fish
        return level;
    }
}
