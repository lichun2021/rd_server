package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status.Error;
import com.hawk.game.protocol.GuildManager.GuildGetRankResp;
import com.hawk.game.protocol.GuildManager.GuildRankType;
import com.hawk.game.guild.guildrank.GuildRankMgr;

public final class GuildGetRankInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	private GuildRankType rType;

	public GuildGetRankInvoker(Player player, GuildRankType rType) {
		this.player = player;
		this.rType = rType;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {

		GuildGetRankResp.Builder resp = GuildRankMgr.getInstance().getRankResp(player.getId(), player.getGuildId(),
				rType);

		if (null != resp) {

			// System.out.printf( "MSGID:%d==%s\n",
			// HP.code.GUILD_GET_RANK_S_VALUE ,resp.build().toString());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_GET_RANK_S_VALUE, resp));
		} else {
			player.sendError(HP.code.GUILD_DISMISS_OFFICER_C_VALUE, Error.GUILD_BOARD_LOCKED_VALUE, 0);
		}
		return true;
	}

}
