package com.hawk.activity.event.impl;

import java.util.HashMap;
import java.util.Map;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.activity.event.speciality.EvolutionEvent;
import com.hawk.activity.event.speciality.OrderEvent;
import com.hawk.activity.event.speciality.SpaceMechaEvent;
import com.hawk.activity.event.speciality.StrongestEvent;

/**
 * 资源采集事件
 * @author PhilChen
 *
 */
public class ResourceCollectEvent extends ActivityEvent implements StrongestEvent, CrossActivityEvent, OrderEvent, EvolutionEvent, SpaceMechaEvent {

	/**
	 * 资源类型_资源数量
	 */
	private Map<Integer, Double> collectMap;

	/**
	 * 资源类型_资源权重
	 */
	private Map<Integer, Integer> weightMap;
	
	/**
	 * 采集花费的时间
	 */
	private int collectTime;
	
	/**
	 * 构造
	 * @param playerId
	 */
	public ResourceCollectEvent(){ super(null);}
	public ResourceCollectEvent(String playerId) {
		super(playerId);
		collectMap = new HashMap<>();
		weightMap = new HashMap<>();
	}
	
	
	
	@Override
	public boolean isOfflineResent() {
		return true;
	}



	/**
	 * 添加资源
	 * @param resourceType
	 * @param collectNum
	 * @param weight
	 */
	public void addCollectResource(int resourceType, double collectNum, int weight) {
		collectMap.put(resourceType, collectNum);
		weightMap.put(resourceType, weight);
	}

	/**
	 * 获取资源数量
	 * @param resourceType
	 * @return
	 */
	public Double getCollectNum(int resourceType) {
		Double collectNum = collectMap.get(resourceType);
		if (collectNum == null) {
			return 0d;
		}
		return collectNum;
	}

	/**
	 * 获取资源权重
	 * @param resType
	 * @return
	 */
	public int getResWeight(int resType) {
		if (!weightMap.containsKey(resType)) {
			return 0;
		}
		return weightMap.get(resType);
	}
	
	public int getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(int collectTime) {
		this.collectTime = collectTime;
	}
	
	public Map<Integer, Double> getCollectMap() {
		return collectMap;
	}

	@Override
	public String toString() {
		return String.format("ResourceCollect, 1:%s,2:%s", collectMap, weightMap);
	}
}
