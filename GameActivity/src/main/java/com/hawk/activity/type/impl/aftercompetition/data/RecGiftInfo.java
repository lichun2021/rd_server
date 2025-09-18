package com.hawk.activity.type.impl.aftercompetition.data;

import com.hawk.game.protocol.Activity.GiftRecRecordPB;

/**
 * 收礼信息
 * 
 * @author lating
 *
 */
public class RecGiftInfo {

	private String fromPlayerId;
	private String fromPlayerName;
	private int giftId;
	private long sendTime;
	private String itemInfo;
	
	public RecGiftInfo() {
	}

	public RecGiftInfo(String fromPlayerId, String fromPlayerName, int giftId, long sendTime, String itemInfo) {
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

	public GiftRecRecordPB.Builder toBuilder() {
		GiftRecRecordPB.Builder builder = GiftRecRecordPB.newBuilder();
		builder.setFromPlayerId(fromPlayerId);
		builder.setFromPlayerName(fromPlayerName);
		builder.setGiftId(giftId);
		builder.setRecieveTime(sendTime);
		builder.setRecItem(itemInfo);
		return builder;
	}
}
