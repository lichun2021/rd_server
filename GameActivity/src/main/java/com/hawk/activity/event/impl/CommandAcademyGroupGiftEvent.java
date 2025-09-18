package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 指挥官学院科技战斗力
 * @author che
 *
 */
public class CommandAcademyGroupGiftEvent extends ActivityEvent {

	
	private int buyCount;

	private boolean isBuy;

	public CommandAcademyGroupGiftEvent(){ super(null);}
	public CommandAcademyGroupGiftEvent(String playerId, int buyCount,boolean isBuy) {
		super(playerId);
		this.buyCount = buyCount;
		this.isBuy = isBuy;
	}

	public int getBuyCount() {
		return buyCount;
	}

	public void setBuyCount(int buyCount) {
		this.buyCount = buyCount;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}


	
	
	

	

	

	
	
	
	
	

}
