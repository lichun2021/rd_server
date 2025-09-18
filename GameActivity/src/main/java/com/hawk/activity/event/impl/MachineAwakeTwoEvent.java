package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 机甲觉醒2(年兽) 攻击发奖事件
 * 
 * @author Jesse
 *
 */
public class MachineAwakeTwoEvent extends ActivityEvent implements OrderEvent {

	// 是否集结攻击
	private boolean isMass;
	// 是否致命一击
	private boolean isKill;
	// 是否最终一击
	private boolean isFinalKill;

	public MachineAwakeTwoEvent(){ super(null);}
	public MachineAwakeTwoEvent(String playerId, boolean isMass, boolean isKill, boolean isFinalKill) {
		super(playerId);
		this.isMass = isMass;
		this.isKill = isKill;
		this.isFinalKill = isFinalKill;
	}

	public boolean isMass() {
		return isMass;
	}

	public boolean isKill() {
		return isKill;
	}

	public boolean isFinalKill() {
		return isFinalKill;
	}

}
