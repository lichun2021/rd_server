package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 开始探索尤里实验室
 * @author golden
 *
 */
public class EventExploreYuri extends MissionEvent {

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_EXPLORE_YURI_TIMES);
		return touchMissionList;
	}
}
