package com.example.backend_quanlynhahanglau.enums;

import lombok.Getter;

@Getter
public enum DiningFloor {
    FLOOR_1("Tầng 1", 100),
    FLOOR_2("Tầng 2", 100);

    private final String displayName;
    private final Integer capacityLimit; // Sức chứa tối đa của tầng

    DiningFloor(String displayName, Integer capacityLimit) {
        this.displayName = displayName;
        this.capacityLimit = capacityLimit;
    }
}

