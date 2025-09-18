package com.hawk.game.module.dayazhizhan.playerteam.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;

public class JoinTeamInvoker  extends HawkMsgInvoker {

	private Player player;
	
	private String teamId;
	
	public JoinTeamInvoker(Player player,String teamId) {
		this.player = player;
		this.teamId = teamId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		DYZZService.getInstance().joinTeamRoom(player, teamId);
		return true;
	}

	
	
}
