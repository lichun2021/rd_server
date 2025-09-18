package com.hawk.game.invoker.warcollege;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;

public class WarCollegeTeamCreateRpcInvoker extends HawkRpcInvoker {
	
	private Player player;
	private int instanceId;//副本id
	
	public WarCollegeTeamCreateRpcInvoker(Player player, int instanceId) {
		this.player = player;
		this.instanceId = instanceId;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int instanceId) {
		this.instanceId = instanceId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int errorCode = ((WarCollegeInstanceService)targetObj).onTeamCreate(player, instanceId);
		result.put("result", errorCode);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int errorCode = (int)result.get("result");
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_CREATE_REQ_VALUE);
		} else {
			player.sendError(HP.code.WAR_COLLEGE_TEAM_CREATE_REQ_VALUE, errorCode, 0);
		}
		
		return true;
	}

}
