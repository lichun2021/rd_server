package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class PlantSoldierStepMaxEvent extends MissionEvent {

	public PlantSoldierStepMaxEvent() {
		super();
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.PLANT_SOLDIER_STEP_MAX);
		return touchMissionList;
	}
}
