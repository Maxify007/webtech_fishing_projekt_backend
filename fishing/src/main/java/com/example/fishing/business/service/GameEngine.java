package com.example.fishing.business.service;

import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.springframework.stereotype.Service;

@Service
public class GameEngine {

    private final FisherRepository fisherRepository;

    public GameEngine(FisherRepository fisherRepository) {
        this.fisherRepository = fisherRepository;
    }

    public Fisher click(long fisherId) {
        Fisher fisher = fisherRepository.findById(fisherId).orElseThrow(() -> new IllegalArgumentException("Fisher not found"));
        fisher.fishingAction();
        fisherRepository.save(fisher);
        return fisher;
    }

    public Fisher buyUpgrade(long fisherId, UpgradeType upgradeType) {
        Fisher fisher = fisherRepository.findById(fisherId)
                .orElseThrow(() -> new IllegalArgumentException("Fisher not found"));

        int before = fisher.getLevelOf(upgradeType);

        // this method already checks cost & fishAmount
        fisher.increaseLevelOf(upgradeType);

        int after = fisher.getLevelOf(upgradeType);

        // nur speichern, wenn sich etwas geändert hat
        if (after > before) {
            fisherRepository.save(fisher);
        }

        return fisher;
    }



    public Fisher passiveTick(long fisherId) {
        Fisher fisher = fisherRepository.findById(fisherId)
                .orElseThrow(() -> new IllegalArgumentException("Fisher not found"));
        fisher.passiveFishingTick();
        fisherRepository.save(fisher);
        return fisher;
    }


}
