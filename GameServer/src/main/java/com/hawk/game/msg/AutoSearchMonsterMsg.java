package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 自动搜索野怪
 *
 */
public class AutoSearchMonsterMsg extends HawkMsg {

	private AutoSearchMonsterMsg() {

	}

	public static AutoSearchMonsterMsg valueOf() {
		AutoSearchMonsterMsg msg = new AutoSearchMonsterMsg();
		return msg;
	}
}
