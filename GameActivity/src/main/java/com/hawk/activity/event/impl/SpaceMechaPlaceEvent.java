package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 星甲召唤舱体放置事件
 * 
 * @author lating
 *
 */
public class SpaceMechaPlaceEvent extends ActivityEvent {
	public SpaceMechaPlaceEvent(){ super(null);}
	public SpaceMechaPlaceEvent(String playerId) {
		super(playerId);
	}

}
