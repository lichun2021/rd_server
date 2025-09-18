package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 击杀士兵
 * @author golden
 *
 */
public class EventKillSolider extends MissionEvent {
	
	int count;
	
	public EventKillSolider(int count) {
		super();
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_KILL_SOLIDER_COUNT);
		return touchMissionList;
	}
}
