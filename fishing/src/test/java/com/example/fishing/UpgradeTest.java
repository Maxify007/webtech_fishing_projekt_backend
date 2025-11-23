package com.example.fishing;

import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UpgradeTest {

    @Test
    void testIncreaseLevel_WhenEnoughFish() {
        // Arrange
        Fisher fisher = new Fisher(1L, "Tester");

        // Startlevel von CLICK_FLAT = 1 (wegen initUpgrades)
        int oldLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        // Genug Fische geben
        fisher.setFishAmount(10_000);

        // Berechne erwartete Kosten
        long expectedRounded = Math.round(Math.pow(1.15, oldLevel));
        long expectedCost = 10 * expectedRounded;

        long oldFishAmount = fisher.getFishAmount();

        // Act
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);

        // Assert
        assertEquals(oldLevel + 1,
                fisher.getLevelOf(UpgradeType.CLICK_FLAT),
                "Upgrade-Level sollte um 1 steigen");

        assertEquals(oldFishAmount - expectedCost,
                fisher.getFishAmount(),
                "Fischmenge sollte um die Upgrade-Kosten reduziert werden");
    }


    @Test
    void testIncreaseLevel_WhenNotEnoughFish() {
        // Arrange
        Fisher fisher = new Fisher(1L, "Tester");

        int oldLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        // Zu wenig Fish
        fisher.setFishAmount(0);

        long oldFishAmount = fisher.getFishAmount();

        // Act
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);

        // Assert
        assertEquals(oldLevel,
                fisher.getLevelOf(UpgradeType.CLICK_FLAT),
                "Upgrade-Level darf NICHT steigen");

        assertEquals(oldFishAmount,
                fisher.getFishAmount(),
                "Fischmenge darf NICHT verändert werden");
    }



    @Test
    void fishPerTenClicksBeforeAndAfterUpgrade() {
        // --- Setup ---
        Fisher fisher = new Fisher(1L, "TestFisher");

        // Level 1 → baseFishPull = 1
        // masteryMultiplier = 1
        // -> 10 clicks => 1 fish

        // --- Act 1: 10 clicks before upgrade ---
        for (int i = 0; i < 10; i++) {
            fisher.fishingAction();
        }

        long fishBefore = fisher.getFishAmount();
        assertEquals(1, fishBefore,
                "Mit CLICK_FLAT=1 sollte man pro 10 Clicks genau 1 Fish bekommen.");

        // --- Upgrade CLICK_FLAT um 1 Level ---
        // Upgrade kostet 10 * Math.round(pow(1.15, level=1)) = 10 * 1 = 10
        fisher.setFishAmount(10); // genug Fish für das Upgrade
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);

        assertEquals(2, fisher.getLevelOf(UpgradeType.CLICK_FLAT),
                "CLICK_FLAT sollte nun Level 2 haben.");

        // Stats neu berechnet → baseFishPull = 2

        // --- Act 2: Wieder 10 clicks ---
        for (int i = 0; i < 10; i++) {
            fisher.fishingAction();
        }

        long fishAfter = fisher.getFishAmount();
        assertEquals(2, fishAfter,
                "Mit CLICK_FLAT=2 sollte man pro 10 Clicks nun 2 Fish bekommen.");
    }
}
