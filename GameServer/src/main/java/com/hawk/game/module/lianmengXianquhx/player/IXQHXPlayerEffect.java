package com.hawk.game.module.lianmengXianquhx.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class IXQHXPlayerEffect extends PlayerEffect {
	private XQHXPlayer parent;

	public IXQHXPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public XQHXPlayer getParent() {
		return parent;
	}

	public void setParent(XQHXPlayer parent) {
		this.parent = parent;
	}
}
