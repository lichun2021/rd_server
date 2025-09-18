package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 充值钻石
 * @author Jesse
 *
 */
public class DragonBoatAchieveFinishEvent extends ActivityEvent {
	/** 商品Id*/
	private int achieveId;
	
	private int count;
	public DragonBoatAchieveFinishEvent(){ super(null);}
	public DragonBoatAchieveFinishEvent(String playerId, int achieveId, int count) {
		super(playerId, true);
		this.achieveId = achieveId;
		this.count = count;
	}
	
	public int getAchieveId() {
		return achieveId;
	}
	
	public int getCount() {
		return count;
	}
}
