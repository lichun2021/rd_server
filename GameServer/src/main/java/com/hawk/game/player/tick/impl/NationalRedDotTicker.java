package com.hawk.game.player.tick.impl;

import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;

public class NationalRedDotTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		player.syncNationRedDot(true);
	}

}
