package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 天赋升级事件
 * 
 * @author golden
 *
 */
public class EventTalentUpgrade extends MissionEvent {

	/** 天赋id */
	int talentId;

	/** 升级前等级 */
	int beforeLevel;

	/** 升级后等级 */
	int afterLevel;

	public EventTalentUpgrade(int talentId, int beforeLevel, int afterLevel) {
		this.talentId = talentId;
		this.beforeLevel = beforeLevel;
		this.afterLevel = afterLevel;
	}

	public int getTalentId() {
		return talentId;
	}

	public int getBeforeLevel() {
		return beforeLevel;
	}

	public int getAfterLevel() {
		return afterLevel;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_TALENT_UPGRADE);
		return touchMissionList;
	}
}
