package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;

/**
 * 机甲召唤活动每日登录事件
 * 
 * @author lating
 *
 */
public class SpaceMechaDailyLoginEvent extends ActivityEvent implements SpaceMechaEvent {
	public SpaceMechaDailyLoginEvent(){ super(null);}
	public SpaceMechaDailyLoginEvent(String playerId) {
		super(playerId);
	}
	
}
