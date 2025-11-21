package com.example.fishing.persistence.entity;

import com.example.fishing.persistence.entity.Fisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FisherRepository extends JpaRepository<Fisher, Long> {

    // Later: multiple save files per player
    List<Fisher> findByPlayerId(Long playerId);
}
