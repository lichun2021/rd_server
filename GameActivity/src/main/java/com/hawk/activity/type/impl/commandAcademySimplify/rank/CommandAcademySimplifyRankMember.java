package com.hawk.activity.type.impl.commandAcademySimplify.rank;

public class CommandAcademySimplifyRankMember {

	
	private String playerId;
	
	private Double score;
	
	private int rank;

	
	public CommandAcademySimplifyRankMember() {
	}
	
	public CommandAcademySimplifyRankMember(String playerId, double score, int rank) {
		super();
		this.playerId = playerId;
		this.score = score;
		this.rank = rank;
	}

	public CommandAcademySimplifyRankMember(String playerId, double score) {
		super();
		this.playerId = playerId;
		this.score = score;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}
	
}
