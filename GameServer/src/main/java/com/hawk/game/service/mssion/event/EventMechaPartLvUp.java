package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 机甲部件升级事件
 * 
 * @author lating
 *
 */
public class EventMechaPartLvUp extends MissionEvent {

	private int soldierId;
	
	public EventMechaPartLvUp(int soldierId) {
		this.soldierId = soldierId;
	}
	
	public int getSoldierId() {
		return soldierId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MECHA_PART_LEVELUP);
		touchMissionList.add(MissionType.MECHA_PART_LEVELUP_MAX);
		return touchMissionList;
	}

}
