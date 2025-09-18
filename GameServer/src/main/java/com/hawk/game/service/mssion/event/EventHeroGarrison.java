package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * {1}英雄驻防{2}次
 * @author golden
 *
 */
public class EventHeroGarrison extends MissionEvent {

	private int heroId;

	private int officeId;
	
	public EventHeroGarrison(int heroId, int officeId) {
		this.heroId = heroId;
		this.officeId = officeId;
	}

	public int getHeroId() {
		return heroId;
	}

	public void setHeroId(int heroId) {
		this.heroId = heroId;
	}
	
	public int getOfficeId() {
		return officeId;
	}

	public void setOfficeId(int officeId) {
		this.officeId = officeId;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_HERO_GARRISON);
		return touchMissionList;
	}
}
