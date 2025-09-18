package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GuildBackGoldCostEvent extends ActivityEvent {
    private int num;

    public GuildBackGoldCostEvent() {
        super(null);
    }

    public GuildBackGoldCostEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
