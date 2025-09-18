package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 英雄升级事件
 * 
 * @author golden
 *
 */
public class EventHeroUpgrade extends MissionEvent {

	/** 英雄id */
	int heroId;

	/** 升级前等级 */
	int beforeLevel;

	/** 升级后等级 */
	int afterLevel;

	public EventHeroUpgrade(int heroId, int beforeLevel, int afterLevel) {
		this.heroId = heroId;
		this.beforeLevel = beforeLevel;
		this.afterLevel = afterLevel;
	}

	public int getHeroId() {
		return heroId;
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
		touchMissionList.add(MissionType.MISSION_HERO_UPGRADE);
		touchMissionList.add(MissionType.MISSION_ANY_HERO_UPGRADE);
		touchMissionList.add(MissionType.MISSION_HAVE_HERO_LVL_COUNT);
		return touchMissionList;
	}
}
