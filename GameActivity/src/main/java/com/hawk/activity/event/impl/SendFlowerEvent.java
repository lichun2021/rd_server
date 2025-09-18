package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

public class SendFlowerEvent extends ActivityEvent {

	private int num;
	private String toPlayerId;

	public SendFlowerEvent(){ super(null);}
	public SendFlowerEvent(String playerId, String toPlayerId, int num) {
		super(playerId);
		this.num = num;
		this.toPlayerId = toPlayerId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public String getToPlayerId() {
		return toPlayerId;
	}

	public void setToPlayerId(String toPlayerId) {
		this.toPlayerId = toPlayerId;
	}

}
