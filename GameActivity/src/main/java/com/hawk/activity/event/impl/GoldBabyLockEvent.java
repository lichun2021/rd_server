package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;


public class GoldBabyLockEvent extends ActivityEvent {
	
	private static final long serialVersionUID = 1L;
	private int poolId;
	public GoldBabyLockEvent(){ super(null);}
	public GoldBabyLockEvent(String playerId,int poolId) {
		super(playerId);
		this.poolId = poolId;
	}
	
	public int getPoolId() {
		return poolId;
	}
}

