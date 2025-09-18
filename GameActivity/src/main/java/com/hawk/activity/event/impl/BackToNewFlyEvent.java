package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class BackToNewFlyEvent extends ActivityEvent {

    public BackToNewFlyEvent() {
        super(null);
    }

    public BackToNewFlyEvent(String playerId) {
        super(playerId);
    }
}
