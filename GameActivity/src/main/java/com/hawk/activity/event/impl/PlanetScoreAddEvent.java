package com.hawk.activity.event.impl;

import com.hawk.activity.event.ActivityEvent;

/**
 * 星能探索积分增加事件
 */
public class PlanetScoreAddEvent extends ActivityEvent {
	/**
	 * 添加的积分数
	 */
	private int scoreAdd;
	/**
	 * 给全服加的基础上，是否要给个人也加上
	 */
	private boolean add2Person = false;

	public PlanetScoreAddEvent(){ 
		super(null);
	}
	
	public PlanetScoreAddEvent(String playerId, int scoreAdd, boolean add2Person) {
		super(playerId, true);
		this.scoreAdd = scoreAdd;
		this.add2Person = add2Person;
	}

	public int getScoreAdd() {
		return scoreAdd;
	}

	public void setScoreAdd(int scoreAdd) {
		this.scoreAdd = scoreAdd;
	}

	public boolean isAdd2Person() {
		return add2Person;
	}

	public void setAdd2Person(boolean add2Person) {
		this.add2Person = add2Person;
	}

}
