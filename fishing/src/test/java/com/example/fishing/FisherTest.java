package com.example.fishing;

import com.example.fishing.business.service.UpgradeFormula;
import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FisherTest {

    @Test
    void constructor_initializesDefaults() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        assertEquals(1L, fisher.getFisherId());
        assertEquals(1L, fisher.getPlayerId());
        assertEquals("TestFisher", fisher.getName());

        assertEquals(0L, fisher.getFishAmount());
        assertEquals(0, fisher.getFishProgress());

        // upgrades created for all UpgradeTypes and start at level 1
        assertEquals(UpgradeType.values().length, fisher.getUpgrades().size());
        for (UpgradeType type : UpgradeType.values()) {
            assertEquals(1, fisher.getLevelOf(type), "Level of " + type + " should start at 1");
        }
    }

    @Test
    void recalculateStats_usesUpgradeLevels() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        // Directly set some specific levels
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_FLAT)
                .findFirst().ifPresent(u -> u.setLevel(5));

        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_RATE)
                .findFirst().ifPresent(u -> u.setLevel(4));

        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(3));

        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_MASTERY_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(2));

        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.PASSIVE_FISH_RATE)
                .findFirst().ifPresent(u -> u.setLevel(10));

        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.PASSIVE_FISH_AMOUNT)
                .findFirst().ifPresent(u -> u.setLevel(7));

        fisher.recalculateStats();

        // expected values using your UpgradeFormula
        long expectedBaseFishPull = UpgradeFormula.flatPerClick(5);
        double expectedLuckRate = UpgradeFormula.luckRate(4);
        double expectedLuckMult = UpgradeFormula.luckMultiplier(3);
        double expectedMastery = UpgradeFormula.masteryMultiplier(2);
        double expectedPassiveRate = UpgradeFormula.passiveFishRate(10);
        double expectedPassiveAmount = UpgradeFormula.passiveFishAmount(7);

        assertEquals(expectedBaseFishPull, fisher.getBaseFishPull());
        assertEquals(expectedLuckRate, fisher.getLuckRate());
        assertEquals(expectedLuckMult, fisher.getLuckMultiplier());
        assertEquals(expectedMastery, fisher.getMasteryMultiplier());
        assertEquals(expectedPassiveRate, fisher.getPassiveFishSpeedMultiplier());
        assertEquals(expectedPassiveAmount, fisher.getPassiveFishPerPull());
    }

    @Test
    void increaseLevelOf_incrementsUpgradeLevel() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        int initialLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);
        int newLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        assertEquals(initialLevel + 1, newLevel);
    }

    @Test
    void calculatePull_withoutLuck_usesBaseAndMasteryOnly() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        // Make stats deterministic:
        // CLICK_FLAT = 10
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_FLAT)
                .findFirst().ifPresent(u -> u.setLevel(10));

        // LUCK_RATE = 0 (no lucky pulls)
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_RATE)
                .findFirst().ifPresent(u -> u.setLevel(0));

        // LUCK_MULTIPLIER doesn't matter when luck never triggers
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(5));

        // MASTERY = 2
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_MASTERY_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(2));

        fisher.recalculateStats();

        long base = fisher.getBaseFishPull();           // from flatPerClick(10)
        double mastery = fisher.getMasteryMultiplier(); // e.g. 1.0 + 2 * 0.05

        long expected = Math.max(1L, Math.round(base * mastery));
        long actual = fisher.calculatePull();

         assertEquals(expected, actual);
    }

    @Test
    void calculatePull_withAlwaysLuckyPull_appliesLuckAndMastery() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        // CLICK_FLAT = 10
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_FLAT)
                .findFirst().ifPresent(u -> u.setLevel(10));

        // LUCK_RATE very high so luckyPull always true (assuming luckRate = level/2.0)
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_RATE)
                .findFirst().ifPresent(u -> u.setLevel(300)); // 150% "chance"

        // LUCK_MULTIPLIER
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_LUCK_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(5));

        // MASTERY
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_MASTERY_MULTIPLIER)
                .findFirst().ifPresent(u -> u.setLevel(2));

        fisher.recalculateStats();

        long base = fisher.getBaseFishPull();
        double luckMult = fisher.getLuckMultiplier();
        double mastery = fisher.getMasteryMultiplier();

        long expected = Math.max(1L, Math.round(base * luckMult * mastery));
        long actual = fisher.calculatePull();

        assertEquals(expected, actual);
    }

    @Test
    void fishingAction_requiresTenStepsBeforePullIsAdded() {
        Fisher fisher = new Fisher(1L, "TestFisher");

        // make pulls deterministic and >=1
        fisher.getUpgrades().stream()
                .filter(u -> u.getType() == UpgradeType.CLICK_FLAT)
                .findFirst().ifPresent(u -> u.setLevel(5));

        fisher.recalculateStats();

        long startFish = fisher.getFishAmount();

        // Call 10 times: fishProgress will go from 0 -> 10,
        // but calculatePull is only called when fishProgress == 10 at the *start* of the method.
        for (int i = 0; i < 10; i++) {
            fisher.fishingAction();
        }

        assertEquals(startFish, fisher.getFishAmount(), "Fish amount should not change yet");

        // 11th call should actually perform the pull
        fisher.fishingAction();

        assertTrue(fisher.getFishAmount() > startFish, "Fish amount should increase after 11th action");
        assertEquals(0, fisher.getFishProgress(), "fishProgress should reset to 0 after a pull");
    }
}