package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;

/**
 * 任命联盟官员
 * 
 * @author Jesse
 *
 */
public class GuildAppointOfficerInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	/** 目标玩家id*/
	private String playerId;

	/** 联盟官职id */
	private int officerId;

	public GuildAppointOfficerInvoker(Player player,String playerId, int officerId) {
		this.player = player;
		this.playerId = playerId;
		this.officerId = officerId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onAppointGuildOfficer(player, playerId, officerId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_APPOINT_OFFICER_C_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_APPOINT_OFFICER_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getPlayerId() {
		return playerId;
	}

	public int getOfficerId() {
		return officerId;
	}

}
