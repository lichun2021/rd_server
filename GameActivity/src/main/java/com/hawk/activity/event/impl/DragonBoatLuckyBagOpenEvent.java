package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 端午节日礼包打开
 * @author che
 *
 */
public class DragonBoatLuckyBagOpenEvent extends ActivityEvent {

	
	private int openCount;

	public DragonBoatLuckyBagOpenEvent(){ super(null);}
	public DragonBoatLuckyBagOpenEvent(String playerId, int openCount) {
		super(playerId);
		this.openCount = openCount;
	}

	public int getOpenCount() {
		return openCount;
	}

	public void setOpenCount(int openCount) {
		this.openCount = openCount;
	}


	
	
	
	
	

}
