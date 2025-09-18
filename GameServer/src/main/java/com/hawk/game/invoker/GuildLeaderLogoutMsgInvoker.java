package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.service.GuildService;

public class GuildLeaderLogoutMsgInvoker extends HawkMsgInvoker {
	
	private String playerId;
	
	private String guildId;
	
	public GuildLeaderLogoutMsgInvoker(String playerId, String guildId) {
		this.playerId = playerId;
		this.guildId = guildId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildService.getInstance().updateGuildLeaderLogoutTime(guildId, playerId);
		return true;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getGuildId() {
		return guildId;
	}
	
}
