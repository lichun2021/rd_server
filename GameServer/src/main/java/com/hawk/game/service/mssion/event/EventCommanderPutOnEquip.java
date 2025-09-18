package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 指挥官穿装备
 * @author golden
 *
 */
public class EventCommanderPutOnEquip extends MissionEvent {
	
	int level;
	
	public EventCommanderPutOnEquip(int level) {
		super();
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_PUT_ON_COMMANDER_EQUIP_TIMES);
		touchMissionList.add(MissionType.MISSION_COMMANDER_PUT_ON_EQUIP_COUNT);
		return touchMissionList;
	}
}
