package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 赛季拼图完成次数刷新事件
 * @author lating
 */
public class SeasonPuzzleRefreshEvent extends ActivityEvent {

	private int completeVal;
	
	public SeasonPuzzleRefreshEvent(){ super(null);}
	public SeasonPuzzleRefreshEvent(String playerId, int completeVal) {
		super(playerId);
		this.completeVal = completeVal;
	}
	
	public int getompleteVal() {
		return completeVal;
	}
	
	public void setompleteVal(int completeVal) {
		this.completeVal = completeVal;
	}
}
