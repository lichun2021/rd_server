package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.service.GuildManorService;

public class GuildDissmiseRemoveManorMsgInvoker extends HawkMsgInvoker {
	private String guildId;
	
	private Player player;
	
	public GuildDissmiseRemoveManorMsgInvoker(String guildId, Player player) {
		this.guildId = guildId;
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildManorService.getInstance().removeManorOnDissmiseGuild(this.guildId, this.player);
		return true;
	}

	public String getGuildId() {
		return guildId;
	}

	public Player getPlayer() {
		return player;
	}
	
}
