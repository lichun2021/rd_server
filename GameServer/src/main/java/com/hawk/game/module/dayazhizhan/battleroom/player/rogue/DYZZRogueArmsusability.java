package com.hawk.game.module.dayazhizhan.battleroom.player.rogue;

public class DYZZRogueArmsusability {
	private final int key;
	private int value;

	public DYZZRogueArmsusability(int key) {
		this.key = key;
	}

	public void incValue() {
		this.value++;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getKey() {
		return key;
	}

}
