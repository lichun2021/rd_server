package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 联盟宝藏挖掘事件
 * 
 * @author golden
 *
 */
public class EventGuildExcavte extends MissionEvent {

	/** 挖掘次数 */
	int times;

	public EventGuildExcavte(int times) {
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_GUILD_EXCAVTE);
		return touchMissionList;
	}
}
