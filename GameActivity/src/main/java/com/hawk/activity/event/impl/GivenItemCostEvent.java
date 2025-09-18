package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GivenItemCostEvent extends ActivityEvent {
    /**
     * 物品ID
     */
    private int itemId;
    /**
     * 物品数量
     */
    private int num;

    public GivenItemCostEvent(){ super(null);}

    public GivenItemCostEvent(String playerId,int itemId,int num) {
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
