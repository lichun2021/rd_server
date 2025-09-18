package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class CollegeGiftBuyMsg extends HawkMsg {

	private int cfgId;
	
	public CollegeGiftBuyMsg(int cfgId) {
		this.cfgId = cfgId;
	}

	public int getCfgId() {
		return cfgId;
	}
}
