package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 采集资源事件
 * 
 * @author lating
 *
 */
public class EventWorldCollectStart extends MissionEvent {

	private int resType;
	
	public EventWorldCollectStart(int resType) {
		this.resType = resType;
	}
	
	public int getResType() {
		return resType;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.WORLD_COLLECT_START);
		touchMissionList.add(MissionType.WORLD_COLLECT_START_CUMULATIVE);
		return touchMissionList;
	}

}
