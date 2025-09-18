package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
/**
 * 刷新联盟击杀数
 * @author Jesse
 *
 */
public class GuildChangeMemberKillCntInvoker extends HawkMsgInvoker {
	/** 联盟Id */
	private String guildId;

	public GuildChangeMemberKillCntInvoker(String guildId) {
		this.guildId = guildId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		// 若该联盟排行被封禁,则不进行排行刷新
		if (!RankService.getInstance().isBan(guildId, RankType.ALLIANCE_KILL_ENEMY_KEY)) {
			long killCnt = GuildService.getInstance().getGuildKillCount(guildId);
			RankService.getInstance().updateRankScore(RankType.ALLIANCE_KILL_ENEMY_KEY, killCnt, guildId);
		}
		return true;
	}

	public String getGuildId() {
		return guildId;
	}
	
}
