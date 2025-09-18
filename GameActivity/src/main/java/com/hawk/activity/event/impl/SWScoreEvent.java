package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class SWScoreEvent extends ActivityEvent implements OrderEvent {
    private long kill;
    private long dead;
    private long enterTime;
    private boolean isLeagua;
    public SWScoreEvent(){ super(null);}
    public SWScoreEvent(String playerId, long kill, long dead, long enterTime, boolean isLeagua) {
        super(playerId, true);
        this.kill = kill;
        this.dead = dead;
        this.enterTime = enterTime;
        this.isLeagua = isLeagua;
    }

    public long getKill() {
        return kill;
    }

    public long getDead() {
        return dead;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public boolean isLeagua() {
        return isLeagua;
    }
}
