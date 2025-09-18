package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.city.CityManager;
import com.hawk.game.player.Player;

/**
 * 城防修复
 * @author jm
 *
 */
public class WorldCityDefRecoverMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public WorldCityDefRecoverMsgInvoker(Player player) {
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

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
