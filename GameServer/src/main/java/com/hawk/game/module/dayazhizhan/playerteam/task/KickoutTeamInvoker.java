package com.hawk.game.module.dayazhizhan.playerteam.task;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;

public class KickoutTeamInvoker  extends HawkMsgInvoker {

	private Player player;
	
	private String memberId;
	
	public KickoutTeamInvoker(Player player,String memberId) {
		this.player = player;
		this.memberId = memberId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		DYZZService.getInstance().removeTeamMember(player, memberId);
		return true;
	}

	
	
}
