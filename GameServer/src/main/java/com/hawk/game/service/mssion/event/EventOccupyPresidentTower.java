package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventOccupyPresidentTower extends MissionEvent {

	private long occupyTime;
	
	public EventOccupyPresidentTower(long occupyTime) {
		this.occupyTime = occupyTime;
	}

	public long getOccupyTime() {
		return occupyTime;
	}
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.OCCUPY_PRESIDENT_TOWER_MINUTE);
		return touchMissionList;
	}	
}
