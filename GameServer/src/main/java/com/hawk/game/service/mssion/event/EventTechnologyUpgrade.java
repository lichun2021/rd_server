package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 科技升级事件
 * 
 * @author golden
 *
 */
public class EventTechnologyUpgrade extends MissionEvent {

	/** 科技id */
	int techId;

	/** 科技类型 */
	int type;
	
	/** 升级前等级 */
	int beforeLevel;

	/** 升级后等级 */
	int afterLevel;

	public EventTechnologyUpgrade(int techId, int beforeLevel, int afterLevel, int type) {
		this.techId = techId;
		this.beforeLevel = beforeLevel;
		this.afterLevel = afterLevel;
		this.type = type;
	}

	public int getTechId() {
		return techId;
	}

	public int getBeforeLevel() {
		return beforeLevel;
	}

	public int getAfterLevel() {
		return afterLevel;
	}
	
	public int getType() {
		return type;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_TECHNOLOGY_UPGRADE);
		touchMissionList.add(MissionType.MISSION_TECHNOLOGY_TYPE_STUDY_TIMES);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_TECHNOLOGY_UPGRADE);
		return touchMissionList;
	}
}
