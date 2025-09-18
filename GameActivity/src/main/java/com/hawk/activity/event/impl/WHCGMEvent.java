package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class WHCGMEvent extends ActivityEvent {
    public static final int UNKNOW = 0;
    public static final int LUCK_GET_GOLD_DRAW_TEN = 1;

    private int type;
    public WHCGMEvent(){
        super(null);
    }

    public WHCGMEvent(String playerId, int type){
        super(playerId);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
