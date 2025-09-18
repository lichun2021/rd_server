package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 占领超级武器
 * @author golden
 *
 */
public class EventControlSuperWeapon extends MissionEvent {
	/**
	 * 新增个点id 区分战区用hf
	 */
	private int pointId;
	public EventControlSuperWeapon(int pointId) {
		this.pointId = pointId;
	}

	public int getPointId() {
		return pointId;
	}

	public void setPointId(int pointId) {
		this.pointId = pointId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_SOLE_SUPER_WEAPON);
		return touchMissionList;
	}
}