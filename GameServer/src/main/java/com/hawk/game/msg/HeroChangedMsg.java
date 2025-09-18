package com.hawk.game.msg;

import org.hawk.msg.HawkMsg;

import com.hawk.game.player.hero.PlayerHero;

public class HeroChangedMsg extends HawkMsg {
	private final PlayerHero hero;

	public HeroChangedMsg(PlayerHero hero) {
		this.hero = hero;
	}

	public PlayerHero getHero() {
		return hero;
	}

}
