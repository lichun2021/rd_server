package com.hawk.game.module.lianmenxhjz.battleroom.msg;

import org.hawk.msg.HawkMsg;

/**
 */
public class XHJZQuitRoomMsg extends HawkMsg {
	private XHJZQuitReason quitReason;

	private XHJZQuitRoomMsg() {
	}

	public static XHJZQuitRoomMsg valueOf(XHJZQuitReason quitReason) {
		XHJZQuitRoomMsg msg = new XHJZQuitRoomMsg();
		msg.quitReason = quitReason;
		return msg;
	}

	public XHJZQuitReason getQuitReason() {
		return quitReason;
	}

	public void setQuitReason(XHJZQuitReason quitReason) {
		this.quitReason = quitReason;
	}

}
