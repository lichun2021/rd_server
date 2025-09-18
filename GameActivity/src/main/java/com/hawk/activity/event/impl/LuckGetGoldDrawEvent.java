package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class LuckGetGoldDrawEvent extends ActivityEvent {
    private int num;
    public LuckGetGoldDrawEvent(){ super(null);}

    public LuckGetGoldDrawEvent(String playerId, int num) {
        super(playerId);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
