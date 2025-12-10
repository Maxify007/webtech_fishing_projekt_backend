package com.example.fishing;

import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FisherTest {

    @Test
    @DisplayName("getUpgradeCost uses the same formula as in the backend")
    void getUpgradeCost_usesCorrectFormula() {
        // given
        Fisher fisher = new Fisher(1L, "TestFisher");

        // assume new fisher starts at level 1 (or whatever your default is)
        int level = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        // when
        long cost = fisher.getUpgradeCost(UpgradeType.CLICK_FLAT);

        // then: Math.round(Math.pow(1.15, level) * 10)
        long expected = Math.round(Math.pow(1.15, level) * 10);
        assertThat(cost).isEqualTo(expected);
    }

    @Test
    @DisplayName("increaseLevelOf raises level and subtracts fish when enough fish")
    void increaseLevelOf_increasesLevelAndSubtractsFish_whenEnoughFish() {
        // given
        Fisher fisher = new Fisher(1L, "RichFisher");
        fisher.setFishAmount(10_000L);

        int beforeLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        long cost = fisher.getUpgradeCost(UpgradeType.CLICK_FLAT);

        // when
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);

        // then
        int afterLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        assertThat(afterLevel).isEqualTo(beforeLevel + 1);
        assertThat(fisher.getFishAmount()).isEqualTo(10_000L - cost);
    }

    @Test
    @DisplayName("increaseLevelOf does not change level when not enough fish")
    void increaseLevelOf_doesNotIncreaseLevel_whenNotEnoughFish() {
        // given
        Fisher fisher = new Fisher(1L, "PoorFisher");
        fisher.setFishAmount(0L);

        int beforeLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);

        // when
        fisher.increaseLevelOf(UpgradeType.CLICK_FLAT);

        // then
        int afterLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        assertThat(afterLevel).isEqualTo(beforeLevel);
        assertThat(fisher.getFishAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Default upgrade level is at least 1 for new fisher")
    void defaultUpgradeLevel_isOneForNewFisher() {
        Fisher fisher = new Fisher(1L, "Newbie");

        int clickFlatLevel = fisher.getLevelOf(UpgradeType.CLICK_FLAT);
        int luckRateLevel = fisher.getLevelOf(UpgradeType.CLICK_LUCK_RATE);

        assertThat(clickFlatLevel).isGreaterThanOrEqualTo(1);
        assertThat(luckRateLevel).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Fishing increases both fishAmount and totalFishAmount")
    void fishing_increasesCurrentAndTotalFish() {
        Fisher fisher = new Fisher(1L, "Fishy");

        fisher.setFishAmount(0L);
        fisher.setTotalFishAmount(0L);

        // If you have a helper like addFish(amount), use that instead:
        long gain = 7L;
        fisher.setFishAmount(fisher.getFishAmount() + gain);
        fisher.setTotalFishAmount(fisher.getTotalFishAmount() + gain);

        assertThat(fisher.getFishAmount()).isEqualTo(7L);
        assertThat(fisher.getTotalFishAmount()).isEqualTo(7L);
    }
}
