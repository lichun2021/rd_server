package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;

/**
 * 解除联盟官员
 * 
 * @author Jesse
 *
 */
public class GuildDismissOfficerInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	/** 目标玩家id*/
	private String tarPlayerId;

	public GuildDismissOfficerInvoker(Player player, String tarPlayerId) {
		this.player = player;
		this.tarPlayerId = tarPlayerId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onDismissGuildOfficer(player, tarPlayerId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_DISMISS_OFFICER_C_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_DISMISS_OFFICER_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTarPlayerId() {
		return tarPlayerId;
	}

}
