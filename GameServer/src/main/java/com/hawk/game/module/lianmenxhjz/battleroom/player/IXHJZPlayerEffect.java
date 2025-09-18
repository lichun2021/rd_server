package com.hawk.game.module.lianmenxhjz.battleroom.player;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;

public abstract class IXHJZPlayerEffect extends PlayerEffect {
	private XHJZPlayer parent;

	public IXHJZPlayerEffect(PlayerData playerData) {
		super(playerData);
	}

	public XHJZPlayer getParent() {
		return parent;
	}

	public void setParent(XHJZPlayer parent) {
		this.parent = parent;
	}
}
