package com.hawk.activity.event.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.activity.event.ActivityEvent;

/**
 * 城内资源采集事件
 * @author PhilChen
 *
 */
public class CityResourceCollectEvent extends ActivityEvent {
	
	/** 资源类型*/
	/** 采集数量*/
	private Map<Integer, Double> collectMap;
	/**
	 * 收集时间	
	 */
	private List<Integer> collectTime;
	public void setCollectTime(List<Integer> collectTime) {
		this.collectTime = collectTime;
	}

	public List<Integer> getCollectTime() {
		return collectTime;
	}

	public CityResourceCollectEvent(){ super(null);}
	public CityResourceCollectEvent(String playerId) {
		super(playerId);
		collectMap = new HashMap<>();
		collectTime = new ArrayList<>();
	}
	
	public void addCollectResource(int resourceType, double collectNum) {
		collectMap.put(resourceType, collectNum);
	}

	public Double getCollectNum(int resourceType) {
		Double collectNum = collectMap.get(resourceType);
		if (collectNum == null) {
			return 0d;
		}
		return collectNum;
	}
}
