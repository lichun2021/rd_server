package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.activity.event.ActivityEvent;

public class EquipQualityAchiveEvent extends ActivityEvent{
	
	Map<Integer,Integer> equips = new HashMap<Integer,Integer>();

	public EquipQualityAchiveEvent(){ super(null);}
	public EquipQualityAchiveEvent(String playerId) {
		super(playerId);
	}
	
	
	
	public void addEquip(int quality,int count){
		int value = count;
		if(this.equips.containsKey(quality)){
			value += this.equips.get(quality);
		}
		this.equips.put(quality,value);
	}
	
	
	public int equipQualityCount(int qualityLimit){
		int addCount = 0;
		for(Entry<Integer,Integer> entry : equips.entrySet()){
			int quality = entry.getKey();
			int count = entry.getValue();
			if(quality >= qualityLimit){
				addCount += count;
			}
		}
		return addCount;
	}



	public Map<Integer, Integer> getEquips() {
		return equips;
	}



	public void setEquips(Map<Integer, Integer> equips) {
		this.equips = equips;
	}



	
	
	
	
	
	

}
