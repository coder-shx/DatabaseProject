package org.com.car_repair.entity;

public enum MechanicType {
    PAINTER("漆工"),
    WELDER("焊工"),
    MECHANIC("机修"),
    ELECTRIC("电工"),
    TIRE("轮胎工"),
    OTHER("其他");

    private final String displayName;

    MechanicType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 