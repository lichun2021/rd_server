package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 发展冲刺进阶奖励
 * 
 * @author lating
 *
 */
public class DevelopSpurtAdvancedUnlockEvent extends ActivityEvent {

	public DevelopSpurtAdvancedUnlockEvent(){ super(null);}
	public DevelopSpurtAdvancedUnlockEvent(String playerId) {
		super(playerId, true);
	}
}
