package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 登录事件(累计登录活动,按注册时间开启)
 * @author Jesse
 *
 */
public class QuestTreasureBoxScoreEvent extends ActivityEvent {
	
	/** 累计登录天数*/
	private int score;

	public QuestTreasureBoxScoreEvent(){ super(null);}
	public QuestTreasureBoxScoreEvent(String playerId, int score) {
		super(playerId);
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}
}
