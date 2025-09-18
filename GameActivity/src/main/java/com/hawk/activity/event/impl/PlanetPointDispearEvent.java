package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 星能矿点消失事件
 */
public class PlanetPointDispearEvent extends ActivityEvent {
	
	/** 坐标点 */
	private int posX;
	private int posY;

	public PlanetPointDispearEvent(){ 
		super(null);
	}
	
	public PlanetPointDispearEvent(String playerId, int posX, int posY) {
		super(playerId, true);
		this.posX = posX;
		this.posY = posY;
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

}
