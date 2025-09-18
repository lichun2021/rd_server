package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class YQZZScoreEvent extends ActivityEvent implements OrderEvent {
    private long militray;
    //进入战场时间
    private long enterTime;
    // 是否是联赛
    private boolean isLeagua;

    public YQZZScoreEvent(){ super(null);}
    public YQZZScoreEvent(String playerId, long militray, long enterTime, boolean isLeagua) {
        super(playerId, true);
        this.militray = militray;
        this.enterTime = enterTime;
        this.isLeagua = isLeagua;
    }

    public long getMilitray() {
        return militray;
    }

    public long getEnterTime() {
        return enterTime;
    }

    public boolean isLeagua() {
        return isLeagua;
    }
}
