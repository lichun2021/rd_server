package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PDDOrderDoneEvent extends ActivityEvent {
    public PDDOrderDoneEvent() {
        super(null);
    }
    public PDDOrderDoneEvent(String playerId) {
        super(playerId, true);
    }
}
