package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 英雄委任事件
 * 
 * @author lating
 *
 */
public class EventHeroAppoint extends MissionEvent {
	
	private int unlockBuildingType;

	public EventHeroAppoint(int unlockBuildingType) {
		this.unlockBuildingType = unlockBuildingType;
	}
	
	public int getUnlockBuildingType() {
		return unlockBuildingType;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.HERO_APPOINT_COUNT);
		return touchMissionList;
	}
	
}
