package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 尤里复仇个人奖励
 * @author Jesse
 *
 */
public class YuriRevengeRewardEvent extends ActivityEvent implements OrderEvent{
	
	private int rewardId;
	
	public YuriRevengeRewardEvent(){ super(null);}
	public YuriRevengeRewardEvent(String playerId, int rewardId) {
		super(playerId);
		this.rewardId = rewardId;
	}

	public int getRewardId() {
		return rewardId;
	}
	
}
