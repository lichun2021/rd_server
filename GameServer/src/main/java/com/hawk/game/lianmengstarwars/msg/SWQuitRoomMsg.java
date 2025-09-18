package com.hawk.game.lianmengstarwars.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class SWQuitRoomMsg extends HawkMsg {
	private SWQuitReason quitReason;
	private String roomId;
	private SWQuitRoomMsg() {
	}

	public static SWQuitRoomMsg valueOf(SWQuitReason quitReason , String roomId) {
		SWQuitRoomMsg msg = new SWQuitRoomMsg();
		msg.quitReason = quitReason;
		msg.roomId = roomId;
		return msg;
	}

	public SWQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(SWQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

}
