package com.hawk.activity.event.impl;

import java.util.Map;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * pvp战斗事件
 * 
 * @author PhilChen
 *
 */
@SuppressWarnings("serial")
public class PvpBattleEvent extends ActivityEvent implements StrongestEvent, CrossActivityEvent, OrderEvent {

	private boolean isAtkWin;

	/** 部队击杀数据 */
	private Map<Integer, Integer> armyKillMap;

	/** 部队击伤数据 */
	private Map<Integer, Integer> armyHurtMap;

	/** 部队击杀数据(带星级) */
	private Map<Long, Integer> armyKillDetailMap;

	/** 部队击伤数据(带星级) */
	private Map<Long, Integer> armyHurtDetailMap;

	private boolean isSameServer;

	/** 是否是进攻方 */
	private boolean isAtk;

	/** 是否攻城战斗 */
	boolean isInCity;

	/** 行军类型 */
	WorldMarchType marchType;
	public PvpBattleEvent(){ super(null);}
	public PvpBattleEvent(String playerId, boolean isAtkWin, Map<Integer, Integer> armyKillMap, Map<Integer, Integer> armyHurtMap, Map<Long, Integer> armyKillDetailMap,
			Map<Long, Integer> armyHurtDetailMap, boolean isSameServer, boolean isAtk, boolean isInCity, WorldMarchType marchType) {
		super(playerId);
		this.isAtkWin = isAtkWin;
		this.armyKillMap = armyKillMap;
		this.armyHurtMap = armyHurtMap;
		this.armyKillDetailMap = armyKillDetailMap;
		this.armyHurtDetailMap = armyHurtDetailMap;
		this.isSameServer = isSameServer;
		this.isAtk = isAtk;
		this.isInCity = isInCity;
		this.marchType = marchType;
	}

	public boolean isAtkWin() {
		return isAtkWin;
	}

	public Map<Integer, Integer> getArmyKillMap() {
		return armyKillMap;
	}

	public Map<Integer, Integer> getArmyHurtMap() {
		return armyHurtMap;
	}

	public Map<Long, Integer> getArmyKillDetailMap() {
		return armyKillDetailMap;
	}

	public Map<Long, Integer> getArmyHurtDetailMap() {
		return armyHurtDetailMap;
	}

	public boolean isSameServer() {
		return isSameServer;
	}

	public boolean isAtk() {
		return isAtk;
	}

	public boolean isInCity() {
		return isInCity;
	}

	public WorldMarchType getMarchType() {
		return marchType;
	}

	@Override
	public String toString() {
		return String.format("PvpBattle,1:%s,2:%s,3:%s", isAtkWin, armyKillMap, armyHurtMap);
	}

}
