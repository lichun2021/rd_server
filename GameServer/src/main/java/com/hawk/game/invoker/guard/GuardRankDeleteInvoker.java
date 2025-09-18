package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.rank.RankService;

public class GuardRankDeleteInvoker extends HawkMsgInvoker {
	String playerId1;
	String playerId2;
	public GuardRankDeleteInvoker(String playerId1, String playerId2) {
		this.playerId1 = playerId1;
		this.playerId2 = playerId2;
	}
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		RankService.getInstance().getGuardRankObject().deleteRank(playerId1, playerId2);
		
		return true;
	}

}
