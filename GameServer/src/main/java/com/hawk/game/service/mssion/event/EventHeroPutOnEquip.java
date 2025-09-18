package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 英雄穿戴装备
 * @author golden
 *
 */
public class EventHeroPutOnEquip extends MissionEvent {
	
	int level;
	
	public EventHeroPutOnEquip(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_PUT_ON_HERO_EQUIP_COUNT);
		return touchMissionList;
	}
}
