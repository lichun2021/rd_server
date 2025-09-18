package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.speciality.OrderEvent;

/**
 * 赛博之战个人积分
 * @author Jesse
 *
 */
public class CWScoreEvent extends ActivityEvent implements OrderEvent {
	private long score;
	boolean joinGame;
	// 是否是联赛
	private boolean isLeagua;
	public CWScoreEvent(){ super(null);}
	public CWScoreEvent(String playerId, long score,boolean joinGame, boolean isLeagua) {
		super(playerId,true);
		this.score = score;
		this.joinGame = joinGame;
		this.isLeagua = isLeagua;
	}

	public final long getScore() {
		return score;
	}

	public final boolean isJoinGame() {
		return joinGame;
	}

	public boolean isLeagua() {
		return isLeagua;
	}
}
