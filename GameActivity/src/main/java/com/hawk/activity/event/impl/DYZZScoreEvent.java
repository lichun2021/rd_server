package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class DYZZScoreEvent extends ActivityEvent implements OrderEvent {
    private long score;
    //进入战场时间
    private long enterTime;
    // 是否是联赛
    private boolean isLeagua;
    public DYZZScoreEvent(){ super(null);}
    public DYZZScoreEvent(String playerId, long score, long enterTime, boolean isLeagua) {
        super(playerId, true);
        this.score = score;
        this.enterTime = enterTime;
        this.isLeagua = isLeagua;
    }

    public long getScore() {
        return score;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public boolean isLeagua() {
        return isLeagua;
    }
}
