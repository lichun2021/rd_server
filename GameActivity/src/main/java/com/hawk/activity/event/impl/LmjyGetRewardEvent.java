package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 联盟军演获得积分奖励
 * @author Jesse
 *
 */
public class LmjyGetRewardEvent extends ActivityEvent implements OrderEvent {

	public LmjyGetRewardEvent(){ super(null);}
	public LmjyGetRewardEvent(String playerId) {
		super(playerId);
	}

}
