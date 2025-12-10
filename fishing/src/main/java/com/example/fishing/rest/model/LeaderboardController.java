package com.example.fishing.rest.model;

import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaderboardController {

    private final FisherRepository fisherRepository;

    public LeaderboardController(FisherRepository fisherRepository) {
        this.fisherRepository = fisherRepository;
    }

    @GetMapping("/leaderboard")
    public List<Fisher> leaderboard() {
        return fisherRepository.findTop10ByOrderByTotalFishAmountDesc();
    }
}

