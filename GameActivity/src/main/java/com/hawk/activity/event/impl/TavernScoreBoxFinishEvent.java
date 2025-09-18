package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 每日任务积分宝箱完成事件
 * @author che
 *
 */
public class TavernScoreBoxFinishEvent extends ActivityEvent {

	private int boxId;
	
	private int score;
	public TavernScoreBoxFinishEvent(){ super(null);}
	public TavernScoreBoxFinishEvent(String playerId, int boxId,int score) {
		super(playerId);
		this.boxId = boxId;
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getBoxId() {
		return boxId;
	}


}
