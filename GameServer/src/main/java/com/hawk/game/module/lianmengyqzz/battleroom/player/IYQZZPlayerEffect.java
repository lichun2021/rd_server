package com.hawk.game.module.lianmengyqzz.battleroom.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class IYQZZPlayerEffect extends PlayerEffect {
	private YQZZPlayer parent;

	public IYQZZPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public YQZZPlayer getParent() {
		return parent;
	}

	public void setParent(YQZZPlayer parent) {
		this.parent = parent;
	}
}
