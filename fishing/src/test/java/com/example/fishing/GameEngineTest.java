package com.example.fishing;

import com.example.fishing.business.service.GameEngine;
import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GameEngineTest {

    @Test
    @DisplayName("Click increases progress and eventually adds fish")
    void click_increasesProgressAndEventuallyAddsFish() {
        FisherRepository repo = mock(FisherRepository.class);
        Fisher fisher = new Fisher(1L, "Clicker");
        fisher.setFisherId(42L);
        fisher.setFishAmount(0);
        fisher.setTotalFishAmount(0);
        fisher.setFishProgress(0);         // adjust to your actual field if named differently
        fisher.setBaseFishPull(5);         // whatever you use as base

        when(repo.findById(42L)).thenReturn(Optional.of(fisher));

        GameEngine engine = new GameEngine(repo /* other deps if needed */);

        // 1) First click should at least increase progress
        Fisher afterFirstClick = engine.click(42L);
        assertThat(afterFirstClick.getFishProgress())
                .as("fishProgress after first click")
                .isGreaterThan(0);

        // 2) After enough clicks, we should see some fish
        Fisher current = afterFirstClick;
        for (int i = 0; i < 20; i++) { // 20 is arbitrary but > 10 to be safe
            when(repo.findById(42L)).thenReturn(Optional.of(current));
            current = engine.click(42L);
        }

        assertThat(current.getFishAmount())
                .as("fishAmount after many clicks")
                .isGreaterThan(0);

        assertThat(current.getTotalFishAmount())
                .as("totalFishAmount should track fishAmount")
                .isEqualTo(current.getFishAmount());

        verify(repo, atLeastOnce()).save(any(Fisher.class));
    }


    @Test
    @DisplayName("Passive Fish only gives fish after delay")
    void passiveTick_onlyAddsFishWhenDelayPassed() {
        FisherRepository repo = mock(FisherRepository.class);
        Fisher fisher = new Fisher(1L, "AFK");
        fisher.setFisherId(99L);
        fisher.setPassiveFishPerPull(3);
        fisher.setPassiveFishSpeedMultiplier(1_000); // 1s per tick

        long now = Instant.now().toEpochMilli();
        fisher.setLastPassiveTickMillis(now - 2_000); // 2s ago

        when(repo.findById(99L)).thenReturn(Optional.of(fisher));

        GameEngine engine = new GameEngine(repo);
        Fisher updated = engine.passiveTick(99L);

        assertThat(updated.getFishAmount()).isGreaterThanOrEqualTo(3);
        assertThat(updated.getTotalFishAmount()).isGreaterThanOrEqualTo(3);
        verify(repo).save(updated);
    }
}
