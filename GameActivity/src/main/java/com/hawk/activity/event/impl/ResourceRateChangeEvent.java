package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 资源产出率变化事件
 * @author PhilChen
 *
 */
public class ResourceRateChangeEvent extends ActivityEvent {
	
	private int buildingType;
	
	/** 资源类型*/
	private int resourceType;
	
	private int addRate;

	public ResourceRateChangeEvent(){ super(null);}
	public ResourceRateChangeEvent(String playerId, int buildingType, int resourceType, int addRate) {
		super(playerId);
		this.buildingType = buildingType;
		this.resourceType = resourceType;
		this.addRate = addRate;
	}
	
	public int getBuildingType() {
		return buildingType;
	}
	
	public int getResourceType() {
		return resourceType;
	}
	
	public int getAddRate() {
		return addRate;
	}
}
