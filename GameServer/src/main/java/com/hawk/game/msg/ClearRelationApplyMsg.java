package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class ClearRelationApplyMsg extends HawkMsg {
	private String playerId;
	public ClearRelationApplyMsg(String playerId) {
		this.playerId = playerId;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
}
