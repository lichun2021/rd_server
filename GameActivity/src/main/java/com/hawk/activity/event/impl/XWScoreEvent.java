package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class XWScoreEvent extends ActivityEvent implements OrderEvent {
    private long score;
    // 是否是联赛
    private boolean isLeagua;
    //进入战场时间
    private long enterTime;

    public XWScoreEvent(){ super(null);}
    public XWScoreEvent(String playerId, long score, boolean isLeagua,long enterTime) {
        super(playerId,true);
        this.score = score;
        this.isLeagua = isLeagua;
        this.enterTime = enterTime;
    }

    public final long getScore() {
        return score;
    }

    public boolean isLeagua() {
        return isLeagua;
    }

    public long getEnterTime() {
        return enterTime;
    }
}
