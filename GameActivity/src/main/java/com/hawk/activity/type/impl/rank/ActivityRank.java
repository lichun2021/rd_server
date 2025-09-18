package com.hawk.activity.type.impl.rank;


public class ActivityRank implements Comparable<ActivityRank> {

	private String id;
	
	private int rank;
	
	private long score;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getScore() {
		return score;
	}	

	public void setScore(long score) {
		this.score = score;
	}

	@Override
	public int compareTo(ActivityRank arg0) {
		return 0;
	}
	
	
}
