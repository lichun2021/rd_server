package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 个人发送祝福语事件
 */
public class PersonSendGreetingsEvent extends ActivityEvent {
	
	private int num;
	
	public PersonSendGreetingsEvent(){ super(null);}
	public PersonSendGreetingsEvent(String playerId, int num) {
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
