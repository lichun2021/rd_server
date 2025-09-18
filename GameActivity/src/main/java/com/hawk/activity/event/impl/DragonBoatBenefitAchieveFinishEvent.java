package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 特惠商人金币消耗
 * @author PhilChen
 *
 */
public class DragonBoatBenefitAchieveFinishEvent extends ActivityEvent {

	
	private int finishNum;

	public DragonBoatBenefitAchieveFinishEvent(){ super(null);}
	public DragonBoatBenefitAchieveFinishEvent(String playerId, int finishNum) {
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
