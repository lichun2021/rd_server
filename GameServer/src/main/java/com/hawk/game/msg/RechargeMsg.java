package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 充值
 * @author Golden
 *
 */
public class RechargeMsg extends HawkMsg {
	
	public static RechargeMsg valueOf() {
		RechargeMsg msg = new RechargeMsg();
		return msg;
	}
}
