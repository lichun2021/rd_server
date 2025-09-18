package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 加入联盟事件
 * 
 * @author golden
 *
 */
public class EventGuildJoin extends MissionEvent {

	/** 联盟id */
	String guildId;

	public EventGuildJoin(String guildId) {
		this.guildId = guildId;
	}

	public String getGuildId() {
		return guildId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_GUILD_JOIN);
		return touchMissionList;
	}
}
