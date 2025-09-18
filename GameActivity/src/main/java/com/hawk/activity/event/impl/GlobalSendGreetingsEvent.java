package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 充值钻石
 * @author Jesse
 *
 */
public class GlobalSendGreetingsEvent extends ActivityEvent {
	
	private int num;
	
	public GlobalSendGreetingsEvent(){ super(null);}
	public GlobalSendGreetingsEvent(String playerId, int num) {
		super(playerId, true);
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}
	
	
}
