package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 赛后庆典接收礼物事件
 * 
 * @author lating
 */
public class AfterCompetitionGiftRecEvent extends ActivityEvent {

	private String fromPlayerId;
	private String fromPlayerName;
	private int giftId;
	private long sendTime;
	private String itemInfo;
	
	public AfterCompetitionGiftRecEvent(){ super(null);}
	
	public AfterCompetitionGiftRecEvent(String playerId, String fromPlayerId, String fromPlayerName, int giftId, long sendTime, String itemInfo) {
		super(playerId);
		this.fromPlayerId = fromPlayerId;
		this.fromPlayerName = fromPlayerName;
		this.giftId = giftId;
		this.sendTime = sendTime;
		this.itemInfo = itemInfo;
	}

	public String getFromPlayerId() {
		return fromPlayerId;
	}

	public void setFromPlayerId(String fromPlayerId) {
		this.fromPlayerId = fromPlayerId;
	}

	public String getFromPlayerName() {
		return fromPlayerName;
	}

	public void setFromPlayerName(String fromPlayerName) {
		this.fromPlayerName = fromPlayerName;
	}

	public int getGiftId() {
		return giftId;
	}

	public void setGiftId(int giftId) {
		this.giftId = giftId;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public String getItemInfo() {
		return itemInfo;
	}

	public void setItemInfo(String itemInfo) {
		this.itemInfo = itemInfo;
	}
	
}
