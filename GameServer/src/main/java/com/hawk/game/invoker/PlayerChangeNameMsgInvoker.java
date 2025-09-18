package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;

public class PlayerChangeNameMsgInvoker extends HawkMsgInvoker {
	
	private Player player;
	
	public PlayerChangeNameMsgInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildService.getInstance().changePlayerName(player.getGuildId(), player.getId(), player.getName());
		return true;
	}

	public Player getPlayer() {
		return player;
	}
	
}
