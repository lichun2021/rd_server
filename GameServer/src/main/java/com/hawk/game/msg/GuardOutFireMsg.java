package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class GuardOutFireMsg extends HawkMsg {
	/**
	 * 主动发起帮助的人
	 */
	String helpPlayerId;
	public GuardOutFireMsg(String helpPlayerId) {
		this.helpPlayerId = helpPlayerId;
	}
	public String getHelpPlayerId() {
		return helpPlayerId;
	}
	public void setHelpPlayerId(String helpPlayerId) {
		this.helpPlayerId = helpPlayerId;
	}
	
	
}
