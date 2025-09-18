package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 泰伯利亚个人积分
 * 
 * @author Jesse
 *
 */
public class TWScoreEvent extends ActivityEvent implements OrderEvent {
	private long score;

	// 是否是联赛
	private boolean isLeagua;
	
	//进入战场时间
	private long enterTime;

	public TWScoreEvent(){ super(null);}
	public TWScoreEvent(String playerId, long score, boolean isLeagua,long enterTime) {
		super(playerId,true);
		this.score = score;
		this.isLeagua = isLeagua;
		this.enterTime = enterTime;
	}

	public final long getScore() {
		return score;
	}

	public boolean isLeagua() {
		return isLeagua;
	}

	public long getEnterTime() {
		return enterTime;
	}

}
