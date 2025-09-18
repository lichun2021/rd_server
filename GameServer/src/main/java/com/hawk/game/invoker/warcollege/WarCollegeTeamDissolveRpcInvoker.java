package com.hawk.game.invoker.warcollege;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;

public class WarCollegeTeamDissolveRpcInvoker extends HawkRpcInvoker {
	Player player;
	public WarCollegeTeamDissolveRpcInvoker(Player player) {
		this.player = player;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		WarCollegeInstanceService instance = (WarCollegeInstanceService) targetObj;
		int errorCode = instance.onTeamDissolve(player.getId());
		result.put("errorCode", errorCode);
		
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int errorCode = (Integer)result.get("errorCode");
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_REQ_VALUE);
		} else {
			player.sendError(HP.code.WAR_COLLEGE_TEAM_REQ_VALUE, errorCode, 0);
		}
		
		return true;
	}

}
