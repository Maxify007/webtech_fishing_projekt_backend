package com.example.fishing.rest.model;

import com.example.fishing.business.service.GameEngine;
import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameEngine gameEngine;

    public GameController(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    // POST /api/game/{fisherId}/click
    @PostMapping("/{fisherId}/click")
    public Fisher click(@PathVariable long fisherId) {
        return gameEngine.click(fisherId);
    }

    // POST /api/game/{fisherId}/upgrade/{type}
    @PostMapping("/{fisherId}/upgrade/{type}/{fishAmount}")
    public Fisher buyUpgrade(@PathVariable long fisherId,
                             @PathVariable UpgradeType type,
                             @PathVariable long fishAmount) {
        return gameEngine.buyUpgrade(fisherId, type, fishAmount);
    }

    private Map<String, Object> buildFisherResponse(Fisher fisher) {
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("fisherId", fisher.getFisherId());
        response.put("playerId", fisher.getPlayerId());
        response.put("name", fisher.getName());
        response.put("fishAmount", fisher.getFishAmount());
        // Upgrade-Level + Kosten
        var upgradeList = fisher.getUpgrades().stream()
                .map(up -> Map.of(
                        "type", up.getType(),
                        "level", up.getLevel(),
                        "cost", fisher.getUpgradeCost(up.getType())
                ))
                .toList();
        response.put("upgrades", upgradeList);
        return response;
    }

    // POST /api/game/{fisherId}/passive
    @PostMapping("/{fisherId}/passive")
    public Map<String, Object> passiveTick(@PathVariable long fisherId) {
        Fisher fisher = gameEngine.passiveTick(fisherId);
        return buildFisherResponse(fisher);
    }

}
