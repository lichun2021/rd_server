package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 增加矿石产量时间(使用提升资源产量类道具，增加303 305 307 309作用号数值，触发此活动)
 * 
 * @author golden
 *
 */
public class UpgradeResourceProductorEvent extends ActivityEvent {

	/** 作用号id*/
	private int effectId;

	public UpgradeResourceProductorEvent(){ super(null);}
	public UpgradeResourceProductorEvent(String playerId, int effectId) {
		super(playerId);
		this.effectId = effectId;
	}

	public int getEffectId() {
		return effectId;
	}
}
