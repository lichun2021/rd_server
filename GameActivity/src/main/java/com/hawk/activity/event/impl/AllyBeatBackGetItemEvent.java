package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;

import com.hawk.activity.event.ActivityEvent;

/**
 * 只有从限时掉落产出的itemId才算
 * @author jm
 *
 */
public class AllyBeatBackGetItemEvent extends ActivityEvent {
	Map<Integer, Integer> itemCount = new HashMap<>();
	public AllyBeatBackGetItemEvent(){ super(null);}
	public AllyBeatBackGetItemEvent(String playerId, Map<Integer, Integer> itemMap) {
		super(playerId);
		this.itemCount = itemMap;
	}
	public Map<Integer, Integer> getItemCount() {
		return itemCount;
	}
	public void setItemCount(Map<Integer, Integer> itemCount) {
		this.itemCount = itemCount;
	}
	
}
