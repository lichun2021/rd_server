package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 机甲进阶事件
 * 
 * @author lating
 *
 */
public class EventMechaAdvance extends MissionEvent {

	private int soldierId;
	
	private int step;
	
	public EventMechaAdvance(int soldierId, int step) {
		this.soldierId = soldierId;
		this.step = step;
	}
	
	public int getSoldierId() {
		return soldierId;
	}
	
	public int getStep() {
		return step;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MECHA_ADVANCE);
		return touchMissionList;
	}

}
