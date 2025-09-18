package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventAgency extends MissionEvent {
	
	int eventId;
	
	public EventAgency(int eventId) {
		this.eventId = eventId;
	}
	
	public int getEventId() {
		return eventId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_AGENCY_TIMES);
		return touchMissionList;
	}
}
