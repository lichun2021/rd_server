package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hawk.activity.event.ActivityEvent;

/**
 * 士兵死伤
 * @author che
 *
 */
public class ArmyHurtDeathEvent extends ActivityEvent {

	private Map<Integer,Integer> hurts = new HashMap<>();
	private Map<Integer,Integer> deaths = new HashMap<>();
	private String dungeonMap;
	private boolean incross;
	
	public ArmyHurtDeathEvent(){ super(null);}
	
	public ArmyHurtDeathEvent(String playerId,String dungeonMap,boolean incross) {
		super(playerId);
		this.dungeonMap = dungeonMap;
		this.incross = incross;
	}
	
	public boolean isInDungeonMap() {
        return StringUtils.isNotEmpty(this.dungeonMap);
    }
	
	public boolean incross() {
		return incross;
	}
	
	public Map<Integer, Integer> getDeaths() {
		return deaths;
	}
	
	public Map<Integer, Integer> getHurts() {
		return hurts;
	}
	
	public void addHurt(int armyid,int count){
		int cnt = this.hurts.getOrDefault(armyid, 0) + count;
		this.hurts.put(armyid, cnt);
	}

	
	public void addDeath(int armyid,int count){
		int cnt = this.deaths.getOrDefault(armyid, 0) + count;
		this.deaths.put(armyid, cnt);
	}
}
