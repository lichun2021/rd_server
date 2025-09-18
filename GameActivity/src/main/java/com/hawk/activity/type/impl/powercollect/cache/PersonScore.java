package com.hawk.activity.type.impl.powercollect.cache;

import com.hawk.activity.type.impl.powercollect.rank.PowerCollectRankData;

public class PersonScore extends PowerCollectRankData{
	
	/** 分数 **/
	private double score;
	
	public PersonScore(){}
	
	public PersonScore(String playerId){
		super(playerId);
	}
	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
	
	public void addScore(double add){
		this.score += add;
	}
}
