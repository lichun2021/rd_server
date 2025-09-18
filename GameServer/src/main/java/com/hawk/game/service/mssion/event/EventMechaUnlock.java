package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 机甲解锁事件
 * 
 * @author lating
 *
 */
public class EventMechaUnlock extends MissionEvent {

	private int soldierId;
	
	public EventMechaUnlock(int soldierId) {
		this.soldierId = soldierId;
	}
	
	public int getSoldierId() {
		return soldierId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MECHA_UNLOCK);
		return touchMissionList;
	}

}
