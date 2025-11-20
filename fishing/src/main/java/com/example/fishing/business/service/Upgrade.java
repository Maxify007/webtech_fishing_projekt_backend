package com.example.fishing.business.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Upgrade {
private UpgradeType type;
private int level;

public Upgrade(UpgradeType type, int level) {
    this.type = type;
    this.level = level;
}
}
