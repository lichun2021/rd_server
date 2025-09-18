package com.hawk.game.module.lianmengtaiboliya.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class TBLYQuitRoomMsg extends HawkMsg {
	private QuitReason quitReason;

	private TBLYQuitRoomMsg() {
	}

	public static TBLYQuitRoomMsg valueOf(QuitReason quitReason) {
		TBLYQuitRoomMsg msg = new TBLYQuitRoomMsg();
		msg.quitReason = quitReason;
		return msg;
	}

	public QuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(QuitReason quitReason) {
		this.quitReason = quitReason;
	}

}
