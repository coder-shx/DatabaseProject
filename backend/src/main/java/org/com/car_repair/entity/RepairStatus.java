package org.com.car_repair.entity;

public enum RepairStatus {
    PENDING("待处理"),
    ASSIGNED("已分配"),
    IN_PROGRESS("维修中"),
    COMPLETED("已完成"),
    CANCELED("已取消");

    private final String displayName;

    RepairStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 