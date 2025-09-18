package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 皮肤buff结束
 * 
 */
public class BuffHeroSkinEndMsg extends HawkMsg {
	int statusId;

	private BuffHeroSkinEndMsg() {
	}

	public static BuffHeroSkinEndMsg valueOf(int status) {
		BuffHeroSkinEndMsg msg = new BuffHeroSkinEndMsg();
		msg.statusId = status;
		return msg;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

}
