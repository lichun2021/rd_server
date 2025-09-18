package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventAgencyMissionFinishInit extends MissionEvent {

	
	public EventAgencyMissionFinishInit() {
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.AGENCY_MISSION_FINISH_INIT);
		touchMissionList.add(MissionType.AGENCY_MISSION_FINISH_INIT_CUMULATIVE);
		return touchMissionList;
	}
	
}