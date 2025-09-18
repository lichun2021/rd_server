package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.log.Source;

/**
 * 修改联盟名称
 * 
 * @author Jesse
 *
 */
public class GuildChangeNameRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟名称 */
	private String guildName;

	/** 消耗信息 */
	private ConsumeItems consume;

	/** 协议Id */
	private int hpCode;
	
	/**
	 * 清除CD时间
	 */
	private boolean clearCDTime;

	public GuildChangeNameRpcInvoker(Player player, String guildName, ConsumeItems consume, int hpCode, boolean clearCDTime) {
		this.player = player;
		this.guildName = guildName;
		this.consume = consume;
		this.hpCode = hpCode;
		this.clearCDTime = clearCDTime;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onChangeGuildName(guildName, player.getGuildId());
		RedisProxy.getInstance().updateChangeContentTime(player.getGuildId(), ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
		if (clearCDTime) {
			RedisProxy.getInstance().removeChangeContentCDTime(player.getGuildId(), ChangeContentType.CHANGE_GUILD_NAME);
		}
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.GUILD_CHANGENAME);
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_CHANGENAME, Params.valueOf("guildId", player.getGuildId()));
			LogUtil.logSecTalkFlow(player, null, LogMsgType.GUILD_NAME_CHANGE, "", guildName);
			player.responseSuccess(hpCode);
			
			// 记录打点日志
			JSONObject changeInfo = new JSONObject();
			changeInfo.put("guildName", guildName);
			LogUtil.logGuildDetail(player, player.getGuildId(), changeInfo.toJSONString(), GuildAction.GUILD_ACTION_25.intVal());
			LogUtil.logGuildFlow(player, GuildOperType.GUILD_CHANGENAME, player.getGuildId(), null);
			return true;
		}
		player.sendError(hpCode , operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getGuildName() {
		return guildName;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
