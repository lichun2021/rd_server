package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

/**
 * 新版野怪定时刷新消息
 * @author golden
 *
 */
public class NewMonsterRefreshTimerMsg extends HawkMsg {
	/**
	 * clock
	 */
	private int clock;
	
	public NewMonsterRefreshTimerMsg(int clock) {
		this.clock = clock;
	}
	
	public int getClock() {
		return clock;
	}

	public void setClock(int clock) {
		this.clock = clock;
	}
}
