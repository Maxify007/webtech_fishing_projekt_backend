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

        fisher.increaseLevelOf(upgradeType);
        fisherRepository.save(fisher);

        return fisher; // now includes upgrades
    }


}
