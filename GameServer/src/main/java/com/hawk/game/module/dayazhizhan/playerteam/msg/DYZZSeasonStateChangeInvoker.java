package com.hawk.game.module.dayazhizhan.playerteam.msg;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.player.Player;

/**
 * 赛季状态变化
 * @author chechangda
 *
 */
public class DYZZSeasonStateChangeInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;


	public DYZZSeasonStateChangeInvoker(Player player) {
		this.player = player;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		DYZZSeasonService.getInstance().checkDYZZSeasonDataSync(player);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	
}
