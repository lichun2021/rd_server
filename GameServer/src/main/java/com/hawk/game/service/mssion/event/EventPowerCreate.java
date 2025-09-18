package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 战力增加事件
 * 
 * @author golden
 *
 */
public class EventPowerCreate extends MissionEvent {

	/** 增加前战力 */
	long beforePower;

	/** 增加后战力 */
	long afterPower;

	public EventPowerCreate(long beforePower, long afterPower) {
		super();
		this.beforePower = beforePower;
		this.afterPower = afterPower;
	}

	public long getBeforePower() {
		return beforePower;
	}

	public long getAfterPower() {
		return afterPower;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_POWER_CREATE);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_POWER_CREATE);
		return touchMissionList;
	}
}
