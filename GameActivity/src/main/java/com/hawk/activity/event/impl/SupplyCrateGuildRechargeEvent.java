package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SupplyCrateGuildRechargeEvent extends ActivityEvent {
    private int num;

    public SupplyCrateGuildRechargeEvent(){
        super(null);
    }

    public SupplyCrateGuildRechargeEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
