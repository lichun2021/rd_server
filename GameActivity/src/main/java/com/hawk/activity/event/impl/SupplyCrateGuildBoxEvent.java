package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SupplyCrateGuildBoxEvent extends ActivityEvent {
    private int num;

    public SupplyCrateGuildBoxEvent(){
        super(null);
    }

    public SupplyCrateGuildBoxEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
