package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 资源采集数量事件
 * 
 * @author golden
 *
 */
public class EventResourceCollectCount extends MissionEvent {

	/** 资源类型 */
	int resourceType;

	/** 采集数量 */
	int count;

	public EventResourceCollectCount(int resourceType, int count) {
		this.resourceType = resourceType;
		this.count = count;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_COLLECT_COUNT);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_COLLECT_COUNT);
		return touchMissionList;
	}
}

