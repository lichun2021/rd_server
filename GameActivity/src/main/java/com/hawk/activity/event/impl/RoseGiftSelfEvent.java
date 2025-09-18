package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class RoseGiftSelfEvent extends ActivityEvent {
    private int num;
    public RoseGiftSelfEvent(){ super(null);}
    public RoseGiftSelfEvent(String playerId, int num) {
        super(playerId);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
