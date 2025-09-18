package com.hawk.game.module.lianmengXianquhx.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class XQHXQuitRoomMsg extends HawkMsg {
	private XQHXQuitReason quitReason;

	private XQHXQuitRoomMsg() {
	}

	public static XQHXQuitRoomMsg valueOf(XQHXQuitReason quitReason) {
		XQHXQuitRoomMsg msg = new XQHXQuitRoomMsg();
		msg.quitReason = quitReason;
		return msg;
	}

	public XQHXQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(XQHXQuitReason quitReason) {
		this.quitReason = quitReason;
	}

}
