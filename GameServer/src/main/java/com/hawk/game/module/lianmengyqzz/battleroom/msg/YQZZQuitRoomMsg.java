package com.hawk.game.module.lianmengyqzz.battleroom.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class YQZZQuitRoomMsg extends HawkMsg {
	private YQZZQuitReason quitReason;
	private String roomId;
	private YQZZQuitRoomMsg() {
	}

	public static YQZZQuitRoomMsg valueOf(YQZZQuitReason quitReason , String roomId) {
		YQZZQuitRoomMsg msg = new YQZZQuitRoomMsg();
		msg.quitReason = quitReason;
		msg.roomId = roomId;
		return msg;
	}

	public YQZZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(YQZZQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

}
