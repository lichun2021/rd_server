package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.city.CityManager;
import com.hawk.game.player.Player;

public class WorldRemoveCityMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public WorldRemoveCityMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		CityManager.getInstance().removeCity(player);
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}
