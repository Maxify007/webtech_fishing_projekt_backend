package com.example.fishing.persistence.entity;

import com.example.fishing.business.service.Upgrade;

import java.util.List;

public class Fisher {
    long id;
    String name;
    long fishAmount;
    List<Upgrade>upgrades;
    long luckRate;
    float luckMultiplier;
    long fishSpeedMultiplier;
    long fishPerPull;
    long MasteryScore;
}