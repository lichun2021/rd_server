package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 英雄升星事件
 * @author golden
 *
 */
public class EventHeroStarUp extends MissionEvent {

	int heroId;
	
	int beforeStar;
	
	int afterStar;
	
	public EventHeroStarUp(int heroId, int beforeStar, int afterStar) {
		super();
		this.heroId = heroId;
		this.beforeStar = beforeStar;
		this.afterStar = afterStar;
	}

	public int getHeroId() {
		return heroId;
	}

	public int getBeforeStar() {
		return beforeStar;
	}

	public int getAfterStar() {
		return afterStar;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_HAVE_HERO_STAR_COUNT);
		touchMissionList.add(MissionType.MISSION_HERO_STAR_UP);
		return touchMissionList;
	}
}
