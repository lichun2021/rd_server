package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 装备升级
 * @author golden
 *
 */
public class EventEquipUpgrade extends MissionEvent {
	
	int cfgId;
	
	int beforeLvl;
	
	int afterLvl;
	
	public EventEquipUpgrade(int cfgId, int beforeLvl, int afterLvl) {
		super();
		this.cfgId = cfgId;
		this.beforeLvl = beforeLvl;
		this.afterLvl = afterLvl;
	}

	public int getCfgId() {
		return cfgId;
	}

	public int getBeforeLvl() {
		return beforeLvl;
	}

	public int getAfterLvl() {
		return afterLvl;
	}
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_EQUIP_LVL_COUNT);
		return touchMissionList;
	}
}
