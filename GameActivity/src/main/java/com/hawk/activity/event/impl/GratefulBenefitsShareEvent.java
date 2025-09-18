package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 感恩福利分享事件
 */
public class GratefulBenefitsShareEvent extends ActivityEvent {

	public GratefulBenefitsShareEvent(){ super(null);}
    public GratefulBenefitsShareEvent(String playerId) {
        super(playerId);
    }
}
