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

    @GetMapping("/leaderboard")
    public List<Fisher> leaderboard() {
        return fisherRepository.findTop10ByOrderByTotalFishAmountDesc();
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

    @DeleteMapping("/{fisherId}")
    public void deleteFisher(@PathVariable long fisherId,
                             @RequestParam long playerId) {

        Fisher fisher = fisherRepository.findById(fisherId)
                .orElseThrow(() -> new IllegalArgumentException("Fisher not found"));

        // Sicherheits-Check: gehört der Fisher wirklich dem Player?
        if (fisher.getPlayerId() != playerId) {
            throw new IllegalStateException("Not your Fisher");
        }

        fisherRepository.delete(fisher);
    }

}
