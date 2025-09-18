package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkException;
import com.hawk.game.player.Player;

public class PayCancelMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public PayCancelMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		try {
			if (player.checkBalance() >= 0) {
				player.getPush().syncPlayerDiamonds();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}

}
