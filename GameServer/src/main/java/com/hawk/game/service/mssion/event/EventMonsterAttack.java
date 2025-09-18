package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻打野怪事件
 * 
 * @author golden
 *
 */
public class EventMonsterAttack extends MissionEvent {

	/** 野怪id(配置id) */
	int monsterId;

	/** 野怪等级 */
	int level;
	/**
	 * 进攻野怪是否胜利
	 */
	boolean win;
	/**
	 * 攻击次数
	 */
	int atkTimes;

	public EventMonsterAttack(int monsterId, int level, boolean win) {
		this.monsterId = monsterId;
		this.level = level;
		this.win = win;
		this.atkTimes = 1;
	}
	
	public EventMonsterAttack(int monsterId, int level, boolean win, int atkTimes) {
		this.monsterId = monsterId;
		this.level = level;
		this.win = win;
		this.atkTimes = atkTimes;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}
	
	public int getAtkTimes() {
		return atkTimes;
	}

	public void setAtkTimes(int atkTimes) {
		this.atkTimes = atkTimes;
	}

	@Override
	public List<MissionType> touchMissions() {
		if(!isWin()) {
			return null;
		}
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_MONSTER_ATTACK);
		touchMissionList.add(MissionType.MISSION_KILL_MONSTER_TIMES);
		touchMissionList.add(MissionType.MISSION_SOLE_KILL_MONSTER);
		return touchMissionList;
	}
	
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_MONSTER_ATTACK);
		return touchMissionList;
	}
}
