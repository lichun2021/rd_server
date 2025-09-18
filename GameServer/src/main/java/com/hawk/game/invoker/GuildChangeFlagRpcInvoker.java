package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 修改联盟旗帜
 * 
 * @author Jesse
 *
 */
public class GuildChangeFlagRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 旗帜Id */
	private int flagId;

	/** 消耗信息 */
	private ConsumeItems consume;

	/** 协议Id */
	private int hpCode;

	public GuildChangeFlagRpcInvoker(Player player, int flagId, ConsumeItems consume, int hpCode) {
		this.player = player;
		this.flagId = flagId;
		this.consume = consume;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onChangeGuildFlag(flagId, player.getGuildId());
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.GUILD_CHANGEFLAG);
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGEFLAG, Params.valueOf("guildId", player.getGuildId()));

			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getFlagId() {
		return flagId;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
