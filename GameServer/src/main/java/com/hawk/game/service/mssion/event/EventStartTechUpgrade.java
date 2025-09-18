package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 开始研究{1}id的科技{2}次
 * @author golden
 *
 */
public class EventStartTechUpgrade extends MissionEvent {

	int techId;
	
	public int getTechId() {
		return techId;
	}

	public EventStartTechUpgrade(int techId) {
		this.techId = techId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_START_TECH__TIMES);
		touchMissionList.add(MissionType.MISSION_TECHNOLOGY_STUDY_TIMES);
		return touchMissionList;
	}
}
