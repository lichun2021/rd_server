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
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.Source;

/**
 * 修改联盟简称
 * 
 * @author Jesse
 *
 */
public class GuildChangeTagRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟简称 */
	private String tag;

	/** 消耗信息 */
	private ConsumeItems consume;

	/** 协议Id */
	private int hpCode;

	public GuildChangeTagRpcInvoker(Player player, String tag, ConsumeItems consume, int hpCode) {
		this.player = player;
		this.tag = tag;
		this.consume = consume;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onChangeGuildTag(tag, player.getGuildId());
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.GUILD_CHANGETAG);
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGETAG, Params.valueOf("guildId", player.getGuildId()));
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_TAG_CHANGE, "", tag);
			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTag() {
		return tag;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
