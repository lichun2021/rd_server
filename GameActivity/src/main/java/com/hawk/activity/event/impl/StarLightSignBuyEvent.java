package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class StarLightSignBuyEvent  extends ActivityEvent {
    private String payGiftId;
    public StarLightSignBuyEvent(){ super(null);}
    public StarLightSignBuyEvent(String playerId, String payGiftId){
        super(playerId, true);
        this.payGiftId = payGiftId;
    }
    public String getPayGiftId() {
        return payGiftId;
    }
}
