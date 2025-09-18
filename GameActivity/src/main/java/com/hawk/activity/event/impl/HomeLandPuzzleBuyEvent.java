package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/***
 * 心愿庄园直购事件
 * @author zhy
 *
 */
public class HomeLandPuzzleBuyEvent extends ActivityEvent {

	private String giftId;


	public HomeLandPuzzleBuyEvent(){ super(null);}
	public HomeLandPuzzleBuyEvent(String playerId, String giftId) {
		super(playerId, true);
		this.giftId = giftId;
	}

	public String getGiftId() {
		return giftId;
	}
}
