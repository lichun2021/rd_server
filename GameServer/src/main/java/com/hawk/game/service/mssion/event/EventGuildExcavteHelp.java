package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 联盟宝藏帮助挖掘
 * @author golden
 *
 */
public class EventGuildExcavteHelp extends MissionEvent {

	/** 挖掘次数 */
	int times;

	public EventGuildExcavteHelp(int times) {
		this.times = times;
	}

	public int getTimes() {
		return times;
	}

	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_GUILD_EXCAVTE_HELP);
		return touchMissionList;
	}
}