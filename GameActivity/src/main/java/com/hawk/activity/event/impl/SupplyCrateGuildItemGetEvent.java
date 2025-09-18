package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SupplyCrateGuildItemGetEvent extends ActivityEvent {
    private int num;

    public SupplyCrateGuildItemGetEvent(){
        super(null);
    }

    public SupplyCrateGuildItemGetEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
