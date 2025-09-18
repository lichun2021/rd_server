package com.hawk.game.crossfortress;

public class FortressRecordItem {

	private int turn;
	
	private int rank;
	
	private int count;

	public FortressRecordItem() {
		
	}
	
	public FortressRecordItem(int turn, int rank, int count) {
		this.turn = turn;
		this.rank = rank;
		this.count = count;
	}
	
	@Override
	public String toString() {
		return turn + ":" + rank + ":" + count;
	}
	
	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
