package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DiffInfoSaveBuyEvent extends ActivityEvent {
    private String payGiftId;
    public DiffInfoSaveBuyEvent() {
        super(null);
    }

    public DiffInfoSaveBuyEvent(String playerId, String payGiftId) {
        super(playerId, true);
        this.payGiftId = payGiftId;
    }

    public String getPayGiftId() {
        return payGiftId;
    }
}
