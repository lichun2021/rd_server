package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class SuperGiftDirectBuyMsg extends HawkMsg {
	/**
	 * 购买的gift
	 */
	private String payGiftId;
	public SuperGiftDirectBuyMsg(String giftId) {
		this.payGiftId = giftId;
	}
	public String getPayGiftId() {
		return payGiftId;
	}
	public void setPayGiftId(String payGiftId) {
		this.payGiftId = payGiftId;
	}
}
