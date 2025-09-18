package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
/**
 * 清除联盟战力
 * @author Jesse
 *
 */
public class GuildClearMemberPowerInvoker extends HawkMsgInvoker {
	/** 联盟Id */
	private String guildId;

	public GuildClearMemberPowerInvoker(String guildId) {
		this.guildId = guildId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		long battlePoint = GuildService.getInstance().getGuildBattlePoint(guildId);
		// 刷新联盟战力排行
		RankService.getInstance().updateRankScore(RankType.ALLIANCE_FIGHT_KEY, battlePoint, guildId);
		return true;
	}

	public String getGuildId() {
		return guildId;
	}
	
}
