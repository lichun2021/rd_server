package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;

/**
 * 移除联盟标记
 * 
 * @author Jesse
 *
 */
public class GuildRemoveSignInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 标记id */
	private int signId;

	public GuildRemoveSignInvoker(Player player, int signId) {
		this.player = player;
		this.signId = signId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onRemoveGuildSign(player, signId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_REMOVE_SIGN_C_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_REMOVE_SIGN_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getSignId() {
		return signId;
	}

}
