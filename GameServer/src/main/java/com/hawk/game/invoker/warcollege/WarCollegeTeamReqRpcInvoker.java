package com.hawk.game.invoker.warcollege;

import java.util.Map;
import java.util.Objects;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.entity.PlayerWarCollegeEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.warcollege.model.WarCollegeTeam;

public class WarCollegeTeamReqRpcInvoker extends HawkRpcInvoker {
	
	private Player player;
	public WarCollegeTeamReqRpcInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		if (!player.hasGuild()) {
			result.put("errorCode", Status.Error.WAR_COLLEGE_NOT_HAVE_GUILD_VALUE);
			return true;
		}
		WarCollegeInstanceService instanceService =(WarCollegeInstanceService)(targetObj);
		if(!instanceService.isOpen()){
			result.put("errorCode", Status.Error.WAR_COLLEGE_INSTANCE_NOT_OPEN_VALUE);
			return true;
		}
		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		if(entity == null){
			throw new NullPointerException("PlayerWarCollegeEntity is null,playerId="+player.getId());
		}
		entity.checkOrReset();//打开的时候检测下是否需要重置
		WarCollegeTeam team = instanceService.getWarCollegeTeamByPlayerId(player.getId());
		int errorCode = 0;
		if (Objects.nonNull(team)) {
			errorCode = instanceService.onTeamReq(player,team);
		} else {
			errorCode = instanceService.onMiniTeamReq(player);
		}
		result.put("errorCode", errorCode);
		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int errorCode = (int)result.get("errorCode");
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_REQ_VALUE);
		} else {
			player.sendError(HP.code.WAR_COLLEGE_TEAM_REQ_VALUE, errorCode, 0);
		}
		
		return true;
	}
}
