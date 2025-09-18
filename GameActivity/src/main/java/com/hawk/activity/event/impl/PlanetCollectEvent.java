package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 星能矿采集事件
 */
public class PlanetCollectEvent extends ActivityEvent {
	
	/** 采集时间 */
	private long time;
	
	/** 坐标点 */
	private int posX;
	private int posY;
	
	/** 采集数量 */
	private int collectNum;

	public PlanetCollectEvent(){ 
		super(null);
	}
	
	public PlanetCollectEvent(String playerId, long time, int posX, int posY, int collectNum) {
		super(playerId, true);
		this.time = time;
		this.posX = posX;
		this.posY = posY;
		this.collectNum = collectNum;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getPosX() {
		return posX;
	}

	public void setPosX(int posX) {
		this.posX = posX;
	}

	public int getPosY() {
		return posY;
	}

	public void setPosY(int posY) {
		this.posY = posY;
	}

	public int getCollectNum() {
		return collectNum;
	}

	public void setCollectNum(int collectNum) {
		this.collectNum = collectNum;
	}
	
}
