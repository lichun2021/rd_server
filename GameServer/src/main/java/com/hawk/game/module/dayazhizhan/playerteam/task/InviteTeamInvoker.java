package com.hawk.game.module.dayazhizhan.playerteam.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;

public class InviteTeamInvoker  extends HawkMsgInvoker {

	private Player player;
	
	public InviteTeamInvoker(Player player) {
		this.player = player;
	}
	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		DYZZService.getInstance().inviteTeam(player);
		return true;
	}

	
	
}
