package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 玩家签到事件
 * @author PhilChen
 *
 */
public class PlayerSignEvent extends ActivityEvent {

	public PlayerSignEvent(){ super(null);}
	public PlayerSignEvent(String playerId) {
		super(playerId);
	}

}
