package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SupplyCrateGuildVitCostEvent extends ActivityEvent {
    private int num;

    public SupplyCrateGuildVitCostEvent(){
        super(null);
    }

    public SupplyCrateGuildVitCostEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
