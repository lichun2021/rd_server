package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 城内资源生产率
 * 
 * @author golden
 *
 */
public class EventResourceProductionRate extends MissionEvent {

	/** 资源类型 */
	int resourceType;

	/** 生产前数量 */
	int beforeNum;

	/*** 生产后数量 */
	int afterNum;

	public EventResourceProductionRate(int resourceType, int beforeNum, int afterNum) {
		this.resourceType = resourceType;
		this.beforeNum = beforeNum;
		this.afterNum = afterNum;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getBeforeNum() {
		return beforeNum;
	}

	public int getAfterNum() {
		return afterNum;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_PRODUCTION_RATE);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_PRODUCTION_RATE);
		return touchMissionList;
	}
}
