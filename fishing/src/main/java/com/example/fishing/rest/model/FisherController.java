package com.example.fishing.rest.model;

import com.example.fishing.business.service.GameEngine;
import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5177")
@Service
public class FisherController {

    private final GameEngine gameEngine;

    public FisherController(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    // POST /api/game/{fisherId}/click
    @PostMapping("/{fisherId}/click")
    public Fisher click(@PathVariable long fisherId) {
        return gameEngine.click(fisherId);
    }

    // POST /api/game/{fisherId}/upgrade/{type}
    @PostMapping("/{fisherId}/upgrade/{type}")
    public Fisher buyUpgrade(@PathVariable long fisherId,
                             @PathVariable UpgradeType type) {
        return gameEngine.buyUpgrade(fisherId, type);
    }
}