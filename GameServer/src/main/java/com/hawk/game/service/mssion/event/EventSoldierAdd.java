package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 士兵增加事件(训练获得不计算在内)
 * 
 * @author golden
 *
 */
public class EventSoldierAdd extends MissionEvent {

	/** 士兵id */
	int soldierId;

	/** 训练前数量 */
	int beforeCount;

	/** 训练后数量 */
	int afterCount;

	public EventSoldierAdd(int soldierId, int beforeCount, int afterCount) {
		this.soldierId = soldierId;
		this.beforeCount = beforeCount;
		this.afterCount = afterCount;
	}

	public int getSoldierId() {
		return soldierId;
	}

	public int getBeforeCount() {
		return beforeCount;
	}

	public int getAfterCount() {
		return afterCount;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_HAVE_SOLIDER);
		touchMissionList.add(MissionType.MISSION_HAVE_STAR_SOLIDER);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_HAVE_SOLIDER);
		return touchMissionList;
	}
}
