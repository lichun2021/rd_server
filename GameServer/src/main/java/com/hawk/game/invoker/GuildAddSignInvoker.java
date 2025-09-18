package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.LogMsgType;

/**
 * 添加联盟标记
 * 
 * @author Jesse
 *
 */
public class GuildAddSignInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 标记信息 */
	private GuildSign guildSign;

	public GuildAddSignInvoker(Player player, GuildSign guildSign) {
		this.player = player;
		this.guildSign = guildSign;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onAddGuildSign(player, guildSign);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_SIGN, "", guildSign.getInfo());
			player.responseSuccess(HP.code.GUILD_ADD_SIGN_C_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_ADD_SIGN_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public GuildSign getGuildSign() {
		return guildSign;
	}

}
