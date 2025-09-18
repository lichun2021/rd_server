package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 占领据点事件
 * @author golden
 *
 */
public class OccupyStrongpointEvent extends ActivityEvent implements OrderEvent {
	// 据点等级
	private int pointLvl;
	private boolean atkWin;

	public OccupyStrongpointEvent(){ super(null);}
	public OccupyStrongpointEvent(String playerId, int pointLvl, boolean atkWin) {
		super(playerId);
		this.pointLvl = pointLvl;
		this.atkWin = atkWin;
	}

	public int getPointLvl() {
		return pointLvl;
	}

	public boolean isAtkWin() {
		return atkWin;
	}

	@Override
	public boolean isOfflineResent() {
		return true;
	}
}
