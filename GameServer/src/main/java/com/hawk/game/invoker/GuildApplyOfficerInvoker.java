package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;

/**
 * 申请联盟官员
 * 
 * @author Jesse
 *
 */
public class GuildApplyOfficerInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟官职id */
	private int officerId;
	
	public GuildApplyOfficerInvoker(Player player, int officerId) {
		this.player = player;
		this.officerId = officerId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onApplyGuildOfficer(player,officerId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_APPLY_OFFICER_C_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_APPLY_OFFICER_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getOfficerId() {
		return officerId;
	}

}
