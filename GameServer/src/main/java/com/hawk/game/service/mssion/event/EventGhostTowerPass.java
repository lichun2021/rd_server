package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 通过幽灵塔第{}层
 * 
 * @author golden
 *
 */
public class EventGhostTowerPass extends MissionEvent {

	private int level;

	private int floor;

	public EventGhostTowerPass(int level, int floor) {
		this.level = level;
		this.floor = floor;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getFloor() {
		return floor;
	}

	public void setFloor(int floor) {
		this.floor = floor;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.GHOST_TOWER_PASS);
		return touchMissionList;
	}
}