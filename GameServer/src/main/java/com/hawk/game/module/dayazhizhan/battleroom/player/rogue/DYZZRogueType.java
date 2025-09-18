package com.hawk.game.module.dayazhizhan.battleroom.player.rogue;

public enum DYZZRogueType {
	BASEHP(1), TIME(2), BUY(3);

	DYZZRogueType(int value) {
		this.value = value;
	}

	private int value;

	public int intValue() {
		return value;
	}
}