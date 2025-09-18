package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

public class EventSoldierTrainStart extends MissionEvent {

	/** 士兵id */
	int soldierId;

	/** 训练数量 */
	int count;

	public EventSoldierTrainStart(int soldierId, int count) {
		this.soldierId = soldierId;
		this.count = count;
	}

	public int getSoldierId() {
		return soldierId;
	}

	public int getCount() {
		return count;
	}


	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_SOLDIER_TRAIN_START);
		touchMissionList.add(MissionType.MISSION_SOLDIER_TRAIN_START_CUMULATIVE);
		return touchMissionList;
	}
}
