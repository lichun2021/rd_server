package com.hawk.game.module.dayazhizhan.playerteam.msg;

import org.hawk.msg.HawkMsg;

public class DYZZSeasonOrderAdvanceBuyMsg extends HawkMsg {

	int giftId;
	
	public DYZZSeasonOrderAdvanceBuyMsg(String giftId) {
		this.giftId = Integer.parseInt(giftId);
	}
	
	public int getGiftId() {
		return giftId;
	}
}
