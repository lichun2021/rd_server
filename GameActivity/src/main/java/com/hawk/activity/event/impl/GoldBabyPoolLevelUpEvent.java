package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class GoldBabyPoolLevelUpEvent extends ActivityEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int poolId;
	public GoldBabyPoolLevelUpEvent(){ super(null);}
	public GoldBabyPoolLevelUpEvent(String playerId,int poolId) {
		super(playerId);
		this.poolId=poolId;
	}
	public int getPoolId() {
		return poolId;
	}


}
