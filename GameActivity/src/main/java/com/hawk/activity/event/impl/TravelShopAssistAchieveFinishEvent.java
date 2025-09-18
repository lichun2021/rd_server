package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 特惠商人金币消耗
 * @author PhilChen
 *
 */
public class TravelShopAssistAchieveFinishEvent extends ActivityEvent {

	
	private int finishNum;

	public TravelShopAssistAchieveFinishEvent(){ super(null);}
	public TravelShopAssistAchieveFinishEvent(String playerId, int finishNum) {
		super(playerId);
		this.finishNum = finishNum;
	}

	public int getFinishNum() {
		return finishNum;
	}

	public void setFinishNum(int finishNum) {
		this.finishNum = finishNum;
	}

	
	

}
