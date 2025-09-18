package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 治疗伤兵
 * @author golden
 *
 */
public class EvenntTreatArmy extends MissionEvent {
	
	private int count;

	public EvenntTreatArmy(int count) {
		super();
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.TREAT_ARMY);
		return touchMissionList;
	}
}
