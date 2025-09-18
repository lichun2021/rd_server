package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻击据点
 * @author golden
 *
 */
public class EventAttackStrongpoint extends MissionEvent {
	
	int level;
	
	boolean isWin;
	
	public EventAttackStrongpoint(int level, boolean isWin) {
		super();
		this.level = level;
		this.isWin = isWin;
	}

	public int getLevel() {
		return level;
	}

	public boolean isWin() {
		return isWin;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATTACK_STRONGPOINT_TIMES);
		touchMissionList.add(MissionType.MISSION_ATTACK_STRONGPOINT_WIN_TIMES);
		touchMissionList.add(MissionType.MISSION_SOLE_STRONGPOINT_WIN_TIMES);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		return touchMissions();
	}
}
