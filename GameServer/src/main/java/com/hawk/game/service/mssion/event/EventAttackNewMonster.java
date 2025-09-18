package com.hawk.game.service.mssion.event;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;

/**
 * 攻击新版野怪事件
 * @author golden
 *
 */
public class EventAttackNewMonster extends MissionEvent {
	/**
	 * 野怪id
	 */
	int monsterId;
	
	/**
	 * 野怪等级
	 */
	int monsterLvl;
	
	/**
	 * 攻击次数
	 */
	int atkTimes;
	
	/**
	 * 实际攻击次数
	 */
	int pracAtkTimes;
	
	/**
	 * 是否胜利
	 */
	boolean isWin;
	
	public EventAttackNewMonster(int monsterId, int monsterLvl, int atkTimes, int pracAtkTimes, boolean isWin) {
		this.monsterId = monsterId;
		this.monsterLvl = monsterLvl;
		this.atkTimes = atkTimes;
		this.pracAtkTimes = pracAtkTimes;
		this.isWin = isWin;
	}

	public int getMonsterId() {
		return monsterId;
	}

	public void setMonsterId(int monsterId) {
		this.monsterId = monsterId;
	}

	public int getMonsterLvl() {
		return monsterLvl;
	}

	public void setMonsterLvl(int monsterLvl) {
		this.monsterLvl = monsterLvl;
	}

	public int getAtkTimes() {
		return atkTimes;
	}

	public void setAtkTimes(int atkTimes) {
		this.atkTimes = atkTimes;
	}

	public int getPracAtkTimes() {
		return pracAtkTimes;
	}

	public void setPracAtkTimes(int pracAtkTimes) {
		this.pracAtkTimes = pracAtkTimes;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	@Override
	public List<MissionType> touchMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATTACK_NEW_MONSTER);
		touchMissionList.add(MissionType.MISSION_ATTACK_NEW_MONSTER_WIN);
		return touchMissionList;
	}
	
	@Override
	public List<MissionType> touchGeneralMissions() {
		List<MissionType> touchMissionList = new ArrayList<MissionType>();
		touchMissionList.add(MissionType.MISSION_ATTACK_NEW_MONSTER);
		return touchMissionList;
	}
}
