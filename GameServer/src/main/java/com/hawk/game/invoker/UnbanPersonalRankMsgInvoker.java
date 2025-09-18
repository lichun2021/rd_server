package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.util.GsConst;

public class UnbanPersonalRankMsgInvoker extends HawkMsgInvoker {
	
	private String playerId;
	
	public UnbanPersonalRankMsgInvoker(String playerId) {
		this.playerId = playerId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
			RankService.getInstance().removeFromBan(rankType, playerId);
		}
		
		return true;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
}
