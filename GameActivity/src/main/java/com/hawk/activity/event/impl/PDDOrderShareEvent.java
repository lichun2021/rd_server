package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class PDDOrderShareEvent extends ActivityEvent {
    public PDDOrderShareEvent() {
        super(null);
    }
    public PDDOrderShareEvent(String playerId) {
        super(playerId, true);
    }
}
