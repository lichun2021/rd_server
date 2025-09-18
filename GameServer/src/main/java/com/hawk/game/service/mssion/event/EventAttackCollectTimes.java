package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻打资源点
 * @author golden
 *
 */
public class EventAttackCollectTimes extends MissionEvent {
	
	int resourceType;
	
	boolean isWin;

	public EventAttackCollectTimes(int resourceType, boolean isWin) {
		super();
		this.resourceType = resourceType;
		this.isWin = isWin;
	}

	public int getResourceType() {
		return resourceType;
	}

	public boolean isWin() {
		return isWin;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATTACK_COLLECT_TIMES);
		return touchMissionList;
	}
}
