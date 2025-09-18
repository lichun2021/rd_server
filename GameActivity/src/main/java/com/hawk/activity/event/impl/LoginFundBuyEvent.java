package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 登录基金直购事件
 * @author hf
 */
public class LoginFundBuyEvent extends ActivityEvent {

	private String giftId;


	public LoginFundBuyEvent(){ super(null);}
	public LoginFundBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}
