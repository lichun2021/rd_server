package com.hawk.game.module.dayazhizhan.battleroom.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class IDYZZPlayerEffect extends PlayerEffect {
	private DYZZPlayer parent;

	public IDYZZPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public DYZZPlayer getParent() {
		return parent;
	}

	public void setParent(DYZZPlayer parent) {
		this.parent = parent;
	}
}
