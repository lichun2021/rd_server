package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class CnyExamBuyEvent extends ActivityEvent {
    private String payGiftId;

    public CnyExamBuyEvent(){
        super(null);
    }

    public CnyExamBuyEvent(String playerId, String payGiftId){
        super(playerId, true);
        this.payGiftId = payGiftId;
    }

    public String getPayGiftId() {
        return payGiftId;
    }
}
