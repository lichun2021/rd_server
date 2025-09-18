package com.hawk.game.invoker.warcollege;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;

/**
 * 快速加入队伍
 * @author jm
 *
 */
public class WarCollegeTeamQuickJoinRpcInvoker extends HawkRpcInvoker {
	private Player player;
	public WarCollegeTeamQuickJoinRpcInvoker(Player player) {
		this.player = player;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		WarCollegeInstanceService warCollegeInstanceService = (WarCollegeInstanceService)targetObj;
		int errorCode = warCollegeInstanceService.onTeamQuickJoin(player);
		result.put("result", errorCode);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int errorCode = (Integer)result.get("result");
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_FAST_JOIN_REQ_VALUE);
		} else {
			player.sendError(HP.code.WAR_COLLEGE_TEAM_FAST_JOIN_REQ_VALUE, errorCode, 0);
		}
		
		return true;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}


}
