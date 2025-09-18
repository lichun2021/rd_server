package com.hawk.activity.type.impl.aftercompetition.data;

import com.hawk.game.protocol.Activity.GiftSendRecordPB;

/**
 * 赠礼信息
 * 
 * @author lating
 *
 */
public class SendGiftInfo {

	private String toPlayerId;
	private String toPlayerName;
	private int giftId;
	private long sendTime;
	private String itemInfo;
	
	public SendGiftInfo() {
	}
	
	public SendGiftInfo(String toPlayerId, String toPlayerName, int giftId, long sendTime, String itemInfo) {
		this.toPlayerId = toPlayerId;
		this.toPlayerName = toPlayerName;
		this.giftId = giftId;
		this.sendTime = sendTime;
		this.itemInfo = itemInfo;
	}

	public String getToPlayerId() {
		return toPlayerId;
	}

	public void setToPlayerId(String toPlayerId) {
		this.toPlayerId = toPlayerId;
	}

	public String getToPlayerName() {
		return toPlayerName;
	}

	public void setToPlayerName(String toPlayerName) {
		this.toPlayerName = toPlayerName;
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

	public GiftSendRecordPB.Builder toBuilder() {
		GiftSendRecordPB.Builder builder = GiftSendRecordPB.newBuilder();
		builder.setToPlayerId(toPlayerId);
		builder.setToPlayerName(toPlayerName);
		builder.setGiftId(giftId);
		builder.setSendTime(sendTime);
		builder.setSendItem(itemInfo);
		return builder;
	}
}
