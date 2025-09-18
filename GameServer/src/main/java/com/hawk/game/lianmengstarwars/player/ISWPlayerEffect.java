package com.hawk.game.lianmengstarwars.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class ISWPlayerEffect extends PlayerEffect {
	private SWPlayer parent;

	public ISWPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public SWPlayer getParent() {
		return parent;
	}

	public void setParent(SWPlayer parent) {
		this.parent = parent;
	}
}
