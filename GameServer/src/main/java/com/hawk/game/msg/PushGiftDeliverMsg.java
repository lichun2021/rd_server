package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

public class PushGiftDeliverMsg extends HawkMsg {
	String payId;
	public PushGiftDeliverMsg(String payId) {
		this.payId = payId;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
}
