package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 抢夺资源
 * @author golden
 *
 */
public class EventGrabResource extends MissionEvent {
	
	int resourceType;
	
	int count;

	public EventGrabResource(int resourceType, int count) {
		super();
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
		touchMissionList.add(MissionType.MISSION_GRAB_RESOURCE_COUNT);
		return touchMissionList;
	}
}
