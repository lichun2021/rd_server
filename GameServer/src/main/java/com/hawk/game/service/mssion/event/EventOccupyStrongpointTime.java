package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 战力据点时长
 * @author golden
 *
 */
public class EventOccupyStrongpointTime extends MissionEvent {
	
	int level;
	
	int time;

	public EventOccupyStrongpointTime(int level, int time) {
		super();
		this.level = level;
		this.time = time;
	}

	public int getLevel() {
		return level;
	}

	public int getTime() {
		return time;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_OCCUPY_STRONGPOINT_TIME);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		return touchMissions();
	}
}
