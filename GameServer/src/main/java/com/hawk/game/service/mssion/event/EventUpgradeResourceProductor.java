package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 使用资源增产道具
 * @author golden
 *
 */
public class EventUpgradeResourceProductor extends MissionEvent {
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_UPGRADE_RESOURCE_PRODUCTOR);
		touchMissionList.add(MissionType.MISSION_UPGRADE_RESOURCE_FIELD_COUNT);
		return touchMissionList;
	}
}
