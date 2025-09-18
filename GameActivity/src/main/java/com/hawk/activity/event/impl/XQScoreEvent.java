package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

public class XQScoreEvent extends ActivityEvent implements OrderEvent {
    private long score;
    // 是否是联赛
    private boolean isLeagua;


    public XQScoreEvent(){ super(null);}
    public XQScoreEvent(String playerId, long score, boolean isLeagua) {
        super(playerId,true);
        this.score = score;
        this.isLeagua = isLeagua;
    }

    public final long getScore() {
        return score;
    }

    public boolean isLeagua() {
        return isLeagua;
    }

}
