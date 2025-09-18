package com.hawk.activity.type.impl.homeland.entity;

public class HomeLandPuzzlePoolItem {
    private final HomeLandPuzzlePoolType pool;
    private final int weight;
    private final int cfgId;
    private String itemInfo;
    private String patternItem;

    public HomeLandPuzzlePoolItem(int cfgId, HomeLandPuzzlePoolType pool, int weight) {
        this.pool = pool;
        this.cfgId = cfgId;
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public int getId() {
        return cfgId;
    }

    public HomeLandPuzzlePoolType getType() {
        return pool;
    }

    public void setItemInfo(String itemInfo) {
        this.itemInfo = itemInfo;
    }

    public void setPatternItem(String patternItem) {
        this.patternItem = patternItem;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public String getPatternItem() {
        return patternItem;
    }
}
