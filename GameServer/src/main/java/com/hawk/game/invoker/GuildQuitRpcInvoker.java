package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 退出联盟
 * 
 * @author Jesse
 *
 */
public class GuildQuitRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟Id */
	private String guildId;

	/** 协议Id */
	private int hpCode;

	public GuildQuitRpcInvoker(Player player, String guildId, int hpCode) {
		this.player = player;
		this.guildId = guildId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onQuitGuild(guildId, player.getId());
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hpCode, operationResult, 0);
			return false;
		}
		
		if (GameUtil.isScoreBatchEnable(player) && !player.isActiveOnline()) {
			LocalRedis.getInstance().addScoreBatchFlag(player.getId(), ScoreType.GUILD_MEMBER_CHANGE.intValue(), "2");
		} else {
			GameUtil.scoreBatch(player, ScoreType.GUILD_MEMBER_CHANGE, 2);
		}
		
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_QUIT, Params.valueOf("guildId", player.getGuildId()));

		// 返回协议
		player.responseSuccess(hpCode);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getGuildId() {
		return guildId;
	}

	public int getHpCode() {
		return hpCode;
	}

}
