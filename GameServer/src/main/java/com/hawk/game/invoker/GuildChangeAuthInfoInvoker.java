package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.GuildAuthInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;

/**
 * 修改联盟权限信息
 * 
 * @author Jesse
 *
 */
public class GuildChangeAuthInfoInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 消耗信息 */
	private List<GuildAuthInfo> authInfos;

	public GuildChangeAuthInfoInvoker(Player player, List<GuildAuthInfo> authInfos) {
		this.player = player;
		this.authInfos = authInfos;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = GuildService.getInstance().onChangeAuthInfo(player, authInfos);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.GUILD_CHANGE_AUTH_INFO_VALUE);
			return true;
		}
		player.sendError(HP.code.GUILD_CHANGE_AUTH_INFO_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public List<GuildAuthInfo> getAuthInfos() {
		return authInfos;
	}

}
