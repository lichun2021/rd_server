package com.hawk.game.module.lianmengfgyl.battleroom.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class IFGYLPlayerEffect extends PlayerEffect {
	private FGYLPlayer parent;

	public IFGYLPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public FGYLPlayer getParent() {
		return parent;
	}

	public void setParent(FGYLPlayer parent) {
		this.parent = parent;
	}
}
