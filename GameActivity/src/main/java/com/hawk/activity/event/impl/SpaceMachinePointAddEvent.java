package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 机甲舱体代币增加事件
 */
public class SpaceMachinePointAddEvent extends ActivityEvent {
	
	/** 代币增加数  */
	private int addPoint;
	public SpaceMachinePointAddEvent(){ super(null);}
	public SpaceMachinePointAddEvent(String playerId, int addPoint) {
		super(playerId);
		this.addPoint = addPoint;
	}

	public int getAddPoint() {
		return addPoint;
	}

}
