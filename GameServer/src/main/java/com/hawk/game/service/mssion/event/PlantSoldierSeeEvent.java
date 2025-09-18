package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 了解泰能
 *
 */
public class PlantSoldierSeeEvent extends MissionEvent {
	

	public PlantSoldierSeeEvent() {
		super();
	}
	

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.PLANT_SOLDIER_SEE);
		return touchMissionList;
	}
}
