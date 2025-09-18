package com.hawk.game.module.lianmengfgyl.battleroom.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class FGYLQuitRoomMsg extends HawkMsg {
	private FGYLQuitReason quitReason;

	private FGYLQuitRoomMsg() {
	}

	public static FGYLQuitRoomMsg valueOf(FGYLQuitReason quitReason) {
		FGYLQuitRoomMsg msg = new FGYLQuitRoomMsg();
		msg.quitReason = quitReason;
		return msg;
	}

	public FGYLQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(FGYLQuitReason quitReason) {
		this.quitReason = quitReason;
	}

}
