package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.service.RelationService;

public class GuardDeleteInvoker extends HawkMsgInvoker {

	String playerId;
	public GuardDeleteInvoker(String playerId) {
		this.playerId = playerId;
	}	
	
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		RelationService.getInstance().onGuardDelete(playerId);
		return true;
	}

}
