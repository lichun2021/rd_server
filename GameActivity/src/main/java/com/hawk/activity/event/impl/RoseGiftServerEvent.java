package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class RoseGiftServerEvent extends ActivityEvent {
    private int num;
    public RoseGiftServerEvent(){ super(null);}
    public RoseGiftServerEvent(String playerId, int num) {
        super(playerId);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
