package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 城内收集{1}资源{2}点 
 * @author golden
 *
 */
public class EventResourceProduction extends MissionEvent {
	
	int resType;
	
	int addNum;

	public EventResourceProduction(int resType, int addNum) {
		super();
		this.resType = resType;
		this.addNum = addNum;
	}

	public int getResType() {
		return resType;
	}

	public int getAddNum() {
		return addNum;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_PRODUCTION);
		return touchMissionList;
	}
}
