package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.log.Action;
import com.hawk.log.Source;
/**
 * 取代联盟盟主
 * @author Jesse
 *
 */
public class GuildImpeachmentLeaderInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 协议Id */
	private int hpCode;
	
	/** 消耗信息 */
	private ConsumeItems consume;

	public GuildImpeachmentLeaderInvoker(Player player, int hpCode, ConsumeItems consume) {
		this.player = player;
		this.hpCode = hpCode;
		this.consume = consume;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getHpCode() {
		return hpCode;
	}

	public void setHpCode(int hpCode) {
		this.hpCode = hpCode;
	}
	
	public ConsumeItems getConsume() {
		return consume;
	}

	public void setConsume(ConsumeItems consume) {
		this.consume = consume;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = GuildService.getInstance().onReplaceLeader(player.getGuildId(), player.getId(), player.getName());
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.GUILD_IMPEACHLEADER);
			BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_IMPEACHLEADER, Params.valueOf("guildId", player.getGuildId()));
			player.responseSuccess(HP.code.GUILDMANAGER_IMPEACHMENTLEADER_C_VALUE);
			return true;
		}
		player.sendError(hpCode, operationResult, 0);
		return false;
	}
	
}
