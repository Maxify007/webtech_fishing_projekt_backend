package com.example.fishing.rest.model;

import com.example.fishing.business.service.UpgradeType;
import com.example.fishing.persistence.entity.Fisher;
import com.example.fishing.persistence.entity.FisherRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fishers")
public class FisherController {

    private final FisherRepository fisherRepository;

    public FisherController(FisherRepository fisherRepository) {
        this.fisherRepository = fisherRepository;
    }

    // GET /api/fishers?playerId=1
    @GetMapping
    public List<Fisher> listFishers(@RequestParam long playerId) {
        return fisherRepository.findByPlayerId(playerId);
    }

    // POST /api/fishers
    @PostMapping
    public Fisher createFisher(@RequestBody CreateFisherRequest req) {
        Fisher fisher = new Fisher(req.playerId(), req.name());
        return fisherRepository.save(fisher);
    }

    // GET /api/fishers/{fisherId}?playerId=1
    @GetMapping("/{fisherId}")
    public Fisher getFisher(@PathVariable long fisherId,
                            @RequestParam long playerId) {
        Fisher fisher = fisherRepository.findById(fisherId)
                .orElseThrow(() -> new IllegalArgumentException("Fisher not found"));

        if (fisher.getPlayerId() != playerId) {
            throw new IllegalStateException("Not your Fisher");
        }
        return fisher;
    }

    @PostMapping("/{fisherId}/upgrade")
    public Fisher upgrade(@PathVariable long fisherId,
                          @RequestParam long playerId,
                          @RequestParam UpgradeType type) {

        Fisher fisher = getFisher(fisherId, playerId);
        fisher.increaseLevelOf(type);
        return fisherRepository.save(fisher);
    }

    @PostMapping("/{fisherId}/click")
    public Fisher click(@PathVariable long fisherId,
                        @RequestParam long playerId) {

        Fisher fisher = getFisher(fisherId, playerId);
        fisher.fishingAction();
        return fisherRepository.save(fisher);
    }

    @PostMapping("/{fisherId}/passive")
    public Fisher passive(@PathVariable long fisherId,
                          @RequestParam long playerId) {

        Fisher fisher = getFisher(fisherId, playerId);
        fisher.passiveFishingTick(); // musst du evtl. noch hinzufügen
        return fisherRepository.save(fisher);
    }

}
