package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;

/**
 * 攻击迷雾要塞(尖塔)
 * @author golden
 *
 */
public class AttackFoggyEvent extends ActivityEvent implements OrderEvent, EvolutionEvent, SpaceMechaEvent {

	boolean isWin;
	int level;
	/**
	 * shi 是否是集结
	 */
	boolean mass;

	public AttackFoggyEvent(){ super(null);}
	public AttackFoggyEvent(String playerId, boolean isWin, int level, boolean isMass) {
		super(playerId);
		this.isWin = isWin;
		this.level = level;
		this.mass = isMass;
	}

	public boolean isWin() {
		return isWin;
	}

	public int getLevel() {
		return level;
	}

	public boolean isMass() {
		return mass;
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}
}
