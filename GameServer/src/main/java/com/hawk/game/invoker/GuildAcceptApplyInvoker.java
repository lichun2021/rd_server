package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;
import com.hawk.log.Source;
/**
 * 接受联盟申请
 * @author admin
 *
 */
public class GuildAcceptApplyInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 申请者Id */
	private String applyPlayerId;

	/** 协议Id */
	private int hpCode;

	public GuildAcceptApplyInvoker(Player player, String applyPlayerId, int hpCode) {
		this.player = player;
		this.applyPlayerId = applyPlayerId;
		this.hpCode = hpCode;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onAcceptGuildApply(player.getGuildId(), applyPlayerId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_ACCEPTAPPLY, Params.valueOf("guildId", player.getGuildId()),
					Params.valueOf("targetPlayer", applyPlayerId));

			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(operationResult, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getApplyPlayerId() {
		return applyPlayerId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
