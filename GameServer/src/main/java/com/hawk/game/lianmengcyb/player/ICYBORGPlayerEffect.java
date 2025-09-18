package com.hawk.game.lianmengcyb.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class ICYBORGPlayerEffect extends PlayerEffect {
	private CYBORGPlayer parent;

	public ICYBORGPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public CYBORGPlayer getParent() {
		return parent;
	}

	public void setParent(CYBORGPlayer parent) {
		this.parent = parent;
	}
}
