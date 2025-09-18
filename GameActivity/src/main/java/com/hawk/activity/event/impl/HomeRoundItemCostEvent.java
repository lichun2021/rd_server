package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 道具消耗
 *
 * @author zhy
 */
public class HomeRoundItemCostEvent extends ActivityEvent {
    /**
     * 物品数量
     */
    private int num;
    /**
     * 物品ID
     */
    private int itemId;

    public HomeRoundItemCostEvent() {
        super(null);
    }

    public HomeRoundItemCostEvent(String playerId, int itemId, int num) {
        super(playerId);
        this.itemId = itemId;
        this.num = num;
    }

    public int getItemId() {
        return itemId;
    }

    public int getNum() {
        return num;
    }
}
