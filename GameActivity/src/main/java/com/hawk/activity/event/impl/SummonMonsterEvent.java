package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 世界召唤野怪事件
 * @author Jesse
 *
 */
public class SummonMonsterEvent extends ActivityEvent {

	private int monsterId;

	public SummonMonsterEvent(){ super(null);}
	public SummonMonsterEvent(String playerId, int monsterId) {
		super(playerId);
		this.monsterId = monsterId;
	}

	public int getMonsterId() {
		return monsterId;
	}

}
