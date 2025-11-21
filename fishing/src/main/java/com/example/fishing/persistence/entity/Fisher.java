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
    private double lastPassiveTickMillis;
    @Transient
    private List<Upgrade> upgrades = new ArrayList<>();
    private int fishProgress;

    private static final Random RNG = new Random();

public Fisher(Long playerId,String name) {
    this.playerId = playerId;
    this.name = name;
    this.fishAmount = 0;
    initUpgrades();
    recalculateStats();

    this.lastPassiveTickMillis = System.currentTimeMillis();
    this.fishProgress = 0;
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
    upgrades.stream()
            .filter(upgrade -> upgrade.getType() == type)
            .findFirst()
            .ifPresent(upgrade -> upgrade.setLevel(upgrade.getLevel() + 1));
    recalculateStats();
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
        fishProgress = 0;
    } else {
        fishProgress++;
    }
}


}

