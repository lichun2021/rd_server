package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventDefenceGhostStrike extends MissionEvent {

	boolean isWin;
	
	int targetId;

	public EventDefenceGhostStrike(boolean isWin, int targetId) {
		super();
		this.isWin = isWin;
		this.targetId = targetId;
	}
	
	public boolean isWin() {
		return isWin;
	}

	public int getTargetId() {
		return targetId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.DEFENCE_GHOST_STRIKE_WIN);
		return touchMissionList;
	}
}
