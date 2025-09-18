package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventMoveCity extends MissionEvent {
	
	/**
	 * 是否主动迁城
	 */
	private boolean initiative;

	public EventMoveCity(boolean initiative) {
		super();
		this.initiative = initiative;
	}

	public boolean isInitiative() {
		return initiative;
	}
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.INITIATIVE_MOVE_CITY);
		return touchMissionList;
	}	
}
