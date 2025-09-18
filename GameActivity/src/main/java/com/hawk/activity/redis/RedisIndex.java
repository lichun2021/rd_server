package com.hawk.activity.redis;

public class RedisIndex {

	private Long index;
	
	private Double score;

	public RedisIndex(Long index, Double score) {
		this.index = index;
		this.score = score;
	}
	
	public Long getIndex() {
		return index;
	}
	
	public Double getScore() {
		return score;
	}
}
