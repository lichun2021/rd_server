package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;

public class BanPersonalRankMsgInvoker extends HawkMsgInvoker {
	
	private String playerId;
	
	public BanPersonalRankMsgInvoker(String playerId) {
		this.playerId = playerId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
			LocalRedis.getInstance().removeFromRank(rankType, playerId);
			RankService.getInstance().refreshRank(rankType);
		}
		
		GuildService.getInstance().clearGuildMemberPower(playerId);
		GuildService.getInstance().clearGuildMemberKillCount(playerId);
		return true;
	}
	
	public String getPlayerId() {
		return playerId;
	}
}
