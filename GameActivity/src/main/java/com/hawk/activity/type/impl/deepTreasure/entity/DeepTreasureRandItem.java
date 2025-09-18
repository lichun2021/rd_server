package com.hawk.activity.type.impl.deepTreasure.entity;

import org.hawk.os.HawkRandObj;

public class DeepTreasureRandItem implements HawkRandObj {
    private final int rewardId;
    private final int weight;

    public static DeepTreasureRandItem valueOf(int rewardId, int weight) {
        return new DeepTreasureRandItem(rewardId, weight);
    }

    public DeepTreasureRandItem(int rewardId, int weight) {
        super();
        this.weight = weight;
        this.rewardId = rewardId;
    }

    public int getRewardId() {
        return rewardId;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
