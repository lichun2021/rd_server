package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 开始资源采集事件
 * 
 * @author golden
 *
 */
public class EventResourceCollectBegin extends MissionEvent {

	/** 资源id(配置id) */
	int resourceId;

	/** 资源等级 */
	int level;

	public EventResourceCollectBegin(int resourceId, int level) {
		this.resourceId = resourceId;
		this.level = level;
	}

	public int getResourceId() {
		return resourceId;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_RESOURCE_COLLECT_BEGIN);
		return touchMissionList;
	}
}
