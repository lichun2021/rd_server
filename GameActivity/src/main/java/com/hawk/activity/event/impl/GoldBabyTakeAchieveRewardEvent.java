package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GoldBabyTakeAchieveRewardEvent extends ActivityEvent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int achieveId;
	public GoldBabyTakeAchieveRewardEvent(){ super(null);}
	public GoldBabyTakeAchieveRewardEvent(String playerId,int achieveId) {
		super(playerId);
		this.achieveId=achieveId;
	}
	public int getAchieveId() {
		return achieveId;
	}

}
