package com.hawk.game.battle.effect;

public class CheckerKVResult {
	public final int first;
	public final int second;

	public static final CheckerKVResult DefaultVal = new CheckerKVResult(0, 0);

	public CheckerKVResult(int first, int second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public String toString() {
		return "(" + first + "," + second + ")";
	}

}
