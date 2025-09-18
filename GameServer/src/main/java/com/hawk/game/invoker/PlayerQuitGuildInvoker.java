package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;

import com.hawk.game.service.GuildService;
/**
 * 玩家推出联盟
 * @author Jesse
 *
 */
public class PlayerQuitGuildInvoker extends HawkMsgInvoker {
	/** 玩家Id */
	private String playerId;

	public PlayerQuitGuildInvoker(String playerId) {
		this.playerId = playerId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildService.getInstance().setPlayerQuitGuildTime(playerId, HawkTime.getMillisecond());
		return true;
	}

	public String getPlayerId() {
		return playerId;
	}
	
}
