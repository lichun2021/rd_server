package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 建筑建造事件
 * 
 * @author golden
 *
 */
public class EventBuildingCreate extends MissionEvent {

	/** 建筑cfgId */
	int buildingCfgId;

	public EventBuildingCreate(int buildingCfgId) {
		this.buildingCfgId = buildingCfgId;
	}

	public int getBuildingCfgId() {
		return buildingCfgId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_BUILD_CREATE);
		touchMissionList.add(MissionType.MISSION_BUILD_UPGRADE);
		touchMissionList.add(MissionType.MISSION_HAVE_BUILD_LEVEL_COUNT);
		touchMissionList.add(MissionType.MISSION_BUILD_COUNT_LEVEL);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_BUILD_CREATE);
		touchMissionList.add(MissionType.MISSION_BUILD_UPGRADE);
		return touchMissionList;
	}
}
