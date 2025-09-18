package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.log.Action;

/**
 * 创建赛博之战战队
 * 
 * @author Jesse
 *
 */
public class CynorgCreateTeamRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 联盟名称 */
	private String name;

	/** 消耗信息 */
	private ConsumeItems consume;

	/** 协议Id */
	private int hpCode;

	public CynorgCreateTeamRpcInvoker(Player player, String name, ConsumeItems consume, int hpCode) {
		this.player = player;
		this.name = name;
		this.consume = consume;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = CyborgWarService.getInstance().onCreateTeam(player, name);
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			consume.consumeAndPush(player, Action.CYBORG_CREATE_TEAM);
			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode , operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public ConsumeItems getConsume() {
		return consume;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
