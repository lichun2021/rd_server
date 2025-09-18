package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 装备科技升级事件
 */
public class ArmourTechLevelUpEvent extends ActivityEvent {



	public ArmourTechLevelUpEvent(){ super(null);}
	public ArmourTechLevelUpEvent(String playerId) {
		super(playerId);
	}

}
