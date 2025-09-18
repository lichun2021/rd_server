package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class DevelopFastBuyEvent extends ActivityEvent {
    private String payGiftId;

    public DevelopFastBuyEvent() {
        super(null);
    }

    public DevelopFastBuyEvent(String playerId, String payGiftId) {
        super(playerId, true);
        this.payGiftId = payGiftId;
    }

    public String getPayGiftId() {
        return payGiftId;
    }
}
