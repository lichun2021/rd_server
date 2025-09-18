package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 镶嵌技能
 * @author golden
 *
 */
public class EventInstallSkill extends MissionEvent {
	
	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ALL_HERO_INSTALL_SKILL);
		touchMissionList.add(MissionType.MISSION_SINGEL_HERO_INSTALL_SKILL);
		return touchMissionList;
	}
}
