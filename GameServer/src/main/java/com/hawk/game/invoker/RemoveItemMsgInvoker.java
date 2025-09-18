package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.player.Player;

public class RemoveItemMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public RemoveItemMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		CrossActivityService.getInstance().removeCrossItem(player);
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}