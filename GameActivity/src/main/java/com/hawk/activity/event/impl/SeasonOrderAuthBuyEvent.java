package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 赛季活动定制礼包购买事件
 *
 * @author jesse
 *
 */
public class SeasonOrderAuthBuyEvent extends ActivityEvent {
    private String payGiftId;
    public SeasonOrderAuthBuyEvent(){ super(null);}
    public SeasonOrderAuthBuyEvent(String playerId, String payGiftId) {
        super(playerId, true);
        this.payGiftId = payGiftId;
    }

    public String getPayGiftId() {
        return payGiftId;
    }
}
