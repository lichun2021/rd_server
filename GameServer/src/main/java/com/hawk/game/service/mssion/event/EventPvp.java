package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 战斗事件
 * 
 * @author golden
 *
 */
public class EventPvp extends MissionEvent {

	/** 是否胜利 */
	boolean win;
	/**
	 * 地方打本等级
	 */
	int constrFactorLvl;

	public EventPvp(boolean win, int constrFactorLvl) {
		this.win = win;
		this.constrFactorLvl = constrFactorLvl;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}
	
	public int getConstrFactorLvl() {
		return constrFactorLvl;
	}

	public void setConstrFactorLvl(int constrFactorLvl) {
		this.constrFactorLvl = constrFactorLvl;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_PVP);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_PVP);
		return touchMissionList;
	}
}
