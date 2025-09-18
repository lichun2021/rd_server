package com.hawk.activity.type.impl.homeland.entity;

public enum HomeLandPuzzlePoolType {
    COMBINATION(2),  // 组合图案
    GRAND_PRIZE(1),   // 大奖图案
    NORMAL(3),       // 普通图案
    ;
    int pool;

    HomeLandPuzzlePoolType(int pool) {
        this.pool = pool;
    }

    public int getPool() {
        return pool;
    }
}
