package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;

/**
 * 占领据点事件
 * @author golden
 *
 */
public class OccupyPylonFinishEvent extends ActivityEvent implements CrossActivityEvent {
	// 据点等级
	private int pointCfgId; 
	public OccupyPylonFinishEvent(){ super(null);}
	public OccupyPylonFinishEvent(String playerId, int pointCfgId) {
		super(playerId);
		this.pointCfgId = pointCfgId;
	}
	

	public int getPointCfgId() {
		return pointCfgId;
	}
	@Override
	public boolean isOfflineResent() {
		return true;
	}
}
