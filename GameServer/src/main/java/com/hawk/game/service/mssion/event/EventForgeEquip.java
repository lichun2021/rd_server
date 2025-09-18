package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 装备打造
 * @author golden
 *
 */
public class EventForgeEquip extends MissionEvent {
	
	int cfgId;
	
	int level;

	int quality;

	public EventForgeEquip(int cfgId, int level, int quality) {
		super();
		this.cfgId = cfgId;
		this.level = level;
		this.quality = quality;
	}

	public int getCfgId() {
		return cfgId;
	}

	public int getLevel() {
		return level;
	}

	public int getQuality() {
		return quality;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_FORGE_EQUIP_COUNT);
		touchMissionList.add(MissionType.MISSION_EQUIP_LVL_COUNT);
		touchMissionList.add(MissionType.MISSION_EQUIP_QUALITY_COUNT);
		return touchMissionList;
	}
}
