package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 超能实验室升级
 * @author Golden
 *
 */
public class SuperLabLevelUpMsg extends HawkMsg {

	int totalLevel;

	public static SuperLabLevelUpMsg valueOf(int totalLevel) {
		SuperLabLevelUpMsg msg = new SuperLabLevelUpMsg();
		msg.totalLevel = totalLevel;
		return msg;
	}

	public int getTotalLevel() {
		return totalLevel;
	}
}
