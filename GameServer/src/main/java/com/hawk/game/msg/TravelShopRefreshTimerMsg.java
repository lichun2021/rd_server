package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 该消息会重复使用,请勿修改.
 * @author jm
 *
 */
public class TravelShopRefreshTimerMsg extends HawkMsg {
	/**
	 * clock
	 */
	private int colock;
	
	public TravelShopRefreshTimerMsg() {
		
	}
	
	public TravelShopRefreshTimerMsg(int colock) {
		this.colock = colock;
	}

	public int getColock() {
		return colock;
	}

	public void setColock(int colock) {
		this.colock = colock;
	}
	
	
}
