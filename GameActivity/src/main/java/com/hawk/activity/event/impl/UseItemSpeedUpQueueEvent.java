package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class UseItemSpeedUpQueueEvent extends ActivityEvent {
    private int queueType;
    private int minute;

    public UseItemSpeedUpQueueEvent(){ super(null);}
    public UseItemSpeedUpQueueEvent(String playerId, int queueType, int minute) {
        super(playerId);
        this.queueType = queueType;
        this.minute = minute;
    }

    public int getQueueType() {
        return queueType;
    }

    public int getMinute() {
        return minute;
    }
}
