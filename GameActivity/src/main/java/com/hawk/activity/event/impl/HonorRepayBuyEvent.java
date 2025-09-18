package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 荣耀返利购买 事件
 */
public class HonorRepayBuyEvent extends ActivityEvent {

    private int times;
    public HonorRepayBuyEvent(){super(null);}
    public HonorRepayBuyEvent(String playerId, int times) {
        super(playerId);
        this.times = times;
    }

    public int getTimes() {
        return times;
    }

}
