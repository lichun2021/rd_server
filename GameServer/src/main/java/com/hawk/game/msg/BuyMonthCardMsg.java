package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 购买月卡事件
 * 
 */
public class BuyMonthCardMsg extends HawkMsg {
	private int monthCardType;

	public static BuyMonthCardMsg valueOf(int monthCardType) {
		BuyMonthCardMsg msg = new BuyMonthCardMsg();
		msg.monthCardType = monthCardType;
		return msg;
	}

	public int getMonthCardType() {
		return monthCardType;
	}

}
