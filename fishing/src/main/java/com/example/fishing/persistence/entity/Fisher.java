package com.example.fishing.persistence.entity;

import com.example.fishing.business.service.Upgrade;
import com.example.fishing.business.service.UpgradeFormula;
import com.example.fishing.business.service.UpgradeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
@Setter
@Getter
@Entity
@Table(name = "fishers")


public class Fisher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment id
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
    @Transient
    private List<Upgrade> upgrades = new ArrayList<>();
    private int fishProgress;

    private static final Random RNG = new Random();

public Fisher(Long playerId,String name) {
    this.playerId = playerId;
    this.name = name;
    this.fishAmount = 0;
    this.fishProgress = 1;
    initUpgrades();
    recalculateStats();

    this.lastPassiveTickMillis = System.currentTimeMillis();
}
    protected Fisher() {}


    public void recalculateStats() {
    int clickFlatLevel = getLevelOf(UpgradeType.CLICK_FLAT);
    int luckRateLevel = getLevelOf(UpgradeType.CLICK_LUCK_RATE);
    int luckMultLevel = getLevelOf(UpgradeType.CLICK_LUCK_MULTIPLIER);
    int masteryLevel = getLevelOf(UpgradeType.CLICK_MASTERY_MULTIPLIER);
    int passiveFishSpeedLevel = getLevelOf(UpgradeType.PASSIVE_FISH_RATE);
    int passiveFishPerPullLevel = getLevelOf(UpgradeType.PASSIVE_FISH_AMOUNT);

    // base click stats
    this.baseFishPull    = UpgradeFormula.flatPerClick(clickFlatLevel);
    this.luckRate        = UpgradeFormula.luckRate(luckRateLevel);
    this.luckMultiplier  = UpgradeFormula.luckMultiplier(luckMultLevel);
    this.masteryMultiplier = UpgradeFormula.masteryMultiplier(masteryLevel);

    // passive fishing stats
    this.passiveFishSpeedMultiplier = UpgradeFormula.passiveFishRate(passiveFishSpeedLevel);
    this.passiveFishPerPull = UpgradeFormula.passiveFishAmount(passiveFishPerPullLevel);
}
public final void initUpgrades(){
    upgrades.clear();
    for (UpgradeType type : EnumSet.allOf(UpgradeType.class)) {
        int startingLevel = 1;
        upgrades.add(new Upgrade(type,startingLevel));
    }
}
public int getLevelOf(UpgradeType type) {
    return upgrades.stream().filter(upgrade -> upgrade.getType() == type)
            .map(Upgrade::getLevel)
            .findFirst()
            .orElse(0);
}

public void increaseLevelOf(UpgradeType type) {
    long rounded = Math.round(Math.pow(1.15, getLevelOf(type)));
    long upgradeCost = 10 * rounded;
    if (fishAmount >= upgradeCost) {
        upgrades.stream()
                .filter(upgrade -> upgrade.getType() == type)
                .findFirst()
                .ifPresent(upgrade -> upgrade.setLevel(upgrade.getLevel() + 1));
        recalculateStats();
        fishAmount -= upgradeCost;
    }
}
public long calculatePull(){
    double pull = this.baseFishPull;
    double roll = RNG.nextDouble() * 100;
    boolean luckyPull = roll < this.luckRate;

    if (luckyPull) {
        pull = pull * this.luckMultiplier;

    }

    pull = pull * this.masteryMultiplier;
    long gainedFish = Math.max(1L,Math.round(pull));
    return gainedFish;
}

public void fishingAction(){
    if (fishProgress == 10){
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

