package com.hawk.game.module.dayazhizhan.battleroom.player.rogue;

import org.hawk.os.HawkRandObj;

public class DYZZRogueRandItem implements HawkRandObj {
	private final int baseId;
	private final int weight;

	public DYZZRogueRandItem(int baseId, int weight) {
		super();
		this.baseId = baseId;
		this.weight = weight;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public int getBaseId() {
		return baseId;
	}

}
