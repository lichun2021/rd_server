package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;

import com.hawk.activity.event.ActivityEvent;

/**
 * 士兵死伤
 * @author che
 *
 */
public class HotBloodWarArmyCureEvent extends ActivityEvent {

	private Map<Integer,Integer> deaths = new HashMap<>();
	
	
	public HotBloodWarArmyCureEvent(){ super(null);}
	public HotBloodWarArmyCureEvent(String playerId) {
		super(playerId);
	}
	
	public Map<Integer, Integer> getDeaths() {
		return deaths;
	}
	

	
	public void addDeath(int armyid,int count){
		int cnt = this.deaths.getOrDefault(armyid, 0) + count;
		this.deaths.put(armyid, cnt);
	}
}
