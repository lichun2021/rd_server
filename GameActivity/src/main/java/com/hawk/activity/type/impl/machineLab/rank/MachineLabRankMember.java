package com.hawk.activity.type.impl.machineLab.rank;

public class MachineLabRankMember {

	
	private String playerId;
	
	private long score;
	
	private int rank;
	

	
	public MachineLabRankMember() {
	}
	
	
	
	public MachineLabRankMember(String playerId, long score, int rank) {
		super();
		this.playerId = playerId;
		this.score = score;
		this.rank = rank;
	}


	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
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
	
	
}
