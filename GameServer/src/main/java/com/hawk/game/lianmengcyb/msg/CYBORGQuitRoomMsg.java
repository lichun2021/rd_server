package com.hawk.game.lianmengcyb.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class CYBORGQuitRoomMsg extends HawkMsg {
	private CYBORGQuitReason quitReason;

	private CYBORGQuitRoomMsg() {
	}

	public static CYBORGQuitRoomMsg valueOf(CYBORGQuitReason quitReason) {
		CYBORGQuitRoomMsg msg = new CYBORGQuitRoomMsg();
		msg.quitReason = quitReason;
		return msg;
	}

	public CYBORGQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(CYBORGQuitReason quitReason) {
		this.quitReason = quitReason;
	}

}
