package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.cyborgWar.CyborgWarService;

/**
 * 修改赛博之战战队名称
 * 
 * @author Jesse
 *
 */
public class CynorgEditNameRpcInvoker extends HawkRpcInvoker {
	/** 玩家 */
	private Player player;

	/** 战队名称 */
	private String name;
	
	private String teamId;

	/** 协议Id */
	private int hpCode;

	public CynorgEditNameRpcInvoker(Player player, String teamId, String name, int hpCode) {
		this.player = player;
		this.teamId = teamId;
		this.name = name;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int operationResult = CyborgWarService.getInstance().onEditTeamName(player, teamId, name);
		result.put("res", operationResult);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		Integer operationResult = (Integer) result.get("res");
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(hpCode);
			return true;
		}
		player.sendError(hpCode , operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
