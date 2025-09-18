package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;

public class SuperVipActivePointAddInvoker extends HawkMsgInvoker {
	private Player player;
	private int points;
	
	public SuperVipActivePointAddInvoker(Player player, int points) {
		this.player = player;
		this.points = points;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
//		if (player.getSuperVipObject().isSuperVipOpen()) {
//			player.getSuperVipObject().addMonthSuperVipScore(points);
//			player.getSuperVipObject().syncSuperVipInfo(false);
//		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public int getPoints() {
		return this.points;
	}

}
