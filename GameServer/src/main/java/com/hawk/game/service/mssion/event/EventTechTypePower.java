package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 科技战力提升
 * @author golden
 *
 */
public class EventTechTypePower extends MissionEvent {
	int type;
	
	int power;

	public EventTechTypePower(int type, int power) {
		this.type = type;
		this.power = power;
	}

	public int getType() {
		return type;
	}

	public int getPower() {
		return power;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_TECH_TYPE_POWER);
		return touchMissionList;
	}
}
