package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GuildBackTeamBattleEvent extends ActivityEvent {
    private int num;

    public GuildBackTeamBattleEvent() {
        super(null);
    }

    public GuildBackTeamBattleEvent(String playerId, int num) {
        super(playerId, true);
        this.num = num;
    }

    public int getNum() {
        return num;
    }
}
