package com.hawk.game.battle;

public abstract class ISoldierbuff {
	private int value;
	private int startRound;
	private int endRound;

	public ISoldierbuff(int value, int startRound, int endRound) {
		this.value = value;
		this.startRound = startRound;
		this.endRound = endRound;
	}

	public boolean isActive(int round) {
		return round >= startRound && round <= endRound;
	}

	public boolean expired(int round) {
		return round > endRound;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getStartRound() {
		return startRound;
	}

	public void setStartRound(int startRound) {
		this.startRound = startRound;
	}

	public int getEndRound() {
		return endRound;
	}

	public void setEndRound(int endRound) {
		this.endRound = endRound;
	}
}
