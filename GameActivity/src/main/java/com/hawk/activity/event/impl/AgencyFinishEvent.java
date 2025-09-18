package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 完成情报中心任务
 * @author che
 *
 */
public class AgencyFinishEvent extends ActivityEvent {

	private int agencyId;
	public AgencyFinishEvent(){ super(null);}
	public AgencyFinishEvent(String playerId, int agencyId) {
		super(playerId);
		this.agencyId = agencyId;
	}
	
	public int getAgencyId() {
		return agencyId;
	}

}
