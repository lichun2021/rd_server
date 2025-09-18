package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GuildBackVitCostEvent  extends ActivityEvent {
    private int num;
    public GuildBackVitCostEvent() {
        super(null);
    }

    public GuildBackVitCostEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
