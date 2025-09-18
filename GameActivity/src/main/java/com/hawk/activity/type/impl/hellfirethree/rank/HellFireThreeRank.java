package com.hawk.activity.type.impl.hellfirethree.rank;


public class HellFireThreeRank implements Comparable<HellFireThreeRank>{
	/**
	 * 玩家ID
	 */
	private String playerId;
	/**
	 * 分数和时间的组合
	 */
	private long score;
	
	@Override
	public int compareTo(HellFireThreeRank o) {
		if (o.score > score) {
			return 1;
		} else if (o.score == score) {
			return 0;
		} else {
			return -1;
		}
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}
}
