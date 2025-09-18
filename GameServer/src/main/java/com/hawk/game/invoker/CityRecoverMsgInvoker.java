package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.city.CityManager;
import com.hawk.game.player.Player;

public class CityRecoverMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public CityRecoverMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		CityManager.getInstance().cityRecover(player);
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}
