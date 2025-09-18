package com.hawk.game.module.lianmengtaiboliya.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class ITBLYPlayerEffect extends PlayerEffect {
	private ITBLYPlayer parent;

	public ITBLYPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public ITBLYPlayer getParent() {
		return parent;
	}

	public void setParent(ITBLYPlayer parent) {
		this.parent = parent;
	}
}
