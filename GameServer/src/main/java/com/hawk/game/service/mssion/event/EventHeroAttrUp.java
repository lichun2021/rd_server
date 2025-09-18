package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 升级英雄属性
 * @author golden
 *
 */
public class EventHeroAttrUp extends MissionEvent {

	int count;
	
	public EventHeroAttrUp(int count) {
		super();
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_HERO_ATTR_UP);
		return touchMissionList;
	}
}
