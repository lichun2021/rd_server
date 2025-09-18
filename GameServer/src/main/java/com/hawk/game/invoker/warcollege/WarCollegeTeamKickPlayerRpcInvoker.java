package com.hawk.game.invoker.warcollege;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;

public class WarCollegeTeamKickPlayerRpcInvoker extends HawkRpcInvoker {
	/**
	 * 主操作玩家
	 */
	private Player player;
	/**
	 * 被删除的玩家
	 */
	private String kickedPlayerId;
	public  WarCollegeTeamKickPlayerRpcInvoker(Player player, String kickedPlayerId) {
		this.player = player;
		this.kickedPlayerId = kickedPlayerId;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		WarCollegeInstanceService instanceService = (WarCollegeInstanceService)targetObj;
		int errorCode = instanceService.onTeamKickPlayer(player.getId(), kickedPlayerId);
		result.put("result", errorCode);
		
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int errorCode = (Integer)result.get("result");
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_KICK_PLAYER_REQ_VALUE);
		} else {
			player.sendError(HP.code.WAR_COLLEGE_TEAM_KICK_PLAYER_REQ_VALUE, errorCode, 0);
		}
		
		return true;
	}

}
