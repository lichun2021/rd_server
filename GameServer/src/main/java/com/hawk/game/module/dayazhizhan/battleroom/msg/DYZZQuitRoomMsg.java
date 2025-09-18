package com.hawk.game.module.dayazhizhan.battleroom.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class DYZZQuitRoomMsg extends HawkMsg {
	private DYZZQuitReason quitReason;
	private String roomId;

	private DYZZQuitRoomMsg() {
	}

	public static DYZZQuitRoomMsg valueOf(DYZZQuitReason quitReason, String roomId) {
		DYZZQuitRoomMsg msg = new DYZZQuitRoomMsg();
		msg.quitReason = quitReason;
		msg.roomId = roomId;
		return msg;
	}

	public DYZZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(DYZZQuitReason quitReason) {
		this.quitReason = quitReason;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}

}
