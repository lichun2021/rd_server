package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 解锁地块事件
 * 
 * @author golden
 *
 */
public class EventUnlockGround extends MissionEvent {

	/** 地块id */
	int groundId;

	public EventUnlockGround(int groundId) {
		this.groundId = groundId;
	}

	public int getGroundId() {
		return groundId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_UNLOCK_GROUND);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_UNLOCK_GROUND);
		return touchMissionList;
	}
}
