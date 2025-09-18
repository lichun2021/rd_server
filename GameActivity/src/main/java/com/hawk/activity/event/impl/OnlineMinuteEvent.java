package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class OnlineMinuteEvent extends ActivityEvent {
    private int minute;

    public OnlineMinuteEvent(){ super(null);}
    public OnlineMinuteEvent(String playerId, int minute) {
        super(playerId);
        this.minute = minute;
    }

    public int getMinute() {
        return minute;
    }
}
