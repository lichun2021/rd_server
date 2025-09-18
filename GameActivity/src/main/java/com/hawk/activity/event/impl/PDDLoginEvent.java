package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PDDLoginEvent extends ActivityEvent {
    public PDDLoginEvent() {
        super(null);
    }
    public PDDLoginEvent(String playerId) {
        super(playerId, true);
    }
}
