package com.hawk.activity.type.impl.planetexploration.cache;

import com.hawk.activity.type.impl.planetexploration.rank.PlanetExploreRankData;

public class PersonScore extends PlanetExploreRankData {
	
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
