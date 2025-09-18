package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 被召回的好友登录.
 * @author jm
 *
 */
public class RecalledFriendLoginEvent extends ActivityEvent {
	private String openId;
	private int facLv;
	public RecalledFriendLoginEvent(){ super(null);}
	public RecalledFriendLoginEvent(String playerId, String openId, int facLv) {
		super(playerId);
		this.openId = openId;
		this.facLv = facLv;
	}
	public String getOpenId() {
		return openId;
	}

	public int getFacLv() {
		return facLv;
	}
}
