package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.rank.RankService;

public class GuardRankAddInvoker extends HawkMsgInvoker {
	String playerId1;
	String playerId2;
	int guardValue;
	long operationTime;
	public GuardRankAddInvoker(String playerId1, String playerId2, int guardValue, long operationTime) {
		this.playerId1 = playerId1;
		this.playerId2 = playerId2;
		this.guardValue = guardValue;
		this.operationTime = operationTime;
	}
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		RankService.getInstance().getGuardRankObject().addRank(playerId1, playerId2, guardValue, operationTime);
		
		return true;
	}

}
