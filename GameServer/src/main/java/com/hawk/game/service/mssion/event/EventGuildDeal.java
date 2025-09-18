package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 联盟交易(资源援助)事件
 * 
 * @author golden
 *
 */
public class EventGuildDeal extends MissionEvent {

	/** 资源类型 */
	private int resourceType;

	/** 交易(援助)数量 */
	private int count;

	public EventGuildDeal(int resourceType, int count) {
		this.resourceType = resourceType;
		this.count = count;
	}

	public int getResourceType() {
		return resourceType;
	}

	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_GUILD_DEAL);
		return touchMissionList;
	}
}
