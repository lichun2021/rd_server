package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 建筑升级事件
 * 
 * @author golden
 *
 */
public class EventBuildingUpgrade extends MissionEvent {

	/** 建筑cfgId */
	int buildingCfgId;

	/** 建造前等级 */
	int beforeLevel;

	/** 建造后等级 */
	int afterLevel;

	public EventBuildingUpgrade(int buildingCfgId, int oldLevel, int curLevel) {
		this.buildingCfgId = buildingCfgId;
		this.beforeLevel = oldLevel;
		this.afterLevel = curLevel;
	}

	public int getBuildingCfgId() {
		return buildingCfgId;
	}

	public int getBeforeLevel() {
		return beforeLevel;
	}

	public int getAfterLevel() {
		return afterLevel;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_BUILD_UPGRADE);
		touchMissionList.add(MissionType.MISSION_HAVE_BUILD_LEVEL_COUNT);
		touchMissionList.add(MissionType.MISSION_HAVE_STAR_SOLIDER);
		touchMissionList.add(MissionType.MISSION_BUILD_COUNT_LEVEL);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_BUILD_UPGRADE);
		return touchMissionList;
	}
}
