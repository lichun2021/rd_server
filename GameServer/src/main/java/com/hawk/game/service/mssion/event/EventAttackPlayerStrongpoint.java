package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻击玩家据点并胜利{2}次
 * @author golden
 *
 */
public class EventAttackPlayerStrongpoint extends MissionEvent {
	boolean isWin;

	public EventAttackPlayerStrongpoint(boolean isWin) {
		super();
		this.isWin = isWin;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATTACK_PLAYER_STRONGPOINT_TIMES);
		return touchMissionList;
	}
}
