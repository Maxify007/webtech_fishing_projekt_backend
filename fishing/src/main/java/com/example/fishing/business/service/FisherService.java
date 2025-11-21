package com.example.fishing.business.service;

import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;

import java.util.List;

public class FisherService {

    private final FisherRepository fisherRepository;

    public FisherService(FisherRepository fisherRepository) {
        this.fisherRepository = fisherRepository;
    }

    public List<Fisher> listFishers(long playerId) {
        return fisherRepository.findByPlayerId(playerId);
    }

    public Fisher createFisher(long playerId, String name) {
        Fisher fisher = new Fisher(playerId, name);
        return fisherRepository.save(fisher);
    }

    public Fisher getFisher(long playerId, long fisherId) {
        Fisher fisher = fisherRepository.findById(fisherId)
                .orElseThrow(() -> new IllegalArgumentException("Fisher not found"));
        if (fisher.getPlayerId() != playerId) {
            throw new IllegalStateException("Not your Fisher");
        }
        return fisher;
    }

}
