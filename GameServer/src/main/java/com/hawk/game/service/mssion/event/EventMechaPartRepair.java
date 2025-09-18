package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 机甲部件修复
 * 
 * @author lating
 *
 */
public class EventMechaPartRepair extends MissionEvent {

	private int partId;
	
	public EventMechaPartRepair(int partId) {
		this.partId = partId;
	}
	
	public int getPartId() {
		return partId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MECHA_PART_REPAIR);
		return touchMissionList;
	}

}
