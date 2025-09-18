package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PDDGoldCostEvent extends ActivityEvent {
    private int num;

    public PDDGoldCostEvent() {
        super(null);
    }

    public PDDGoldCostEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
