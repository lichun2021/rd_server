package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;

public class RemoveCityShieldMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public RemoveCityShieldMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (worldPoint != null && worldPoint.getProtectedEndTime() == Integer.MAX_VALUE) {
			player.removeCityShield();
		}
		
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}
