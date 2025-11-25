package com.example.fishing;

import com.example.fishing.business.service.GameEngine;
import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import com.example.fishing.business.service.UpgradeType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class UpgradeTest {

    FisherRepository fisherRepository = Mockito.mock(FisherRepository.class);
    GameEngine gameEngine = new GameEngine(fisherRepository);

    // -----------------------------
    // TEST 1 – Erfolgreiches Upgrade
    // -----------------------------
    @Test
    void testBuyUpgrade_clickFlat_success() {

        Fisher fisher = new Fisher(1L, "Bob");
        fisher.setFishAmount(100);                               // genug Fisch
        long initialLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        long cost = fisher.getUpgradeCost(UpgradeType.CLICK_FLAT);

        Mockito.when(fisherRepository.findById(1L))
                .thenReturn(java.util.Optional.of(fisher));

        // neue Signatur: kein fishAmount mehr übergeben
        Fisher result = gameEngine.buyUpgrade(1L, UpgradeType.CLICK_FLAT);

        assertEquals(initialLevel + 1, result.getLevelOf(UpgradeType.CLICK_FLAT));
        assertEquals(100 - cost, result.getFishAmount());
        Mockito.verify(fisherRepository).save(fisher);
    }

    // -----------------------------
    // TEST 2 – Nicht genug Fisch
    // -----------------------------
    @Test
    void testBuyUpgrade_clickFlat_notEnoughFish() {

        Fisher fisher = new Fisher(1L, "Bob");
        fisher.setFishAmount(1); // zu wenig
        long initialLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        Mockito.when(fisherRepository.findById(1L))
                .thenReturn(java.util.Optional.of(fisher));

        // neue Signatur: kein fishAmount mehr übergeben
        Fisher result = gameEngine.buyUpgrade(1L, UpgradeType.CLICK_FLAT);

        // Level soll gleich bleiben
        assertEquals(initialLevel, result.getLevelOf(UpgradeType.CLICK_FLAT));

        // Fisch soll gleich bleiben
        assertEquals(1, result.getFishAmount());

        // save() DARF NICHT aufgerufen werden
        Mockito.verify(fisherRepository, Mockito.never()).save(Mockito.any());
    }


    // -----------------------------
    // TEST 3 – Erst fischen → Upgrade → wieder fischen
    // -----------------------------
    @Test
    void testClickThenUpgradeThenClickAgain() {

        Fisher fisher = new Fisher(1L, "Bob");

        // Mock Repository
        Mockito.when(fisherRepository.findById(1L))
                .thenReturn(java.util.Optional.of(fisher));

        // ---- 1) Genug klicken, um 1 Fisch zu bekommen ----
        for (int i = 0; i < 10; i++) {
            gameEngine.click(1L);
        }

        assertEquals(1, fisher.getFishAmount());

        long upgradeCost = fisher.getUpgradeCost(UpgradeType.CLICK_FLAT);
        fisher.setFishAmount(upgradeCost); // genug Fisch für Upgrade

        // ---- 2) Upgrade kaufen ----
        gameEngine.buyUpgrade(1L, UpgradeType.CLICK_FLAT);
        assertEquals(2, fisher.getLevelOf(UpgradeType.CLICK_FLAT));

        // ---- 3) Wieder klicken bis neuer Fisch entsteht ----
        for (int i = 0; i < 10; i++) {
            gameEngine.click(1L);
        }

        // ---- Ergebnis prüfen ----
        assertEquals(2, fisher.getFishAmount());
    }
}
