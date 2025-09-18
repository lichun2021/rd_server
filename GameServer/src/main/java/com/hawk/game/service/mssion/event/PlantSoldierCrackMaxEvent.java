package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class PlantSoldierCrackMaxEvent extends MissionEvent {

	public PlantSoldierCrackMaxEvent() {
		super();
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.PLANT_SOLDIER_CRACK_MAX);
		return touchMissionList;
	}
}
