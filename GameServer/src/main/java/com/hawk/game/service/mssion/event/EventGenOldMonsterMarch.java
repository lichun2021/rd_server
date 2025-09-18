package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 向{1}等级野怪发起出征{2}次
 * @author golden
 *
 */
public class EventGenOldMonsterMarch extends MissionEvent {

	int level;
	
	public EventGenOldMonsterMarch(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.GEN_OLD_MONSTER_MARCH);
		touchMissionList.add(MissionType.GEN_OLD_MONSTER_MARCH_CUMULATIVE);
		return touchMissionList;
	}
}