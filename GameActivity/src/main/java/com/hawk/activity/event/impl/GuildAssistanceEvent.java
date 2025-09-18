package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;

import com.hawk.activity.event.ActivityEvent;

/**
 * 联盟资源援助事件
 * @author PhilChen
 *
 */
public class GuildAssistanceEvent extends ActivityEvent {
	
	private Map<Integer, Integer> resMap;
	
	public GuildAssistanceEvent(){ super(null);}
	public GuildAssistanceEvent(String playerId) {
		super(playerId);
		this.resMap = new HashMap<>();
	}
	
	public int getResNum(int resId) {
		Integer num = resMap.get(resId);
		if (num == null) {
			return 0;
		}
		return num;
	}
	
	public void addRes(int resId, int num) {
		resMap.put(resId, num);
	}

	public Map<Integer, Integer> getResMap() {
		return resMap;
	}
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}
}
