package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;

/**
 * 占领据点事件
 * @author golden
 *
 */
public class OccupyStrongpointFinishEvent extends ActivityEvent implements CrossActivityEvent {
	// 据点等级
	private int pointLvl; 
	// 占领时长(s)
	private int occupyTime; 
	public OccupyStrongpointFinishEvent(){ super(null);}
	public OccupyStrongpointFinishEvent(String playerId, int pointLvl, int occupyTime) {
		super(playerId);
		this.pointLvl = pointLvl;
		this.occupyTime = occupyTime;
	}
	public int getPointLvl() {
		return pointLvl;
	}
	public int getOccupyTime() {
		return occupyTime;
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}
}
