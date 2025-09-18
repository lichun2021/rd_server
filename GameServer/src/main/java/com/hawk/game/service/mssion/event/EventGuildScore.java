package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 联盟个人积分增加事件
 * 
 * @author golden
 *
 */
public class EventGuildScore extends MissionEvent {

	/** 增加后积分 */
	long afterScore;

	/** 增加积分 */
	int addScore;
	
	public EventGuildScore(int addValue, long afterScore) {
		this.addScore = addValue;
		this.afterScore = afterScore;
	}

	public long getAfterScore() {
		return afterScore;
	}
	
	public int getAddScore() {
		return addScore;
	}

	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_GUILD_SCORE);
		return touchMissionList;
	}
}