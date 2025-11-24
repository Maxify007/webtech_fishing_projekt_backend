package com.example.fishing.persistence.entity;

import com.example.fishing.business.service.Upgrade;
import com.example.fishing.business.service.UpgradeFormula;
import com.example.fishing.business.service.UpgradeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.antlr.v4.runtime.misc.Utils.count;

@Setter
@Getter
@Entity
@Table(name = "fishers")
public class Fisher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fisherId;

    private Long playerId;
    private String name;

    private long fishAmount;
    private long baseFishPull;

    private double luckRate;
    private double luckMultiplier;

    private double masteryMultiplier;

    private double passiveFishSpeedMultiplier;
    private double passiveFishPerPull;
    private long lastPassiveTickMillis;

    private int fishProgress;

    private static final Random RNG = new Random();

    /**
     * Persisted upgrade levels:
     * Creates table fisher_upgrades(fisher_id, type, level)
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "fisher_upgrades",
            joinColumns = @JoinColumn(name = "fisher_id")
    )
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "type")
    @Column(name = "level")
    private Map<UpgradeType, Integer> upgradeLevels =
            new EnumMap<>(UpgradeType.class);

    /**
     * Constructor for new fishers
     */
    public Fisher(Long playerId, String name) {
        this.playerId = playerId;
        this.name = name;
        this.fishAmount = 0;
        this.fishProgress = 1;
        this.lastPassiveTickMillis = System.currentTimeMillis();

        initUpgradeLevels();   // <-- initialize persisted levels
        recalculateStats();
    }

    protected Fisher() {}

    /**
     * For OLD fishers already stored before this change:
     * When JPA loads them and upgradeLevels is empty, seed defaults.
     */
    @PostLoad
    private void onLoad() {
        if (upgradeLevels == null || upgradeLevels.isEmpty()) {
            initUpgradeLevels();
            recalculateStats();
        }
    }

    private void initUpgradeLevels() {
        upgradeLevels.clear();
        for (UpgradeType type : EnumSet.allOf(UpgradeType.class)) {
            upgradeLevels.put(type, 1);
        }
    }

    /**
     * Keep frontend contract: return List<Upgrade> even though we store a Map.
     * This is NOT persisted; it's derived from upgradeLevels.
     */
    @Transient
    public List<Upgrade> getUpgrades() {
        List<Upgrade> list = new ArrayList<>();
        for (Map.Entry<UpgradeType, Integer> e : upgradeLevels.entrySet()) {
            list.add(new Upgrade(e.getKey(), e.getValue()));
        }
        return list;
    }

    public int getLevelOf(UpgradeType type) {
        return upgradeLevels.getOrDefault(type, 0);
    }

    public void decreaseFishAmount(long subtractAmount) {
        fishAmount -= subtractAmount;
    }

    public void increaseLevelOf(UpgradeType type) {
        // (you removed cost checks earlier; keeping your current logic)
        upgradeLevels.put(type, getLevelOf(type) + 1);
        recalculateStats();
    }

    public long getUpgradeCost(UpgradeType type) {
        long rounded = Math.round(Math.pow(1.15, getLevelOf(type)));
        long upgradeCost = 10 * rounded;
        return upgradeCost;
    }

    public void recalculateStats() {
        int clickFlatLevel = getLevelOf(UpgradeType.CLICK_FLAT);
        int luckRateLevel = getLevelOf(UpgradeType.CLICK_LUCK_RATE);
        int luckMultLevel = getLevelOf(UpgradeType.CLICK_LUCK_MULTIPLIER);
        int masteryLevel = getLevelOf(UpgradeType.CLICK_MASTERY_MULTIPLIER);
        int passiveFishSpeedLevel = getLevelOf(UpgradeType.PASSIVE_FISH_RATE);
        int passiveFishPerPullLevel = getLevelOf(UpgradeType.PASSIVE_FISH_AMOUNT);

        this.baseFishPull = UpgradeFormula.flatPerClick(clickFlatLevel);
        this.luckRate = UpgradeFormula.luckRate(luckRateLevel);
        this.luckMultiplier = UpgradeFormula.luckMultiplier(luckMultLevel);
        this.masteryMultiplier = UpgradeFormula.masteryMultiplier(masteryLevel);

        this.passiveFishSpeedMultiplier = UpgradeFormula.passiveFishRate(passiveFishSpeedLevel);
        this.passiveFishPerPull = UpgradeFormula.passiveFishAmount(passiveFishPerPullLevel);
    }

    public long calculatePull() {
        double pull = this.baseFishPull;
        double roll = RNG.nextDouble() * 100;
        boolean luckyPull = roll < this.luckRate;

        if (luckyPull) {
            pull = pull * this.luckMultiplier;
        }

        pull = pull * this.masteryMultiplier;
        return Math.max(1L, Math.round(pull));
    }

    public void fishingAction() {
        if (fishProgress == 10) {
            fishAmount = fishAmount + calculatePull();
            fishProgress = 1;
        } else {
            fishProgress++;
        }
    }

    public void passiveFishingTick() {
        long now = System.currentTimeMillis();
        // Zeit seit letztem Tick
        long elapsed = now - lastPassiveTickMillis;
        // Dauer eines Ticks in Millisekunden
        // Level 1  -> 30000ms  (1 Tick alle 30s)
        // Level 100 -> 3000ms  (1 Tick alle 3s)
        double tickDuration = passiveFishSpeedMultiplier;
        // wie viele Ticks sind vergangen?
        long ticks = (long)(elapsed / tickDuration);

        if (ticks > 0) {
            long gainedFish = (long)(ticks * passiveFishPerPull);
            fishAmount += gainedFish;
            // neuen Timestamp setzen
            lastPassiveTickMillis += (long)(ticks * tickDuration);
        }
    }

}
