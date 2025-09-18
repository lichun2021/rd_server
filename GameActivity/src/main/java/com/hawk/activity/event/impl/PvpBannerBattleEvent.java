package com.hawk.activity.event.impl;

import java.util.Map;
import com.hawk.activity.event.ActivityEvent;

/**
 * 夺旗杀敌pvp战斗事件
 * 
 */
public class PvpBannerBattleEvent extends ActivityEvent {

	/** 部队击杀数据*/
	private Map<Integer, Integer> armyKillMap;
	
	/** 部队击伤数据*/
	private Map<Integer, Integer> armyHurtMap;
	/**
	 * 是否是进攻方
	 */
	private boolean isAtk;

	public PvpBannerBattleEvent(){ super(null);}
	public PvpBannerBattleEvent(String playerId, Map<Integer, Integer> armyKillMap, Map<Integer, Integer> armyHurtMap, boolean isAtk) {
		super(playerId);
		this.armyKillMap = armyKillMap;
		this.armyHurtMap = armyHurtMap;
		this.isAtk = isAtk;
	}
		
	public Map<Integer, Integer> getArmyKillMap() {
		return armyKillMap;
	}

	public Map<Integer, Integer> getArmyHurtMap() {
		return armyHurtMap;
	}
	
	@Override
	public String toString() {
		return String.format("PvpBannerBattle,1:%s,2:%s,3:%s", getPlayerId(), armyKillMap, armyHurtMap);
	}

	public boolean isAtk() {
		return isAtk;
	}

}
